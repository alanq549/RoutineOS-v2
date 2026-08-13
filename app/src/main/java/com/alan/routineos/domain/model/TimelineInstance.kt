package com.alan.routineos.domain.model

/**
 * A compute-only model representing a specific occurrence of an ActivityNode.
 */
data class TimelineInstance(
    val node: ActivityNode,
    val scheduledDate: Long, // Epoch Day
    val execution: ActivityExecution? = null
)
