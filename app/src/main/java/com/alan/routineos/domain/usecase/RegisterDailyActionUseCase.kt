package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import java.util.*
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
        // Completion only allowed on leaf nodes (executable steps)
        if (entry.children.isNotEmpty()) return

        val instance = materializeIfVirtual(entry.root)
        repository.upsertDailyInstance(instance.copy(status = DailyInstanceStatus.COMPLETED))
        
        val target = instance.target
        if (target is ScheduleTarget.Node) {
            repository.registerExecution(
                nodeId = target.id,
                scheduledDate = instance.scheduledDate,
                metadataJson = metadataJson,
                dailyInstanceId = instance.id
            )
        }
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
        
        val targetStatus = if (instance.isAdHoc || instance.sourceRuleId == null) {
            DailyInstanceStatus.MODIFIED 
        } else {
            DailyInstanceStatus.PLANNED
        }
        
        repository.upsertDailyInstance(instance.copy(status = targetStatus))

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
