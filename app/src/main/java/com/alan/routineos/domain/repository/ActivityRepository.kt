package com.alan.routineos.domain.repository

import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.LifeSystem
import com.alan.routineos.domain.model.MetadataSchema
import com.alan.routineos.domain.model.ScheduleException
import com.alan.routineos.domain.model.ScheduleRule
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivityDefinitions(): Flow<List<ActivityDefinition>>
    fun getAllNodes(): Flow<List<ActivityNode>>
    suspend fun getActivityDefinitionById(id: String): ActivityDefinition?
    suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition)
    suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition)
    fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>>
    suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode>
    suspend fun getNodeById(id: String): ActivityNode?
    suspend fun upsertNode(node: ActivityNode)
    suspend fun deleteNode(node: ActivityNode)
    suspend fun reorderNodes(nodeIds: List<String>)
    suspend fun moveNode(nodeId: String, newParentId: String?)
    
    suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String? = null)
    fun getAllExecutions(): Flow<List<ActivityExecution>>
    fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>>
    fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>>
    suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long)

    // Scheduling
    fun getAllRules(): Flow<List<ScheduleRule>>
    fun getAllExceptions(): Flow<List<ScheduleException>>
    fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>>
    suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule>
    fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>>
    suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRule>
    fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRule>>
    suspend fun upsertRule(rule: ScheduleRule)
    suspend fun deleteRule(rule: ScheduleRule)

    fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>>
    suspend fun upsertException(exception: ScheduleException)
    suspend fun deleteException(exception: ScheduleException)

    // Daily Instances
    fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>>
    fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>>
    suspend fun upsertDailyInstance(instance: DailyInstance)
    suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance?

    // Metadata Schemas
    fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?>
    suspend fun upsertMetadataSchema(schema: MetadataSchema)
    suspend fun deleteMetadataSchema(targetId: String, targetType: String)

    // Systems
    fun getAllSystems(): Flow<List<LifeSystem>>
    suspend fun getSystemsList(): List<LifeSystem>
    suspend fun getSystemById(id: String): LifeSystem?
    suspend fun upsertSystem(system: LifeSystem)
    suspend fun deleteSystem(system: LifeSystem)
}
