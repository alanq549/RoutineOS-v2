package com.alan.routineos.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.GetHierarchicalTimelineUseCase
import com.alan.routineos.domain.usecase.RegisterDailyActionUseCase
import com.alan.routineos.domain.usecase.DailyAction
import com.alan.routineos.domain.usecase.TimelineEntry
import com.alan.routineos.feature.dashboard.ActivityDetailUiEvent
import com.alan.routineos.feature.today.model.TodayProgress
import com.alan.routineos.feature.today.model.TodaySubNodeUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getHierarchicalTimelineUseCase: GetHierarchicalTimelineUseCase,
    private val registerDailyActionUseCase: RegisterDailyActionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(isLoading = true))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ActivityDetailUiEvent>()
    val uiEvent: SharedFlow<ActivityDetailUiEvent> = _uiEvent.asSharedFlow()

    private var currentEntries = listOf<HierarchicalTimelineEntry>()

    init {
        loadData()
    }

    private fun loadData() {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
        
        viewModelScope.launch {
            getHierarchicalTimelineUseCase(today).collect { entries ->
                currentEntries = entries
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        dateText = today.format(dateFormatter).uppercase(),
                        progress = calculateProgress(entries),
                        timelineItems = entries.map { entry -> entry.toUiModel() },
                        nextActivity = findNextActivity(entries.map { entry -> entry.toUiModel() })
                    )
                }
            }
        }
    }

    fun onActionTriggered(instanceId: String, actionType: String) {
        val entry = findEntry(instanceId) ?: return
        
        viewModelScope.launch {
            when (actionType) {
                "SKIP" -> registerDailyActionUseCase(entry, DailyAction.Skip)
                "COMPLETE" -> handleCompleteRequest(entry)
            }
        }
    }

    private suspend fun handleCompleteRequest(entry: TimelineEntry) {
        val target = entry.instance.target
        if (target is ScheduleTarget.Node) {
            val schema = repository.getMetadataSchema(target.id, "NODE").firstOrNull()
            if (schema != null && schema.fields.isNotEmpty()) {
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

    private fun findEntry(instanceId: String): TimelineEntry? {
        currentEntries.forEach { hierarchical ->
            if (hierarchical.root.instance.id == instanceId) return hierarchical.root
            hierarchical.children.forEach { child ->
                if (child.instance.id == instanceId) return child
            }
        }
        return null
    }

    private fun HierarchicalTimelineEntry.toUiModel(): TodayTimelineUiModel {
        val children = children.map { it.toSubNodeModel() }
        val startTime = root.instance.plannedStartTime?.let { formatMinutes(it) } ?: ""
        
        return TodayTimelineUiModel(
            id = root.instance.id,
            title = root.instance.titleSnapshot,
            timeRangeText = startTime,
            status = root.instance.status,
            isMaterialized = root.isMaterialized,
            hasConflict = root.conflict?.hasConflict ?: false,
            subNodes = children
        )
    }

    private fun TimelineEntry.toSubNodeModel(): TodaySubNodeUiModel {
        return TodaySubNodeUiModel(
            id = instance.id,
            title = instance.titleSnapshot,
            timeText = instance.plannedStartTime?.let { formatMinutes(it) } ?: "",
            status = instance.status
        )
    }

    private fun formatMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    private fun calculateProgress(entries: List<HierarchicalTimelineEntry>): TodayProgress {
        val allEntries = entries.flatMap { listOf(it.root) + it.children }
        val completed = allEntries.count { it.instance.status == DailyInstanceStatus.MODIFIED }
        return TodayProgress(completed, allEntries.size) 
    }

    private fun findNextActivity(items: List<TodayTimelineUiModel>): TodayTimelineUiModel? {
        return items.firstOrNull { it.status == DailyInstanceStatus.PLANNED }
    }

    fun onAddAdHoc(title: String) {
        val today = LocalDate.now()
        val minutes = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE)
        
        val adHocInstance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = null,
            scheduledDate = today.toEpochDay(),
            titleSnapshot = title,
            descriptionSnapshot = "Ad-hoc task",
            plannedStartTime = minutes,
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = true
        )
        viewModelScope.launch {
            repository.upsertDailyInstance(adHocInstance)
            _uiEvent.emit(ActivityDetailUiEvent.SchedulingUpsertSuccess("Actividad añadida"))
        }
    }
}
