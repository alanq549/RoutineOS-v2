package com.alan.routineos.domain.model

enum class ScheduleRuleType {
    FIXED_DAYS,
    FLEXIBLE_FREQUENCY
}

sealed class ScheduleTarget {
    data class Definition(val id: String) : ScheduleTarget()
    data class Node(val id: String) : ScheduleTarget()
}

data class ScheduleRule(
    val id: String,
    val target: ScheduleTarget,
    val type: ScheduleRuleType,
    val daysOfWeek: Set<Int> = emptySet(), // 1 (Mon) to 7 (Sun)
    val frequencyPerPeriod: Int = 0,
    val startTime: Int? = null,
    val endTime: Int? = null,
    val durationMinutes: Int? = null,
    val metadataJson: String = "{}"
)
