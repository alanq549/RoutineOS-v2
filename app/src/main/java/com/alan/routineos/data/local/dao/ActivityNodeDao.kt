package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityNodeDao {
    @Query("SELECT * FROM activity_nodes WHERE isDeleted = 0")
    fun getAllNodes(): Flow<List<ActivityNodeEntity>>

    @Query("SELECT * FROM activity_nodes WHERE activityDefinitionId = :activityDefinitionId AND isDeleted = 0")
    fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNodeEntity>>

    @Upsert
    suspend fun insertNode(node: ActivityNodeEntity)

    @Delete
    suspend fun deleteNode(node: ActivityNodeEntity)

    @Update
    suspend fun updateNodes(nodes: List<ActivityNodeEntity>)

    @Query("SELECT * FROM activity_nodes WHERE id = :id")
    suspend fun getNodeById(id: String): ActivityNodeEntity?

    @Query("SELECT * FROM activity_nodes WHERE activityDefinitionId = :activityDefinitionId AND isDeleted = 0")
    suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNodeEntity>
}
