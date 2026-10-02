package com.alan.routineos.feature.planning

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun PlanningWorkspace(
    onAddActivity: () -> Unit,
    onActivityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    RoutineScaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        },
        bottomBar = bottomBar
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PlanningRoute(
                onNavigateToActivityCreation = onAddActivity,
                onNavigateToActivityDetail = onActivityClick
            )
        }
    }
}
