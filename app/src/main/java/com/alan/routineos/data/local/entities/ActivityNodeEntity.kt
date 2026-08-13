package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_nodes",
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
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("activityDefinitionId"),
        Index("parentId")
    ]
)
data class ActivityNodeEntity(
    @PrimaryKey
    val id: String,
    val activityDefinitionId: String,
    val parentId: String?,
    val position: Int,
    val title: String,
    val description: String = "",
    val isDeleted: Boolean = false
)
