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

        val allOccurrences = mutableListOf<ResolvedOccurrence>()
        
        var current = start
        while (!current.isAfter(end)) {
            val materializedToday = materializedRange.filter { it.scheduledDate == current.toEpochDay() }
            
            val resolved = resolutionEngine.resolve(
                date = current,
                rules = rules,
                exceptions = exceptions,
                materialized = materializedToday,
                definitions = definitions,
                nodes = nodes
            )
            
            allOccurrences.addAll(resolved)
            current = current.plusDays(1)
        }
        
        return allOccurrences
    }
}
