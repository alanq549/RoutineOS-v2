package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

/**
 * RoutineBottomBar is a presentation-only component.
 */
@Composable
fun RoutineBottomBar(
    modifier: Modifier = Modifier,
    containerColor: Color = RoutineTheme.colors.background.copy(alpha = 0.85f),
    borderColor: Color = RoutineTheme.colors.border,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RoutineTheme.dimensions.bottomBarHeight)
            .background(containerColor)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
