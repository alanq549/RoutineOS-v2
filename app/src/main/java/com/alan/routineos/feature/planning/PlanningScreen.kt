package com.alan.routineos.feature.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.components.PlanningExceptionCard
import com.alan.routineos.feature.planning.components.PlanningTimeBlock
import com.alan.routineos.feature.planning.components.PlanningUnscheduledCard
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
                days = uiState.days,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Routine Blocks
            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                RoutineSectionHeader(title = "BLOQUES DE RUTINA")
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                uiState.blocks.forEach { block ->
                    PlanningTimeBlock(block = block)
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exceptions
            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                RoutineSectionHeader(title = "EXCEPCIONES")
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                uiState.exceptions.forEach { exception ->
                    PlanningExceptionCard(exception = exception)
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Unscheduled
            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    RoutineSectionHeader(title = "SIN HORARIO", modifier = Modifier.weight(1f))
                    Text(
                        text = "PENDIENTES FLEXIBLES",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = RoutineTheme.spacing.md)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)) {
                    uiState.unscheduledItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            PlanningUnscheduledCard(item = item)
                        }
                    }
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
