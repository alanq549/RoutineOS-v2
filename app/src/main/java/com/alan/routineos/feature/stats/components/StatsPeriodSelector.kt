package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.model.StatsPeriod

@Composable
fun StatsPeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.pill)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.pill)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatsPeriodButton(
            title = "Semana",
            isSelected = selectedPeriod == StatsPeriod.WEEK,
            onClick = { onPeriodSelected(StatsPeriod.WEEK) },
            modifier = Modifier.weight(1f)
        )
        StatsPeriodButton(
            title = "Mes",
            isSelected = selectedPeriod == StatsPeriod.MONTH,
            onClick = { onPeriodSelected(StatsPeriod.MONTH) },
            modifier = Modifier.weight(1f)
        )
        StatsPeriodButton(
            title = "Año",
            isSelected = selectedPeriod == StatsPeriod.YEAR,
            onClick = { onPeriodSelected(StatsPeriod.YEAR) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatsPeriodButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoutineTheme.shapes.pill)
            .background(if (isSelected) RoutineTheme.colors.surface3 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = RoutineTheme.typography.labelCaps,
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
