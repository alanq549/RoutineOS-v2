package com.alan.routineos.feature.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.today.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PlanningViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getHierarchicalTimelineUseCase: GetHierarchicalTimelineUseCase,
    private val registerDailyActionUseCase: RegisterDailyActionUseCase,
    private val addActivityToDayUseCase: AddActivityToDayUseCase,
    private val simulateMoveUseCase: SimulateMoveUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    
    private val _uiState = MutableStateFlow(PlanningUiState(isLoading = true))
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    private val expandedIds = MutableStateFlow<Set<String>>(emptySet())
    private var currentEntries = listOf<HierarchicalTimelineEntry>()

    init {
        loadData()
        loadAvailableActivities()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            combine(_selectedDate, expandedIds) { date, expanded ->
                date to expanded
            }.flatMapLatest { (date, expanded) ->
                getHierarchicalTimelineUseCase(date).map { entries ->
                    currentEntries = entries
                    val scheduled = entries.filter { it.effectiveStartTimeMinutes != null }
                    val unscheduled = entries.filter { it.effectiveStartTimeMinutes == null }
                    val exceptions = entries.filter { it.root.isMaterialized && it.root.instance.status != DailyInstanceStatus.PLANNED }

                    PlanningUiState(
                        isLoading = false,
                        selectedDate = date,
                        weekDays = generateWeekDays(date),
                        timelineEntries = scheduled.map { it.toUiModel(expanded.contains(it.root.instance.id)) },
                        unscheduledItems = unscheduled.map { it.toUiModel(expanded.contains(it.root.instance.id)) },
                        exceptions = exceptions.map { it.toUiModel(expanded.contains(it.root.instance.id)) },
                        editingSpontaneousEntry = _uiState.value.editingSpontaneousEntry,
                        isCatalogOpen = _uiState.value.isCatalogOpen,
                        availableActivities = _uiState.value.availableActivities
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadAvailableActivities() {
        repository.getActivityDefinitions()
            .onEach { defs ->
                _uiState.update { it.copy(availableActivities = defs) }
            }
            .launchIn(viewModelScope)
    }

    private fun generateWeekDays(selected: LocalDate): List<PlanningDay> {
        val startOfWeek = selected.minusDays(selected.dayOfWeek.value.toLong() - 1)
        return (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            PlanningDay(
                id = date.toString(),
                name = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                dayOfMonth = date.dayOfMonth.toString(),
                isSelected = date.isEqual(selected)
            )
        }
    }

    private fun HierarchicalTimelineEntry.toUiModel(isExpanded: Boolean): TodayTimelineUiModel {
        val start = effectiveStartTimeMinutes
        val duration = totalDurationMinutes
        val timeRange = if (start != null) {
            val end = if (duration != null) start + duration else null
            if (end != null && end > start) "${formatMinutes(start)} - ${formatMinutes(end)}"
            else formatMinutes(start)
        } else ""

        return TodayTimelineUiModel(
            id = root.instance.id,
            title = root.instance.titleSnapshot,
            description = root.instance.descriptionSnapshot,
            timeRangeText = timeRange,
            startTimeMinutes = start,
            status = root.instance.status,
            isMaterialized = root.isMaterialized,
            isAdHoc = root.instance.isAdHoc,
            isExpandable = children.isNotEmpty(),
            isExpanded = isExpanded,
            completedSubNodesCount = completedCount,
            totalSubNodesCount = totalCount,
            conflict = root.conflict?.let { 
                ConflictUiModel(
                    hasConflict = it.hasConflict, 
                    impact = it.impact, 
                    details = it.details.map { d -> 
                        ConflictDetailUiModel(d.otherInstanceId, "OTRA", d.relationship, d.impact, d.isInterruption) 
                    },
                    suggestions = it.suggestions
                )
            } ?: ConflictUiModel(false),
            subNodes = children.map { it.toSubNodeUiModel() }
        )
    }

    private fun HierarchicalTimelineEntry.toSubNodeUiModel(): TodaySubNodeUiModel {
        return TodaySubNodeUiModel(
            id = root.instance.id,
            title = root.instance.titleSnapshot,
            status = root.instance.status,
            startTimeMinutes = effectiveStartTimeMinutes,
            timeText = effectiveStartTimeMinutes?.let { formatMinutes(it) } ?: "",
            children = children.map { it.toSubNodeUiModel() }
        )
    }

    private fun formatMinutes(minutes: Int): String {
        return "%02d:%02d".format(minutes / 60, minutes % 60)
    }

    fun onDaySelected(dayId: String) {
        _selectedDate.value = LocalDate.parse(dayId)
    }

    fun toggleExpand(id: String) {
        expandedIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun onActionTriggered(instanceId: String, actionType: String) {
        val entry = findEntry(instanceId) ?: return
        viewModelScope.launch {
            when {
                actionType == "SKIP" -> registerDailyActionUseCase(entry, DailyAction.Skip)
                actionType == "RESET" -> registerDailyActionUseCase(entry, DailyAction.Reset)
                actionType.startsWith("MOVE_CONFIRM:") -> {
                    val minutes = actionType.removePrefix("MOVE_CONFIRM:").toInt()
                    onAttemptMove(instanceId, minutes)
                }
                actionType == "DELETE_INSTANCE" -> repository.deleteDailyInstance(instanceId)
                actionType == "EDIT_SPONTANEOUS" -> {
                    _uiState.update { it.copy(editingSpontaneousEntry = entry) }
                }
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
        
        if (pending.entry.root.instance.isAdHoc && pending.newEndTime != null) {
            onUpdateSpontaneousSchedule(pending.entry.root.instance.id, pending.newStartTime, pending.newEndTime)
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

    fun onAddFromCatalog(definitionId: String, startTime: Int? = null, endTime: Int? = null) {
        val activity = _uiState.value.availableActivities.find { it.id == definitionId } ?: return
        viewModelScope.launch {
            addActivityToDayUseCase(activity, _selectedDate.value, startTime, endTime)
            _uiState.update { it.copy(isCatalogOpen = false) }
        }
    }

    fun onAddAdHoc(title: String, startTime: Int? = null, endTime: Int? = null) {
        val adHocInstance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = _selectedDate.value.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "Ad-hoc (Planning)",
            plannedStartTime = startTime,
            plannedEndTime = endTime,
            plannedDurationMinutes = if (startTime != null && endTime != null) endTime - startTime else null,
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = true
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(adHocInstance)
        }
    }

    fun onOpenCatalog() {
        _uiState.update { it.copy(isCatalogOpen = true) }
    }

    fun onCloseCatalog() {
        _uiState.update { it.copy(isCatalogOpen = false) }
    }

    fun onDismissSpontaneousEditor() {
        _uiState.update { it.copy(editingSpontaneousEntry = null) }
    }

    fun onDeleteInstance(id: String) {
        viewModelScope.launch {
            repository.deleteDailyInstance(id)
            onDismissSpontaneousEditor()
        }
    }

    fun onUpdateSpontaneousTitle(id: String, title: String) {
        val entry = findEntry(id) ?: return
        val updated = entry.root.instance.copy(titleSnapshot = title)
        viewModelScope.launch {
            repository.upsertDailyInstance(updated)
        }
    }

    fun onUpdateSpontaneousSchedule(id: String, start: Int?, end: Int?) {
        val entry = findEntry(id) ?: return
        val updated = entry.root.instance.copy(
            plannedStartTime = start,
            plannedEndTime = end,
            plannedDurationMinutes = if (start != null && end != null) end - start else null,
            status = DailyInstanceStatus.MODIFIED
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(updated)
        }
    }

    private fun findEntry(id: String): HierarchicalTimelineEntry? {
        currentEntries.forEach { entry ->
            if (entry.root.instance.id == id) return entry
            val foundChild = findChild(entry.children, id)
            if (foundChild != null) return foundChild
        }
        return null
    }

    private fun findChild(children: List<HierarchicalTimelineEntry>, id: String): HierarchicalTimelineEntry? {
        children.forEach { child ->
            if (child.root.instance.id == id) return child
            val found = findChild(child.children, id)
            if (found != null) return found
        }
        return null
    }
}
