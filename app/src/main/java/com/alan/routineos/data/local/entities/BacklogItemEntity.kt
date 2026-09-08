package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "backlog_items",
    foreignKeys = [
        ForeignKey(
            entity = ActivityDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("definitionId")]
)
data class BacklogItemEntity(
    @PrimaryKey
    val id: String,
    val definitionId: String?,
    val title: String,
    val status: String // OPEN, RESOLVED, ARCHIVED
)
