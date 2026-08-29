package com.alan.routineos.feature.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.usecase.ResolveTimelineUseCase
import com.alan.routineos.domain.usecase.TimelineEntry
import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.today.model.ConflictDetailUiModel
import com.alan.routineos.feature.today.model.ConflictUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
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
    private val resolveTimelineUseCase: ResolveTimelineUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    
    private val _uiState = MutableStateFlow(PlanningUiState(isLoading = true))
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            _selectedDate.flatMapLatest { date ->
                resolveTimelineUseCase(date).map { entries ->
                    val scheduled = entries.filter { it.instance.plannedStartTime != null }
                    val unscheduled = entries.filter { it.instance.plannedStartTime == null }
                    val exceptions = entries.filter { it.isMaterialized && it.instance.status != DailyInstanceStatus.PLANNED }

                    PlanningUiState(
                        isLoading = false,
                        selectedDate = date,
                        weekDays = generateWeekDays(date),
                        timelineEntries = scheduled.map { it.toUiModel() },
                        unscheduledItems = unscheduled.map { it.toUiModel() },
                        exceptions = exceptions.map { it.toUiModel() }
                    )
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
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

    private fun TimelineEntry.toUiModel(): TodayTimelineUiModel {
        val startTime = instance.plannedStartTime?.let { formatMinutes(it) } ?: ""
        return TodayTimelineUiModel(
            id = instance.id,
            title = instance.titleSnapshot,
            description = instance.descriptionSnapshot,
            timeRangeText = startTime,
            status = instance.status,
            isMaterialized = isMaterialized,
            conflict = conflict?.let { 
                ConflictUiModel(
                    hasConflict = it.hasConflict, 
                    impact = it.impact, 
                    details = it.details.map { d -> 
                        ConflictDetailUiModel(d.otherInstanceId, "OTRA", d.relationship, d.impact, d.isInterruption) 
                    },
                    suggestions = it.suggestions
                )
            } ?: ConflictUiModel(false)
        )
    }

    private fun formatMinutes(minutes: Int): String {
        return "%02d:%02d".format(minutes / 60, minutes % 60)
    }

    fun onDaySelected(dayId: String) {
        _selectedDate.value = LocalDate.parse(dayId)
    }
}
