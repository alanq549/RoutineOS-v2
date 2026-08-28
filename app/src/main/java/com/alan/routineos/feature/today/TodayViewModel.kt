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

            combine(nodesFlow, timelineFlow, expandedIds, timeProvider.minuteTicker) { nodes, entries, expanded, _ ->
                currentEntries = entries
                resolveMetadataForEntries(entries, nodes).map { metaMap ->
                    mapToUiState(entries, metaMap, expanded, nodes, today, dateFormatter)
                }
            }.flatMapLatest { it }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun resolveMetadataForEntries(
        entries: List<HierarchicalTimelineEntry>,
        allNodes: List<ActivityNode>
    ): Flow<Map<String, MetadataSnapshot>> {
        // Find ALL descendants recursively for each entry root to ensure deep metadata resolve
        val allTargetIds = entries.flatMap { entry ->
            val rootId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
            val descendants = if (rootId != null) getDescendantIds(rootId, allNodes) else emptyList()
            listOfNotNull(rootId) + descendants
        }.distinct()

        val flows = allTargetIds.map { nodeId ->
            repository.getMetadataSchema(nodeId, "NODE").map { schema ->
                nodeId to (schema?.toSnapshot() ?: MetadataSnapshot())
            }
        }
        return if (flows.isEmpty()) flowOf(emptyMap()) else combine(flows) { it.toMap() }
    }

    private fun getDescendantIds(parentId: String, allNodes: List<ActivityNode>): List<String> {
        val children = allNodes.filter { it.parentId == parentId }.map { it.id }
        return children + children.flatMap { getDescendantIds(it, allNodes) }
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
        allNodes: List<ActivityNode>,
        today: LocalDate,
        dateFormatter: DateTimeFormatter
    ): TodayUiState {
        val now = timeProvider.now()
        val currentMinutes = now.hour * 60 + now.minute
        var totalTasks = 0
        var completedTasks = 0

        val uiModels = entries.mapIndexed { index, entry ->
            val rootTargetId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
            
            totalTasks++
            if (entry.root.instance.status == DailyInstanceStatus.COMPLETED) completedTasks++

            val structuralChildren = if (rootTargetId != null) {
                allNodes.filter { it.parentId == rootTargetId }.map { node ->
                    val scheduledChild = entry.children.find { (it.instance.target as? ScheduleTarget.Node)?.id == node.id }
                    
                    totalTasks++
                    if (scheduledChild?.instance?.status == DailyInstanceStatus.COMPLETED) completedTasks++
                    
                    val meta = metaMap[node.id] ?: MetadataSnapshot()
                    TodaySubNodeUiModel(
                        id = scheduledChild?.instance?.id ?: "structural_${node.id}",
                        title = node.title,
                        timeText = scheduledChild?.instance?.plannedStartTime?.let { formatMinutes(it) } ?: "",
                        status = scheduledChild?.instance?.status ?: DailyInstanceStatus.PLANNED,
                        contextMetadata = meta.context,
                        operationalMetadata = meta.operational
                    )
                }
            } else emptyList()

            val rootMeta = metaMap[rootTargetId] ?: MetadataSnapshot()
            val completedSubNodesCount = structuralChildren.count { it.status == DailyInstanceStatus.COMPLETED }
            val totalSubNodesCount = structuralChildren.size
            
            // Infer end boundary from the next scheduled activity if explicit data is missing
            val nextScheduled = entries.drop(index + 1).find { it.root.instance.plannedStartTime != null }
            val nextStartTime = nextScheduled?.root?.instance?.plannedStartTime

            entry.toUiModel(
                structuralChildren, 
                rootMeta, 
                expanded.contains(entry.root.instance.id),
                completedSubNodesCount,
                totalSubNodesCount,
                currentMinutes,
                nextStartTime
            )
        }

        return TodayUiState(
            isLoading = false,
            dateText = today.format(dateFormatter).uppercase(),
            progress = TodayProgress(completedTasks, totalTasks),
            timelineItems = uiModels,
            nextActivity = findNextActivity(uiModels)
        )
    }

    private fun HierarchicalTimelineEntry.toUiModel(
        discoveredChildren: List<TodaySubNodeUiModel>,
        meta: MetadataSnapshot,
        isExpanded: Boolean,
        completedSubNodes: Int,
        totalSubNodes: Int,
        currentMinutes: Int,
        inferredEndTime: Int?
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
            status = root.instance.status,
            isMaterialized = root.isMaterialized,
            hasConflict = root.conflict?.hasConflict ?: false,
            subNodes = discoveredChildren,
            contextMetadata = meta.context,
            operationalMetadata = meta.operational,
            isExpandable = discoveredChildren.isNotEmpty(),
            isExpanded = isExpanded,
            isAdHoc = root.instance.isAdHoc,
            completedSubNodesCount = completedSubNodes,
            totalSubNodesCount = totalSubNodes,
            temporalState = temporalState
        )
    }

    fun onActionTriggered(instanceId: String, actionType: String) {
        viewModelScope.launch {
            val entry = findEntry(instanceId) ?: if (instanceId.startsWith("structural_")) {
                createStructuralEntry(instanceId.removePrefix("structural_"))
            } else null
            
            if (entry == null) return@launch

            when {
                actionType == "SKIP" -> registerDailyActionUseCase(entry, DailyAction.Skip)
                actionType == "COMPLETE" -> handleCompleteRequest(entry)
                actionType.startsWith("MOVE_CONFIRM") -> {
                    val minutes = actionType.split(":")[1].toInt()
                    registerDailyActionUseCase(entry, DailyAction.Move(minutes))
                }
            }
        }
    }

    private suspend fun createStructuralEntry(nodeId: String): TimelineEntry? {
        val node = repository.getNodeById(nodeId) ?: return null
        return TimelineEntry(
            instance = DailyInstance(
                id = "structural_virtual_$nodeId",
                target = ScheduleTarget.Node(nodeId),
                scheduledDate = LocalDate.now().toEpochDay(),
                titleSnapshot = node.title,
                descriptionSnapshot = node.description,
                status = DailyInstanceStatus.PLANNED
            ),
            isMaterialized = false
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
            registerDailyActionUseCase(entry, DailyAction.Complete(metadataJson))
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

    private fun findEntry(instanceId: String): TimelineEntry? {
        currentEntries.forEach { hierarchical ->
            if (hierarchical.root.instance.id == instanceId) return hierarchical.root
            hierarchical.children.forEach { child ->
                if (child.instance.id == instanceId) return child
            }
        }
        return null
    }

    private fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun calculateProgress(entries: List<HierarchicalTimelineEntry>): TodayProgress {
        // Deprecated: logic moved to mapToUiState to account for structural children
        return TodayProgress(0, 0)
    }

    private fun findNextActivity(items: List<TodayTimelineUiModel>): TodayTimelineUiModel? {
        // 1. Prioritize CURRENT
        val current = items.find { it.status == DailyInstanceStatus.PLANNED && it.temporalState == TimelineTemporalState.CURRENT }
        if (current != null) return current
        
        // 2. Next UPCOMING
        return items.find { it.status == DailyInstanceStatus.PLANNED && it.temporalState == TimelineTemporalState.UPCOMING }
    }

    fun onAddAdHoc(title: String, startTime: Int? = null) {
        val today = LocalDate.now()
        val minutes = startTime ?: (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE))
        val adHocInstance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = today.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "Ad-hoc task",
            plannedStartTime = minutes,
            plannedDurationMinutes = 30, // Default duration for intersections
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = true
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(adHocInstance)
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad añadida"))
        }
    }
}
