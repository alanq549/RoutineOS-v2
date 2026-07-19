package com.alan.routineos.core.designsystem.color

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class RoutineColorScheme(
    val background: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val error: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color
)

val DarkRoutineColorScheme = RoutineColorScheme(
    background = Color(0xFF0A0D12),
    surface1 = Color(0xFF12161D),
    surface2 = Color(0xFF171C24),
    surface3 = Color(0x8C12161D),
    border = Color(0xFF232A34),
    primary = Color(0xFF5AF0B3),
    secondary = Color(0xFFA4C8FF),
    tertiary = Color(0xFFDBD1FF),
    error = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF0A0D12),
    onSurface = Color(0xFFDEE3EB),
    onSurfaceVariant = Color(0xFFBBCAC0)
)

val LocalRoutineColors = staticCompositionLocalOf {
    DarkRoutineColorScheme
}
