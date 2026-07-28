package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.components.ActivityCard

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RoutineTheme.spacing.md)
                .padding(bottom = RoutineTheme.spacing.xl)
        ) {
            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            // My Activities Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mis actividades",
                    style = RoutineTheme.typography.headlineMedium,
                    color = RoutineTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${uiState.myActivities.size} activas",
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                    color = RoutineTheme.colors.primary,
                    modifier = Modifier
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            if (uiState.myActivities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay actividades registradas",
                        style = RoutineTheme.typography.bodyBase,
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            } else {
                uiState.myActivities.forEach { activity ->
                    ActivityCard(activity = activity)
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = { },
            containerColor = RoutineTheme.colors.primary,
            contentColor = RoutineTheme.colors.onPrimary,
            shape = RoutineTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(RoutineTheme.spacing.lg)
                .size(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add activity")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    RoutineTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                myActivities = emptyList()
            )
        )
    }
}
