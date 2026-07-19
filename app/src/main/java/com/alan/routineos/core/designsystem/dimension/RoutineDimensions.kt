package com.alan.routineos.core.designsystem.dimension

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RoutineDimensions(
    val iconSmall: Dp = 20.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val fabSize: Dp = 56.dp,
    val toolbarHeight: Dp = 64.dp,
    val bottomBarHeight: Dp = 80.dp,
    val cardElevation: Dp = 0.dp // Elevation is tonal/border based in this design
)

val LocalRoutineDimensions = staticCompositionLocalOf {
    RoutineDimensions()
}
