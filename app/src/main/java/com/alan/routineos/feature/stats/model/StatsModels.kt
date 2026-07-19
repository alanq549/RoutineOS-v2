package com.alan.routineos.feature.stats.model

enum class StatsPeriod {
    WEEK, MONTH, YEAR
}

/**
 * Rhythm Models (Weekly)
 */
data class WeeklyRhythmData(
    val dateRange: String,
    val summaryMetrics: List<StatsSummaryMetric>,
    val days: List<RhythmDay>,
    val comparison: List<StatsComparison>,
    val systems: List<StatsSystemMetric>,
    val insights: List<StatsInsight>
)

data class RhythmDay(
    val id: String,
    val name: String,
    val label: String,
    val isSelected: Boolean = false,
    val blocks: List<RhythmBlock>,
    val details: RhythmDayDetail
)

data class RhythmBlock(
    val type: RhythmBlockType,
    val weight: Float // height relative to others
)

enum class RhythmBlockType {
    COMPLETED, PARTIAL, SPONTANEOUS, SKIPPED
}

data class RhythmDayDetail(
    val title: String,
    val subtitle: String,
    val completionPercentage: Int,
    val items: List<RhythmDetailItem>,
    val bodyLoad: BodyLoadUiModel? = null
)

data class RhythmDetailItem(
    val title: String,
    val type: RhythmBlockType,
    val iconName: String
)

/**
 * Cycle Models (Monthly)
 */
data class MonthlyCycleData(
    val monthName: String,
    val summaryMetrics: List<StatsSummaryMetric>,
    val completionPercentage: Int,
    val activeDaysText: String,
    val weeks: List<CycleWeek>,
    val outcomeBreakdown: List<StatsSummaryMetric>,
    val comparison: List<StatsComparison>,
    val systems: List<StatsSystemMetric>,
    val insights: List<StatsInsight>
)

data class CycleWeek(
    val id: String,
    val label: String,
    val percentage: Int,
    val isSelected: Boolean = false,
    val comparisonText: String? = null, // e.g. "-8% vs anterior"
    val bodyLoad: BodyLoadUiModel? = null
)

/**
 * Year Models (Cycle Evolution)
 */
data class YearlyCycleData(
    val year: String,
    val months: List<CycleMonth>
)

data class CycleMonth(
    val id: String,
    val name: String,
    val percentage: Int,
    val isSelected: Boolean = false,
    val activeDays: Int,
    val totalDays: Int,
    val trendText: String,
    val principalSystems: List<StatsSystemMetric>,
    val bodyLoad: BodyLoadUiModel? = null
)

/**
 * Shared Stats Models
 */
data class StatsSummaryMetric(
    val label: String,
    val value: String,
    val isPrimary: Boolean = false
)

data class StatsComparison(
    val title: String,
    val plannedValue: String,
    val actualValue: String,
    val percentage: Float,
    val partialPercentage: Float = 0f
)

data class StatsSystemMetric(
    val title: String,
    val percentage: Int,
    val details: String,
    val iconName: String
)

data class StatsInsight(
    val message: String,
    val iconName: String
)
