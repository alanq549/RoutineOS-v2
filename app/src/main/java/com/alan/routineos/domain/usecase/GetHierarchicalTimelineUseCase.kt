package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resolves the daily timeline and organizes it into a recursive hierarchy.
 * Discovers structural nodes and ad-hoc temporal hierarchies.
 * Calculates completion status derived exclusively from executable leaf nodes.
 */
class GetHierarchicalTimelineUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val resolveTimelineUseCase: ResolveTimelineUseCase
) {
    operator fun invoke(date: LocalDate): Flow<List<HierarchicalTimelineEntry>> {
        return combine(
            repository.getActivityDefinitions(),
            repository.getAllNodes(),
            resolveTimelineUseCase(date)
        ) { definitions, allNodes, flatTimeline ->
            buildRecursiveHierarchy(definitions, allNodes, flatTimeline, date)
        }
    }

    private fun buildRecursiveHierarchy(
        definitions: List<ActivityDefinition>,
        allNodes: List<ActivityNode>,
        flatTimeline: List<TimelineEntry>,
        date: LocalDate
    ): List<HierarchicalTimelineEntry> {
        val nodeMap = allNodes.associateBy { it.id }
        val defMap = definitions.associateBy { it.id }
        
        // Split flatTimeline into structural and ad-hoc
        val structuralEntries = flatTimeline.filter { it.instance.target != null }
        val adHocEntries = flatTimeline.filter { it.instance.target == null }
        
        val entryMap = structuralEntries.associateBy { getEntryTargetKey(it) }
        val adHocMap = adHocEntries.associateBy { it.instance.id }

        // 1. Identify all scheduled structural targets and their ancestors
        val allTargetKeysInHierarchy = mutableSetOf<String>()
        val scheduledKeys = structuralEntries.map { getEntryTargetKey(it) }.toSet()
        
        structuralEntries.forEach { entry ->
            val target = entry.instance.target ?: return@forEach
            allTargetKeysInHierarchy.add(getEntryTargetKey(entry))
            
            if (target is ScheduleTarget.Node) {
                var currentId = nodeMap[target.id]?.parentId
                while (currentId != null) {
                    allTargetKeysInHierarchy.add("NODE_$currentId")
                    currentId = nodeMap[currentId]?.parentId
                }
                
                val defId = nodeMap[target.id]?.activityDefinitionId
                if (defId != null) {
                    val definitionKey = "DEF_$defId"
                    val rootNodesOfDefInHierarchy = allNodes.filter { 
                        it.activityDefinitionId == defId && it.parentId == null && 
                        (scheduledKeys.contains("NODE_${it.id}") || hasScheduledDescendant(it, allNodes, scheduledKeys))
                    }
                    
                    if (scheduledKeys.contains(definitionKey) || rootNodesOfDefInHierarchy.size > 1) {
                        allTargetKeysInHierarchy.add(definitionKey)
                    }
                }
            }
        }

        // 2. Identify structural roots
        val rootKeys = allTargetKeysInHierarchy.filter { key ->
            val entry = entryMap[key] ?: createVirtualFromKey(key, nodeMap, defMap, date) ?: return@filter false
            !hasAncestorInSet(entry, nodeMap, allTargetKeysInHierarchy)
        }

        val structuralRoots = rootKeys.mapNotNull { key ->
            val entry = entryMap[key] ?: createVirtualFromKey(key, nodeMap, defMap, date)
            entry?.let { buildEntryNode(it, nodeMap, entryMap, adHocMap, allNodes, date, mutableSetOf()) }
        }
        
        // 3. Ad-hoc roots (instances with target == null and parentInstanceId == null)
        val adHocRoots = adHocEntries.filter { it.instance.parentInstanceId == null }
            .map { buildEntryNode(it, nodeMap, entryMap, adHocMap, allNodes, date, mutableSetOf()) }

        return (structuralRoots + adHocRoots).sortedBy { it.effectiveStartTimeMinutes ?: Int.MAX_VALUE }
    }

    private fun hasAncestorInSet(
        entry: TimelineEntry,
        nodeMap: Map<String, ActivityNode>,
        keySet: Set<String>
    ): Boolean {
        val target = entry.instance.target ?: return false
        if (target is ScheduleTarget.Node) {
            val node = nodeMap[target.id] ?: return false
            if (node.parentId != null && keySet.contains("NODE_${node.parentId}")) return true
            if (keySet.contains("DEF_${node.activityDefinitionId}")) return true
        }
        return false
    }

    private fun hasScheduledDescendant(
        node: ActivityNode,
        allNodes: List<ActivityNode>,
        scheduledKeys: Set<String>
    ): Boolean {
        val children = allNodes.filter { it.parentId == node.id }
        return children.any { child ->
            scheduledKeys.contains("NODE_${child.id}") || hasScheduledDescendant(child, allNodes, scheduledKeys)
        }
    }

    private fun createVirtualFromKey(
        key: String,
        nodeMap: Map<String, ActivityNode>,
        defMap: Map<String, ActivityDefinition>,
        date: LocalDate
    ): TimelineEntry? {
        return when {
            key.startsWith("NODE_") -> {
                val id = key.removePrefix("NODE_")
                nodeMap[id]?.let { createVirtualStructuralEntry(it, date) }
            }
            key.startsWith("DEF_") -> {
                val id = key.removePrefix("DEF_")
                defMap[id]?.let { def ->
                    TimelineEntry(
                        instance = DailyInstance(
                            id = "structural_virtual_def_${def.id}",
                            target = ScheduleTarget.Definition(def.id),
                            scheduledDate = date.toEpochDay(),
                            titleSnapshot = def.title,
                            descriptionSnapshot = def.description,
                            status = DailyInstanceStatus.PLANNED
                        ),
                        isMaterialized = false
                    )
                }
            }
            else -> null
        }
    }

    private fun buildEntryNode(
        entry: TimelineEntry,
        nodeMap: Map<String, ActivityNode>,
        entryMap: Map<String, TimelineEntry>,
        adHocMap: Map<String, TimelineEntry>,
        allNodes: List<ActivityNode>,
        date: LocalDate,
        visited: MutableSet<String>
    ): HierarchicalTimelineEntry {
        val targetKey = if (entry.instance.target != null) getEntryTargetKey(entry) else "ADHOC_${entry.instance.id}"
        
        if (visited.contains(targetKey)) return createLeaf(entry)
        visited.add(targetKey)
        
        val children = when {
            entry.instance.target is ScheduleTarget.Node -> {
                allNodes.filter { it.parentId == entry.instance.target.id }.map { childNode ->
                    val childKey = "NODE_${childNode.id}"
                    entryMap[childKey] ?: createVirtualStructuralEntry(childNode, date)
                }
            }
            entry.instance.target is ScheduleTarget.Definition -> {
                allNodes.filter { it.activityDefinitionId == entry.instance.target.id && it.parentId == null }.map { childNode ->
                    val childKey = "NODE_${childNode.id}"
                    entryMap[childKey] ?: createVirtualStructuralEntry(childNode, date)
                }
            }
            entry.instance.target == null -> {
                adHocMap.values.filter { it.instance.parentInstanceId == entry.instance.id }
            }
            else -> emptyList()
        }
        
        val recursiveChildren = children.map { childEntry ->
            buildEntryNode(childEntry, nodeMap, entryMap, adHocMap, allNodes, date, visited.toMutableSet())
        }

        return if (recursiveChildren.isEmpty()) {
            createLeaf(entry)
        } else {
            val totalLeaves = recursiveChildren.sumOf { it.totalCount }
            val completedLeaves = recursiveChildren.sumOf { it.completedCount }
            
            // Temporal Logic V3:
            val explicitStart = entry.instance.plannedStartTime
            val childrenStart = recursiveChildren.mapNotNull { it.effectiveStartTimeMinutes }.minOrNull()
            
            // Rule: Explicit start is immutable.
            val effectiveStart = explicitStart ?: childrenStart

            // Duration Logic V3:
            val explicitDuration = entry.instance.plannedDurationMinutes
            val explicitEnd = entry.instance.plannedEndTime
            
            // Derivation only if NOT explicit range
            val childrenDuration = if (recursiveChildren.any { it.effectiveStartTimeMinutes != null || it.totalDurationMinutes != null }) {
                val start = effectiveStart
                val maxEnd = recursiveChildren.mapNotNull { child ->
                    val childStart = child.effectiveStartTimeMinutes
                    val childDur = child.totalDurationMinutes ?: 0 // Points contribute 0 to range extension
                    if (childStart != null) childStart + childDur else null
                }.maxOrNull()
                
                if (start != null && maxEnd != null && maxEnd > start) maxEnd - start else 0
            } else {
                0
            }
            
            val isExplicitPoint = entry.instance.plannedStartTime != null && 
                                entry.instance.plannedEndTime == null && 
                                entry.instance.plannedDurationMinutes == null

            val totalDuration = when {
                isExplicitPoint -> null
                explicitEnd != null && explicitStart != null -> explicitEnd - explicitStart
                explicitDuration != null -> explicitDuration
                else -> if (childrenDuration > 0) childrenDuration else null
            }

            val completion = when {
                completedLeaves == totalLeaves -> HierarchyCompletion.COMPLETED
                completedLeaves == 0 -> HierarchyCompletion.NOT_STARTED
                else -> HierarchyCompletion.IN_PROGRESS
            }
            
            HierarchicalTimelineEntry(
                root = entry,
                children = recursiveChildren,
                completedCount = completedLeaves,
                totalCount = totalLeaves,
                totalDurationMinutes = totalDuration,
                effectiveStartTimeMinutes = effectiveStart,
                completion = completion
            )
        }
    }

    private fun createLeaf(entry: TimelineEntry): HierarchicalTimelineEntry {
        val isDone = entry.instance.status == DailyInstanceStatus.COMPLETED
        return HierarchicalTimelineEntry(
            root = entry,
            children = emptyList(),
            completedCount = if (isDone) 1 else 0,
            totalCount = 1,
            totalDurationMinutes = entry.instance.plannedDurationMinutes,
            effectiveStartTimeMinutes = entry.instance.plannedStartTime,
            completion = if (isDone) HierarchyCompletion.COMPLETED else HierarchyCompletion.NOT_STARTED
        )
    }

    private fun createVirtualStructuralEntry(node: ActivityNode, date: LocalDate): TimelineEntry {
        return TimelineEntry(
            instance = DailyInstance(
                id = "structural_virtual_${node.id}",
                target = ScheduleTarget.Node(node.id),
                scheduledDate = date.toEpochDay(),
                titleSnapshot = node.title,
                descriptionSnapshot = node.description,
                status = DailyInstanceStatus.PLANNED
            ),
            isMaterialized = false
        )
    }

    private fun getEntryTargetKey(entry: TimelineEntry): String {
        return when (val target = entry.instance.target) {
            is ScheduleTarget.Node -> "NODE_${target.id}"
            is ScheduleTarget.Definition -> "DEF_${target.id}"
            else -> "UNKNOWN"
        }
    }
}
