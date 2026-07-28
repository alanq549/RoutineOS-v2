package com.alan.routineos.feature.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.core.navigation.AppRoutes
import com.alan.routineos.feature.dashboard.DashboardRoute
import com.alan.routineos.feature.system.SystemRoute

@Composable
fun PlanningWorkspace(
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.Planner.route

    RoutineScaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Apply insets here to the header
            ) {
                // Main Header
                Column(
                    modifier = Modifier
                        .padding(horizontal = RoutineTheme.spacing.marginMobile)
                        .padding(top = RoutineTheme.spacing.lg)
                ) {
                    Text(
                        text = "Planificar",
                        style = RoutineTheme.typography.displayLarge,
                        color = RoutineTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp)) // Tightened
                    
                    PlanningSegmentedSelector(
                        currentRoute = currentRoute,
                        onNavigateToMode = { route ->
                            navController.navigate(route) {
                                popUpTo(AppRoutes.Planner.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Tightened gap to content
            }
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        // NavHost will start exactly below the TopBar Column
        NavHost(
            navController = navController,
            startDestination = AppRoutes.Planner.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(AppRoutes.Planner.route) {
                PlanningRoute()
            }
            composable(AppRoutes.Activities.route) {
                DashboardRoute()
            }
            composable(AppRoutes.Systems.route) {
                SystemRoute()
            }
        }
    }
}

@Composable
private fun PlanningSegmentedSelector(
    currentRoute: String?,
    onNavigateToMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.medium)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
            .padding(4.dp)
    ) {
        SegmentModeButton(
            title = "PLANIFICADOR",
            isSelected = currentRoute == AppRoutes.Planner.route,
            onClick = { onNavigateToMode(AppRoutes.Planner.route) },
            modifier = Modifier.weight(1f)
        )
        SegmentModeButton(
            title = "ACTIVIDADES",
            isSelected = currentRoute == AppRoutes.Activities.route,
            onClick = { onNavigateToMode(AppRoutes.Activities.route) },
            modifier = Modifier.weight(1f)
        )
        SegmentModeButton(
            title = "SISTEMAS",
            isSelected = currentRoute == AppRoutes.Systems.route,
            onClick = { onNavigateToMode(AppRoutes.Systems.route) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentModeButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoutineTheme.shapes.small)
            .background(if (isSelected) RoutineTheme.colors.surface3 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = RoutineTheme.typography.labelCaps,
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
