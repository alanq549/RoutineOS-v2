package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * Historical adapter that uses TimelineResolutionEngine to reconstruct past occurrences.
 */
class HistoricalOccurrenceResolver @Inject constructor(
    private val repository: ActivityRepository,
    private val resolutionEngine: TimelineResolutionEngine
) {

    suspend fun resolveRange(start: LocalDate, end: LocalDate): List<ResolvedOccurrence> {
        val definitions = repository.getActivityDefinitions().first()
        val nodes = repository.getAllNodes().first()
        val rules = repository.getAllRules().first()
        val exceptions = repository.getAllExceptions().first()
        val materializedRange = repository.getDailyInstancesForDateRange(start.toEpochDay(), end.toEpochDay()).first()
        val allExecutions = repository.getAllExecutions().first()

        val allOccurrences = mutableListOf<ResolvedOccurrence>()
        val consumedExecutionIds = mutableSetOf<String>()
        
        var current = start
        while (!current.isAfter(end)) {
            val materializedToday = materializedRange.filter { it.scheduledDate == current.toEpochDay() }
            val executionsToday = allExecutions.filter { it.scheduledDate == current.toEpochDay() }
            
            val resolved = resolutionEngine.resolve(
                date = current,
                rules = rules,
                exceptions = exceptions,
                materialized = materializedToday,
                definitions = definitions,
                nodes = nodes
            )
            
            val enriched = resolved.map { occ ->
                // Try to find a matching execution
                val match = executionsToday.find { exec ->
                    !consumedExecutionIds.contains(exec.id) && (
                        exec.dailyInstanceId == occ.instance.id || 
                        matchBySnapshot(exec, occ.instance)
                    )
                }
                if (match != null) consumedExecutionIds.add(match.id)
                
                occ.copy(execution = match)
            }
            
            allOccurrences.addAll(enriched)
            current = current.plusDays(1)
        }
        
        return allOccurrences
    }

    private fun matchBySnapshot(exec: ActivityExecution, instance: DailyInstance): Boolean {
        return when (val target = instance.target) {
            is ScheduleTarget.Node -> exec.nodeId == target.id || exec.titleSnapshot == instance.titleSnapshot
            is ScheduleTarget.Definition -> exec.activityIdSnapshot == target.id
            else -> false
        }
    }
}
