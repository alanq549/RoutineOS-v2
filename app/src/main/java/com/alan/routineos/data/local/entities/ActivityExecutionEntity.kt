package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_executions",
    foreignKeys = [
        ForeignKey(
            entity = ActivityNodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("nodeId")]
)
data class ActivityExecutionEntity(
    @PrimaryKey
    val id: String,
    val nodeId: String,
    val completedAt: Long,
    val metadataJson: String = "{}"
)
