package com.alan.routineos.domain.model

import com.alan.routineos.domain.usecase.ConflictResult

/**
 * Represents a move operation that has been simulated but not yet persisted.
 */
data class PendingMove(
    val entry: HierarchicalTimelineEntry,
    val newStartTime: Int,
    val newEndTime: Int?,
    val conflictResult: ConflictResult
)
