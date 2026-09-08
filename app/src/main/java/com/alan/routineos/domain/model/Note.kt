package com.alan.routineos.domain.model

data class Note(
    val id: String,
    val content: String,
    val definitionId: String? = null,
    val backlogId: String? = null,
    val instanceId: String? = null
)
