package com.alan.routineos.domain.model

data class ActivityExecution(
    val id: String,
    val nodeId: String,
    val completedAt: Long,
    val metadataJson: String = "{}",
)
