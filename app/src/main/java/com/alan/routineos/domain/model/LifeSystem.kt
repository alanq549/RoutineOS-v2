package com.alan.routineos.domain.model

data class LifeSystem(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val colorHex: String,
    val isArchived: Boolean = false
)
