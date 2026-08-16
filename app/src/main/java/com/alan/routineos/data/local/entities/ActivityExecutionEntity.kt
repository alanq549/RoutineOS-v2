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
        ),
        ForeignKey(
            entity = DailyInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["dailyInstanceId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [
        Index("nodeId"),
        Index("dailyInstanceId")
    ]
)
data class ActivityExecutionEntity(
    @PrimaryKey
    val id: String,
    val nodeId: String,
    val dailyInstanceId: String? = null,
    val scheduledDate: Long, // Epoch Day (preserved for V6 compatibility)
    val completedAt: Long,
    val metadataJson: String = "{}"
)
