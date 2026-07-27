package com.alan.routineos.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.alan.routineos.feature.account.AccountRoute
import com.alan.routineos.feature.planning.PlanningWorkspace
import com.alan.routineos.feature.stats.StatsRoute
import com.alan.routineos.feature.today.TodayRoute

@Composable
fun AppNavHost(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Today.route,
        modifier = modifier
    ) {
        composable(AppRoutes.Today.route) {
            TodayRoute(bottomBar = bottomBar)
        }

        composable(AppRoutes.Planning.route) {
            PlanningWorkspace(bottomBar = bottomBar)
        }

        composable(AppRoutes.Stats.route) {
            StatsRoute(bottomBar = bottomBar)
        }
        composable(AppRoutes.Account.route) {
            AccountRoute(bottomBar = bottomBar)
        }
    }
}
