package com.alan.routineos.feature.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.planning.model.SearchTargetUiModel
import com.alan.routineos.feature.planning.model.UnifiedLinkingResult
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
    private val _editorRole = MutableStateFlow(EditorRole.EVENT)
    private val _pendingMove = MutableStateFlow<PendingMove?>(null)

    // Context Drafts
    private val _draftTasks = MutableStateFlow<List<DailyInstance>>(emptyList())
    private val _draftNote = MutableStateFlow<String>("")
    private val _draftReminderAbs = MutableStateFlow<Int?>(null)
    private val _draftReminderRel = MutableStateFlow<Int?>(null)

    // Contextual Linking
    private val _catalogSearchQuery = MutableStateFlow("")
    private val _selectedSemanticTarget = MutableStateFlow<SearchTargetUiModel?>(null)
    private val _selectedContextualOccurrence = MutableStateFlow<TodayTimelineUiModel?>(null)

    private var currentEntries = listOf<HierarchicalTimelineEntry>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlanningUiState> = combine(
        _selectedDate,
        _expandedIds,
        _editingSpontaneousEntry,
        _isCreatingNewEvent,
        _pendingMove,
        _draftTasks,
        _draftNote,
        _draftReminderAbs,
        _draftReminderRel,
        _editorRole,
        _catalogSearchQuery,
        _selectedSemanticTarget,
        _selectedContextualOccurrence,
        repository.getActivityDefinitions(),
        repository.getAllNodes()
    ) { args ->
        val date = args[0] as LocalDate
        val expanded = args[1] as Set<String>
        val editing = args[2] as HierarchicalTimelineEntry?
        val creating = args[3] as Boolean
        val pending = args[4] as PendingMove?
        val tasks = args[5] as List<DailyInstance>
        val note = args[6] as String
        val reminderAbs = args[7] as Int?
        val reminderRel = args[8] as Int?
        val role = args[9] as EditorRole
        val linkingQuery = args[10] as String
        val selectedSemantic = args[11] as SearchTargetUiModel?
        val selectedContextual = args[12] as TodayTimelineUiModel?
        val allDefs = args[13] as List<ActivityDefinition>
        val allNodes = args[14] as List<ActivityNode>

        DataPackage(
            date, expanded, editing, creating, pending, tasks, note, 
            reminderAbs, reminderRel, role, linkingQuery, selectedSemantic,
            selectedContextual, allDefs, allNodes
        )
    }.flatMapLatest { p ->
        getHierarchicalTimelineUseCase(p.date).map { entries ->
            currentEntries = entries
            val scheduled = entries.filter { it.effectiveStartTimeMinutes != null }
            val unscheduled = entries.filter { it.effectiveStartTimeMinutes == null }
            val exceptions = entries.filter { 
                it.root.isMaterialized && 
                !it.root.instance.isAdHoc &&
                it.root.instance.status != DailyInstanceStatus.PLANNED 
            }

            val allUiTimelineModels = (scheduled + unscheduled).map { it.toUiModel(p.expanded.contains(it.root.instance.id)) }

            val unifiedCatalog: List<UnifiedLinkingResult> = if (p.linkingQuery.isBlank()) emptyList() 
            else {
                val matchesDefs = p.allDefinitions
                    .filter { it.title.contains(p.linkingQuery, ignoreCase = true) }
                    .map { UnifiedLinkingResult.SemanticDefinition(it.id, it.title, it.description) }
                
                val matchesNodes = p.allNodes
                    .filter { it.title.contains(p.linkingQuery, ignoreCase = true) }
                    .map { node ->
                        val parentDef = p.allDefinitions.find { it.id == node.activityDefinitionId }
                        UnifiedLinkingResult.SemanticNode(node.id, node.title, parentDef?.title)
                    }

                val matchesOccurrences = allUiTimelineModels
                    .filter { it.id != p.editing?.root?.instance?.id }
                    .filter { it.title.contains(p.linkingQuery, ignoreCase = true) }
                    .map { UnifiedLinkingResult.ContextualOccurrence(it) }
                
                matchesDefs + matchesNodes + matchesOccurrences
            }

            PlanningUiState(
                isLoading = false,
                selectedDate = p.date,
                weekDays = generateWeekDays(p.date),
                weekRangeText = generateWeekRangeText(p.date),
                isShowingToday = isDateInCurrentWeek(p.date),
                timelineEntries = scheduled.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                unscheduledItems = unscheduled.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                exceptions = exceptions.map { it.toUiModel(p.expanded.contains(it.root.instance.id)) },
                editingSpontaneousEntry = p.editing?.let { entry ->
                    // Enrich editing entry with current drafts
                    entry.copy(
                        associatedItems = p.tasks.map { task ->
                            HierarchicalTimelineEntry(
                                root = TimelineEntry(task, true),
                                completion = if (task.status == DailyInstanceStatus.COMPLETED) HierarchyCompletion.COMPLETED else HierarchyCompletion.NOT_STARTED
                            )
                        },
                        note = if (p.note.isNotBlank()) Note(
                            id = "draft", 
                            content = p.note, 
                            instanceId = entry.root.instance.id,
                            dateSnapshot = p.date.toEpochDay(),
                            titleSnapshot = entry.root.instance.titleSnapshot
                        ) else null
                    )
                },
                editorRole = p.role,
                unifiedCatalog = unifiedCatalog,
                catalogSearchQuery = p.linkingQuery,
                selectedSemanticTarget = p.selectedSemantic,
                selectedContextualOccurrence = p.selectedContextual,
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
        val pending: PendingMove?,
        val tasks: List<DailyInstance>,
        val note: String,
        val reminderAbs: Int?,
        val reminderRel: Int?,
        val role: EditorRole,
        val linkingQuery: String,
        val selectedSemantic: SearchTargetUiModel?,
        val selectedContextual: TodayTimelineUiModel?,
        val allDefinitions: List<ActivityDefinition>,
        val allNodes: List<ActivityNode>
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
            actionProtocol = root.instance.actionProtocol,
            itemType = when (root.instance.role) {
                DailyInstanceRole.ACTIVITY -> PlanningItemType.ACTIVITY
                DailyInstanceRole.REMINDER -> PlanningItemType.REMINDER
                DailyInstanceRole.TASK -> PlanningItemType.TASK
            },
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
            subNodes = children.map { it.toSubNodeUiModel() },
            context = if (associatedItems.isNotEmpty() || note != null || root.instance.reminderAbs != null || root.instance.reminderRel != null) {
                ContextItemsUiModel(
                    tasks = associatedItems.map { 
                        AssociatedTaskUiModel(
                            id = it.root.instance.id,
                            title = it.root.instance.titleSnapshot,
                            status = it.root.instance.status,
                            isCompleted = it.root.instance.status == DailyInstanceStatus.COMPLETED,
                            timeText = it.effectiveStartTimeMinutes?.let { formatMinutes(it) } ?: ""
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
        val tempId = UUID.randomUUID().toString()
        val tempEntry = HierarchicalTimelineEntry(
            root = TimelineEntry(
                instance = DailyInstance(
                    id = tempId,
                    target = null,
                    scheduledDate = _selectedDate.value.toEpochDay(),
                    titleSnapshot = "",
                    descriptionSnapshot = "New Event",
                    status = DailyInstanceStatus.MODIFIED,
                    isAdHoc = true,
                    actionProtocol = ActionProtocol.TIMER,
                    role = DailyInstanceRole.ACTIVITY
                ),
                isMaterialized = true
            )
        )
        // Reset Drafts for new event
        _draftTasks.value = emptyList()
        _draftNote.value = ""
        _draftReminderAbs.value = null
        _draftReminderRel.value = null
        _editorRole.value = EditorRole.EVENT

        _isCreatingNewEvent.value = true
        _editingSpontaneousEntry.value = tempEntry
    }

    fun onUpdateEditorRole(role: EditorRole) {
        _editorRole.value = role
    }

    fun onAddDraftTask(title: String) {
        if (title.isBlank()) return
        val current = _draftTasks.value
        val newTask = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = _selectedDate.value.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "",
            actionProtocol = ActionProtocol.CHECK,
            role = DailyInstanceRole.TASK,
            status = DailyInstanceStatus.PLANNED,
            associatedInstanceId = _editingSpontaneousEntry.value?.root?.instance?.id
        )
        _draftTasks.value = current + newTask
    }

    fun onRemoveDraftTask(id: String) {
        _draftTasks.update { it.filter { t -> t.id != id } }
    }

    fun onUpdateDraftNote(content: String) {
        _draftNote.value = content
    }

    fun onUpdateDraftReminder(abs: Int?, rel: Int?) {
        _draftReminderAbs.value = abs
        _draftReminderRel.value = rel
    }

    fun onUpdateCatalogSearch(query: String) {
        _catalogSearchQuery.value = query
    }

    fun onLinkToDefinition(target: SearchTargetUiModel?) {
        _selectedSemanticTarget.value = target
        _catalogSearchQuery.value = ""
    }

    fun onLinkToOccurrence(occurrence: TodayTimelineUiModel?) {
        _selectedContextualOccurrence.value = occurrence
        _catalogSearchQuery.value = ""
    }

    fun onSelectUnifiedResult(result: UnifiedLinkingResult) {
        when (result) {
            is UnifiedLinkingResult.SemanticDefinition -> {
                onLinkToDefinition(SearchTargetUiModel(result.id, result.title, null, ScheduleTarget.Definition(result.id)))
            }
            is UnifiedLinkingResult.SemanticNode -> {
                onLinkToDefinition(SearchTargetUiModel(result.id, result.title, result.parentTitle, ScheduleTarget.Node(result.id)))
            }
            is UnifiedLinkingResult.ContextualOccurrence -> {
                onLinkToOccurrence(result.item)
            }
        }
    }

    fun onSetTimeToNow(id: String) {
        val now = Calendar.getInstance()
        val minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        onUpdateSpontaneousSchedule(id, minutes, null)
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
                    // Initialize Drafts
                    _draftTasks.value = entry.associatedItems.map { it.root.instance }
                    _draftNote.value = entry.note?.content ?: ""
                    _draftReminderAbs.value = entry.root.instance.reminderAbs
                    _draftReminderRel.value = entry.root.instance.reminderRel
                    
                    _editorRole.value = when (entry.root.instance.role) {
                        DailyInstanceRole.ACTIVITY -> EditorRole.EVENT
                        DailyInstanceRole.TASK -> EditorRole.TASK
                        DailyInstanceRole.REMINDER -> EditorRole.REMINDER
                    }

                    // Initialize Selected Definition (Semantic Target)
                    val target = entry.root.instance.target
                    if (target is ScheduleTarget.Definition) {
                        val def = repository.getActivityDefinitionById(target.id)
                        _selectedSemanticTarget.value = def?.let { SearchTargetUiModel(it.id, it.title, null, ScheduleTarget.Definition(it.id)) }
                    } else if (target is ScheduleTarget.Node) {
                        val node = repository.getNodeById(target.id)
                        val parentDef = node?.let { repository.getActivityDefinitionById(it.activityDefinitionId) }
                        _selectedSemanticTarget.value = node?.let { SearchTargetUiModel(it.id, it.title, parentDef?.title, ScheduleTarget.Node(it.id)) }
                    } else {
                        _selectedSemanticTarget.value = null
                    }

                    // Initialize Selected Contextual Occurrence
                    val assocId = entry.root.instance.associatedInstanceId
                    if (assocId != null) {
                        // Find the occurrence UI model in currentEntries to populate it
                        val assocEntry = findEntry(assocId)
                        _selectedContextualOccurrence.value = assocEntry?.toUiModel(false)
                    } else {
                        _selectedContextualOccurrence.value = null
                    }
                    
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
            // Sync editor state if editing existing item
            if (_editingSpontaneousEntry.value?.root?.instance?.id == id) {
                _editingSpontaneousEntry.value = entry.copy(root = entry.root.copy(instance = updated))
            }
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
            // Sync editor state
            if (_editingSpontaneousEntry.value?.root?.instance?.id == id) {
                _editingSpontaneousEntry.value = entry.copy(root = entry.root.copy(instance = updated))
            }
        }
    }

    fun onSaveNewEvent() {
        val entry = _editingSpontaneousEntry.value ?: return
        val role = _editorRole.value
        val linkedSemantic = _selectedSemanticTarget.value
        val linkedOccurrence = _selectedContextualOccurrence.value
        
        val anchor = entry.root.instance.copy(
            target = linkedSemantic?.target ?: entry.root.instance.target,
            associatedInstanceId = linkedOccurrence?.id ?: entry.root.instance.associatedInstanceId,
            actionProtocol = when (role) {
                EditorRole.EVENT -> ActionProtocol.TIMER
                else -> ActionProtocol.CHECK
            },
            role = if (_isCreatingNewEvent.value) {
                when (role) {
                    EditorRole.EVENT -> DailyInstanceRole.ACTIVITY
                    EditorRole.TASK -> DailyInstanceRole.TASK
                    EditorRole.REMINDER -> DailyInstanceRole.REMINDER
                }
            } else {
                entry.root.instance.role // Preserve role on edit
            },
            reminderAbs = if (role == EditorRole.REMINDER) _draftReminderAbs.value else null,
            reminderRel = if (role == EditorRole.REMINDER) _draftReminderRel.value else null,
            isAdHoc = linkedSemantic == null
        )
        if (anchor.titleSnapshot.isBlank()) return

        // Context items only for ACTIVITY role
        val tasks = if (anchor.role == DailyInstanceRole.ACTIVITY) {
            _draftTasks.value.map { it.copy(associatedInstanceId = anchor.id) }
        } else emptyList()

        val noteContent = _draftNote.value.trim()
        val note = if (noteContent.isNotBlank() && anchor.role == DailyInstanceRole.TASK) {
            // Preservation logic: use existing ID if editing
            val existingId = entry.note?.id ?: UUID.randomUUID().toString()
            Note(
                id = existingId,
                content = noteContent,
                instanceId = anchor.id,
                dateSnapshot = anchor.scheduledDate,
                targetTypeSnapshot = "INSTANCE",
                targetIdSnapshot = anchor.id,
                titleSnapshot = anchor.titleSnapshot
            )
        } else null

        viewModelScope.launch {
            repository.upsertActivityWithContext(anchor, tasks, note)
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
