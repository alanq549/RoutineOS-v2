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
    private val distributeChildrenInWindowUseCase: DistributeChildrenInWindowUseCase,
    private val simulateMoveUseCase: SimulateMoveUseCase,
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
                    mapToUiState(
                        entries = entries, 
                        metaMap = metaMap, 
                        expanded = expanded, 
                        today = today, 
                        dateFormatter = dateFormatter,
                        currentEditing = _uiState.value.editingSpontaneousEntry,
                        currentSuggestions = _uiState.value.temporalSuggestions
                    )
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
        dateFormatter: DateTimeFormatter,
        currentEditing: HierarchicalTimelineEntry? = null,
        currentSuggestions: List<SuggestedTimeWindow> = emptyList()
    ): TodayUiState {
        val now = timeProvider.now()
        val currentMinutes = now.hour * 60 + now.minute
        
        val allLeaves = collectAllLeafEntries(entries)
        val totalTasks = allLeaves.size
        val completedTasks = allLeaves.count { it.instance.status == DailyInstanceStatus.COMPLETED }

        val initialUiModels = entries.mapIndexed { index, entry ->
            val nextScheduled = entries.drop(index + 1).find { it.root.instance.plannedStartTime != null }
            val nextStartTime = nextScheduled?.root?.instance?.plannedStartTime
            
            // Sub-node mapping with time inference
            val subNodeModels = entry.children.mapIndexed { subIndex, child ->
                val childStartTime = child.root.instance.plannedStartTime ?: run {
                    val parentStart = entry.root.instance.plannedStartTime
                    val parentDuration = entry.totalDurationMinutes ?: (entry.children.size * 60)
                    if (parentStart != null && entry.children.isNotEmpty()) {
                        parentStart + (parentDuration / entry.children.size) * subIndex
                    } else parentStart
                }
                mapToSubNodeUiModel(child, metaMap, entries, childStartTime)
            }
            
            val rootTargetId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
            val rootMeta = metaMap[rootTargetId] ?: MetadataSnapshot()
            
            entry.toUiModel(subNodeModels, rootMeta, expanded.contains(entry.root.instance.id), currentMinutes, nextStartTime, entries)
        }

        // Interception Grouping Logic (Elevated: check if root or ANY subnode is being interrupted)
        val finalUiModels = mutableListOf<TodayTimelineUiModel>()
        val consumedInterrupterIds = mutableSetOf<String>()

        initialUiModels.forEach { model ->
            if (consumedInterrupterIds.contains(model.id)) return@forEach

            // Check if this root model OR any of its descendants are interrupted
            val allConflicts = collectAllConflicts(model)
            val interruption = allConflicts.flatMap { it.details }.find { it.isInterruption }
            
            val interrupter = if (interruption != null) {
                initialUiModels.find { it.id == interruption.otherInstanceId && it.conflict.isInterrupter }
            } else null

            if (interrupter != null && !consumedInterrupterIds.contains(interrupter.id)) {
                finalUiModels.add(model.copy(
                    interception = InterceptionUiModel(
                        interrupter = interrupter
                    )
                ))
                consumedInterrupterIds.add(interrupter.id)
            } else {
                finalUiModels.add(model)
            }
        }

        return TodayUiState(
            isLoading = false,
            dateText = today.format(dateFormatter).uppercase(),
            progress = TodayProgress(completedTasks, totalTasks),
            timelineItems = finalUiModels,
            nextActivity = findNextActivity(finalUiModels),
            focusItemId = calculateFocusItemId(finalUiModels),
            editingSpontaneousEntry = currentEditing,
            temporalSuggestions = currentSuggestions
        )
    }

    private fun collectAllConflicts(model: TodayTimelineUiModel): List<ConflictUiModel> {
        val list = mutableListOf(model.conflict)
        fun collect(subs: List<TodaySubNodeUiModel>) {
            subs.forEach { 
                list.add(it.conflict)
                collect(it.children)
            }
        }
        collect(model.subNodes)
        return list
    }

    private fun mapToSubNodeUiModel(
        entry: HierarchicalTimelineEntry, 
        metaMap: Map<String, MetadataSnapshot>,
        allEntries: List<HierarchicalTimelineEntry>,
        inferredStartTime: Int? = null
    ): TodaySubNodeUiModel {
        val nodeId = (entry.root.instance.target as? ScheduleTarget.Node)?.id
        val meta = if (nodeId != null) metaMap[nodeId] ?: MetadataSnapshot() else MetadataSnapshot()
        
        val startTime = entry.root.instance.plannedStartTime ?: inferredStartTime
        val timeText = startTime?.let { formatMinutes(it) } ?: ""

        val childModels = entry.children.mapIndexed { index, child ->
            val childStartTime = child.root.instance.plannedStartTime ?: run {
                val parentDuration = entry.totalDurationMinutes ?: (entry.children.size * 60)
                if (startTime != null && entry.children.isNotEmpty()) {
                    startTime + (parentDuration / entry.children.size) * index
                } else startTime
            }
            mapToSubNodeUiModel(child, metaMap, allEntries, childStartTime)
        }
        
        return TodaySubNodeUiModel(
            id = entry.root.instance.id,
            title = entry.root.instance.titleSnapshot,
            timeText = timeText,
            startTimeMinutes = startTime,
            status = entry.root.instance.status,
            contextMetadata = meta.context,
            operationalMetadata = meta.operational,
            completedCount = entry.completedCount,
            totalCount = entry.totalCount,
            completion = entry.completion,
            children = childModels,
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

        val interrupter = hasConflict && current.isAdHoc

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
        val start = root.instance.plannedStartTime ?: effectiveStartTimeMinutes
        val duration = totalDurationMinutes
        val explicitEnd = root.instance.plannedEndTime ?: if (start != null && duration != null) start + duration else null
        val finalEnd = explicitEnd ?: inferredEndTime

        val timeRange = if (start != null) {
            val isExplicitPoint = root.instance.plannedStartTime != null && 
                                root.instance.plannedEndTime == null && 
                                root.instance.plannedDurationMinutes == null
            
            if (!isExplicitPoint && finalEnd != null && finalEnd > start) {
                "${formatMinutes(start)} - ${formatMinutes(finalEnd)}"
            } else {
                formatMinutes(start)
            }
        } else ""

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
            timeRangeText = timeRange,
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
            completion = completion,
            actionProtocol = root.instance.actionProtocol,
            context = if (associatedItems.isNotEmpty() || note != null || root.instance.reminderAbs != null || root.instance.reminderRel != null) {
                ContextItemsUiModel(
                    tasks = associatedItems.map { 
                        AssociatedTaskUiModel(
                            id = it.root.instance.id,
                            title = it.root.instance.titleSnapshot,
                            status = it.root.instance.status,
                            isCompleted = it.root.instance.status == DailyInstanceStatus.COMPLETED
                        )
                    },
                    reminder = when {
                        root.instance.reminderAbs != null -> AssociatedReminderUiModel(formatMinutes(root.instance.reminderAbs), false)
                        root.instance.reminderRel != null -> AssociatedReminderUiModel("${root.instance.reminderRel} min", true, root.instance.reminderRel)
                        else -> null
                    },
                    note = note?.let { AssociatedNoteUiModel(it.id, it.content, "", it.titleSnapshot) }
                )
            } else null
        )
    }

    fun onActionTriggered(instanceId: String, actionType: String) {
        viewModelScope.launch {
            val entry = findEntry(instanceId) ?: if (instanceId.startsWith("structural_virtual_")) {
                createStructuralEntry(instanceId.removePrefix("structural_virtual_"))
            } else null
            
            if (entry == null) return@launch

            when {
                actionType == "SKIP" -> registerDailyActionUseCase(entry, DailyAction.Skip)
                actionType == "RESET" -> registerDailyActionUseCase(entry, DailyAction.Reset)
                actionType == "COMPLETE" -> handleCompleteRequest(entry)
                actionType.startsWith("MOVE_TO:") -> {
                    val minutes = actionType.removePrefix("MOVE_TO:").toInt()
                    registerDailyActionUseCase(entry, DailyAction.Move(minutes))
                }
                actionType.startsWith("MOVE_CONFIRM") -> {
                    val minutes = actionType.split(":")[1].toInt()
                    onAttemptMove(instanceId, minutes)
                }
                actionType == "EDIT_SPONTANEOUS" -> onEditSpontaneous(instanceId)
                actionType == "DELETE_INSTANCE" -> onDeleteInstance(instanceId)
            }
        }
    }

    private fun onAttemptMove(instanceId: String, newStartTime: Int, newEndTime: Int? = null) {
        val entry = findEntry(instanceId) ?: return
        
        // 1. Collect current instances for simulation
        val currentInstances = collectAllInstances(currentEntries)
        
        // 2. Simulate
        val conflict = simulateMoveUseCase(
            currentInstances = currentInstances,
            targetId = instanceId,
            newStartTime = newStartTime,
            newEndTime = newEndTime
        )

        // 3. Decision
        if (conflict.impact == TemporalImpact.WARNING) {
            _uiState.update { it.copy(pendingMove = PendingMove(entry, newStartTime, newEndTime, conflict)) }
        } else {
            performMove(entry, newStartTime)
        }
    }

    fun onConfirmPendingMove() {
        val pending = _uiState.value.pendingMove ?: return
        
        // If it was an ad-hoc edit through the sheet, it might have an explicit end.
        // If it was a timeline move, we use the recursive logic.
        if (pending.entry.root.instance.isAdHoc && pending.newEndTime != null) {
            performUpdateSchedule(pending.entry, pending.newStartTime, pending.newEndTime)
        } else {
            performMove(pending.entry, pending.newStartTime)
        }
        onCancelPendingMove()
    }

    fun onCancelPendingMove() {
        _uiState.update { it.copy(pendingMove = null) }
    }

    private fun performMove(entry: HierarchicalTimelineEntry, newStartTime: Int) {
        viewModelScope.launch {
            registerDailyActionUseCase(entry, DailyAction.Move(newStartTime))
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad reprogramada"))
        }
    }

    private fun collectAllInstances(entries: List<HierarchicalTimelineEntry>): List<DailyInstance> {
        val list = mutableListOf<DailyInstance>()
        fun collect(items: List<HierarchicalTimelineEntry>) {
            items.forEach { 
                list.add(it.root.instance)
                collect(it.children)
            }
        }
        collect(entries)
        return list
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

    private suspend fun handleCompleteRequest(entry: HierarchicalTimelineEntry) {
        val target = entry.root.instance.target
        if (target is ScheduleTarget.Node) {
            val schema = repository.getMetadataSchema(target.id, "NODE").firstOrNull()
            val operationalFields = schema?.fields?.filter { !it.isReadOnly } ?: emptyList()
            if (operationalFields.isNotEmpty()) {
                _uiState.update { it.copy(captureSchema = schema, captureTargetId = entry.root.instance.id) }
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

    fun onAddAdHoc(
        title: String,
        startTime: Int? = null,
        parentInstanceId: String? = null,
        endTime: Int? = null
    ) {
        val today = LocalDate.now()
        val minutes = startTime ?: (LocalTime.now().hour * 60 + LocalTime.now().minute)
        val duration = if (startTime != null && endTime != null) endTime - startTime else null
        
        val adHocInstance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = today.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "Ad-hoc task",
            plannedStartTime = if (startTime == null && endTime == null) null else minutes,
            plannedEndTime = endTime,
            plannedDurationMinutes = duration,
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = true,
            parentInstanceId = parentInstanceId
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(adHocInstance)
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad añadida"))
        }
    }

    fun onDistributeChildren(parentInstanceId: String) {
        val parentEntry = findEntry(parentInstanceId) ?: return
        val childrenWithoutTime = parentEntry.children
            .filter { it.root.instance.plannedStartTime == null }
            .map { it.root.instance }

        if (childrenWithoutTime.isEmpty()) return

        val suggestions = distributeChildrenInWindowUseCase(parentEntry.root.instance, childrenWithoutTime)
        _uiState.update { it.copy(temporalSuggestions = suggestions) }
    }

    fun onConfirmDistribution(suggestions: List<SuggestedTimeWindow>) {
        viewModelScope.launch {
            suggestions.forEach { suggestion ->
                val entry = findEntry(suggestion.childId) ?: return@forEach
                val updatedInstance = entry.root.instance.copy(
                    plannedStartTime = suggestion.startTimeMinutes,
                    plannedEndTime = suggestion.endTimeMinutes,
                    plannedDurationMinutes = suggestion.endTimeMinutes - suggestion.startTimeMinutes,
                    status = DailyInstanceStatus.MODIFIED
                )
                repository.upsertDailyInstance(updatedInstance)
            }
            _uiState.update { it.copy(temporalSuggestions = emptyList()) }
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Horarios aplicados"))
        }
    }

    fun clearSuggestions() {
        _uiState.update { it.copy(temporalSuggestions = emptyList()) }
    }

    fun onEditSpontaneous(id: String) {
        val entry = findEntry(id) ?: return
        _uiState.update { it.copy(editingSpontaneousEntry = entry) }
    }

    fun onDismissSpontaneousEditor() {
        _uiState.update { it.copy(editingSpontaneousEntry = null) }
    }

    fun onDeleteInstance(id: String) {
        viewModelScope.launch {
            repository.deleteDailyInstance(id)
            onDismissSpontaneousEditor()
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad eliminada"))
        }
    }

    fun onUpdateInstanceTitle(id: String, title: String) {
        val entry = findEntry(id) ?: return
        val updated = entry.root.instance.copy(titleSnapshot = title)
        viewModelScope.launch {
            repository.upsertDailyInstance(updated)
            if (_uiState.value.editingSpontaneousEntry?.root?.instance?.id == id) {
                _uiState.update { it.copy(editingSpontaneousEntry = entry.copy(root = entry.root.copy(instance = updated))) }
            }
        }
    }

    fun onUpdateInstanceSchedule(id: String, startTime: Int?, endTime: Int?) {
        val entry = findEntry(id) ?: return
        
        if (startTime != null) {
            val currentInstances = collectAllInstances(currentEntries)
            val simulation = simulateMoveUseCase(currentInstances, id, startTime, endTime)
            
            if (simulation.impact == TemporalImpact.WARNING) {
                _uiState.update { it.copy(pendingMove = PendingMove(entry, startTime, endTime, simulation)) }
                return
            }
        }
        
        performUpdateSchedule(entry, startTime, endTime)
    }

    private fun performUpdateSchedule(entry: HierarchicalTimelineEntry, startTime: Int?, endTime: Int?) {
        val duration = if (startTime != null && endTime != null) endTime - startTime else null
        val updated = entry.root.instance.copy(
            plannedStartTime = startTime,
            plannedEndTime = endTime,
            plannedDurationMinutes = duration,
            status = DailyInstanceStatus.MODIFIED
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(updated)
            // Refresh editor state to prevent UI desync or accidental dismissal
            if (_uiState.value.editingSpontaneousEntry?.root?.instance?.id == entry.root.instance.id) {
                _uiState.update { it.copy(editingSpontaneousEntry = entry.copy(root = entry.root.copy(instance = updated))) }
            }
        }
    }
}
