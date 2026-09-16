package com.alan.routineos.feature.system

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.components.SystemBentoGrid
import com.alan.routineos.feature.system.components.SystemStatusLegend
import com.alan.routineos.feature.system.components.SystemSummaryHero
import com.alan.routineos.feature.system.components.LifeSystemEditorSheet

@Composable
fun SystemScreen(
    uiState: SystemUiState,
    onAddClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onUpdateFields: (String, String, String) -> Unit,
    onSave: () -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = RoutineTheme.spacing.marginMobile)
                .padding(bottom = RoutineTheme.spacing.xl)
        ) {
            SystemSummaryHero(
                summary = uiState.summary,
                onAddClick = onAddClick,
                modifier = Modifier.padding(top = RoutineTheme.spacing.sm)
            )

            Spacer(modifier = Modifier.height(RoutineTheme.spacing.xl))

            SystemBentoGrid(
                lifeAreas = uiState.lifeAreas,
                onCardClick = onCardClick
            )

            Spacer(modifier = Modifier.height(40.dp))

            SystemStatusLegend()
        }

        if (uiState.editingSystem != null) {
            LifeSystemEditorSheet(
                system = uiState.editingSystem,
                isCreationMode = uiState.isCreatingNewSystem,
                onUpdateFields = onUpdateFields,
                onSave = onSave,
                onDelete = onDelete,
                onDismiss = onDismiss
            )
        }
    }
}
