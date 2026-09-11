package com.alan.routineos.feature.today.model

import com.alan.routineos.domain.model.DailyInstanceStatus

data class AssociatedTaskUiModel(
    val id: String,
    val title: String,
    val status: DailyInstanceStatus,
    val isCompleted: Boolean,
    val timeText: String = ""
)

data class AssociatedReminderUiModel(
    val formattedTime: String,
    val isRelative: Boolean,
    val offsetMinutes: Int? = null
)

data class AssociatedNoteUiModel(
    val id: String,
    val content: String,
    val dateLabel: String = "",
    val titleSnapshot: String? = null
)

data class ContextItemsUiModel(
    val tasks: List<AssociatedTaskUiModel> = emptyList(),
    val reminder: AssociatedReminderUiModel? = null,
    val note: AssociatedNoteUiModel? = null
)
