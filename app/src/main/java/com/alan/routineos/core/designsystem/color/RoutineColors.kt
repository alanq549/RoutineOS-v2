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
    val onSurfaceVariant: Color,
    val roleEvent: Color = Color(0xFF34D399), // Emerald
    val roleTask: Color = Color(0xFF818CF8), // Indigo
    val roleReminder: Color = Color(0xFFFBBF24) // Amber
)

val DarkRoutineColorScheme = RoutineColorScheme(
    background = Color(0xFF0B0F14), // Stitch Base
    surface1 = Color(0xFF111721), // Stitch Surface
    surface2 = Color(0xFF161F2C), // Stitch Card
    surface3 = Color(0x8C111721),
    border = Color(0xFF1E293B), // Stitch Border
    primary = Color(0xFF34D399), // Stitch Emerald
    secondary = Color(0xFF64748B), // Neutral Slate
    tertiary = Color(0xFFDBD1FF),
    error = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF003825),
    onSurface = Color(0xFFE9EDF2),
    onSurfaceVariant = Color(0xFF94A3B8), // Stitch Subtle Slate
    roleEvent = Color(0xFF34D399),
    roleTask = Color(0xFF818CF8),
    roleReminder = Color(0xFFFBBF24)
)

val LocalRoutineColors = staticCompositionLocalOf {
    DarkRoutineColorScheme
}
