package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.HierarchicalTimelineEntry
import com.alan.routineos.domain.model.ScheduleTarget
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Resolves the daily timeline and organizes it into a parent-child hierarchy.
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
            groupHierarchy(allNodes, flatTimeline)
        }
    }

    private fun groupHierarchy(
        allNodes: List<com.alan.routineos.domain.model.ActivityNode>,
        flatTimeline: List<TimelineEntry>
    ): List<HierarchicalTimelineEntry> {
        val nodeMap = allNodes.associateBy { it.id }
        val entryMap = flatTimeline.associateBy { getEntryNodeId(it) }

        val rootEntries = mutableListOf<TimelineEntry>()
        val childMap = mutableMapOf<String, MutableList<TimelineEntry>>()

        flatTimeline.forEach { entry ->
            val parentId = findParentInTimeline(entry, nodeMap, entryMap)
            if (parentId != null) {
                childMap.getOrPut(parentId) { mutableListOf() }.add(entry)
            } else {
                rootEntries.add(entry)
            }
        }

        return rootEntries.map { HierarchicalTimelineEntry(it, childMap[getEntryNodeId(it)] ?: emptyList()) }
    }

    private fun findParentInTimeline(
        entry: TimelineEntry,
        nodeMap: Map<String, com.alan.routineos.domain.model.ActivityNode>,
        entryMap: Map<String, TimelineEntry>
    ): String? {
        val target = entry.instance.target
        if (target !is ScheduleTarget.Node) return null

        val node = nodeMap[target.id]
        val parentId = node?.parentId ?: return null

        return if (entryMap.containsKey(parentId)) parentId else null
    }

    private fun getEntryNodeId(entry: TimelineEntry): String {
        return (entry.instance.target as? ScheduleTarget.Node)?.id ?: entry.instance.id
    }
}
