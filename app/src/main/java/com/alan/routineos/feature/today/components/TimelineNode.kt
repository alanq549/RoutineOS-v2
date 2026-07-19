package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TimelineItemStatus

@Composable
fun TimelineNode(
    status: TimelineItemStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        TimelineItemStatus.COMPLETED -> RoutineTheme.colors.primary
        TimelineItemStatus.ACTIVE -> RoutineTheme.colors.primary
        TimelineItemStatus.PENDING -> RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
        TimelineItemStatus.SKIPPED -> RoutineTheme.colors.surface2
    }

    Box(
        modifier = modifier
            .size(12.dp)
            .background(color, CircleShape)
            .border(2.dp, RoutineTheme.colors.background, CircleShape)
    )
}
