package com.alan.routineos.domain.model

/**
 * Defines the completion health of a container node based on its executable children (leaves).
 * Structural only, does not replace ActivityExecutionStatus or DailyInstanceStatus.
 */
enum class HierarchyCompletion {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
