package com.alan.routineos.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.core.util.TimeProvider
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.dashboard.ActivityDetailUiEvent
import com.alan.routineos.feature.today.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getHierarchicalTimelineUseCase: GetHierarchicalTimelineUseCase,
    private val registerDailyActionUseCase: RegisterDailyActionUseCase,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(isLoading = true))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ActivityDetailUiEvent>()
    val uiEvent: SharedFlow<ActivityDetailUiEvent> = _uiEvent.asSharedFlow()

    private val expandedIds = MutableStateFlow<Set<String>>(emptySet())
    private var currentEntries = listOf<HierarchicalTimelineEntry>()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
        
        viewModelScope.launch {
            val nodesFlow = repository.getAllNodes()
            val timelineFlow = getHierarchicalTimelineUseCase(today)

            combine(nodesFlow, timelineFlow, expandedIds, timeProvider.minuteTicker) { _, entries, expanded, _ ->
                currentEntries = entries
                resolveMetadataForEntries(entries).map { metaMap ->
                    mapToUiState(entries, metaMap, expanded, today, dateFormatter)
                }
            }.flatMapLatest { it }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun resolveMetadataForEntries(
        entries: List<HierarchicalTimelineEntry>
    ): Flow<Map<String, MetadataSnapshot>> {
        val allNodeIds = mutableSetOf<String>()
        fun collectIds(list: List<HierarchicalTimelineEntry>) {
            list.forEach { 
                (it.root.instance.target as? ScheduleTarget.Node)?.id?.let { id -> allNodeIds.add(id) }
                collectIds(it.children)
            }
        }
        collectIds(entries)

        val flows = allNodeIds.map { nodeId ->
            repository.getMetadataSchema(nodeId, "NODE").map { schema ->
                nodeId to (schema?.toSnapshot() ?: MetadataSnapshot())
            }
        }
        return if (flows.isEmpty()) flowOf(emptyMap()) else combine(flows) { it.toMap() }
    }

    private data class MetadataSnapshot(
        val context: List<Pair<String, String>> = emptyList(),
        val operational: List<Pair<String, String>> = emptyList()
    )

    private fun MetadataSchema.toSnapshot(): MetadataSnapshot {
        val context = fields.filter { it.isReadOnly }.map { it.name to (it.defaultValue ?: "") }
        val operational = fields.filter { !it.isReadOnly }.map { it.name to (it.defaultValue ?: "?") }
        return MetadataSnapshot(context, operational)
    }

    private fun mapToUiState(
        entries: List<HierarchicalTimelineEntry>,
        metaMap: Map<String, MetadataSnapshot>,
        expanded: Set<String>,
        today: LocalDate,
        dateFormatter: DateTimeFormatter
    ): TodayUiState {
        val now = timeProvider.now()
        val currentMinutes = now.hour * 60 + now.minute
        
        val allLeaves = collectAllLeafEntries(entries)
        val totalTasks = allLeaves.size
        val completedTasks = allLeaves.count { it.instance.status == DailyInstanceStatus.COMPLETED }

        val uiModels = entries.mapIndexed { index, entry ->
            val subNodeModels = entry.children.map { mapToSubNodeUiModel(it, metaMap, entries) }
            val rootTargetId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
            val rootMeta = metaMap[rootTargetId] ?: MetadataSnapshot()
            
            val nextScheduled = entries.drop(index + 1).find { it.root.instance.plannedStartTime != null }
            val nextStartTime = nextScheduled?.root?.instance?.plannedStartTime

            entry.toUiModel(subNodeModels, rootMeta, expanded.contains(entry.root.instance.id), currentMinutes, nextStartTime, entries)
        }

        return TodayUiState(
            isLoading = false,
            dateText = today.format(dateFormatter).uppercase(),
            progress = TodayProgress(completedTasks, totalTasks),
            timelineItems = uiModels,
            nextActivity = findNextActivity(uiModels),
            focusItemId = calculateFocusItemId(uiModels)
        )
    }

    private fun mapToSubNodeUiModel(
        entry: HierarchicalTimelineEntry, 
        metaMap: Map<String, MetadataSnapshot>,
        allEntries: List<HierarchicalTimelineEntry>
    ): TodaySubNodeUiModel {
        val nodeId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
        val meta = if (nodeId != null) metaMap[nodeId] ?: MetadataSnapshot() else MetadataSnapshot()
        
        return TodaySubNodeUiModel(
            id = entry.root.instance.id,
            title = entry.root.instance.titleSnapshot,
            timeText = entry.root.instance.plannedStartTime?.let { formatMinutes(it) } ?: "",
            status = entry.root.instance.status,
            contextMetadata = meta.context,
            operationalMetadata = meta.operational,
            completedCount = entry.completedCount,
            totalCount = entry.totalCount,
            completion = entry.completion,
            children = entry.children.map { mapToSubNodeUiModel(it, metaMap, allEntries) },
            conflict = entry.root.conflict?.toUiModel(allEntries, entry.root.instance) ?: ConflictUiModel(false)
        )
    }

    private fun ConflictResult.toUiModel(allEntries: List<HierarchicalTimelineEntry>, current: DailyInstance): ConflictUiModel {
        val detailUiList = details.map { detail ->
            val otherTitle = findInHierarchy(allEntries, detail.otherInstanceId)?.root?.instance?.titleSnapshot ?: "OTRA"
            ConflictDetailUiModel(
                otherInstanceId = detail.otherInstanceId,
                otherTitle = otherTitle,
                relationship = detail.relationship,
                impact = detail.impact,
                isInterruption = detail.isInterruption
            )
        }

        val interrupter = hasConflict && (
            current.isAdHoc || 
            (current.mobility == TemporalMobility.FLEXIBLE && detailUiList.any { it.impact == TemporalImpact.WARNING })
        )

        return ConflictUiModel(
            hasConflict = hasConflict,
            impact = impact,
            details = detailUiList,
            suggestions = suggestions,
            isInterrupter = interrupter
        )
    }

    private fun findInHierarchy(entries: List<HierarchicalTimelineEntry>, id: String): HierarchicalTimelineEntry? {
        entries.forEach { entry ->
            if (entry.root.instance.id == id) return entry
            findChildInHierarchy(entry.children, id)?.let { return it }
        }
        return null
    }

    private fun findChildInHierarchy(children: List<HierarchicalTimelineEntry>, id: String): HierarchicalTimelineEntry? {
        children.forEach { child ->
            if (child.root.instance.id == id) return child
            findChildInHierarchy(child.children, id)?.let { return it }
        }
        return null
    }

    private fun collectAllLeafEntries(entries: List<HierarchicalTimelineEntry>): List<TimelineEntry> {
        val leaves = mutableListOf<TimelineEntry>()
        fun collect(list: List<HierarchicalTimelineEntry>) {
            list.forEach { 
                if (it.children.isEmpty()) leaves.add(it.root)
                else collect(it.children)
            }
        }
        collect(entries)
        return leaves
    }

    private fun HierarchicalTimelineEntry.toUiModel(
        subNodeModels: List<TodaySubNodeUiModel>,
        meta: MetadataSnapshot,
        isExpanded: Boolean,
        currentMinutes: Int,
        inferredEndTime: Int?,
        allEntries: List<HierarchicalTimelineEntry>
    ): TodayTimelineUiModel {
        val start = root.instance.plannedStartTime
        val duration = root.instance.plannedDurationMinutes
        val explicitEnd = root.instance.plannedEndTime ?: if (start != null && duration != null) start + duration else null
        val finalEnd = explicitEnd ?: inferredEndTime

        val temporalState = when {
            start != null && currentMinutes < start -> TimelineTemporalState.UPCOMING
            start != null && finalEnd != null && currentMinutes >= start && currentMinutes < finalEnd -> TimelineTemporalState.CURRENT
            finalEnd != null && currentMinutes >= finalEnd && root.instance.status == DailyInstanceStatus.PLANNED -> TimelineTemporalState.OVERDUE
            start != null && currentMinutes >= start && finalEnd == null -> TimelineTemporalState.STALE_PENDING
            else -> TimelineTemporalState.UPCOMING
        }

        return TodayTimelineUiModel(
            id = root.instance.id,
            title = root.instance.titleSnapshot,
            description = root.instance.descriptionSnapshot,
            timeRangeText = root.instance.plannedStartTime?.let { formatMinutes(it) } ?: "",
            startTimeMinutes = start,
            endTimeMinutes = finalEnd,
            status = root.instance.status,
            isMaterialized = root.isMaterialized,
            conflict = root.conflict?.toUiModel(allEntries, root.instance) ?: ConflictUiModel(false),
            subNodes = subNodeModels,
            contextMetadata = meta.context,
            operationalMetadata = meta.operational,
            isExpandable = subNodeModels.isNotEmpty(),
            isExpanded = isExpanded,
            isAdHoc = root.instance.isAdHoc,
            completedSubNodesCount = completedCount,
            totalSubNodesCount = totalCount,
            temporalState = temporalState,
            completion = completion
        )
    }

    fun onActionTriggered(instanceId: String, actionType: String) {
        viewModelScope.launch {
            val entry = findEntry(instanceId) ?: if (instanceId.startsWith("structural_virtual_")) {
                createStructuralEntry(instanceId.removePrefix("structural_virtual_"))
            } else null
            
            // Actions only allowed on leaf nodes
            if (entry == null || entry.children.isNotEmpty()) return@launch

            when {
                actionType == "SKIP" -> registerDailyActionUseCase(entry.root, DailyAction.Skip)
                actionType == "COMPLETE" -> handleCompleteRequest(entry.root)
                actionType.startsWith("MOVE_TO:") -> {
                    val minutes = actionType.removePrefix("MOVE_TO:").toInt()
                    registerDailyActionUseCase(entry.root, DailyAction.Move(minutes))
                }
                actionType.startsWith("MOVE_CONFIRM") -> {
                    val minutes = actionType.split(":")[1].toInt()
                    registerDailyActionUseCase(entry.root, DailyAction.Move(minutes))
                }
            }
        }
    }

    private suspend fun createStructuralEntry(nodeId: String): HierarchicalTimelineEntry? {
        val node = repository.getNodeById(nodeId) ?: return null
        return HierarchicalTimelineEntry(
            root = TimelineEntry(
                instance = DailyInstance(
                    id = "structural_virtual_$nodeId",
                    target = ScheduleTarget.Node(nodeId),
                    scheduledDate = LocalDate.now().toEpochDay(),
                    titleSnapshot = node.title,
                    descriptionSnapshot = node.description,
                    status = DailyInstanceStatus.PLANNED
                ),
                isMaterialized = false
            ),
            children = emptyList()
        )
    }

    private suspend fun handleCompleteRequest(entry: TimelineEntry) {
        val target = entry.instance.target
        if (target is ScheduleTarget.Node) {
            val schema = repository.getMetadataSchema(target.id, "NODE").firstOrNull()
            val operationalFields = schema?.fields?.filter { !it.isReadOnly } ?: emptyList()
            if (operationalFields.isNotEmpty()) {
                _uiState.update { it.copy(captureSchema = schema, captureTargetId = entry.instance.id) }
                return
            }
        }
        registerDailyActionUseCase(entry, DailyAction.Complete())
        _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad completada"))
    }

    fun onMetadataCaptured(instanceId: String, metadataJson: String) {
        val entry = findEntry(instanceId) ?: return
        viewModelScope.launch {
            registerDailyActionUseCase(entry.root, DailyAction.Complete(metadataJson))
            onCloseCapture()
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad completada con datos"))
        }
    }

    fun onCloseCapture() {
        _uiState.update { it.copy(captureSchema = null, captureTargetId = null) }
    }

    fun toggleExpand(instanceId: String) {
        expandedIds.update {
            if (it.contains(instanceId)) it - instanceId else it + instanceId
        }
    }

    private fun findEntry(instanceId: String): HierarchicalTimelineEntry? {
        currentEntries.forEach { hierarchical ->
            if (hierarchical.root.instance.id == instanceId) return hierarchical
            findChildEntry(hierarchical.children, instanceId)?.let { return it }
        }
        return null
    }

    private fun findChildEntry(children: List<HierarchicalTimelineEntry>, id: String): HierarchicalTimelineEntry? {
        children.forEach { child ->
            if (child.root.instance.id == id) return child
            findChildEntry(child.children, id)?.let { return it }
        }
        return null
    }

    private fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun findNextActivity(items: List<TodayTimelineUiModel>): TodayTimelineUiModel? {
        val current = items.find { it.status == DailyInstanceStatus.PLANNED && it.temporalState == TimelineTemporalState.CURRENT }
        if (current != null) return current
        return items.find { it.status == DailyInstanceStatus.PLANNED && it.temporalState == TimelineTemporalState.UPCOMING }
    }

    private fun calculateFocusItemId(items: List<TodayTimelineUiModel>): String? {
        if (items.isEmpty()) return null
        val current = items.find { it.temporalState == TimelineTemporalState.CURRENT }
        if (current != null) return current.id
        val upcoming = items.find { it.temporalState == TimelineTemporalState.UPCOMING }
        if (upcoming != null) return upcoming.id
        return items.last().id
    }

    fun onAddAdHoc(title: String, startTime: Int? = null) {
        val today = LocalDate.now()
        val minutes = startTime ?: (LocalTime.now().hour * 60 + LocalTime.now().minute)
        val adHocInstance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = today.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "Ad-hoc task",
            plannedStartTime = minutes,
            plannedDurationMinutes = 30,
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = true
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(adHocInstance)
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad añadida"))
        }
    }
}
