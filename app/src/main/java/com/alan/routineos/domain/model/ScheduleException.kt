package com.alan.routineos.domain.model

enum class ScheduleExceptionType {
    SKIPPED,
    RESCHEDULED
}

data class ScheduleException(
    val id: String,
    val scheduleRuleId: String,
    val originalDate: Long, // Epoch Day
    val type: ScheduleExceptionType,
    val newDate: Long? = null // Epoch Day
)
