package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.components.ActivityCard
import com.alan.routineos.feature.dashboard.components.LifeSystemEditorSheet

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onAddActivity: () -> Unit,
    onActivityClick: (String) -> Unit,
    onSystemSelected: (String?) -> Unit,
    onAddSystem: () -> Unit,
    onEditSystem: (String) -> Unit,
    onUpdateSystemFields: (String, String, String) -> Unit,
    onSaveSystem: () -> Unit,
    onDeleteSystem: (String) -> Unit,
    onDismissSystemEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = RoutineTheme.spacing.xl)
        ) {
            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            // My Activities Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RoutineTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Actividades",
                        style = RoutineTheme.typography.headlineMedium,
                        color = RoutineTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${uiState.myActivities.size}",
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                        color = RoutineTheme.colors.primary,
                        modifier = Modifier
                            .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                TextButton(onClick = onAddSystem) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(" SISTEMA", style = RoutineTheme.typography.labelCaps)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SYSTEM FILTER ROW
            LazyRow(
                contentPadding = PaddingValues(horizontal = RoutineTheme.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SystemFilterChip(
                        name = "Todas",
                        count = uiState.totalActivitiesCount,
                        icon = Icons.Default.AccountTree,
                        color = RoutineTheme.colors.onSurfaceVariant,
                        isSelected = uiState.selectedSystemId == null,
                        onClick = { onSystemSelected(null) }
                    )
                }
                items(uiState.allSystems) { system ->
                    SystemFilterChip(
                        name = system.title,
                        count = uiState.systemCounts[system.id] ?: 0,
                        icon = getTechnicalIcon(system.iconKey),
                        color = Color(android.graphics.Color.parseColor(system.colorHex)),
                        isSelected = uiState.selectedSystemId == system.id,
                        onClick = { onSystemSelected(system.id) },
                        onLongClick = { onEditSystem(system.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
                if (uiState.myActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.selectedSystemId == null) "No hay actividades registradas" else "Sin actividades en este sistema",
                            style = RoutineTheme.typography.bodyBase,
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                    }
                } else {
                    uiState.myActivities.forEach { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onActivityClick(activity.id) }
                        )
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))
                    }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = onAddActivity,
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

        if (uiState.editingSystem != null) {
            LifeSystemEditorSheet(
                system = uiState.editingSystem,
                isCreationMode = uiState.isCreatingNewSystem,
                onUpdateFields = onUpdateSystemFields,
                onSave = onSaveSystem,
                onDelete = onDeleteSystem,
                onDismiss = onDismissSystemEditor
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SystemFilterChip(
    name: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) color.copy(alpha = 0.12f) else RoutineTheme.colors.surface2
    val borderColor = if (isSelected) color.copy(alpha = 0.8f) else RoutineTheme.colors.border.copy(alpha = 0.4f)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(10.dp), // Slightly more technical radius
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .height(44.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Technical Count Badge
            Surface(
                color = if (isSelected) color.copy(alpha = 0.2f) else RoutineTheme.colors.background.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(width = 24.dp, height = 18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = count.toString(),
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (isSelected) color else RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                color = if (isSelected) Color.White else RoutineTheme.colors.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

private fun getTechnicalIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "fitness_center" -> Icons.Default.FitnessCenter
        "school" -> Icons.Default.School
        "work" -> Icons.Default.Work
        "favorite" -> Icons.Default.Favorite
        "bedtime" -> Icons.Default.Bedtime
        "rocket" -> Icons.Default.RocketLaunch
        "account_tree" -> Icons.Default.AccountTree
        "science" -> Icons.Default.Science
        "psychology" -> Icons.Default.Psychology
        "palette" -> Icons.Default.Palette
        else -> Icons.Default.AccountTree
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    RoutineTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                myActivities = emptyList()
            ),
            onAddActivity = {},
            onActivityClick = {},
            onSystemSelected = {},
            onAddSystem = {},
            onEditSystem = {},
            onUpdateSystemFields = { _, _, _ -> },
            onSaveSystem = {},
            onDeleteSystem = {},
            onDismissSystemEditor = {}
        )
    }
}
