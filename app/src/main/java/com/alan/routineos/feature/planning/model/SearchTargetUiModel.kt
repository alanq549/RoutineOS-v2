package com.alan.routineos.feature.planning.model

import com.alan.routineos.domain.model.ScheduleTarget

data class SearchTargetUiModel(
    val id: String,
    val title: String,
    val subtitle: String?, // Parent definition title if it's a sub-step/node
    val target: ScheduleTarget
)
