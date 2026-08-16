package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_rules",
    foreignKeys = [
        ForeignKey(
            entity = ActivityDefinitionEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityDefinitionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActivityNodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityNodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("activityDefinitionId"),
        Index("activityNodeId")
    ]
)
data class ScheduleRuleEntity(
    @PrimaryKey
    val id: String,
    val activityDefinitionId: String? = null,
    val activityNodeId: String? = null,
    val type: String, // FIXED_DAYS, FLEXIBLE_FREQUENCY
    val daysOfWeek: String = "", // Comma-separated ints: "1,3,5"
    val frequencyPerPeriod: Int = 0,
    val startTime: Int? = null, // Minutes from midnight
    val endTime: Int? = null,
    val durationMinutes: Int? = null,
    val metadataJson: String = "{}"
)
