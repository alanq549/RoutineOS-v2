package com.alan.routineos.feature.planning

import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import java.time.LocalDate

data class PlanningUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<PlanningDay> = emptyList(),
    val timelineEntries: List<TodayTimelineUiModel> = emptyList(),
    val unscheduledItems: List<TodayTimelineUiModel> = emptyList(),
    val exceptions: List<TodayTimelineUiModel> = emptyList()
)
