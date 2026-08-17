package com.alan.routineos.domain.model

import com.alan.routineos.domain.usecase.TimelineEntry

/**
 * Domain model for a timeline entry that can contain children instances.
 */
data class HierarchicalTimelineEntry(
    val root: TimelineEntry,
    val children: List<TimelineEntry> = emptyList()
)
