package com.alan.routineos.data.local.dao

import androidx.room.*
import com.alan.routineos.data.local.entities.MetadataSchemaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MetadataSchemaDao {
    @Query("SELECT * FROM metadata_schemas WHERE targetId = :targetId AND targetType = :targetType LIMIT 1")
    fun getSchema(targetId: String, targetType: String): Flow<MetadataSchemaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchema(schema: MetadataSchemaEntity)

    @Delete
    suspend fun deleteSchema(schema: MetadataSchemaEntity)
}
