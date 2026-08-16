package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.DailyInstance
import javax.inject.Inject

data class ConflictResult(
    val hasConflict: Boolean,
    val conflictingInstanceIds: List<String> = emptyList()
)

/**
 * Detects overlapping time windows between DailyInstances.
 */
class ConflictDetectorUseCase @Inject constructor() {

    fun detectConflicts(instances: List<DailyInstance>): Map<String, ConflictResult> {
        val results = mutableMapOf<String, ConflictResult>()
        
        instances.forEach { current ->
            val startTime = current.plannedStartTime ?: return@forEach
            val endTime = current.plannedEndTime 
                ?: current.plannedDurationMinutes?.let { startTime + it }
                ?: (startTime + 30) // Fallback to 30 mins
            
            val conflicts = instances.filter { other ->
                if (other.id == current.id) return@filter false
                val otherStart = other.plannedStartTime ?: return@filter false
                val otherEnd = other.plannedEndTime
                    ?: other.plannedDurationMinutes?.let { otherStart + it }
                    ?: (otherStart + 30)
                
                // Overlap check: (StartA < EndB) and (EndA > StartB)
                startTime < otherEnd && endTime > otherStart
            }
            
            if (conflicts.isNotEmpty()) {
                results[current.id] = ConflictResult(true, conflicts.map { it.id })
            } else {
                results[current.id] = ConflictResult(false)
            }
        }
        
        return results
    }
}
