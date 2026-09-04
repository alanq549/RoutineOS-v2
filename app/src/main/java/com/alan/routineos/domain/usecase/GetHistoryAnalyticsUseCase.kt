package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Analytics engine that calculates KPIs based on historical occurrences and executions.
 * Implements strict non-inference policy for missing data.
 */
class GetHistoryAnalyticsUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val occurrenceResolver: HistoricalOccurrenceResolver
) {

    suspend fun execute(start: LocalDate, end: LocalDate): HistorySnapshot {
        val occurrences = occurrenceResolver.resolveRange(start, end)
        val allNodes = repository.getAllNodes().first()
        val leafNodeIds = allNodes.filter { node -> allNodes.none { it.parentId == node.id } }.map { it.id }.toSet()
        
        // Filter to include only leaf node occurrences or ad-hoc without targets
        val leafOccurrences = occurrences.filter { occ ->
            val target = occ.instance.target
            target == null || (target is ScheduleTarget.Node && leafNodeIds.contains(target.id))
        }

        val totalOccurrences = leafOccurrences.size
        val omittedCount = leafOccurrences.count { it.instance.status == DailyInstanceStatus.OMITTED }
        val completedCount = leafOccurrences.count { it.instance.status == DailyInstanceStatus.COMPLETED }
        val eligibleCount = totalOccurrences - omittedCount
        val missedCount = eligibleCount - completedCount

        val completionRate = if (eligibleCount > 0) completedCount.toFloat() / eligibleCount else null

        // Execution Consistency: Days with >= 70% completion
        val dailyRates = leafOccurrences.groupBy { it.date }.map { (_, dayOccs) ->
            val dTotal = dayOccs.size
            val dOmitted = dayOccs.count { it.instance.status == DailyInstanceStatus.OMITTED }
            val dCompleted = dayOccs.count { it.instance.status == DailyInstanceStatus.COMPLETED }
            val dEligible = dTotal - dOmitted
            if (dEligible > 0) dCompleted.toFloat() / dEligible else null
        }.filterNotNull()

        val consistentDays = dailyRates.count { it >= 0.7f }
        val consistencyScore = if (dailyRates.isNotEmpty()) consistentDays.toFloat() / dailyRates.size else null

        // Temporal Metrics & Focus Index (N/A policy - placeholders for future implementation)
        return HistorySnapshot(
            completionRate = completionRate,
            totalOccurrences = totalOccurrences,
            completedCount = completedCount,
            missedCount = missedCount,
            omittedCount = omittedCount,
            executionConsistency = consistencyScore,
            startDeviationAvgMinutes = null,
            durationDeviationAvgMinutes = null
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
                // Try parsing double directly or from content string
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
