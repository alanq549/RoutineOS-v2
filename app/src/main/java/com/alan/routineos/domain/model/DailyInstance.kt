package com.alan.routineos.domain.model

enum class DailyInstanceStatus {
    PLANNED,
    MODIFIED,
    OMITTED
}

data class DailyInstance(
    val id: String,
    val target: ScheduleTarget?, // Null for ad-hoc without source
    val scheduledDate: Long, // Epoch Day
    val titleSnapshot: String,
    val descriptionSnapshot: String,
    val plannedStartTime: Int? = null,
    val plannedEndTime: Int? = null,
    val plannedDurationMinutes: Int? = null,
    val status: DailyInstanceStatus = DailyInstanceStatus.PLANNED,
    val sourceRuleId: String? = null,
    val isAdHoc: Boolean = false
)
