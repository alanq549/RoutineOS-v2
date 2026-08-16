package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.repository.ActivityRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Persists a virtual instance into the database, creating a frozen snapshot.
 */
class MaterializeInstanceUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        virtualInstance: DailyInstance,
        status: DailyInstanceStatus = DailyInstanceStatus.PLANNED
    ): DailyInstance {
        // Check if already materialized to avoid duplicates (Index will catch this too, but let's be safe)
        val target = virtualInstance.target ?: throw IllegalArgumentException("Cannot materialize ad-hoc without target via this UseCase")
        
        val targetId = when(target) {
            is com.alan.routineos.domain.model.ScheduleTarget.Definition -> target.id
            is com.alan.routineos.domain.model.ScheduleTarget.Node -> target.id
        }
        
        val existing = repository.getDailyInstanceByTarget(targetId, virtualInstance.scheduledDate)
        if (existing != null) return existing

        val materialized = virtualInstance.copy(
            id = UUID.randomUUID().toString(),
            status = status
        )
        
        repository.upsertDailyInstance(materialized)
        return materialized
    }
}
