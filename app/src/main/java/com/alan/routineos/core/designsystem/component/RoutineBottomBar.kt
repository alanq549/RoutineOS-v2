package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alan.routineos.core.designsystem.theme.RoutineTheme

/**
 * RoutineBottomBar is a presentation-only component.
 */
@Composable
fun RoutineBottomBar(
    modifier: Modifier = Modifier,
    containerColor: Color = RoutineTheme.colors.background.copy(alpha = 0.85f),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(RoutineTheme.dimensions.bottomBarHeight)
            .background(containerColor),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
