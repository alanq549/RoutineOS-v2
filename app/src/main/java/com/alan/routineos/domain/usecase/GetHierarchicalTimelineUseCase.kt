package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resolves the daily timeline and organizes it into a recursive hierarchy.
 * Discovers structural nodes to allow manual materialization of sub-steps.
 * Calculates completion status derived exclusively from executable leaf nodes.
 */
class GetHierarchicalTimelineUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val resolveTimelineUseCase: ResolveTimelineUseCase
) {
    operator fun invoke(date: LocalDate): Flow<List<HierarchicalTimelineEntry>> {
        return combine(
            repository.getAllNodes(),
            resolveTimelineUseCase(date)
        ) { allNodes, flatTimeline ->
            buildRecursiveHierarchy(allNodes, flatTimeline, date)
        }
    }

    private fun buildRecursiveHierarchy(
        allNodes: List<ActivityNode>,
        flatTimeline: List<TimelineEntry>,
        date: LocalDate
    ): List<HierarchicalTimelineEntry> {
        val nodeMap = allNodes.associateBy { it.id }
        val entryMap = flatTimeline.associateBy { getEntryTargetKey(it) }

        // 1. Root Identification: An entry is a root if it doesn\u0027t have an ancestor already present in flatTimeline.
        val rootEntries = flatTimeline.filter { entry ->
            !hasScheduledAncestor(entry, nodeMap, entryMap)
        }

        // 2. Recursively build tree for each root
        return rootEntries.map { rootEntry ->
            buildEntryNode(rootEntry, nodeMap, entryMap, allNodes, date, mutableSetOf())
        }
    }

    private fun hasScheduledAncestor(
        entry: TimelineEntry,
        nodeMap: Map<String, ActivityNode>,
        entryMap: Map<String, TimelineEntry>
    ): Boolean {
        val target = entry.instance.target ?: return false
        
        // If the target is a Node, check its parents
        if (target is ScheduleTarget.Node) {
            var currentId = nodeMap[target.id]?.parentId
            while (currentId != null) {
                if (entryMap.containsKey("NODE_$currentId")) return true
                currentId = nodeMap[currentId]?.parentId
            }
            
            // Also check the Definition parent of the node
            val definitionId = nodeMap[target.id]?.activityDefinitionId
            if (definitionId != null && entryMap.containsKey("DEF_$definitionId")) return true
        }
        
        return false
    }

    private fun buildEntryNode(
        entry: TimelineEntry,
        nodeMap: Map<String, ActivityNode>,
        entryMap: Map<String, TimelineEntry>,
        allNodes: List<ActivityNode>,
        date: LocalDate,
        visited: MutableSet<String> // Prevent infinite loops
    ): HierarchicalTimelineEntry {
        val target = entry.instance.target
        val targetKey = getEntryTargetKey(entry)
        
        if (targetKey != "UNKNOWN" && visited.contains(targetKey)) {
            // Cycle detected, return leaf
            return createLeaf(entry)
        }
        if (targetKey != "UNKNOWN") visited.add(targetKey)
        
        // Discover structural children from the global node list
        val structuralChildren = when (target) {
            is ScheduleTarget.Node -> allNodes.filter { it.parentId == target.id }
            is ScheduleTarget.Definition -> allNodes.filter { it.activityDefinitionId == target.id && it.parentId == null }
            else -> emptyList()
        }
        
        val recursiveChildren = structuralChildren.map { childNode ->
            val childKey = "NODE_${childNode.id}"
            val childEntry = entryMap[childKey] ?: createVirtualStructuralEntry(childNode, date)
            buildEntryNode(childEntry, nodeMap, entryMap, allNodes, date, visited.toMutableSet())
        }

        return if (recursiveChildren.isEmpty()) {
            createLeaf(entry)
        } else {
            // CONTAINER NODE: Completion is derived from ALL descendant leaves recursively
            val totalLeaves = recursiveChildren.sumOf { it.totalCount }
            val completedLeaves = recursiveChildren.sumOf { it.completedCount }
            
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
