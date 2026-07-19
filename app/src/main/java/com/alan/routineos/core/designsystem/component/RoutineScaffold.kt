package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
        androidx.compose.material3.Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            containerColor = RoutineTheme.colors.background,
            contentColor = RoutineTheme.colors.onSurface
        ) { paddingValues ->
            // Removed internal Box with padding to prevent double-padding bugs.
            // Screens are now responsible for using paddingValues.
            content(paddingValues)
        }
    }
}
