package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.DeadlineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeadlineDao {
    @Query("SELECT * FROM deadlines")
    fun getAllDeadlines(): Flow<List<DeadlineEntity>>

    @Upsert
    suspend fun upsertDeadline(deadline: DeadlineEntity)

    @Delete
    suspend fun deleteDeadline(deadline: DeadlineEntity)
}
