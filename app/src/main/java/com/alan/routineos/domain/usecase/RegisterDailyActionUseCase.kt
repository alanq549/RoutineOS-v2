package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

sealed class DailyAction {
    data class Complete(val metadataJson: String = "{}") : DailyAction()
    object Skip : DailyAction()
    data class Move(val newStartTime: Int) : DailyAction()
    object Reset : DailyAction()
}

/**
 * Handles atomic and recursive actions on timeline nodes.
 * Ensures structural integrity by propagating changes through the hierarchy.
 */
class RegisterDailyActionUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val materializeInstanceUseCase: MaterializeInstanceUseCase
) {
    suspend operator fun invoke(
        entry: HierarchicalTimelineEntry,
        action: DailyAction
    ) {
        when (action) {
            is DailyAction.Complete -> handleComplete(entry, action.metadataJson)
            is DailyAction.Skip -> handleSkipRecursive(entry)
            is DailyAction.Move -> handleMoveRecursive(entry, action.newStartTime)
            is DailyAction.Reset -> handleResetRecursive(entry)
        }
    }

    private suspend fun handleComplete(entry: HierarchicalTimelineEntry, metadataJson: String) {
        // Completion allowed on leaf nodes (executable steps) or any Task (CHECK)
        // Note: Associated tasks are handled as individual entries in this use case
        if (entry.children.isNotEmpty() && entry.root.instance.actionProtocol == ActionProtocol.TIMER) return
        
        val instance = materializeIfVirtual(entry.root)
        
        // Reminder instances (which don't have a distinct NOTIFY protocol now) shouldn't produce executions if they aren't meant to be executable.
        // But to protect executions, any instance with reminder only metadata but no executable protocol shouldn't be completed here, 
        // or we guarantee it doesn't write execution if it's pure reminder metadata. 
        // Actually, if it has a valid operational protocol like CHECK or TIMER, it writes history.
        // If it's a pure reminder metadata item with no title or title but purely for warning attention, it shouldn't produce execution.
        // Let's protect it based on an explicit check or rule if needed. For now, just save status and execution.
        repository.upsertDailyInstance(instance.copy(status = DailyInstanceStatus.COMPLETED))
        
        // Register execution for the fact history (Blindaje de Historial CHECK/TIMER)
        repository.registerInstanceExecution(instance, metadataJson)
    }

    private suspend fun handleSkipRecursive(entry: HierarchicalTimelineEntry) {
        val instance = materializeIfVirtual(entry.root)
        
        // If leaf, mark as OMITTED. If container, status is derived but we mark the instance record.
        repository.upsertDailyInstance(instance.copy(status = DailyInstanceStatus.OMITTED))
        
        // Propagate to all descendants
        entry.children.forEach { child ->
            handleSkipRecursive(child)
        }
    }

    private suspend fun handleMoveRecursive(entry: HierarchicalTimelineEntry, newStartTime: Int) {
        val currentStart = entry.root.instance.plannedStartTime ?: entry.effectiveStartTimeMinutes ?: return
        val offset = newStartTime - currentStart
        
        applyMoveOffset(entry, offset)
    }

    private suspend fun applyMoveOffset(entry: HierarchicalTimelineEntry, offsetMinutes: Int) {
        val instance = materializeIfVirtual(entry.root)
        val originalStart = instance.plannedStartTime ?: entry.effectiveStartTimeMinutes
        
        if (originalStart != null) {
            repository.upsertDailyInstance(
                instance.copy(
                    status = DailyInstanceStatus.MODIFIED,
                    plannedStartTime = originalStart + offsetMinutes
                )
            )
        }

        entry.children.forEach { child ->
            applyMoveOffset(child, offsetMinutes)
        }
    }

    private suspend fun handleResetRecursive(entry: HierarchicalTimelineEntry) {
        val instance = materializeIfVirtual(entry.root)
        
        if (instance.sourceRuleId != null) {
            // Rule Override: Delete the instance to revert to original rule projection
            repository.deleteDailyInstance(instance.id)
        } else {
            // Pure Ad-hoc: Reset status to initial state
            val targetStatus = DailyInstanceStatus.MODIFIED
            repository.upsertDailyInstance(instance.copy(status = targetStatus))
        }

        // IMPORTANT: Non-destructive RESET. We do NOT delete ActivityExecution records.
        // History analysis will filter based on the final DailyInstance status.

        entry.children.forEach { child ->
            handleResetRecursive(child)
        }
    }

    private suspend fun materializeIfVirtual(entry: TimelineEntry): DailyInstance {
        return if (entry.isMaterialized) {
            entry.instance
        } else {
            materializeInstanceUseCase(entry.instance)
        }
    }
}
