package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

class UnassignActivityFromSystemUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(activityId: String) {
        val activity = repository.getActivityDefinitionById(activityId)
            ?: throw IllegalArgumentException("Activity $activityId not found")

        repository.upsertActivityDefinition(activity.copy(systemId = null))
    }
}
