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
import androidx.compose.ui.unit.sp
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
                .padding(bottom = 80.dp) // Space for bottom nav
        ) {
            // Week Selector (Stitch V3 style)
            PlanningWeekHeader(
                days = uiState.weekDays,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 1. ROUTINE BLOCKS
            PlanningSection(title = "BLOQUES DE RUTINA") {
                if (uiState.timelineEntries.isEmpty()) {
                    EmptySectionMessage("Sin actividades programadas")
                } else {
                    uiState.timelineEntries.forEach { entry ->
                        PlanningTimeBlock(item = entry)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. EXCEPTIONS
            if (uiState.exceptions.isNotEmpty()) {
                PlanningSection(title = "EXCEPCIONES") {
                    uiState.exceptions.forEach { entry ->
                        PlanningExceptionCard(item = entry)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. UNSCHEDULED (PENDIENTES FLEXIBLES)
            PlanningSection(
                title = "SIN HORARIO",
                subtitle = "PENDIENTES FLEXIBLES"
            ) {
                if (uiState.unscheduledItems.isEmpty()) {
                    EmptySectionMessage("No hay pendientes flexibles")
                } else {
                    // 2-column grid for unscheduled
                    uiState.unscheduledItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PlanningUnscheduledCard(
                                        title = item.title,
                                        description = item.description
                                    )
                                }
                            }
                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { /* TODO: Quick Add from Planning */ },
            containerColor = RoutineTheme.colors.primary,
            contentColor = RoutineTheme.colors.onPrimary,
            shape = RoutineTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(RoutineTheme.spacing.lg)
                .padding(bottom = 16.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick Add"
            )
        }
    }
}

@Composable
private fun PlanningSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = RoutineTheme.typography.labelCaps.copy(letterSpacing = 2.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun EmptySectionMessage(message: String) {
    Text(
        text = message,
        style = RoutineTheme.typography.bodyBase,
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
