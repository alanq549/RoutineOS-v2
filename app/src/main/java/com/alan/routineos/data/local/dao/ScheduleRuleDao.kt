package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleRuleDao {
    @Query("SELECT * FROM schedule_rules")
    fun getAllRules(): Flow<List<ScheduleRuleEntity>>

    @Query("""
        SELECT sr.* FROM schedule_rules sr
        JOIN activity_nodes an ON sr.activityNodeId = an.id
        WHERE sr.activityNodeId = :nodeId AND an.isDeleted = 0
    """)
    fun getRulesForNode(nodeId: String): Flow<List<ScheduleRuleEntity>>

    @Query("SELECT * FROM schedule_rules WHERE activityDefinitionId = :definitionId")
    fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRuleEntity>>

    @Upsert
    suspend fun insertRule(rule: ScheduleRuleEntity)

    @Delete
    suspend fun deleteRule(rule: ScheduleRuleEntity)
}
