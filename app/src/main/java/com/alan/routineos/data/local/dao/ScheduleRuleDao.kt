package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleRuleDao {
    @Query("SELECT * FROM schedule_rules WHERE nodeId = :nodeId")
    fun getRulesForNode(nodeId: String): Flow<List<ScheduleRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ScheduleRuleEntity)

    @Delete
    suspend fun deleteRule(rule: ScheduleRuleEntity)
}
