package com.alan.routineos.feature.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.routines.components.RoutineBentoCard
import com.alan.routineos.feature.routines.components.RoutineTemplateCard

@Composable
fun RoutineLibraryScreen(
    uiState: RoutineLibraryUiState,
    onCategorySelected: (String) -> Unit,
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
            // Categories
            LazyRow(
                modifier = Modifier.padding(top = RoutineTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories) { category ->
                    CategoryChip(category = category, onClick = { onCategorySelected(category.id) })
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // My Routines
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mis rutinas",
                    style = RoutineTheme.typography.headlineMedium,
                    color = RoutineTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "3 activas",
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                    color = RoutineTheme.colors.primary,
                    modifier = Modifier
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            uiState.myRoutines.forEach { routine ->
                RoutineBentoCard(routine = routine)
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recommended
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Plantillas recomendadas",
                    style = RoutineTheme.typography.headlineMedium,
                    color = RoutineTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "VER TODAS",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.primary,
                    modifier = Modifier.clickable { }
                )
            }

            Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

            uiState.recommendedTemplates.forEach { template ->
                RoutineTemplateCard(template = template)
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
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
            Icon(Icons.Default.Add, contentDescription = "Add routine")
        }
    }
}

@Composable
private fun CategoryChip(category: com.alan.routineos.feature.routines.model.RoutineCategory, onClick: () -> Unit) {
    val containerColor = if (category.isSelected) RoutineTheme.colors.primary else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (category.isSelected) RoutineTheme.colors.onPrimary else RoutineTheme.colors.onSurfaceVariant
    val borderColor = if (category.isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.border

    Box(
        modifier = Modifier
            .clip(RoutineTheme.shapes.pill)
            .background(containerColor)
            .border(1.dp, borderColor, RoutineTheme.shapes.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = category.name,
            style = RoutineTheme.typography.labelCaps,
            color = contentColor
        )
    }
}
