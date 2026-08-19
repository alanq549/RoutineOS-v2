package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_definitions",
    foreignKeys = [
        ForeignKey(
            entity = SystemEntity::class,
            parentColumns = ["id"],
            childColumns = ["systemId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("systemId")]
)
data class ActivityDefinitionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val systemId: String? = null,
    val isDeleted: Boolean = false
)
