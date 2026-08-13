package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_exceptions",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleRuleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleRuleId")]
)
data class ScheduleExceptionEntity(
    @PrimaryKey
    val id: String,
    val scheduleRuleId: String,
    val originalDate: Long, // Epoch Day
    val type: String, // SKIPPED, RESCHEDULED
    val newDate: Long? = null // Epoch Day
)
