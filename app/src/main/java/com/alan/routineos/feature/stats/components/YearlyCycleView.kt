package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.components.body.BodyLoadCard
import com.alan.routineos.feature.stats.model.*

@Composable
fun YearlyCycleView(
    data: YearlyCycleData,
    selectedMonthId: String?,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMonth = data.months.find { it.id == selectedMonthId } ?: data.months.first()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
    ) {
        // Year Selector
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
                Text(data.year, style = RoutineTheme.typography.headlineMedium, color = RoutineTheme.colors.onSurface)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
            }
        }

        // Yearly Grid
        YearlyCycleGrid(
            months = data.months,
            selectedMonthId = selectedMonthId,
            onMonthSelected = onMonthSelected
        )

        // Month Detail
        MonthDetailSection(month = selectedMonth)

        // Body Load
        selectedMonth.bodyLoad?.let { bodyLoad ->
            BodyLoadCard(state = bodyLoad)
        }
    }
}

@Composable
private fun YearlyCycleGrid(
    months: List<CycleMonth>,
    selectedMonthId: String?,
    onMonthSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        months.chunked(3).forEach { rowMonths ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowMonths.forEach { month ->
                    MonthCycleItem(
                        month = month,
                        isSelected = month.id == selectedMonthId,
                        onClick = { onMonthSelected(month.id) },
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
    month: CycleMonth,
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
            .border(1.dp, if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.3f) else RoutineTheme.colors.border, RoutineTheme.shapes.medium)
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
                    sweepAngle = (month.percentage.toFloat() / 100f) * 360f,
                    useCenter = false,
                    topLeft = Offset.Zero,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            if (month.percentage > 0) {
                Text(
                    text = "${month.percentage}%",
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurface
                )
            }
        }
        Text(
            text = month.name,
            style = RoutineTheme.typography.labelCaps,
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun MonthDetailSection(month: CycleMonth) {
    RoutineCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(month.name, style = RoutineTheme.typography.headlineMedium)
                    Text("${month.activeDays} de ${month.totalDays} días activos", style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp), color = RoutineTheme.colors.onSurfaceVariant)
                }
                Text(month.trendText, style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp), color = RoutineTheme.colors.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("PRINCIPALES SISTEMAS", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                month.principalSystems.forEach { sys ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.small)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = if (sys.iconName == "school") Icons.Default.School else if (sys.iconName == "fitness_center") Icons.Default.FitnessCenter else Icons.Default.Terminal,
                                contentDescription = null,
                                tint = RoutineTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(sys.title, style = RoutineTheme.typography.bodyBase)
                        }
                        Text("${sys.percentage}%", style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp), color = RoutineTheme.colors.primary)
                    }
                }
            }
        }
    }
}
