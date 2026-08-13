package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.data.mapper.toEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.ScheduleException
import com.alan.routineos.domain.model.ScheduleRule
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class OfflineActivityRepository @Inject constructor(
    private val activityDefinitionDao: ActivityDefinitionDao,
    private val activityNodeDao: ActivityNodeDao,
    private val activityExecutionDao: ActivityExecutionDao,
    private val scheduleRuleDao: ScheduleRuleDao,
    private val scheduleExceptionDao: ScheduleExceptionDao
) : ActivityRepository {

    override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> {
        return activityDefinitionDao.getAllActivityDefinitions().map { entities ->
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

    override suspend fun upsertNode(node: ActivityNode) {
        activityNodeDao.insertNode(node.toEntity())
    }

    override suspend fun deleteNode(node: ActivityNode) {
        activityNodeDao.deleteNode(node.toEntity())
    }

    override suspend fun reorderNodes(nodeIds: List<String>) {
        val entities = nodeIds.mapIndexedNotNull { index, id ->
            activityNodeDao.getNodeById(id)?.copy(position = index)
        }
        activityNodeDao.updateNodes(entities)
    }

    override suspend fun moveNode(nodeId: String, newParentId: String?) {
        activityNodeDao.getNodeById(nodeId)?.let { node ->
            activityNodeDao.insertNode(node.copy(parentId = newParentId))
        }
    }

    override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String) {
        val execution = ActivityExecutionEntity(
            id = UUID.randomUUID().toString(),
            nodeId = nodeId,
            scheduledDate = scheduledDate,
            completedAt = System.currentTimeMillis(),
            metadataJson = metadataJson
        )
        activityExecutionDao.insertExecution(execution)
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

    override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getRulesForNode(nodeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> {
        return scheduleRuleDao.getRulesForDefinition(definitionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertRule(rule: ScheduleRule) {
        scheduleRuleDao.insertRule(rule.toEntity())
    }

    override suspend fun deleteRule(rule: ScheduleRule) {
        scheduleRuleDao.deleteRule(rule.toEntity())
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
}
