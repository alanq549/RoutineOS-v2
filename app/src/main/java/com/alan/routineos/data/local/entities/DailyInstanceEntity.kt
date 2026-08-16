package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_instances",
    indices = [
        Index(
            value = ["targetType", "targetId", "scheduledDate"],
            unique = true
        )
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
    val sourceRuleId: String? = null
)
