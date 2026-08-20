package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import java.util.*
import javax.inject.Inject

sealed class DailyAction {
    data class Complete(val metadataJson: String = "{}") : DailyAction()
    object Skip : DailyAction()
    data class Move(val newStartTime: Int) : DailyAction()
}

/**
 * Handles atomic actions on a timeline instance (Virtual or Materialized).
 * Ensures materialization happens before state changes.
 */
class RegisterDailyActionUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val materializeInstanceUseCase: MaterializeInstanceUseCase
) {
    suspend operator fun invoke(
        entry: TimelineEntry,
        action: DailyAction
    ) {
        val instance = if (entry.isMaterialized) {
            entry.instance
        } else {
            materializeInstanceUseCase(entry.instance)
        }

        when (action) {
            is DailyAction.Complete -> handleComplete(instance, action.metadataJson)
            is DailyAction.Skip -> handleSkip(instance)
            is DailyAction.Move -> handleMove(instance, action.newStartTime)
        }
    }

    private suspend fun handleComplete(instance: DailyInstance, metadataJson: String) {
        // 1. Update instance status
        repository.upsertDailyInstance(instance.copy(status = DailyInstanceStatus.MODIFIED))
        
        // 2. Register execution ONLY if it has a Node target
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

    private suspend fun handleSkip(instance: DailyInstance) {
        repository.upsertDailyInstance(instance.copy(status = DailyInstanceStatus.OMITTED))
    }

    private suspend fun handleMove(instance: DailyInstance, newStartTime: Int) {
        repository.upsertDailyInstance(
            instance.copy(
                status = DailyInstanceStatus.MODIFIED,
                plannedStartTime = newStartTime
            )
        )
    }
}
