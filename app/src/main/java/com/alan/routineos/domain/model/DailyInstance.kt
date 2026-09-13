package com.alan.routineos.domain.model

enum class DailyInstanceStatus {
    PLANNED,
    MODIFIED,
    COMPLETED,
    OMITTED
}

enum class ActionProtocol {
    TIMER,
    CHECK
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
    val mobility: TemporalMobility = TemporalMobility.FLEXIBLE,
    val sourceRuleId: String? = null,
    val isAdHoc: Boolean = false,
    val parentInstanceId: String? = null,
    val backlogId: String? = null,
    val actionProtocol: ActionProtocol = ActionProtocol.TIMER,
    val reminderAbs: Int? = null,
    val reminderRel: Int? = null,
    val associatedInstanceId: String? = null
)
