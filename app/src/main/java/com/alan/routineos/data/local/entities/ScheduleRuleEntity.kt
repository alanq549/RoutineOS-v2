package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_rules",
    indices = [
        Index(
            value = ["targetId", "targetType", "daysOfWeek", "startTime"],
            unique = true
        )
    ]
)
data class ScheduleRuleEntity(
    @PrimaryKey
    val id: String,
    val targetId: String,
    val targetType: String, // DEFINITION, NODE
    val type: String, // FIXED_DAYS, FLEXIBLE_FREQUENCY
    val daysOfWeek: String = "", // Comma-separated ints: "1,3,5"
    val frequencyPerPeriod: Int = 0,
    val startTime: Int? = null, // Minutes from midnight
    val endTime: Int? = null,
    val durationMinutes: Int? = null,
    val mobility: String = "FLEXIBLE", // IMMOBILE, FLEXIBLE
    val metadataJson: String = "{}"
)
