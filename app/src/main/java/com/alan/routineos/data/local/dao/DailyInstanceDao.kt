package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyInstanceDao {
    @Query("SELECT * FROM daily_instances WHERE scheduledDate = :date")
    fun getInstancesForDate(date: Long): Flow<List<DailyInstanceEntity>>

    @Query("SELECT * FROM daily_instances WHERE targetId = :targetId AND scheduledDate = :date LIMIT 1")
    suspend fun getInstanceByTarget(targetId: String, date: Long): DailyInstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstance(instance: DailyInstanceEntity)

    @Delete
    suspend fun deleteInstance(instance: DailyInstanceEntity)
}
