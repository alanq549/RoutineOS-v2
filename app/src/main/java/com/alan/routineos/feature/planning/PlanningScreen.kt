package com.alan.routineos.feature.planning

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.components.PlanningTimeBlock
import com.alan.routineos.feature.planning.components.PlanningWeekHeader

@Composable
fun PlanningScreen(
    uiState: PlanningUiState,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = RoutineTheme.spacing.lg)
        ) {
            // Week Selector
            PlanningWeekHeader(
                days = uiState.weekDays,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Activity Blocks
            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                RoutineSectionHeader(title = "CRONOGRAMA")
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                
                if (uiState.timelineEntries.isEmpty()) {
                    Text(
                        text = "Sin actividades para este día",
                        style = RoutineTheme.typography.bodyBase,
                        color = RoutineTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    uiState.timelineEntries.forEach { entry ->
                        PlanningTimeBlock(item = entry)
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
                    }
                }
            }
        }
        
        // FAB - Active for new Ad-hoc planning if needed in future
        FloatingActionButton(
            onClick = { /* Future: Quick Add from Planning */ },
            containerColor = RoutineTheme.colors.primary,
            contentColor = RoutineTheme.colors.onPrimary,
            shape = RoutineTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(RoutineTheme.spacing.lg)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Add"
            )
        }
    }
}
