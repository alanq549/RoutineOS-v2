package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_rules",
    foreignKeys = [
        ForeignKey(
            entity = ActivityNodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("nodeId")]
)
data class ScheduleRuleEntity(
    @PrimaryKey
    val id: String,
    val nodeId: String,
    val type: String, // FIXED_DAYS, FLEXIBLE_FREQUENCY
    val daysOfWeek: String = "", // Comma-separated ints: "1,3,5"
    val frequencyPerPeriod: Int = 0
)
