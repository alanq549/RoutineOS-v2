package com.alan.routineos.feature.system.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.model.LifeArea

@Composable
fun SystemBentoGrid(
    lifeAreas: List<LifeArea>,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
    ) {
        lifeAreas.forEach { area ->
            SystemCard(
                area = area,
                onClick = { onCardClick(area.id) }
            )
        }
    }
}
