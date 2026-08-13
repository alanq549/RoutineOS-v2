package com.alan.routineos.domain.model

data class ActivityNode(
    val id: String,
    val activityDefinitionId: String,
    val parentId: String?,
    val position: Int,
    val title: String,
    val description: String = "",
    val isDeleted: Boolean = false
)
