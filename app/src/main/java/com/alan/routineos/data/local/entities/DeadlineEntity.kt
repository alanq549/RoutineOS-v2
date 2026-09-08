package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deadlines",
    foreignKeys = [
        ForeignKey(
            entity = ActivityDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["definitionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BacklogItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["backlogId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DailyInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["instanceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("definitionId"),
        Index("backlogId"),
        Index("instanceId")
    ]
)
data class DeadlineEntity(
    @PrimaryKey
    val id: String,
    val dueAt: Long,
    val definitionId: String? = null,
    val backlogId: String? = null,
    val instanceId: String? = null
)
