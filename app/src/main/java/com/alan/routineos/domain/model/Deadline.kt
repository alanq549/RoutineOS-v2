package com.alan.routineos.domain.model

data class Deadline(
    val id: String,
    val dueAt: Long,
    val definitionId: String? = null,
    val backlogId: String? = null,
    val instanceId: String? = null
)
