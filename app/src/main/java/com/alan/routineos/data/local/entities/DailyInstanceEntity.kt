package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_instances",
    foreignKeys = [
        ForeignKey(
            entity = DailyInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentInstanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BacklogItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["backlogId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ScheduleRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceRuleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(
            value = ["sourceRuleId", "scheduledDate"],
            unique = true
        ),
        Index("parentInstanceId"),
        Index("backlogId"),
        Index("sourceRuleId")
    ]
)
data class DailyInstanceEntity(
    @PrimaryKey
    val id: String,
    val targetId: String?,
    val targetType: String, // DEFINITION, NODE, AD_HOC
    val scheduledDate: Long, // Epoch Day
    val titleSnapshot: String,
    val descriptionSnapshot: String,
    val plannedStartTime: Int? = null, // Minutes from midnight
    val plannedEndTime: Int? = null,
    val plannedDurationMinutes: Int? = null,
    val status: String, // PLANNED, MODIFIED, OMITTED
    val mobility: String = "FLEXIBLE", // IMMOBILE, FLEXIBLE
    val sourceRuleId: String? = null,
    val parentInstanceId: String? = null,
    val backlogId: String? = null
)
