package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.data.mapper.toEntity
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.ValidateActivityNodeUseCase
import com.alan.routineos.domain.usecase.ValidateMetadataSchemaUseCase
import com.alan.routineos.domain.usecase.ValidateScheduleRuleUseCase
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class OfflineActivityRepository @Inject constructor(
    private val activityDefinitionDao: ActivityDefinitionDao,
    private val activityNodeDao: ActivityNodeDao,
    private val activityExecutionDao: ActivityExecutionDao,
    private val scheduleRuleDao: ScheduleRuleDao,
    private val scheduleExceptionDao: ScheduleExceptionDao,
    private val dailyInstanceDao: DailyInstanceDao,
    private val metadataSchemaDao: MetadataSchemaDao,
    private val validateActivityNodeUseCase: ValidateActivityNodeUseCase,
    private val validateScheduleRuleUseCase: ValidateScheduleRuleUseCase,
    private val validateMetadataSchemaUseCase: ValidateMetadataSchemaUseCase
) : ActivityRepository {

    override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> {
        return activityDefinitionDao.getAllActivityDefinitions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllNodes(): Flow<List<ActivityNode>> {
        return activityNodeDao.getAllNodes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? {
        return activityDefinitionDao.getActivityDefinitionById(id)?.toDomain()
    }

    override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {
        activityDefinitionDao.insertActivityDefinition(activityDefinition.toEntity())
    }

    override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {
        activityDefinitionDao.deleteActivityDefinition(activityDefinition.toEntity())
    }

    override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> {
        return activityNodeDao.getNodesForActivityDefinition(activityDefinitionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> {
        return activityNodeDao.getNodesListForActivityDefinition(activityDefinitionId).map { it.toDomain() }
    }

    override suspend fun getNodeById(id: String): ActivityNode? {
        return activityNodeDao.getNodeById(id)?.toDomain()
    }

    override suspend fun upsertNode(node: ActivityNode) {
        val allNodes = activityNodeDao.getNodesListForActivityDefinition(node.activityDefinitionId)
            .map { it.toDomain() }
        
        val error = validateActivityNodeUseCase(node, allNodes)
        if (error != null) {
            throw IllegalArgumentException("Node validation failed: $error")
        }
        
        activityNodeDao.insertNode(node.toEntity())
    }

    override suspend fun deleteNode(node: ActivityNode) {
        activityNodeDao.deleteNode(node.toEntity())
    }

    override suspend fun reorderNodes(nodeIds: List<String>) {
        if (nodeIds.isEmpty()) return
        
        // Load first node to get activityDefinitionId and parentId (for sibling boundary check)
        val firstNode = activityNodeDao.getNodeById(nodeIds[0]) ?: return
        val expectedParentId = firstNode.parentId
        val expectedDefId = firstNode.activityDefinitionId

        val entities = nodeIds.mapIndexedNotNull { index, id ->
            val node = activityNodeDao.getNodeById(id)
            if (node != null && node.parentId == expectedParentId && node.activityDefinitionId == expectedDefId) {
                node.copy(position = index)
            } else {
                null
            }
        }
        activityNodeDao.updateNodes(entities)
    }

    override suspend fun moveNode(nodeId: String, newParentId: String?) {
        val nodeEntity = activityNodeDao.getNodeById(nodeId) ?: return
        val node = nodeEntity.toDomain().copy(parentId = newParentId)
        
        val allNodes = activityNodeDao.getNodesListForActivityDefinition(node.activityDefinitionId)
            .map { it.toDomain() }

        val error = validateActivityNodeUseCase(node, allNodes)
        if (error != null) {
            throw IllegalArgumentException("Node move validation failed: $error")
        }

        activityNodeDao.insertNode(node.toEntity())
    }

    override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {
        val execution = ActivityExecution(
            id = UUID.randomUUID().toString(),
            nodeId = nodeId,
            dailyInstanceId = dailyInstanceId,
            scheduledDate = scheduledDate,
            completedAt = System.currentTimeMillis(),
            metadataJson = metadataJson
        )
        activityExecutionDao.insertExecution(execution.toEntity())
    }

    override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> {
        return activityExecutionDao.getExecutionsForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> {
        return activityExecutionDao.getExecutionsForNodeOnDate(nodeId, scheduledDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {
        activityExecutionDao.deleteExecutionsForNodeOnDate(nodeId, scheduledDate)
    }

    override fun getAllRules(): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getAllRules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getRulesForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule> {
        return scheduleRuleDao.getRulesListForNode(nodeId).map { it.toDomain() }
    }

    override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getRulesForDefinition(definitionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRule> {
        return scheduleRuleDao.getRulesListForDefinition(definitionId).map { it.toDomain() }
    }

    override fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getRulesForActivityTree(definitionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertRule(rule: ScheduleRule) {
        val existingRules = when (val target = rule.target) {
            is ScheduleTarget.Definition -> getRulesListForDefinition(target.id)
            is ScheduleTarget.Node -> getRulesListForNode(target.id)
        }
        
        val error = validateScheduleRuleUseCase(rule, existingRules)
        if (error != null) {
            throw IllegalArgumentException(error.userMessage)
        }
        
        scheduleRuleDao.insertRule(rule.toEntity())
    }

    override suspend fun deleteRule(rule: ScheduleRule) {
        scheduleRuleDao.deleteRule(rule.toEntity())
    }

    override fun getAllExceptions(): Flow<List<ScheduleException>> {
        return scheduleExceptionDao.getAllExceptions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> {
        return scheduleExceptionDao.getExceptionsForRule(ruleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertException(exception: ScheduleException) {
        scheduleExceptionDao.insertException(exception.toEntity())
    }

    override suspend fun deleteException(exception: ScheduleException) {
        scheduleExceptionDao.deleteException(exception.toEntity())
    }

    override fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>> {
        return dailyInstanceDao.getInstancesForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertDailyInstance(instance: DailyInstance) {
        dailyInstanceDao.insertInstance(instance.toEntity())
    }

    override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? {
        return dailyInstanceDao.getInstanceByTarget(targetId, date)?.toDomain()
    }

    override fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?> {
        return metadataSchemaDao.getSchema(targetId, targetType).map { it?.toDomain() }
    }

    override suspend fun upsertMetadataSchema(schema: MetadataSchema) {
        val error = validateMetadataSchemaUseCase(schema)
        if (error != null) {
            throw IllegalArgumentException(error.userMessage)
        }

        val currentEntity = metadataSchemaDao.getSchema(
            when (val target = schema.target) {
                is ScheduleTarget.Definition -> target.id
                is ScheduleTarget.Node -> target.id
            },
            when (schema.target) {
                is ScheduleTarget.Definition -> "DEFINITION"
                is ScheduleTarget.Node -> "NODE"
            }
        ).firstOrNull()

        val nextVersion = if (currentEntity != null && currentEntity.fieldsJson != Json.encodeToString(schema.fields)) {
            currentEntity.schemaVersion + 1
        } else {
            currentEntity?.schemaVersion ?: 1
        }

        metadataSchemaDao.insertSchema(schema.copy(schemaVersion = nextVersion).toEntity())
    }

    override suspend fun deleteMetadataSchema(targetId: String, targetType: String) {
        metadataSchemaDao.getSchema(targetId, targetType).firstOrNull()?.let {
            metadataSchemaDao.deleteSchema(it)
        }
    }
}
