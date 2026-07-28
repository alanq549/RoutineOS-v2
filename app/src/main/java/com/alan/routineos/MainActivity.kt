package com.alan.routineos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alan.routineos.core.designsystem.component.RoutineBottomBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.core.navigation.AppNavHost
import com.alan.routineos.core.navigation.AppRoutes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBar: @Composable () -> Unit = {
                    RoutineBottomBar {
                        BottomNavItem(
                            label = "Today",
                            icon = Icons.Default.CalendarToday,
                            isSelected = currentRoute == AppRoutes.Today.route,
                            onClick = { 
                                navController.navigate(AppRoutes.Today.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        BottomNavItem(
                            label = "Planning",
                            icon = Icons.Default.CalendarMonth,
                            isSelected = isPlanningRoute(currentRoute),
                            onClick = { 
                                navController.navigate(AppRoutes.Planning.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        BottomNavItem(
                            label = "Stats",
                            icon = Icons.Default.Insights,
                            isSelected = currentRoute == AppRoutes.Stats.route,
                            onClick = { 
                                navController.navigate(AppRoutes.Stats.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        BottomNavItem(
                            label = "Account",
                            icon = Icons.Default.Person,
                            isSelected = currentRoute == AppRoutes.Account.route,
                            onClick = { 
                                navController.navigate(AppRoutes.Account.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                AppNavHost(
                    navController = navController,
                    bottomBar = bottomBar
                )
            }
        }
    }
}

private fun isPlanningRoute(route: String?): Boolean {
    return route == AppRoutes.Planner.route || 
           route == AppRoutes.Activities.route || 
           route == AppRoutes.Systems.route ||
           route == AppRoutes.Planning.route
}

@Composable
private fun RowScope.BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
    val background = if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent
    
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoutineTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoutineTheme.shapes.small)
                .background(background)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color
            )
            Text(
                text = label,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                color = color
            )
        }
    }
}
