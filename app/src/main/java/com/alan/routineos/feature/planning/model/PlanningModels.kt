package com.alan.routineos.feature.planning.model

enum class PlanningBlockType {
    FLEXIBLE, EXACT, ACTIVITY
}

data class PlanningBlock(
    val id: String,
    val title: String,
    val type: PlanningBlockType,
    val startTime: String,
    val endTime: String?,
    val description: String?,
    val location: String? = null,
    val conflictMessage: String? = null
)

data class PlanningDay(
    val id: String,
    val name: String,
    val dayOfMonth: String,
    val isSelected: Boolean = false
)

data class PlanningException(
    val id: String,
    val dateText: String,
    val label: String,
    val title: String,
    val timeRange: String
)

data class PlanningUnscheduled(
    val id: String,
    val title: String,
    val description: String,
    val icon: String // String identifier for Material Icons or custom icons
)
