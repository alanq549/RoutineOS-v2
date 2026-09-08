package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.DailyInstance
import javax.inject.Inject

/**
 * Simulates a move operation to detect conflicts before persisting.
 */
class SimulateMoveUseCase @Inject constructor(
    private val conflictDetector: ConflictDetectorUseCase
) {
    operator fun invoke(
        currentInstances: List<DailyInstance>,
        targetId: String,
        newStartTime: Int,
        newEndTime: Int?,
        nodeMap: Map<String, ActivityNode> = emptyMap()
    ): ConflictResult {
        // 1. Create a simulated list where the target instance has the new times
        val simulatedList = currentInstances.map { instance ->
            if (instance.id == targetId) {
                instance.copy(
                    plannedStartTime = newStartTime,
                    plannedEndTime = newEndTime,
                    plannedDurationMinutes = if (newEndTime != null) newEndTime - newStartTime else null
                )
            } else {
                instance
            }
        }

        // 2. Detect conflicts on the simulated list
        val results = conflictDetector.detectConflicts(simulatedList, nodeMap)

        // 3. Return the result specifically for the moved instance
        return results[targetId] ?: ConflictResult(hasConflict = false)
    }
}
