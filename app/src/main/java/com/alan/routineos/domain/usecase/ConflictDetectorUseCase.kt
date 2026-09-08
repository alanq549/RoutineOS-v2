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
        val instanceMap = instances.associateBy { it.id }
        
        instances.forEach { current ->
            if (hasCycle(current, instanceMap)) {
                results[current.id] = ConflictResult(
                    hasConflict = true,
                    impact = TemporalImpact.WARNING,
                    details = listOf(ConflictDetail("CYCLE", TemporalRelationship.OVERLAP, TemporalImpact.WARNING, false))
                )
                return@forEach
            }

            val startA = current.plannedStartTime ?: return@forEach
            
            // Point or Range
            val endA = current.plannedEndTime 
                ?: current.plannedDurationMinutes?.let { startA + it }
                ?: (startA + 1) // Treat Point as 1-min interval for overlap check
            
            val details = mutableListOf<ConflictDetail>()

            instances.filter { it.id != current.id }.forEach { other ->
                val startB = other.plannedStartTime ?: return@forEach
                val endB = other.plannedEndTime
                    ?: other.plannedDurationMinutes?.let { startB + it }
                    ?: (startB + 1)
                
                if (startA < endB && endA > startB) {
                    val rel = determineRelationship(startA, endA, startB, endB)
                    val imp = determineImpact(current, other, rel, nodeMap, instanceMap)
                    
                    val isInterruption = imp == TemporalImpact.WARNING && (
                        current.isAdHoc ||
                            other.isAdHoc ||
                            current.mobility == TemporalMobility.IMMOBILE ||
                            other.mobility == TemporalMobility.IMMOBILE
                        )
                    
                    details.add(ConflictDetail(other.id, rel, imp, isInterruption))
                }
            }
            
            // Out of Parent Window check
            current.parentInstanceId?.let { pId ->
                instanceMap[pId]?.let { parent ->
                    val pStart = parent.plannedStartTime
                    val pEnd = parent.plannedEndTime ?: parent.plannedDurationMinutes?.let { pStart?.plus(it) }
                    
                    if (pStart != null && pEnd != null) {
                        if (startA < pStart || endA > pEnd) {
                            details.add(ConflictDetail(parent.id, TemporalRelationship.OVERLAP, TemporalImpact.WARNING, false))
                        }
                    }
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

    private fun hasCycle(instance: DailyInstance, map: Map<String, DailyInstance>): Boolean {
        var current = instance.parentInstanceId
        val visited = mutableSetOf(instance.id)
        while (current != null) {
            if (visited.contains(current)) return true
            visited.add(current)
            current = map[current]?.parentInstanceId
        }
        return false
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
        nodeMap: Map<String, ActivityNode>,
        instanceMap: Map<String, DailyInstance>
    ): TemporalImpact {
        val currentIsChild = isStructuralChild(current, other, nodeMap, instanceMap)
        val otherIsChild = isStructuralChild(other, current, nodeMap, instanceMap)
        val isStructural = currentIsChild || otherIsChild
        
        if (isStructural) {
            if (currentIsChild && rel != TemporalRelationship.CONTAINED_BY) return TemporalImpact.WARNING
            if (otherIsChild && rel != TemporalRelationship.CONTAINS) return TemporalImpact.WARNING
            return TemporalImpact.INFO
        }

        return TemporalImpact.WARNING
    }

    private fun isStructuralChild(
        child: DailyInstance, 
        parent: DailyInstance, 
        nodeMap: Map<String, ActivityNode>,
        instanceMap: Map<String, DailyInstance>
    ): Boolean {
        var currentAdHocParentId = child.parentInstanceId
        while (currentAdHocParentId != null) {
            if (currentAdHocParentId == parent.id) return true
            currentAdHocParentId = instanceMap[currentAdHocParentId]?.parentInstanceId
        }

        val childNodeId = (child.target as? ScheduleTarget.Node)?.id ?: return false
        val node = nodeMap[childNodeId] ?: return false

        when (val parentTarget = parent.target) {
            is ScheduleTarget.Node -> {
                var currentId = node.parentId
                while (currentId != null) {
                    if (currentId == parentTarget.id) return true
                    currentId = nodeMap[currentId]?.parentId
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
