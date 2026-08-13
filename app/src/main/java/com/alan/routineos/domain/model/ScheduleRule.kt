package com.alan.routineos.domain.model

enum class ScheduleRuleType {
    FIXED_DAYS,
    FLEXIBLE_FREQUENCY
}

data class ScheduleRule(
    val id: String,
    val nodeId: String,
    val type: ScheduleRuleType,
    val daysOfWeek: Set<Int> = emptySet(), // 1 (Mon) to 7 (Sun)
    val frequencyPerPeriod: Int = 0
)
