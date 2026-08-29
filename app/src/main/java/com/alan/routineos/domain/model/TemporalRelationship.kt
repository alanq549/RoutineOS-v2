package com.alan.routineos.domain.model

/**
 * Mathematical relationship between two temporal intervals [start, end).
 */
enum class TemporalRelationship {
    NONE,
    OVERLAP,
    CONTAINS,
    CONTAINED_BY
}

/**
 * Represents a suggested action to resolve a temporal conflict.
 */
data class ConflictSuggestion(
    val type: SuggestionType,
    val newStartTimeMinutes: Int? = null,
    val message: String
)

enum class SuggestionType {
    MOVE
}
