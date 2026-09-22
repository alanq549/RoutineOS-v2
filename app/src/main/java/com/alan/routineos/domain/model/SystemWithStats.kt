package com.alan.routineos.domain.model

data class SystemWithStats(
    val system: LifeSystem,
    val activityCount: Int,
    val scheduledCount: Int,
    val completedCount: Int,
    val skippedCount: Int,
    val pendingCount: Int,
    val successRate: Float? // Null if no results yet
)
