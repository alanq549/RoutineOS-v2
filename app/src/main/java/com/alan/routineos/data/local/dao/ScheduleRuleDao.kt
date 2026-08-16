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
        JOIN activity_nodes an ON sr.targetId = an.id
        WHERE sr.targetId = :nodeId AND sr.targetType = 'NODE' AND an.isDeleted = 0
    """)
    fun getRulesForNode(nodeId: String): Flow<List<ScheduleRuleEntity>>

    @Query("SELECT * FROM schedule_rules WHERE targetId = :definitionId AND targetType = 'DEFINITION'")
    fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRuleEntity>>

    @Query("""
        SELECT sr.* FROM schedule_rules sr
        LEFT JOIN activity_nodes an ON sr.targetId = an.id
        WHERE (sr.targetId = :definitionId AND sr.targetType = 'DEFINITION')
        OR (an.activityDefinitionId = :definitionId AND sr.targetType = 'NODE' AND an.isDeleted = 0)
    """)
    fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRuleEntity>>

    @Upsert
    suspend fun insertRule(rule: ScheduleRuleEntity)

    @Delete
    suspend fun deleteRule(rule: ScheduleRuleEntity)

    @Query("SELECT * FROM schedule_rules WHERE targetId = :nodeId AND targetType = 'NODE'")
    suspend fun getRulesListForNode(nodeId: String): List<ScheduleRuleEntity>

    @Query("SELECT * FROM schedule_rules WHERE targetId = :definitionId AND targetType = 'DEFINITION'")
    suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRuleEntity>
}
