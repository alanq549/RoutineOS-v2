package com.alan.routineos.domain.model

data class SystemWithStats(
    val system: LifeSystem,
    val activityCount: Int,
    val instanceCount: Int,
    val completionCount: Int,
    val successRate: Float
)
