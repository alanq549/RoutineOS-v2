package com.alan.routineos.feature.planning

import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.HierarchicalTimelineEntry
import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import java.time.LocalDate

enum class EditorRole {
    TASK,
    SPONTANEOUS,
    SCHEDULED
}

data class PlanningUiState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val weekDays: List<PlanningDay> = emptyList(),
    val weekRangeText: String = "",
    val isShowingToday: Boolean = true,
    val timelineEntries: List<TodayTimelineUiModel> = emptyList(),
    val unscheduledItems: List<TodayTimelineUiModel> = emptyList(),
    val exceptions: List<TodayTimelineUiModel> = emptyList(),
    val editingSpontaneousEntry: HierarchicalTimelineEntry? = null,
    val editorRole: EditorRole = EditorRole.SPONTANEOUS,
    val isCreatingNewEvent: Boolean = false,
    val pendingMove: com.alan.routineos.domain.model.PendingMove? = null
)
