package com.alan.routineos.core.designsystem.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoutineTheme.shapes.small,
    color: Color = RoutineTheme.colors.background,
    contentColor: Color = RoutineTheme.colors.onSurface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        content = content
    )
}
