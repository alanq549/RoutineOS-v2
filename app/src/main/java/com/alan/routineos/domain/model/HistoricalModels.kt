package com.alan.routineos.domain.model

import java.time.LocalDate

/**
 * Represents a logical occurrence in time derived from rules or overrides.
 */
data class ResolvedOccurrence(
    val id: String,
    val instance: DailyInstance,
    val isMaterialized: Boolean,
    val date: LocalDate,
    val isAdHoc: Boolean = false
)

/**
 * Summary of historical performance for a given period or scope.
 */
data class HistorySnapshot(
    val completionRate: Float?, // sum(Completed) / sum(Eligible)
    val totalOccurrences: Int,
    val completedCount: Int,
    val missedCount: Int,
    val omittedCount: Int,
    val executionConsistency: Float?, // Days with >= 70% completion
    val startDeviationAvgMinutes: Int? = null, // N/A for now
    val durationDeviationAvgMinutes: Int? = null // N/A for now
)

/**
 * A series of numeric values extracted from metadata over time.
 */
data class TrendSeries(
    val fieldName: String,
    val values: List<Pair<LocalDate, Double>>
)
