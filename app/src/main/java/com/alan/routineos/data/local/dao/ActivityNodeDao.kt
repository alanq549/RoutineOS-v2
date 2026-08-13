package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityNodeDao {
    @Query("SELECT * FROM activity_nodes WHERE activityDefinitionId = :activityDefinitionId")
    fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNode(node: ActivityNodeEntity)

    @Delete
    suspend fun deleteNode(node: ActivityNodeEntity)

    @Update
    suspend fun updateNodes(nodes: List<ActivityNodeEntity>)

    @Query("SELECT * FROM activity_nodes WHERE id = :id")
    suspend fun getNodeById(id: String): ActivityNodeEntity?

    @Query("SELECT * FROM activity_nodes WHERE activityDefinitionId = :activityDefinitionId")
    suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNodeEntity>
}
