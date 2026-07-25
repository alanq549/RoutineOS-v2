package com.alan.routineos.domain.repository

import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivityDefinitions(): Flow<List<ActivityDefinition>>
    suspend fun getActivityDefinitionById(id: String): ActivityDefinition?
    suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition)
    suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition)
    fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>>
    suspend fun upsertNode(node: ActivityNode)
    suspend fun deleteNode(node: ActivityNode)
}
