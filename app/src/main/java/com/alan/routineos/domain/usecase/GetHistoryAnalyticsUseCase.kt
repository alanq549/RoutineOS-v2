package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

/**
 * Analytics engine that calculates KPIs based on historical occurrences and executions.
 * Implements granular data for high-fidelity UI (Ritmos/Ciclos).
 */
class GetHistoryAnalyticsUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val occurrenceResolver: HistoricalOccurrenceResolver
) {

    suspend fun execute(start: LocalDate, end: LocalDate): HistorySnapshot {
        val resolvedOccs = occurrenceResolver.resolveRange(start, end)
        val allNodes = repository.getAllNodes().first()
        val allDefinitions = repository.getActivityDefinitions().first()
        val allSystems = repository.getAllSystems().first()
        
        val leafNodeIds = allNodes.filter { node -> allNodes.none { it.parentId == node.id } }.map { it.id }.toSet()
        
        // Enrich occurrences with activity titles for UI grouping
        val enrichedOccurrences = resolvedOccs.map { occ ->
            val target = occ.instance.target
            val activityTitle = if (target is ScheduleTarget.Node) {
                val node = allNodes.find { it.id == target.id }
                allDefinitions.find { it.id == node?.activityDefinitionId }?.title
            } else if (target is ScheduleTarget.Definition) {
                allDefinitions.find { it.id == target.id }?.title
            } else null
            
            occ.copy(activityTitle = activityTitle)
        }

        // Metrics are calculated based on leaf nodes (to avoid double counting with parents)
        // or ad-hoc instances (which are always counted).
        val metricOccurrences = enrichedOccurrences.filter { occ ->
            val target = occ.instance.target
            target == null || (target is ScheduleTarget.Node && leafNodeIds.contains(target.id))
        }

        // 1. Daily Stats Breakdown
        val dailyStatsList = mutableListOf<DailyStats>()
        var current = start
        while (!current.isAfter(end)) {
            val dayMetricOccs = metricOccurrences.filter { it.date == current }
            val dayAllOccs = enrichedOccurrences.filter { it.date == current }
            
            val completed = dayMetricOccs.count { it.instance.status == DailyInstanceStatus.COMPLETED && !it.isAdHoc }
            val omitted = dayMetricOccs.count { it.instance.status == DailyInstanceStatus.OMITTED }
            val eligible = dayMetricOccs.count { !it.isAdHoc } - omitted
            val missed = eligible - completed
            val spontaneous = dayMetricOccs.count { it.isAdHoc }

            dailyStatsList.add(DailyStats(
                date = current,
                completionRate = if (eligible > 0) completed.toFloat() / eligible else null,
                completedCount = completed,
                omittedCount = omitted,
                missedCount = missed,
                spontaneousCount = spontaneous,
                occurrences = dayAllOccs // Include ALL (containers + leaves) for the UI list
            ))
            current = current.plusDays(1)
        }

        // 2. Weekly Stats Breakdown
        val weekFields = WeekFields.of(Locale.getDefault())
        val weeklyStatsList = dailyStatsList.groupBy { 
            it.date.get(weekFields.weekOfWeekBasedYear())
        }.map { (_, dayStats) ->
            val sCompleted = dayStats.sumOf { it.completedCount }
            val sOmitted = dayStats.sumOf { it.omittedCount }
            val sTotal = dayStats.sumOf { it.occurrences.filter { occ ->
                val target = occ.instance.target
                target == null || (target is ScheduleTarget.Node && leafNodeIds.contains(target.id))
            }.size }
            val sEligible = sTotal - sOmitted

            WeeklyStats(
                startOfWeek = dayStats.minBy { it.date }.date,
                endOfWeek = dayStats.maxBy { it.date }.date,
                completionRate = if (sEligible > 0) sCompleted.toFloat() / sEligible else null,
                dailyStats = dayStats
            )
        }

        // 3. Monthly Stats Breakdown
        val monthlyStatsList = dailyStatsList.groupBy { YearMonth.from(it.date) }.map { (ym, dayStats) ->
            val mCompleted = dayStats.sumOf { it.completedCount }
            val mOmitted = dayStats.sumOf { it.omittedCount }
            val mTotal = dayStats.sumOf { it.occurrences.filter { occ ->
                val target = occ.instance.target
                target == null || (target is ScheduleTarget.Node && leafNodeIds.contains(target.id))
            }.size }
            val mEligible = mTotal - mOmitted
            
            val mWeekly = dayStats.chunked(7).map { weekDays ->
                val wCompleted = weekDays.sumOf { it.completedCount }
                val wOmitted = weekDays.sumOf { it.omittedCount }
                val wTotal = weekDays.sumOf { it.occurrences.filter { occ ->
                    val target = occ.instance.target
                    target == null || (target is ScheduleTarget.Node && leafNodeIds.contains(target.id))
                }.size }
                val wEligible = wTotal - wOmitted
                WeeklyStats(
                    startOfWeek = weekDays.first().date,
                    endOfWeek = weekDays.last().date,
                    completionRate = if (wEligible > 0) wCompleted.toFloat() / wEligible else null,
                    dailyStats = weekDays
                )
            }

            MonthlyStats(
                yearMonth = ym,
                completionRate = if (mEligible > 0) mCompleted.toFloat() / mEligible else null,
                weeklyStats = mWeekly
            )
        }

        // 4. System Adherence
        val systemAdherence = allSystems.map { system ->
            val systemDefIds = allDefinitions.filter { it.systemId == system.id }.map { it.id }.toSet()
            val systemOccurrences = metricOccurrences.filter { occ ->
                val target = occ.instance.target
                if (target is ScheduleTarget.Node) {
                    val node = allNodes.find { it.id == target.id }
                    node?.activityDefinitionId != null && systemDefIds.contains(node.activityDefinitionId)
                } else if (target is ScheduleTarget.Definition) {
                    systemDefIds.contains(target.id)
                } else false
            }

            val sTotal = systemOccurrences.count { !it.isAdHoc }
            val sCompleted = systemOccurrences.count { it.instance.status == DailyInstanceStatus.COMPLETED && !it.isAdHoc }
            val sOmitted = systemOccurrences.count { it.instance.status == DailyInstanceStatus.OMITTED && !it.isAdHoc }
            val sEligible = sTotal - sOmitted
            val sMissed = sEligible - sCompleted

            SystemAdherence(
                systemId = system.id,
                systemName = system.title,
                completionRate = if (sEligible > 0) sCompleted.toFloat() / sEligible else null,
                omittedRate = if (sTotal > 0) sOmitted.toFloat() / sTotal else null,
                missedRate = if (sEligible > 0) sMissed.toFloat() / sEligible else null,
                completedCount = sCompleted,
                omittedCount = sOmitted,
                missedCount = sMissed
            )
        }

        // 5. Activity Adherence
        val activityAdherence = allDefinitions.map { definition ->
            val activityOccurrences = metricOccurrences.filter { occ ->
                val target = occ.instance.target
                if (target is ScheduleTarget.Node) {
                    val node = allNodes.find { it.id == target.id }
                    node?.activityDefinitionId == definition.id
                } else if (target is ScheduleTarget.Definition) {
                    target.id == definition.id
                } else false
            }

            val aTotal = activityOccurrences.count { !it.isAdHoc }
            val aCompleted = activityOccurrences.count { it.instance.status == DailyInstanceStatus.COMPLETED && !it.isAdHoc }
            val aOmitted = activityOccurrences.count { it.instance.status == DailyInstanceStatus.OMITTED && !it.isAdHoc }
            val aEligible = aTotal - aOmitted
            val aMissed = aEligible - aCompleted
            val aSpontaneous = activityOccurrences.count { it.isAdHoc }

            ActivityAdherence(
                activityId = definition.id,
                activityTitle = definition.title,
                completionRate = if (aEligible > 0) aCompleted.toFloat() / aEligible else null,
                omittedRate = if (aTotal > 0) aOmitted.toFloat() / aTotal else null,
                missedRate = if (aEligible > 0) aMissed.toFloat() / aEligible else null,
                spontaneousCount = aSpontaneous,
                completedCount = aCompleted,
                omittedCount = aOmitted,
                missedCount = aMissed
            )
        }.filter { it.completedCount > 0 || it.omittedCount > 0 || it.missedCount > 0 }

        // Global Averages (Respecting the requested range strictly)
        val totalOccurrences = metricOccurrences.count { !it.isAdHoc }
        val completedCount = metricOccurrences.count { it.instance.status == DailyInstanceStatus.COMPLETED && !it.isAdHoc }
        val omittedCount = metricOccurrences.count { it.instance.status == DailyInstanceStatus.OMITTED && !it.isAdHoc }
        val eligibleCount = totalOccurrences - omittedCount
        val missedCount = eligibleCount - completedCount
        val completionRate = if (eligibleCount > 0) completedCount.toFloat() / eligibleCount else null

        val consistentDays = dailyStatsList.count { it.completionRate != null && it.completionRate >= 0.7f }
        val consistencyScore = if (dailyStatsList.isNotEmpty()) consistentDays.toFloat() / dailyStatsList.size else null

        return HistorySnapshot(
            completionRate = completionRate,
            totalOccurrences = totalOccurrences,
            completedCount = completedCount,
            missedCount = missedCount,
            omittedCount = omittedCount,
            executionConsistency = consistencyScore,
            systemAdherence = systemAdherence,
            activityAdherence = activityAdherence,
            dailyStats = dailyStatsList,
            weeklyStats = weeklyStatsList,
            monthlyStats = monthlyStatsList
        )
    }

    suspend fun getTrendSeries(
        nodeId: String, 
        fieldName: String, 
        start: LocalDate, 
        end: LocalDate
    ): TrendSeries {
        val executions = repository.getExecutionsForNode(nodeId).first()
            .filter { 
                val date = LocalDate.ofEpochDay(it.scheduledDate)
                !date.isBefore(start) && !date.isAfter(end) 
            }
        
        val jsonParser = Json { ignoreUnknownKeys = true }
        
        val values = executions.mapNotNull { exec ->
            try {
                val element = jsonParser.parseToJsonElement(exec.metadataJson)
                val primitive = element.jsonObject[fieldName]?.jsonPrimitive
                val value = primitive?.doubleOrNull ?: primitive?.contentOrNull?.toDoubleOrNull()
                if (value != null) {
                    LocalDate.ofEpochDay(exec.scheduledDate) to value
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.first }

        return TrendSeries(fieldName, values)
    }
}
