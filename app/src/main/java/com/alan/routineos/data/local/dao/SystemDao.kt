package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.SystemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemDao {
    @Query("SELECT * FROM life_systems")
    fun getAllSystems(): Flow<List<SystemEntity>>

    @Query("SELECT * FROM life_systems")
    suspend fun getSystemsList(): List<SystemEntity>

    @Query("SELECT * FROM life_systems WHERE id = :id")
    suspend fun getSystemById(id: String): SystemEntity?

    @Upsert
    suspend fun upsertSystem(system: SystemEntity)

    @Delete
    suspend fun deleteSystem(system: SystemEntity)
}
