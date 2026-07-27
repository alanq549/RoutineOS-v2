package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.data.mapper.toEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineActivityRepository @Inject constructor(
    private val activityDefinitionDao: ActivityDefinitionDao,
    private val activityNodeDao: ActivityNodeDao
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
}
