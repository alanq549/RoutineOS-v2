package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * Materializes or moves a BacklogItem assignment into a specific day.
 * Enforces Single Active Instance Rule: an OPEN BacklogItem can have at most ONE
 * active uncompleted DailyInstance (PLANNED / MODIFIED) in the agenda.
 */
class AssignBacklogItemToDayUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        item: BacklogItem,
        date: LocalDate
    ) {
        // Find existing instances for this backlog item across current schedule
        val todayEpoch = LocalDate.now().toEpochDay()
        val instances = repository.getDailyInstancesForDateRange(todayEpoch - 30, todayEpoch + 30)
            .firstOrNull() ?: emptyList()

        val activeInstance = instances.find { 
            it.backlogId == item.id && (it.status == DailyInstanceStatus.PLANNED || it.status == DailyInstanceStatus.MODIFIED)
        }

        if (activeInstance != null) {
            // Move existing active instance to new date
            repository.upsertDailyInstance(activeInstance.copy(scheduledDate = date.toEpochDay()))
        } else {
            // Materialize new DailyInstance
            val newInstance = DailyInstance(
                id = UUID.randomUUID().toString(),
                target = item.definitionId?.let { ScheduleTarget.Definition(it) },
                scheduledDate = date.toEpochDay(),
                titleSnapshot = item.title,
                descriptionSnapshot = "",
                plannedStartTime = null,
                plannedEndTime = null,
                plannedDurationMinutes = null,
                status = DailyInstanceStatus.PLANNED,
                mobility = TemporalMobility.FLEXIBLE,
                sourceRuleId = null,
                isAdHoc = (item.definitionId == null),
                parentInstanceId = null,
                backlogId = item.id,
                actionProtocol = if (item.definitionId != null) ActionProtocol.TIMER else ActionProtocol.CHECK,
                role = if (item.definitionId != null) DailyInstanceRole.ACTIVITY else DailyInstanceRole.TASK,
                reminderAbs = null,
                reminderRel = null,
                associatedInstanceId = null
            )
            repository.upsertDailyInstance(newInstance)
        }
    }
}
