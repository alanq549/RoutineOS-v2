package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "metadata_schemas",
    indices = [
        Index(
            value = ["targetType", "targetId"],
            unique = true
        )
    ]
)
data class MetadataSchemaEntity(
    @PrimaryKey
    val id: String,
    val targetId: String,
    val targetType: String, // DEFINITION, NODE
    val fieldsJson: String, // Serialized List<MetadataField>
    val schemaVersion: Int
)
