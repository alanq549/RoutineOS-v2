package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.repository.ActivityRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Persists a virtual instance into the database, creating a frozen snapshot.
 * Identifies recurring virtual occurrences strictly via (sourceRuleId, scheduledDate).
 */
class MaterializeInstanceUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        virtualInstance: DailyInstance,
        status: DailyInstanceStatus = DailyInstanceStatus.PLANNED
    ): DailyInstance {
        val ruleId = virtualInstance.sourceRuleId
        if (ruleId != null) {
            // Recurring occurrence: Check by (sourceRuleId, scheduledDate)
            val existing = repository.getDailyInstanceBySourceRule(ruleId, virtualInstance.scheduledDate)
            if (existing != null) return existing

            val materialized = virtualInstance.copy(
                id = UUID.randomUUID().toString(),
                status = status
            )
            repository.upsertDailyInstance(materialized)
            return materialized
        } else {
            // Punctual / Ad-hoc instance without ScheduleRule
            val materialized = if (virtualInstance.id.isBlank()) {
                virtualInstance.copy(id = UUID.randomUUID().toString(), status = status)
            } else {
                virtualInstance.copy(status = status)
            }
            repository.upsertDailyInstance(materialized)
            return materialized
        }
    }
}
