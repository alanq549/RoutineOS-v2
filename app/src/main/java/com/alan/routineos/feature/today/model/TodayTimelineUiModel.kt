package com.alan.routineos.feature.today.model

import com.alan.routineos.domain.model.*

enum class TimelineTemporalState {
    UPCOMING,
    CURRENT,
    OVERDUE,
    STALE_PENDING
}

data class ConflictUiModel(
    val hasConflict: Boolean,
    val impact: TemporalImpact = TemporalImpact.NONE,
    val relationship: TemporalRelationship = TemporalRelationship.NONE,
    val suggestions: List<ConflictSuggestion> = emptyList()
)

/**
 * UI representation of a sub-node step.
 * Supports recursion for deep hierarchies and completion metadata.
 */
data class TodaySubNodeUiModel(
    val id: String,
    val title: String,
    val timeText: String,
    val status: DailyInstanceStatus,
    val contextMetadata: List<Pair<String, String>> = emptyList(),
    val operationalMetadata: List<Pair<String, String>> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val completion: HierarchyCompletion = HierarchyCompletion.NOT_STARTED,
    val children: List<TodaySubNodeUiModel> = emptyList(),
    val conflict: ConflictUiModel = ConflictUiModel(false)
)

data class TodayTimelineUiModel(
    val id: String,
    val title: String,
    val description: String = "",
    val timeRangeText: String,
    val status: DailyInstanceStatus,
    val isMaterialized: Boolean,
    val conflict: ConflictUiModel = ConflictUiModel(false),
    val subNodes: List<TodaySubNodeUiModel> = emptyList(),
    val contextMetadata: List<Pair<String, String>> = emptyList(),
    val operationalMetadata: List<Pair<String, String>> = emptyList(),
    val isExpandable: Boolean = false,
    val isExpanded: Boolean = false,
    val isAdHoc: Boolean = false,
    val completedSubNodesCount: Int = 0,
    val totalSubNodesCount: Int = 0,
    val temporalState: TimelineTemporalState = TimelineTemporalState.UPCOMING,
    val completion: HierarchyCompletion = HierarchyCompletion.NOT_STARTED
)
