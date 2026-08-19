package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

class AssignActivityToSystemUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(activityId: String, systemId: String) {
        val activity = repository.getActivityDefinitionById(activityId)
            ?: throw IllegalArgumentException("Activity $activityId not found")
        
        repository.getSystemById(systemId)
            ?: throw IllegalArgumentException("System $systemId not found")

        repository.upsertActivityDefinition(activity.copy(systemId = systemId))
    }
}
