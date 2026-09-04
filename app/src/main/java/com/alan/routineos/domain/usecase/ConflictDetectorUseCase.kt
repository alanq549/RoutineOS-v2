package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import javax.inject.Inject

data class ConflictResult(
    val hasConflict: Boolean,
    val impact: TemporalImpact = TemporalImpact.NONE,
    val details: List<ConflictDetail> = emptyList(),
    val suggestions: List<ConflictSuggestion> = emptyList()
)

data class ConflictDetail(
    val otherInstanceId: String,
    val relationship: TemporalRelationship,
    val impact: TemporalImpact,
    val isInterruption: Boolean
)

/**
 * Detects overlapping time windows and relationships between DailyInstances.
 * Uses semi-open intervals [start, end).
 */
class ConflictDetectorUseCase @Inject constructor() {

    fun detectConflicts(
        instances: List<DailyInstance>,
        nodeMap: Map<String, ActivityNode> = emptyMap()
    ): Map<String, ConflictResult> {
        val results = mutableMapOf<String, ConflictResult>()
        
        instances.forEach { current ->
            val startA = current.plannedStartTime ?: return@forEach
            
            // Temporal Heuristic: If no explicit end or duration is provided, we assume a 30-min 
            // resolution window for conflict detection. This is NOT a real duration but a 
            // mathematical anchor for interval resolution.
            val endA = current.plannedEndTime 
                ?: current.plannedDurationMinutes?.let { startA + it }
                ?: (startA + 30)
            
            val details = mutableListOf<ConflictDetail>()

            instances.filter { it.id != current.id }.forEach { other ->
                val startB = other.plannedStartTime ?: return@forEach
                val endB = other.plannedEndTime
                    ?: other.plannedDurationMinutes?.let { startB + it }
                    ?: (startB + 30)
                
                // Overlap check [s, e): s1 < e2 && e1 > s2
                if (startA < endB && endA > startB) {
                    val rel = determineRelationship(startA, endA, startB, endB)
                    val imp = determineImpact(current, other, rel, nodeMap)
                    
                    val isInterruption = imp == TemporalImpact.WARNING && (
                        current.isAdHoc ||
                            other.isAdHoc ||
                            current.mobility == TemporalMobility.IMMOBILE ||
                            other.mobility == TemporalMobility.IMMOBILE
                        )
                    
                    details.add(ConflictDetail(other.id, rel, imp, isInterruption))
                }
            }
            
            val worstImpact = details.maxByOrNull { it.impact.ordinal }?.impact ?: TemporalImpact.NONE
            
            results[current.id] = ConflictResult(
                hasConflict = details.isNotEmpty(),
                impact = worstImpact,
                details = details
            )
        }
        
        return results
    }

    private fun determineRelationship(s1: Int, e1: Int, s2: Int, e2: Int): TemporalRelationship {
        if (s1 == s2 && e1 == e2) return TemporalRelationship.OVERLAP
        
        return when {
            s1 <= s2 && e1 >= e2 -> TemporalRelationship.CONTAINS
            s2 <= s1 && e2 >= e1 -> TemporalRelationship.CONTAINED_BY
            else -> TemporalRelationship.OVERLAP
        }
    }

    private fun determineImpact(
        current: DailyInstance, 
        other: DailyInstance, 
        rel: TemporalRelationship,
        nodeMap: Map<String, ActivityNode>
    ): TemporalImpact {
        val isStructural = isStructuralChild(current, other, nodeMap) || isStructuralChild(other, current, nodeMap)
        
        // INFO only if there is a real structural relationship and it's a container relationship
        if (isStructural && (rel == TemporalRelationship.CONTAINS || rel == TemporalRelationship.CONTAINED_BY)) {
            return TemporalImpact.INFO
        }

        // WARNING if they are independent overlaps or one is immobile
        if (!isStructural) {
            return TemporalImpact.WARNING
        }

        return TemporalImpact.NONE
    }

    private fun isStructuralChild(child: DailyInstance, parent: DailyInstance, nodeMap: Map<String, ActivityNode>): Boolean {
        val childNodeId = (child.target as? ScheduleTarget.Node)?.id ?: return false
        val node = nodeMap[childNodeId] ?: return false

        when (val parentTarget = parent.target) {
            is ScheduleTarget.Node -> {
                var currentParentId = node.parentId
                while (currentParentId != null) {
                    if (currentParentId == parentTarget.id) return true
                    currentParentId = nodeMap[currentParentId]?.parentId
                }
            }
            is ScheduleTarget.Definition -> {
                if (node.activityDefinitionId == parentTarget.id && node.parentId == null) return true
            }
            else -> {}
        }
        return false
    }
}
