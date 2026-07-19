package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoutineTheme.shapes.medium,
    containerColor: Color = RoutineTheme.colors.surface1,
    contentColor: Color = RoutineTheme.colors.onSurface,
    border: BorderStroke? = BorderStroke(1.dp, RoutineTheme.colors.border),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        content = content
    )
}
