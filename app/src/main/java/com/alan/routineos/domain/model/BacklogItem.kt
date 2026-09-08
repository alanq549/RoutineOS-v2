package com.alan.routineos.domain.model

enum class BacklogItemStatus {
    OPEN, RESOLVED, ARCHIVED
}

data class BacklogItem(
    val id: String,
    val definitionId: String?,
    val title: String,
    val status: BacklogItemStatus
)
