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

        // Find relevant immobile blocks to jump over
        val immobileBlocks = allInstances
            .filter { it.id != target.id && it.mobility == TemporalMobility.IMMOBILE }
            .mapNotNull { it.plannedStartTime?.let { s -> it.plannedEndTime?.let { e -> s to e } } }
            .sortedBy { it.first }

        immobileBlocks.forEach { (_, end) ->
            val candidateStart = end
            if (candidateStart > start) {
                val candidateInstance = target.copy(
                    plannedStartTime = candidateStart,
                    plannedEndTime = candidateStart + duration
                )
                
                if (validateGlobalState(candidateInstance, allInstances, nodeMap)) {
                    suggestions.add(
                        ConflictSuggestion(
                            type = SuggestionType.MOVE,
                            newStartTimeMinutes = candidateStart,
                            message = "Move after immobile block to ${formatTime(candidateStart)}"
                        )
                    )
                }
            }
        }

        return suggestions.take(2) // Limit UI noise
    }

    private fun validateGlobalState(
        candidate: DailyInstance,
        allInstances: List<DailyInstance>,
        nodeMap: Map<String, ActivityNode>
    ): Boolean {
        val simulatedList = allInstances.map { if (it.id == candidate.id) candidate else it }
        val results = conflictDetector.detectConflicts(simulatedList, nodeMap)
        
        // Suggestion is valid if it doesn\u0027t generate any WARNING impacts for the candidate
        val result = results[candidate.id]
        return result?.impact != TemporalImpact.WARNING
    }

    private fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }
}
