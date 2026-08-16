package com.alan.routineos.domain.model

enum class MetadataFieldType {
    NUMBER,
    TEXT,
    BOOLEAN,
    SELECT
}

data class MetadataField(
    val name: String,
    val type: MetadataFieldType,
    val options: List<String>? = null // Used for SELECT type
)

data class MetadataSchema(
    val id: String,
    val target: ScheduleTarget,
    val fields: List<MetadataField> = emptyList()
)
