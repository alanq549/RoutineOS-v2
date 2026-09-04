package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.MonthlyStats
import com.alan.routineos.domain.model.WeeklyStats
import java.time.LocalDate
import java.util.*

@Composable
fun MonthlyCycleView(
    monthStats: MonthlyStats,
    modifier: Modifier = Modifier
) {
    var selectedWeekStart by remember { mutableStateOf(monthStats.weeklyStats.lastOrNull()?.startOfWeek) }
    val selectedWeek = monthStats.weeklyStats.find { it.startOfWeek == selectedWeekStart } ?: monthStats.weeklyStats.lastOrNull()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
    ) {
        // Range & Summary
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = monthStats.yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()).uppercase(),
                style = RoutineTheme.typography.headlineMedium, 
                color = RoutineTheme.colors.onSurface
            )
        }

        // Main Cycle Chart (Concentric Rings)
        RoutineCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                    CycleRingsCanvas(weeks = monthStats.weeklyStats, selectedWeekStart = selectedWeekStart)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthStats.completionRate?.let { "${(it * 100).toInt()}%" } ?: "0%", 
                            style = RoutineTheme.typography.displayLarge.copy(fontSize = 56.sp), 
                            color = RoutineTheme.colors.primary
                        )
                        Text("ADHERENCIA", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                selectedWeek?.let {
                    Text(
                        text = "Semana del ${it.startOfWeek.dayOfMonth} seleccionada", 
                        style = RoutineTheme.typography.labelCaps, 
                        color = RoutineTheme.colors.primary, 
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Weekly Nav List
        WeeklyBreakdownList(
            weeks = monthStats.weeklyStats,
            selectedWeekStart = selectedWeekStart,
            onWeekSelected = { selectedWeekStart = it }
        )
    }
}

@Composable
private fun CycleRingsCanvas(weeks: List<WeeklyStats>, selectedWeekStart: LocalDate?) {
    val strokeWidthDp = 8.dp
    val spacingDp = 16.dp
    
    val primaryColor = RoutineTheme.colors.primary
    val trackColor = RoutineTheme.colors.surface2

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 2 - 20.dp.toPx()
        
        weeks.asReversed().forEachIndexed { index, week ->
            val radius = baseRadius - (spacingDp.toPx() * index)
            val sweepAngle = (week.completionRate ?: 0f) * 360f
            val isSelected = week.startOfWeek == selectedWeekStart
            val alpha = if (index == 0) 1f else 0.4f + (0.15f * (3 - index))

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthDp.toPx())
            )
            // Progress
            drawArc(
                color = if (isSelected) primaryColor else primaryColor.copy(alpha = alpha.coerceIn(0.2f, 0.8f)),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = if (isSelected) strokeWidthDp.toPx() * 1.5f else strokeWidthDp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun WeeklyBreakdownList(
    weeks: List<WeeklyStats>,
    selectedWeekStart: LocalDate?,
    onWeekSelected: (LocalDate) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weeks.forEach { week ->
            val isSelected = week.startOfWeek == selectedWeekStart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoutineTheme.shapes.small)
                    .background(if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.1f) else RoutineTheme.colors.surface2)
                    .border(1.dp, if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.3f) else Color.Transparent, RoutineTheme.shapes.small)
                    .clickable { onWeekSelected(week.startOfWeek) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Semana del ${week.startOfWeek.dayOfMonth}", 
                    style = RoutineTheme.typography.bodyBase, 
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, 
                    color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurface
                )
                Text(
                    text = week.completionRate?.let { "${(it * 100).toInt()}%" } ?: "0%", 
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp), 
                    color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}
