package com.alan.routineos.feature.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.components.PlanningTimeBlock
import com.alan.routineos.feature.planning.components.PlanningWeekHeader

@Composable
fun PlanningScreen(
    uiState: PlanningUiState,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSegment by remember { mutableStateOf(0) } // 0: Planificador, 1: Rutinas

    RoutineScaffold(
        modifier = modifier,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                RoutineTopBar(
                    title = {
                        Text(
                            text = "Planificar",
                            style = RoutineTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoutineTheme.colors.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = RoutineTheme.colors.onSurface)
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = RoutineTheme.colors.onSurface)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = RoutineTheme.spacing.lg)
        ) {
            // Segmented Control
            SegmentedControl(
                selectedIndex = selectedSegment,
                onSegmentSelected = { selectedSegment = it },
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Week Selector
            PlanningWeekHeader(
                days = uiState.weekDays,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Activity Blocks
            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                Text(
                    text = "BLOQUES DE RUTINA",
                    style = RoutineTheme.typography.labelCaps.copy(letterSpacing = 2.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
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
        
        // FAB
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(280.dp)
            .height(40.dp)
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.medium)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SegmentButton(
            text = "Planificador",
            isSelected = selectedIndex == 0,
            onClick = { onSegmentSelected(0) },
            modifier = Modifier.weight(1f)
        )
        SegmentButton(
            text = "Rutinas",
            isSelected = selectedIndex == 1,
            onClick = { onSegmentSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) RoutineTheme.colors.surface3 else Color.Transparent,
        shape = RoutineTheme.shapes.small,
        modifier = modifier.fillMaxHeight()
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = RoutineTheme.typography.labelCaps,
                color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
            )
        }
    }
}
