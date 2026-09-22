package com.alan.routineos.feature.planning

import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.HierarchicalTimelineEntry
import com.alan.routineos.feature.planning.model.PlanningDay
import com.alan.routineos.feature.planning.model.SearchTargetUiModel
import com.alan.routineos.feature.planning.model.UnifiedLinkingResult
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import java.time.LocalDate

enum class EditorRole {
    EVENT,
    TASK,
    REMINDER
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
    val editorRole: EditorRole = EditorRole.EVENT,
    val unifiedCatalog: List<UnifiedLinkingResult> = emptyList(),
    val catalogSearchQuery: String = "",
    val selectedSemanticTarget: SearchTargetUiModel? = null,
    val selectedContextualOccurrence: TodayTimelineUiModel? = null,
    val isCreatingNewEvent: Boolean = false,
    val pendingMove: com.alan.routineos.domain.model.PendingMove? = null
)
