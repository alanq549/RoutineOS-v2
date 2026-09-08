package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.BacklogItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BacklogItemDao {
    @Query("SELECT * FROM backlog_items")
    fun getAllBacklogItems(): Flow<List<BacklogItemEntity>>

    @Upsert
    suspend fun upsertBacklogItem(item: BacklogItemEntity)

    @Delete
    suspend fun deleteBacklogItem(item: BacklogItemEntity)
}
