package com.alan.routineos.feature.planning.model

import com.alan.routineos.feature.today.model.TodayTimelineUiModel

sealed interface UnifiedLinkingResult {
    val id: String
    val title: String

    data class SemanticDefinition(
        override val id: String,
        override val title: String,
        val description: String?
    ) : UnifiedLinkingResult

    data class SemanticNode(
        override val id: String,
        override val title: String,
        val parentTitle: String?
    ) : UnifiedLinkingResult

    data class ContextualOccurrence(
        val item: TodayTimelineUiModel
    ) : UnifiedLinkingResult {
        override val id: String = item.id
        override val title: String = item.title
    }
}
