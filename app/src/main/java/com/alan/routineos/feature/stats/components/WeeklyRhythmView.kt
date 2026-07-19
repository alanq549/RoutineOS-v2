package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.components.body.BodyLoadCard
import com.alan.routineos.feature.stats.model.*

@Composable
fun WeeklyRhythmView(
    data: WeeklyRhythmData,
    selectedDayId: String?,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDay = data.days.find { it.id == selectedDayId } ?: data.days.first()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
    ) {
        // Range & Summary
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
                Text(data.dateRange, style = RoutineTheme.typography.dataLarge, color = RoutineTheme.colors.onSurface)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
            }
        }

        SummaryMetricsGrid(metrics = data.summaryMetrics)

        // Main Rhythm Chart
        RoutineCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "RITMO SEMANAL",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.days.forEach { day ->
                        RhythmDayColumn(
                            day = day,
                            isSelected = day.id == selectedDayId,
                            onClick = { onDaySelected(day.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Day Detail
        DayDetailSection(detail = selectedDay.details)

        // Body Load
        selectedDay.details.bodyLoad?.let { bodyLoad ->
            BodyLoadCard(state = bodyLoad)
        }

        // Comparison & Systems
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)) {
            ComparisonCard(comparisons = data.comparison, modifier = Modifier.weight(1f))
            SystemsCard(systems = data.systems, modifier = Modifier.weight(1f))
        }

        // Insights
        InsightsSection(insights = data.insights)
    }
}

@Composable
private fun SummaryMetricsGrid(metrics: List<StatsSummaryMetric>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
    ) {
        metrics.forEach { metric ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.medium)
                    .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = metric.label,
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = metric.value,
                        style = RoutineTheme.typography.dataLarge,
                        color = if (metric.isPrimary) RoutineTheme.colors.primary else RoutineTheme.colors.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun RhythmDayColumn(
    day: RhythmDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoutineTheme.shapes.small)
            .background(if (isSelected) RoutineTheme.colors.primary.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom)
        ) {
            day.blocks.forEach { block ->
                val color = when (block.type) {
                    RhythmBlockType.COMPLETED -> RoutineTheme.colors.primary
                    RhythmBlockType.PARTIAL -> RoutineTheme.colors.primary.copy(alpha = 0.4f)
                    RhythmBlockType.SPONTANEOUS -> RoutineTheme.colors.secondary
                    RhythmBlockType.SKIPPED -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(block.weight)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                        .then(
                            if (block.type == RhythmBlockType.SKIPPED) {
                                Modifier.border(1.dp, RoutineTheme.colors.border, RoundedCornerShape(2.dp))
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (block.type == RhythmBlockType.COMPLETED) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = RoutineTheme.colors.onPrimary, modifier = Modifier.size(10.dp))
                    }
                }
            }
        }
        Text(
            text = day.label,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun DayDetailSection(detail: RhythmDayDetail) {
    RoutineCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(detail.title, style = RoutineTheme.typography.headlineMedium, color = RoutineTheme.colors.onSurface)
                    Text(detail.subtitle, style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp), color = RoutineTheme.colors.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .background(RoutineTheme.colors.primary.copy(alpha = 0.1f), RoutineTheme.shapes.pill)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("${detail.completionPercentage}% completado", style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp), color = RoutineTheme.colors.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.small)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(
                                imageVector = if (item.iconName == "school") Icons.Default.School else if (item.iconName == "add") Icons.Default.Add else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (item.type == RhythmBlockType.SPONTANEOUS) RoutineTheme.colors.secondary else RoutineTheme.colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(item.title, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                        }
                        Text(
                            item.type.name,
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = if (item.type == RhythmBlockType.COMPLETED) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonCard(comparisons: List<StatsComparison>, modifier: Modifier = Modifier) {
    RoutineCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("PLANIFICADO VS REALIZADO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            comparisons.forEach { comp ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(comp.title, style = RoutineTheme.typography.bodyBase, fontWeight = FontWeight.SemiBold)
                        Text(comp.actualValue + " / " + comp.plannedValue, style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoutineTheme.shapes.pill).background(RoutineTheme.colors.surface2)) {
                        Box(modifier = Modifier.fillMaxWidth(comp.percentage).fillMaxHeight().background(RoutineTheme.colors.primary))
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemsCard(systems: List<StatsSystemMetric>, modifier: Modifier = Modifier) {
    RoutineCard(modifier = modifier) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("POR SISTEMA", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            systems.forEach { sys ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(RoutineTheme.colors.primary, RoutineTheme.shapes.pill))
                        Text(sys.title, style = RoutineTheme.typography.bodyBase)
                    }
                    Text("${sys.percentage}% · ${sys.details}", style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InsightsSection(insights: List<StatsInsight>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("INSIGHTS", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            insights.forEach { insight ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.medium)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (insight.iconName == "edit_calendar") Icons.Default.EditCalendar else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = RoutineTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(insight.message, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                }
            }
        }
    }
}
