package com.alan.routineos.feature.planning

import com.alan.routineos.feature.planning.model.*

data class PlanningUiState(
    val isLoading: Boolean = false,
    val selectedSegment: PlanningSegment = PlanningSegment.PLANNER,
    val days: List<PlanningDay> = emptyList(),
    val blocks: List<PlanningBlock> = emptyList(),
    val exceptions: List<PlanningException> = emptyList(),
    val unscheduledItems: List<PlanningUnscheduled> = emptyList()
)

enum class PlanningSegment {
    PLANNER, ACTIVITIES
}
