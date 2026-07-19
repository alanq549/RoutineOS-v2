package com.alan.routineos.core.designsystem.typography

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Technical Premium Narrative Typography.
 * 
 * Note: Inter and JetBrains Mono are currently unavailable in res/font.
 * Falling back to SansSerif and Monospace respectively.
 */
@Immutable
data class RoutineTypography(
    val displayLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp
    ),
    val headlineMedium: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp
    ),
    val bodyBase: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    val dataLarge: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).sp
    ),
    val labelCaps: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

val LocalRoutineTypography = staticCompositionLocalOf {
    RoutineTypography()
}
