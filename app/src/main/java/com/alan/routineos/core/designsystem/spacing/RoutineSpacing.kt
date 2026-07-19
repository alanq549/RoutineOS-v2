package com.alan.routineos.core.designsystem.spacing

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RoutineSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val marginMobile: Dp = 16.dp,
    val marginDesktop: Dp = 24.dp,
    val indentation: Dp = 20.dp
)

val LocalRoutineSpacing = staticCompositionLocalOf {
    RoutineSpacing()
}
