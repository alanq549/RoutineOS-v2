package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ActivityDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = BacklogItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["backlogId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = DailyInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["instanceId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ActivityExecutionEntity::class,
            parentColumns = ["id"],
            childColumns = ["executionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("definitionId"),
        Index("backlogId"),
        Index("instanceId"),
        Index("executionId")
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val definitionId: String? = null,
    val backlogId: String? = null,
    val instanceId: String? = null,
    val executionId: String? = null,
    
    // Historical Context Snapshots
    val dateSnapshot: Long,
    val targetTypeSnapshot: String? = null,
    val targetIdSnapshot: String? = null,
    val titleSnapshot: String? = null
)
