package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDefinitionDao {
    @Query("SELECT * FROM activity_definitions WHERE isDeleted = 0")
    fun getAllActivityDefinitions(): Flow<List<ActivityDefinitionEntity>>

    @Query("SELECT * FROM activity_definitions WHERE id = :id AND isDeleted = 0")
    suspend fun getActivityDefinitionById(id: String): ActivityDefinitionEntity?

    @Upsert
    suspend fun insertActivityDefinition(activityDefinition: ActivityDefinitionEntity)

    @Delete
    suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinitionEntity)

    @Query("SELECT * FROM activity_definitions WHERE isDeleted = 0")
    suspend fun getDefinitionsList(): List<ActivityDefinitionEntity>
}
