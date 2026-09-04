package com.alan.routineos.domain.model

import com.alan.routineos.domain.usecase.TimelineEntry

/**
 * Domain model for a timeline entry that can contain children instances.
 * Supports recursive deep hierarchies and completion derivation from leaf nodes.
 */
data class HierarchicalTimelineEntry(
    val root: TimelineEntry,
    val children: List<HierarchicalTimelineEntry> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val totalDurationMinutes: Int? = null,
    val effectiveStartTimeMinutes: Int? = null,
    val completion: HierarchyCompletion = HierarchyCompletion.NOT_STARTED
)
