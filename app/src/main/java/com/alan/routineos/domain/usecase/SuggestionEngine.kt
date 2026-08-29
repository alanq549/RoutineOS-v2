package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import javax.inject.Inject

/**
 * Generates and validates MOVE suggestions for flexible tasks.
 */
class SuggestionEngine @Inject constructor(
    private val conflictDetector: ConflictDetectorUseCase
) {

    fun generateSuggestions(
        target: DailyInstance,
        allInstances: List<DailyInstance>,
        nodeMap: Map<String, ActivityNode>
    ): List<ConflictSuggestion> {
        if (target.mobility != TemporalMobility.FLEXIBLE) return emptyList()

        val suggestions = mutableListOf<ConflictSuggestion>()
        val start = target.plannedStartTime ?: return emptyList()
        val duration = target.plannedDurationMinutes ?: 30

        // Find relevant immobile blocks or fixed times to jump over
        val obstacleEndTimes = allInstances
            .filter { it.id != target.id && (it.mobility == TemporalMobility.IMMOBILE || it.plannedStartTime != null) }
            .mapNotNull { 
                val s = it.plannedStartTime ?: return@mapNotNull null
                val e = it.plannedEndTime ?: it.plannedDurationMinutes?.let { d -> s + d } ?: (s + 30)
                e
            }
            .filter { it > start }
            .distinct()
            .sorted()

        obstacleEndTimes.forEach { candidateStart ->
            val candidateInstance = target.copy(
                plannedStartTime = candidateStart,
                plannedEndTime = candidateStart + duration
            )
            
            if (validateGlobalState(candidateInstance, allInstances, nodeMap)) {
                suggestions.add(
                    ConflictSuggestion(
                        type = SuggestionType.MOVE,
                        newStartTimeMinutes = candidateStart,
                        message = "Mover a las ${formatTime(candidateStart)}"
                    )
                )
            }
        }

        return suggestions.take(2)
    }

    private fun validateGlobalState(
        candidate: DailyInstance,
        allInstances: List<DailyInstance>,
        nodeMap: Map<String, ActivityNode>
    ): Boolean {
        val simulatedList = allInstances.map { if (it.id == candidate.id) candidate else it }
        val results = conflictDetector.detectConflicts(simulatedList, nodeMap)
        
        val result = results[candidate.id]
        // Suggestion is valid if it doesn\u0027t generate any WARNING impacts
        return result?.impact != TemporalImpact.WARNING
    }

    private fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }
}
