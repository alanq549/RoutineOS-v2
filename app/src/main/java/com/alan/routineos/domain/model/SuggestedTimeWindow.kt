package com.alan.routineos.domain.model

/**
 * Represents a temporal proposal for a child instance.
 */
data class SuggestedTimeWindow(
    val childId: String,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int
)
