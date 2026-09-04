package com.alan.routineos.feature.stats

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.components.*
import com.alan.routineos.feature.stats.model.StatsPeriod

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    RoutineScaffold(
        modifier = modifier,
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Main Header
            Column(
                modifier = Modifier
                    .padding(horizontal = RoutineTheme.spacing.marginMobile)
                    .padding(top = RoutineTheme.spacing.lg)
            ) {
                Text(
                    "Estadísticas",
                    style = RoutineTheme.typography.displayLarge,
                    color = RoutineTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                StatsPeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RoutineTheme.colors.primary)
                }
            } else if (uiState.historySnapshot == null || uiState.historySnapshot.totalOccurrences == 0) {
                EmptyStatsView()
            } else {
                StatsScrollableContent(uiState)
            }
        }
    }
}

@Composable
private fun StatsScrollableContent(uiState: StatsUiState) {
    val snapshot = uiState.historySnapshot!!
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = RoutineTheme.spacing.marginMobile)
            .padding(bottom = RoutineTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // GLOBAL SUMMARY
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
        ) {
            SummaryCard(
                label = "Adherencia Total",
                value = snapshot.completionRate?.let { "${(it * 100).toInt()}%" } ?: "0%",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Consistencia",
                value = snapshot.executionConsistency?.let { "${(it * 100).toInt()}%" } ?: "N/A",
                modifier = Modifier.weight(1f)
            )
        }

        // GRANULAR VIEW (Animated Rhythm/Cycle)
        AnimatedContent(
            targetState = uiState.selectedPeriod,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "StatsViewAnimation"
        ) { period ->
            when (period) {
                StatsPeriod.DAY -> {
                    // In "Day" view, we show the daily breakdown directly without the weekly rhythm bar
                    snapshot.dailyStats.lastOrNull()?.let { 
                         DaySummaryContent(detail = it)
                    }
                }
                StatsPeriod.WEEK -> {
                    WeeklyRhythmView(days = snapshot.dailyStats)
                }
                StatsPeriod.MONTH -> {
                    snapshot.monthlyStats.firstOrNull()?.let { 
                        MonthlyCycleView(monthStats = it)
                    }
                }
                StatsPeriod.YEAR -> {
                    YearlyCycleView(year = "2026", months = snapshot.monthlyStats)
                }
            }
        }

        // ADHERENCE BREAKDOWN (Systems)
        AdherenceList(
            title = "Adherencia por Sistema",
            systems = snapshot.systemAdherence
        )

        // ADHERENCE BREAKDOWN (Activities - Universidad, Pull Day, etc.)
        if (snapshot.activityAdherence.isNotEmpty()) {
            AdherenceList(
                title = "Adherencia por Actividad",
                activities = snapshot.activityAdherence
            )
        }

        // FOCUS INDEX (N/A in v1)
        FocusPieChart()

        // TRENDS
        uiState.trendSeries.forEach { series ->
            MetadataLineChart(series = series)
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = RoutineTheme.colors.surface2,
        shape = RoutineTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                color = RoutineTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = RoutineTheme.typography.dataLarge,
                color = RoutineTheme.colors.primary
            )
        }
    }
}

@Composable
private fun EmptyStatsView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Sin Actividad",
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurfaceVariant
            )
            Text(
                "No hay ocurrencias registradas en este periodo",
                style = RoutineTheme.typography.bodyBase,
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
