package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import javax.inject.Inject

data class ConflictResult(
    val hasConflict: Boolean,
    val relationship: TemporalRelationship = TemporalRelationship.NONE,
    val impact: TemporalImpact = TemporalImpact.NONE,
    val conflictingInstanceIds: List<String> = emptyList(),
    val suggestions: List<ConflictSuggestion> = emptyList()
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
            val endA = current.plannedEndTime 
                ?: current.plannedDurationMinutes?.let { startA + it }
                ?: (startA + 30)
            
            val conflictingIds = mutableListOf<String>()
            var worstRelationship = TemporalRelationship.NONE
            var worstImpact = TemporalImpact.NONE

            instances.filter { it.id != current.id }.forEach { other ->
                val startB = other.plannedStartTime ?: return@forEach
                val endB = other.plannedEndTime
                    ?: other.plannedDurationMinutes?.let { startB + it }
                    ?: (startB + 30)
                
                // Overlap check [s, e): s1 < e2 && e1 > s2
                if (startA < endB && endA > startB) {
                    conflictingIds.add(other.id)
                    
                    val rel = determineRelationship(startA, endA, startB, endB)
                    val imp = determineImpact(current, other, rel, nodeMap)
                    
                    // Update worst cases
                    if (rel.ordinal > worstRelationship.ordinal) worstRelationship = rel
                    if (imp.ordinal > worstImpact.ordinal) worstImpact = imp
                }
            }
            
            results[current.id] = ConflictResult(
                hasConflict = conflictingIds.isNotEmpty(),
                relationship = worstRelationship,
                impact = worstImpact,
                conflictingInstanceIds = conflictingIds
            )
        }
        
        return results
    }

    private fun determineRelationship(s1: Int, e1: Int, s2: Int, e2: Int): TemporalRelationship {
        if (s1 == s2 && e1 == e2) return TemporalRelationship.OVERLAP // Requirement: equal intervals are OVERLAP
        
        return when {
            s1 <= s2 && e1 >= e2 -> TemporalRelationship.CONTAINS
            s2 <= s1 && e2 >= e1 -> TemporalRelationship.CONTAINED_BY
            else -> TemporalRelationship.OVERLAP
        }
    }

    private fun determineImpact(
        a: DailyInstance, 
        b: DailyInstance, 
        rel: TemporalRelationship,
        nodeMap: Map<String, ActivityNode>
    ): TemporalImpact {
        // INFO only if there is a real structural relationship
        val isStructural = isStructuralChild(a, b, nodeMap) || isStructuralChild(b, a, nodeMap)
        
        if (isStructural && (rel == TemporalRelationship.CONTAINS || rel == TemporalRelationship.CONTAINED_BY)) {
            return TemporalImpact.INFO
        }

        // WARNING if one is IMMOBILE or they are independent overlaps
        if (a.mobility == TemporalMobility.IMMOBILE || b.mobility == TemporalMobility.IMMOBILE) {
            return TemporalImpact.WARNING
        }

        return if (isStructural) TemporalImpact.NONE else TemporalImpact.WARNING
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
                // If it\u0027s a root node of the definition
                if (node.activityDefinitionId == parentTarget.id && node.parentId == null) return true
            }
            else -> {}
        }
        return false
    }
}
