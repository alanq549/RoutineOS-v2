package com.alan.routineos.domain.repository

import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.ScheduleException
import com.alan.routineos.domain.model.ScheduleRule
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivityDefinitions(): Flow<List<ActivityDefinition>>
    suspend fun getActivityDefinitionById(id: String): ActivityDefinition?
    suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition)
    suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition)
    fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>>
    suspend fun upsertNode(node: ActivityNode)
    suspend fun deleteNode(node: ActivityNode)
    suspend fun reorderNodes(nodeIds: List<String>)
    suspend fun moveNode(nodeId: String, newParentId: String?)
    
    suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String)
    fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>>
    fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>>
    suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long)

    // Scheduling
    fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>>
    suspend fun upsertRule(rule: ScheduleRule)
    suspend fun deleteRule(rule: ScheduleRule)

    fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>>
    suspend fun upsertException(exception: ScheduleException)
    suspend fun deleteException(exception: ScheduleException)
}
