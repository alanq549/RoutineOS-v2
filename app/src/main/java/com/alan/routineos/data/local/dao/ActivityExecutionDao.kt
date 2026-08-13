package com.alan.routineos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityExecutionDao {
    @Upsert
    suspend fun insertExecution(execution: ActivityExecutionEntity)

    @Query("SELECT * FROM activity_executions WHERE nodeId = :nodeId ORDER BY completedAt DESC")
    fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecutionEntity>>

    @Query("SELECT * FROM activity_executions WHERE nodeId = :nodeId AND scheduledDate = :scheduledDate")
    fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecutionEntity>>

    @Query("DELETE FROM activity_executions WHERE nodeId = :nodeId AND scheduledDate = :scheduledDate")
    suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long)

    @Query("DELETE FROM activity_executions WHERE nodeId = :nodeId")
    suspend fun deleteExecutionsForNode(nodeId: String)
}
