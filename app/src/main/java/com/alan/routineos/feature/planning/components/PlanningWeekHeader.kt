package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.model.PlanningDay

@Composable
fun PlanningWeekHeader(
    days: List<PlanningDay>,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RoutineTheme.spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            DayItem(
                day = day,
                onClick = { onDaySelected(day.id) }
            )
        }
    }
}

@Composable
private fun DayItem(
    day: PlanningDay,
    onClick: () -> Unit
) {
    val containerColor = if (day.isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.surface2
    val contentColor = if (day.isSelected) RoutineTheme.colors.onPrimary else RoutineTheme.colors.onSurface
    
    val isWeekend = day.name.equals("Sáb", ignoreCase = true) || day.name.equals("Dom", ignoreCase = true)

    Column(
        modifier = Modifier
            .width(48.dp)
            .height(72.dp)
            .clip(RoutineTheme.shapes.medium)
            .background(containerColor)
            .border(
                width = 1.dp, 
                color = if (day.isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.border.copy(alpha = 0.3f),
                shape = RoutineTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.name.uppercase(),
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = if (day.isSelected) contentColor else if (isWeekend) RoutineTheme.colors.tertiary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = day.dayOfMonth,
            style = RoutineTheme.typography.displayLarge.copy(fontSize = 18.sp),
            color = contentColor,
            fontWeight = if (day.isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}
