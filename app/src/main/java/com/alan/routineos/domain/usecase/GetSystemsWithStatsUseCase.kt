package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@Deprecated("To be integrated into Stats module in future phases. Currently unused in consolidated Activities view.")
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

                val completedCount = systemInstances.count { it.status == DailyInstanceStatus.COMPLETED }
                val skippedCount = systemInstances.count { it.status == DailyInstanceStatus.OMITTED }
                val pendingCount = systemInstances.count { it.status == DailyInstanceStatus.PLANNED || it.status == DailyInstanceStatus.MODIFIED }
                
                val totalWithResult = completedCount + skippedCount

                SystemWithStats(
                    system = system,
                    activityCount = systemActivities.size,
                    scheduledCount = systemInstances.size,
                    completedCount = completedCount,
                    skippedCount = skippedCount,
                    pendingCount = pendingCount,
                    successRate = if (totalWithResult == 0) null else completedCount.toFloat() / totalWithResult
                )
            }
        }
    }
}
