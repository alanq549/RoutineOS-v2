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
    private val simulateMoveUseCase: SimulateMoveUseCase
) : ViewModel() {

    private val localeES = Locale.forLanguageTag("es-ES")
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _expandedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _editingSpontaneousEntry = MutableStateFlow<HierarchicalTimelineEntry?>(null)
    private val _isCreatingNewEvent = MutableStateFlow(false)
    private val _pendingMove = MutableStateFlow<PendingMove?>(null)

    private var currentEntries = listOf<HierarchicalTimelineEntry>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlanningUiState> = combine(
        _selectedDate,
        _expandedIds,
        _editingSpontaneousEntry,
        _isCreatingNewEvent,
        _pendingMove
    ) { date, expanded, editing, creating, pending ->
        // This emit is synchronous in the combine
        DataPackage(date, expanded, editing, creating, pending)
    }.flatMapLatest { p ->
        getHierarchicalTimelineUseCase(p.date).map { entries ->
            currentEntries = entries
            val scheduled = entries.filter { it.effectiveStartTimeMinutes != null }
            val unscheduled = entries.filter { it.effectiveStartTimeMinutes == null }
            val exceptions = entries.filter { it.root.isMaterialized && it.root.instance.status != DailyInstanceStatus.PLANNED }

            PlanningUiState(
                isLoading = false,
                selectedDate = p.date,
                weekDays = generateWeekDays(p.date),
                weekRangeText = generateWeekRangeText(p.date),
                isShowingToday = isDateInCurrentWeek(p.date),
                timelineEntries = scheduled.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                unscheduledItems = unscheduled.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                exceptions = exceptions.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                editingSpontaneousEntry = p.editing,
                isCreatingNewEvent = p.creating,
                pendingMove = p.pending
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PlanningUiState(isLoading = true)
    )

    private data class DataPackage(
        val date: LocalDate,
        val expanded: Set<String>,
        val editing: HierarchicalTimelineEntry?,
        val creating: Boolean,
        val pending: PendingMove?
    )

    private fun generateWeekDays(selected: LocalDate): List<PlanningDay> {
        val startOfWeek = selected.minusDays(selected.dayOfWeek.value.toLong() - 1)
        return (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            PlanningDay(
                id = date.toString(),
                name = date.dayOfWeek.getDisplayName(TextStyle.SHORT, localeES).replaceFirstChar { it.uppercase() },
                dayOfMonth = date.dayOfMonth.toString(),
                isSelected = date.isEqual(selected)
            )
        }
    }

    private fun generateWeekRangeText(selected: LocalDate): String {
        val startOfWeek = selected.minusDays(selected.dayOfWeek.value.toLong() - 1)
        val endOfWeek = startOfWeek.plusDays(6)
        
        val startDay = startOfWeek.dayOfMonth
        val startMonth = startOfWeek.month.getDisplayName(TextStyle.FULL, localeES)
        val endDay = endOfWeek.dayOfMonth
        val endMonth = endOfWeek.month.getDisplayName(TextStyle.FULL, localeES)
        val year = startOfWeek.year

        return if (startOfWeek.month == endOfWeek.month) {
            "$startDay – $endDay $startMonth $year"
        } else {
            val endYear = endOfWeek.year
            if (startOfWeek.year == endOfWeek.year) {
                "$startDay $startMonth – $endDay $endMonth $year"
            } else {
                "$startDay $startMonth $year – $endDay $endMonth $endYear"
            }
        }
    }

    private fun isDateInCurrentWeek(date: LocalDate): Boolean {
        val today = LocalDate.now()
        val startOfCurrentWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val endOfCurrentWeek = startOfCurrentWeek.plusDays(6)
        return !date.isBefore(startOfCurrentWeek) && !date.isAfter(endOfCurrentWeek)
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
            endTimeMinutes = root.instance.plannedEndTime,
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
        _expandedIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun onAddEventClick() {
        val tempEntry = HierarchicalTimelineEntry(
            root = TimelineEntry(
                instance = DailyInstance(
                    id = "new_event_${UUID.randomUUID()}",
                    target = null,
                    scheduledDate = _selectedDate.value.toEpochDay(),
                    titleSnapshot = "",
                    descriptionSnapshot = "New Event",
                    status = DailyInstanceStatus.MODIFIED,
                    isAdHoc = true
                ),
                isMaterialized = true
            )
        )
        _isCreatingNewEvent.value = true
        _editingSpontaneousEntry.value = tempEntry
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
                    _isCreatingNewEvent.value = false
                    _editingSpontaneousEntry.value = entry
                }
            }
        }
    }

    private fun onAttemptMove(instanceId: String, newStartTime: Int, newEndTime: Int? = null) {
        val entry = findEntry(instanceId) ?: return
        
        val currentInstances = collectAllInstances(currentEntries)
        val conflict = simulateMoveUseCase(
            currentInstances = currentInstances,
            targetId = instanceId,
            newStartTime = newStartTime,
            newEndTime = newEndTime
        )

        if (conflict.impact == TemporalImpact.WARNING) {
            _pendingMove.value = PendingMove(entry, newStartTime, newEndTime, conflict)
        } else {
            performMove(entry, newStartTime)
        }
    }

    fun onConfirmPendingMove() {
        val pending = _pendingMove.value ?: return
        if (pending.entry.root.instance.isAdHoc && pending.newEndTime != null) {
            onUpdateSpontaneousSchedule(pending.entry.root.instance.id, pending.newStartTime, pending.newEndTime)
        } else {
            performMove(pending.entry, pending.newStartTime)
        }
        onCancelPendingMove()
    }

    fun onCancelPendingMove() {
        _pendingMove.value = null
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

    fun onDismissSpontaneousEditor() {
        _isCreatingNewEvent.value = false
        _editingSpontaneousEntry.value = null
    }

    fun onDeleteInstance(id: String) {
        viewModelScope.launch {
            repository.deleteDailyInstance(id)
            onDismissSpontaneousEditor()
        }
    }

    fun onUpdateSpontaneousTitle(id: String, title: String) {
        if (_isCreatingNewEvent.value && _editingSpontaneousEntry.value?.root?.instance?.id == id) {
            val current = _editingSpontaneousEntry.value!!
            val updated = current.root.instance.copy(titleSnapshot = title)
            _editingSpontaneousEntry.value = current.copy(root = current.root.copy(instance = updated))
            return
        }
        
        val entry = findEntry(id) ?: return
        val updated = entry.root.instance.copy(titleSnapshot = title)
        viewModelScope.launch {
            repository.upsertDailyInstance(updated)
        }
    }

    fun onUpdateSpontaneousSchedule(id: String, start: Int?, end: Int?) {
        if (_isCreatingNewEvent.value && _editingSpontaneousEntry.value?.root?.instance?.id == id) {
            val current = _editingSpontaneousEntry.value!!
            val updated = current.root.instance.copy(
                plannedStartTime = start,
                plannedEndTime = end,
                plannedDurationMinutes = if (start != null && end != null) end - start else null
            )
            _editingSpontaneousEntry.value = current.copy(root = current.root.copy(instance = updated))
            return
        }

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

    fun onSaveNewEvent() {
        val newEvent = _editingSpontaneousEntry.value?.root?.instance ?: return
        if (newEvent.titleSnapshot.isBlank()) return

        viewModelScope.launch {
            repository.upsertDailyInstance(newEvent.copy(id = UUID.randomUUID().toString()))
            onDismissSpontaneousEditor()
        }
    }

    fun nextWeek() {
        _selectedDate.value = _selectedDate.value.plusWeeks(1)
    }

    fun prevWeek() {
        _selectedDate.value = _selectedDate.value.minusWeeks(1)
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun jumpToDate(date: LocalDate) {
        _selectedDate.value = date
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
            findChildEntry(child.children, id)?.let { return it }
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
}
