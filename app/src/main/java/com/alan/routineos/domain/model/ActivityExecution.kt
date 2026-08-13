package com.alan.routineos.domain.model

data class ActivityExecution(
    val id: String,
    val nodeId: String,
    val scheduledDate: Long, // Epoch Day
    val completedAt: Long,
    val metadataJson: String = "{}",
)
