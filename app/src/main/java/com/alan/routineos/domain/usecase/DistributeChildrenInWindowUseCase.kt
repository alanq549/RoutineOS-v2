package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.SuggestedTimeWindow
import javax.inject.Inject

/**
 * Calculates a fair distribution of time for children without schedules
 * within a parent's explicit time window.
 */
class DistributeChildrenInWindowUseCase @Inject constructor() {

    operator fun invoke(
        parent: DailyInstance,
        childrenWithoutTime: List<DailyInstance>
    ): List<SuggestedTimeWindow> {
        if (childrenWithoutTime.isEmpty()) return emptyList()
        
        val start = parent.plannedStartTime ?: return emptyList()
        val end = parent.plannedEndTime 
            ?: parent.plannedDurationMinutes?.let { start + it }
            ?: (start + 30) // Heuristic for suggestion calculation only

        val totalDuration = end - start
        val slice = totalDuration / childrenWithoutTime.size

        return childrenWithoutTime.mapIndexed { index, child ->
            SuggestedTimeWindow(
                childId = child.id,
                startTimeMinutes = start + (index * slice),
                endTimeMinutes = start + ((index + 1) * slice)
            )
        }
    }
}
