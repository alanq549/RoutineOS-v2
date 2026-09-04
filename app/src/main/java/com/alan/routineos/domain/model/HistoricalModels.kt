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
    val activityTitle: String? = null,
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
    val durationDeviationAvgMinutes: Int? = null, // N/A for now
    val systemAdherence: List<SystemAdherence> = emptyList(),
    val activityAdherence: List<ActivityAdherence> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList(),
    val weeklyStats: List<WeeklyStats> = emptyList(),
    val monthlyStats: List<MonthlyStats> = emptyList()
)

/**
 * Granular stats for a specific day.
 */
data class DailyStats(
    val date: LocalDate,
    val completionRate: Float?,
    val completedCount: Int,
    val omittedCount: Int,
    val missedCount: Int,
    val spontaneousCount: Int,
    val occurrences: List<ResolvedOccurrence> = emptyList()
)

/**
 * Aggregated stats for a week.
 */
data class WeeklyStats(
    val startOfWeek: LocalDate,
    val endOfWeek: LocalDate,
    val completionRate: Float?,
    val dailyStats: List<DailyStats>
)

/**
 * Aggregated stats for a month.
 */
data class MonthlyStats(
    val yearMonth: java.time.YearMonth,
    val completionRate: Float?,
    val weeklyStats: List<WeeklyStats>
)

/**
 * Adherence metrics per Life System.
 */
data class SystemAdherence(
    val systemId: String,
    val systemName: String,
    val completionRate: Float?,
    val omittedRate: Float?,
    val missedRate: Float?,
    val completedCount: Int,
    val omittedCount: Int,
    val missedCount: Int
)

/**
 * Adherence metrics per Activity Definition.
 */
data class ActivityAdherence(
    val activityId: String,
    val activityTitle: String,
    val completionRate: Float?,
    val omittedRate: Float?,
    val missedRate: Float?,
    val spontaneousCount: Int,
    val completedCount: Int,
    val omittedCount: Int,
    val missedCount: Int
)

/**
 * A series of numeric values extracted from metadata over time.
 */
data class TrendSeries(
    val fieldName: String,
    val values: List<Pair<LocalDate, Double>>
)
