package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alan.routineos.core.designsystem.theme.RoutineTheme

/**
 * RoutineTopBar is a presentation-only component.
 * It uses tonal layering and translucency to achieve a "glass" feel without runtime blur.
 */
@Composable
fun RoutineTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = RoutineTheme.colors.background.copy(alpha = 0.85f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RoutineTheme.dimensions.toolbarHeight)
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RoutineTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = RoutineTheme.spacing.sm)
            ) {
                title()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}
