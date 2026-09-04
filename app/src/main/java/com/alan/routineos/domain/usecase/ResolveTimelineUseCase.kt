package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class TimelineEntry(
    val instance: DailyInstance,
    val isMaterialized: Boolean,
    val conflict: ConflictResult? = null
)

/**
 * Resolves the timeline for a specific date by merging rules, exceptions, and persisted instances.
 * Delegated to TimelineResolutionEngine for semantic consistency.
 */
class ResolveTimelineUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val resolutionEngine: TimelineResolutionEngine,
    private val conflictDetector: ConflictDetectorUseCase,
    private val suggestionEngine: SuggestionEngine
) {
    operator fun invoke(date: LocalDate): Flow<List<TimelineEntry>> {
        val epochDay = date.toEpochDay()

        return combine(
            repository.getActivityDefinitions(),
            repository.getAllNodes(),
            repository.getAllRules(),
            repository.getAllExceptions(),
            repository.getDailyInstancesForDate(epochDay)
        ) { definitions, nodes, rules, exceptions, materialized ->
            
            val resolved = resolutionEngine.resolve(
                date = date,
                rules = rules,
                exceptions = exceptions,
                materialized = materialized,
                definitions = definitions,
                nodes = nodes
            )

            val entries = resolved.map { occ ->
                TimelineEntry(
                    instance = occ.instance,
                    isMaterialized = occ.isMaterialized
                )
            }

            detectConflicts(entries, nodes.associateBy { it.id })
        }
    }

    private fun detectConflicts(entries: List<TimelineEntry>, nodeMap: Map<String, ActivityNode>): List<TimelineEntry> {
        val instances = entries.map { it.instance }
        val initialConflicts = conflictDetector.detectConflicts(instances, nodeMap)
        
        return entries.map { entry ->
            val result = initialConflicts[entry.instance.id] ?: ConflictResult(false)
            val suggestions = if (result.impact == TemporalImpact.WARNING) {
                suggestionEngine.generateSuggestions(entry.instance, instances, nodeMap)
            } else emptyList()
            
            entry.copy(conflict = result.copy(suggestions = suggestions))
        }.sortedBy { it.instance.plannedStartTime ?: Int.MAX_VALUE }
    }
}
