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
import java.time.format.TextStyle
import java.util.*

@Composable
fun YearlyCycleView(
    year: String,
    months: List<MonthlyStats>,
    modifier: Modifier = Modifier
) {
    var selectedMonthYm by remember { mutableStateOf(months.lastOrNull()?.yearMonth) }
    val selectedMonth = months.find { it.yearMonth == selectedMonthYm } ?: months.lastOrNull()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
    ) {
        // Year Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(year, style = RoutineTheme.typography.headlineMedium, color = RoutineTheme.colors.onSurface)
        }

        // Yearly Grid (3x4)
        YearlyCycleGrid(
            months = months,
            selectedMonthYm = selectedMonthYm,
            onMonthSelected = { selectedMonthYm = it }
        )

        // Month Detail
        selectedMonth?.let { month ->
            MonthSummaryCard(month = month)
        }
    }
}

@Composable
private fun YearlyCycleGrid(
    months: List<MonthlyStats>,
    selectedMonthYm: java.time.YearMonth?,
    onMonthSelected: (java.time.YearMonth) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Chunk by 3 for a 3x4 grid on mobile
        months.chunked(3).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthCycleItem(
                        month = month,
                        isSelected = month.yearMonth == selectedMonthYm,
                        onClick = { onMonthSelected(month.yearMonth) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill row if incomplete
                repeat(3 - rowMonths.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MonthCycleItem(
    month: MonthlyStats,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = RoutineTheme.colors.primary
    val trackColor = RoutineTheme.colors.surface2
    val strokeWidth = 4.dp

    Column(
        modifier = modifier
            .clip(RoutineTheme.shapes.medium)
            .background(if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.1f) else RoutineTheme.colors.surface1)
            .border(1.dp, if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.3f) else Color.Transparent, RoutineTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2
                drawCircle(color = trackColor, radius = radius, style = Stroke(width = strokeWidth.toPx()))
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = (month.completionRate ?: 0f) * 360f,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = month.completionRate?.let { "${(it * 100).toInt()}%" } ?: "0%",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurface
            )
        }
        Text(
            text = month.yearMonth.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
            style = RoutineTheme.typography.labelCaps,
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun MonthSummaryCard(month: MonthlyStats) {
    RoutineCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = month.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }, 
                        style = RoutineTheme.typography.headlineMedium
                    )
                    val activeDays = month.weeklyStats.sumOf { w -> w.dailyStats.count { it.occurrences.isNotEmpty() } }
                    Text(
                        text = "$activeDays días con actividad", 
                        style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp), 
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
                Text(
                    text = "ADHERENCIA ${month.completionRate?.let { (it * 100).toInt() } ?: 0}%", 
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp), 
                    color = RoutineTheme.colors.primary
                )
            }
        }
    }
}
