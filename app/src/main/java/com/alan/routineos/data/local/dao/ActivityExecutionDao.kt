package com.alan.routineos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityExecutionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: ActivityExecutionEntity)

    @Query("SELECT * FROM activity_executions WHERE nodeId = :nodeId ORDER BY completedAt DESC")
    fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecutionEntity>>

    @Query("DELETE FROM activity_executions WHERE nodeId = :nodeId")
    suspend fun deleteExecutionsForNode(nodeId: String)
}
