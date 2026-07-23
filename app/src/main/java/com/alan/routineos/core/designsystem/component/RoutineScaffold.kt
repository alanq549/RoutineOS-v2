package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    RoutineSurface(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            containerColor = RoutineTheme.colors.background,
            contentColor = RoutineTheme.colors.onSurface,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            // Propagate paddingValues to content
            content(paddingValues)
        }
    }
}
