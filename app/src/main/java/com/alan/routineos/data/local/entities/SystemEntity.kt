package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "life_systems")
data class SystemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false
)
