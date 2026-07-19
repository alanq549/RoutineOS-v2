package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.alan.routineos.feature.stats.components.body.BodyLoadCard
import com.alan.routineos.feature.stats.model.*

@Composable
fun MonthlyCycleView(
    data: MonthlyCycleData,
    selectedWeekId: String?,
    onWeekSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedWeek = data.weeks.find { it.id == selectedWeekId } ?: data.weeks.first()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
    ) {
        // Range & Summary
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
                Text(data.monthName, style = RoutineTheme.typography.headlineMedium, color = RoutineTheme.colors.onSurface)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
            }
        }

        // Main Cycle Chart
        RoutineCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                    CycleRingsCanvas(weeks = data.weeks)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${data.completionPercentage}%", style = RoutineTheme.typography.displayLarge.copy(fontSize = 56.sp), color = RoutineTheme.colors.primary)
                        Text("CUMPLIMIENTO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(data.activeDaysText, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                Text("${selectedWeek.label} seleccionada", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.primary, modifier = Modifier.padding(top = 8.dp))
            }
        }

        // Selected Week Metrics & Nav
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)) {
            WeeklySummaryCard(week = selectedWeek, modifier = Modifier.weight(1f))
            CycleNavigationList(weeks = data.weeks, selectedWeekId = selectedWeekId, onWeekSelected = onWeekSelected, modifier = Modifier.weight(1f))
        }

        // Body Load
        selectedWeek.bodyLoad?.let { bodyLoad ->
            BodyLoadCard(state = bodyLoad)
        }

        // Outcome Breakdown
        OutcomeBreakdownSection(metrics = data.outcomeBreakdown)

        // Systems this Month
        SystemsBreakdownSection(systems = data.systems)

        // Comparison & Insights
        CycleComparisonCard(comparisons = data.comparison)
        
        CycleInsightsSection(insights = data.insights)
    }
}

@Composable
private fun CycleRingsCanvas(weeks: List<CycleWeek>) {
    val strokeWidthDp = 8.dp
    val spacingDp = 16.dp
    
    val primaryColor = RoutineTheme.colors.primary
    val trackColor = RoutineTheme.colors.surface2

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 2 - 20.dp.toPx()
        
        weeks.asReversed().forEachIndexed { index, week ->
            val radius = baseRadius - (spacingDp.toPx() * index)
            val sweepAngle = (week.percentage.toFloat() / 100f) * 360f
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
                color = if (week.isSelected) primaryColor else primaryColor.copy(alpha = alpha.coerceIn(0.2f, 0.8f)),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = if (week.isSelected) strokeWidthDp.toPx() * 1.5f else strokeWidthDp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun WeeklySummaryCard(week: CycleWeek, modifier: Modifier = Modifier) {
    RoutineCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(week.label, style = RoutineTheme.typography.headlineMedium)
                week.comparisonText?.let {
                    Text(it, style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.error)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            GridMetrics()
        }
    }
}

@Composable
private fun GridMetrics() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricItem("COMPLETADO", "68%", Modifier.weight(1f))
            MetricItem("DÍAS ACTIVOS", "6", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricItem("CAMBIOS", "3", Modifier.weight(1f))
            MetricItem("PARCIALES", "2", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = RoutineTheme.colors.onSurfaceVariant)
        Text(value, style = RoutineTheme.typography.dataLarge, color = RoutineTheme.colors.primary)
    }
}

@Composable
private fun CycleNavigationList(weeks: List<CycleWeek>, selectedWeekId: String?, onWeekSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        weeks.forEach { week ->
            val isSelected = week.id == selectedWeekId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoutineTheme.shapes.small)
                    .background(if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.1f) else RoutineTheme.colors.surface2)
                    .border(1.dp, if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.3f) else Color.Transparent, RoutineTheme.shapes.small)
                    .clickable { onWeekSelected(week.id) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(week.label, style = RoutineTheme.typography.bodyBase, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurface)
                Text("${week.percentage}%", style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp), color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun OutcomeBreakdownSection(metrics: List<StatsSummaryMetric>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("RESULTADO DEL MES", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            metrics.forEach { metric ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(12.dp)
                ) {
                    Text(metric.label, style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = RoutineTheme.colors.onSurfaceVariant)
                    Text(metric.value, style = RoutineTheme.typography.dataLarge, color = RoutineTheme.colors.primary)
                }
            }
        }
    }
}

@Composable
private fun SystemsBreakdownSection(systems: List<StatsSystemMetric>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("SISTEMAS ESTE MES", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            systems.forEach { sys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(16.dp),
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
                    Text("${sys.percentage}% · ${sys.details}", style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CycleComparisonCard(comparisons: List<StatsComparison>) {
    RoutineCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("TIEMPO PLANIFICADO VS REAL", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            comparisons.forEach { comp ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(comp.title, style = RoutineTheme.typography.bodyBase)
                        Text(comp.actualValue, style = if (comp.title == "Real") RoutineTheme.colors.primary.let { RoutineTheme.typography.dataLarge.copy(color = it) } else RoutineTheme.typography.dataLarge.copy(color = RoutineTheme.colors.onSurfaceVariant))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoutineTheme.shapes.pill).background(RoutineTheme.colors.surface2)) {
                        Box(modifier = Modifier.fillMaxWidth(comp.percentage).fillMaxHeight().background(RoutineTheme.colors.primary.copy(alpha = if (comp.title == "Planificado") 0.4f else 1f)))
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleInsightsSection(insights: List<StatsInsight>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("INSIGHTS DEL CICLO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            insights.forEach { insight ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(16.dp)
                ) {
                    Text(insight.message, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                }
            }
        }
    }
}
