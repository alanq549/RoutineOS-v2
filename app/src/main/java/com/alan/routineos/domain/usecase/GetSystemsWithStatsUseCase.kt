package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

class GetSystemsWithStatsUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(): Flow<List<SystemWithStats>> {
        val todayEpoch = LocalDate.now().toEpochDay()
        val startOfWeek = todayEpoch - 7

        return combine(
            repository.getAllSystems(),
            repository.getActivityDefinitions(),
            repository.getAllNodes(),
            repository.getDailyInstancesForDateRange(startOfWeek, todayEpoch),
            repository.getAllExecutions()
        ) { systems, definitions, nodes, instances, executions ->
            
            val nodeToDefMap = nodes.associate { it.id to it.activityDefinitionId }
            val instanceToExecutionMap = executions.groupBy { it.dailyInstanceId }

            systems.map { system ->
                val systemActivities = definitions.filter { it.systemId == system.id }
                val systemDefIds = systemActivities.map { it.id }.toSet()

                val systemInstances = instances.filter { instance ->
                    val defId = when (instance.target) {
                        is ScheduleTarget.Definition -> instance.target.id
                        is ScheduleTarget.Node -> nodeToDefMap[instance.target.id]
                        else -> null
                    }
                    systemDefIds.contains(defId)
                }

                val completions = systemInstances.count { instance ->
                    instanceToExecutionMap[instance.id]?.isNotEmpty() ?: false
                }

                SystemWithStats(
                    system = system,
                    activityCount = systemActivities.size,
                    instanceCount = systemInstances.size,
                    completionCount = completions,
                    successRate = if (systemInstances.isEmpty()) 0f else completions.toFloat() / systemInstances.size
                )
            }
        }
    }
}
