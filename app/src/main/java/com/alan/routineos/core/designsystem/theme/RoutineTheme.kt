package com.alan.routineos.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.alan.routineos.core.designsystem.color.DarkRoutineColorScheme
import com.alan.routineos.core.designsystem.color.LocalRoutineColors
import com.alan.routineos.core.designsystem.color.RoutineColorScheme
import com.alan.routineos.core.designsystem.dimension.LocalRoutineDimensions
import com.alan.routineos.core.designsystem.dimension.RoutineDimensions
import com.alan.routineos.core.designsystem.shape.LocalRoutineShapes
import com.alan.routineos.core.designsystem.shape.RoutineShapes
import com.alan.routineos.core.designsystem.spacing.LocalRoutineSpacing
import com.alan.routineos.core.designsystem.spacing.RoutineSpacing
import com.alan.routineos.core.designsystem.typography.LocalRoutineTypography
import com.alan.routineos.core.designsystem.typography.RoutineTypography

object RoutineTheme {
    val colors: RoutineColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalRoutineColors.current

    val typography: RoutineTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalRoutineTypography.current

    val spacing: RoutineSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalRoutineSpacing.current

    val shapes: RoutineShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalRoutineShapes.current

    val dimensions: RoutineDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalRoutineDimensions.current
}

@Composable
fun RoutineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Technical Premium Narrative is primarily a Dark Mode design.
    // We default to DarkRoutineColorScheme.
    val colors = if (darkTheme) DarkRoutineColorScheme else DarkRoutineColorScheme

    val materialColorScheme = darkColorScheme(
        primary = colors.primary,
        secondary = colors.secondary,
        tertiary = colors.tertiary,
        background = colors.background,
        surface = colors.surface1,
        error = colors.error,
        onPrimary = colors.onPrimary,
        onSurface = colors.onSurface,
        onSurfaceVariant = colors.onSurfaceVariant,
        outline = colors.border
    )

    CompositionLocalProvider(
        LocalRoutineColors provides colors,
        LocalRoutineSpacing provides RoutineSpacing(),
        LocalRoutineTypography provides RoutineTypography(),
        LocalRoutineShapes provides RoutineShapes(),
        LocalRoutineDimensions provides RoutineDimensions()
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
