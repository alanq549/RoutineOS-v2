package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ScheduleExceptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleExceptionDao {
    @Query("SELECT * FROM schedule_exceptions")
    fun getAllExceptions(): Flow<List<ScheduleExceptionEntity>>

    @Query("SELECT * FROM schedule_exceptions WHERE scheduleRuleId = :ruleId")
    fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleExceptionEntity>>

    @Upsert
    suspend fun insertException(exception: ScheduleExceptionEntity)

    @Delete
    suspend fun deleteException(exception: ScheduleExceptionEntity)
}
