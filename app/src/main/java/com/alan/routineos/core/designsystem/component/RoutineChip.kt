package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineChip(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color = RoutineTheme.colors.surface2,
    contentColor: Color = RoutineTheme.colors.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .background(containerColor, RoutineTheme.shapes.pill)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.pill)
            .padding(horizontal = RoutineTheme.spacing.sm, vertical = RoutineTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps,
            color = contentColor
        )
    }
}
