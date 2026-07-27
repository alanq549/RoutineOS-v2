package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_definitions")
data class ActivityDefinitionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String
)
