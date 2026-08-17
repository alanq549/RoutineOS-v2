package com.alan.routineos.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class MetadataFieldType {
    NUMBER,
    TEXT,
    BOOLEAN,
    SELECT
}

@Serializable
data class MetadataField(
    val id: String,
    val name: String,
    val type: MetadataFieldType,
    val required: Boolean = false,
    val isReadOnly: Boolean = false,
    val options: List<String>? = null,
    val defaultValue: String? = null,
    val unit: String? = null
)

data class MetadataSchema(
    val id: String,
    val target: ScheduleTarget,
    val fields: List<MetadataField> = emptyList(),
    val schemaVersion: Int = 1
)
