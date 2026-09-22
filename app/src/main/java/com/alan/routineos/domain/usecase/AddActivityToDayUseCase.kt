package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/**
 * Materializes an existing ActivityDefinition into a specific day.
 * This creates a custom DailyInstance for that date without altering global rules.
 */
class AddActivityToDayUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        activity: ActivityDefinition,
        date: LocalDate,
        startTimeMinutes: Int? = null,
        endTimeMinutes: Int? = null
    ) {
        val duration = if (startTimeMinutes != null && endTimeMinutes != null) {
            endTimeMinutes - startTimeMinutes
        } else null

        val instance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = ScheduleTarget.Definition(activity.id),
            scheduledDate = date.toEpochDay(),
            titleSnapshot = activity.title,
            descriptionSnapshot = activity.description,
            plannedStartTime = startTimeMinutes,
            plannedEndTime = endTimeMinutes,
            plannedDurationMinutes = duration,
            status = DailyInstanceStatus.MODIFIED,
            isAdHoc = false // It's an occurrence of a structural target
        )
        
        repository.upsertDailyInstance(instance)
    }
}
