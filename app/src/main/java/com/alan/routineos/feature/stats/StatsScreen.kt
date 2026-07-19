package com.alan.routineos.feature.stats

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.components.MonthlyCycleView
import com.alan.routineos.feature.stats.components.StatsPeriodSelector
import com.alan.routineos.feature.stats.components.WeeklyRhythmView
import com.alan.routineos.feature.stats.components.YearlyCycleView
import com.alan.routineos.feature.stats.model.StatsPeriod

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onDaySelected: (String) -> Unit,
    onWeekSelected: (String) -> Unit,
    onMonthSelected: (String) -> Unit,
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RoutineTheme.spacing.marginMobile)
                    .padding(bottom = RoutineTheme.spacing.xl)
            ) {
                AnimatedContent(
                    targetState = uiState.selectedPeriod,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "StatsPeriodAnimation"
                ) { period ->
                    when (period) {
                        StatsPeriod.WEEK -> {
                            uiState.weeklyData?.let { data ->
                                WeeklyRhythmView(
                                    data = data,
                                    selectedDayId = uiState.selectedDayId,
                                    onDaySelected = onDaySelected
                                )
                            }
                        }
                        StatsPeriod.MONTH -> {
                            uiState.monthlyData?.let { data ->
                                MonthlyCycleView(
                                    data = data,
                                    selectedWeekId = uiState.selectedWeekId,
                                    onWeekSelected = onWeekSelected
                                )
                            }
                        }
                        StatsPeriod.YEAR -> {
                            uiState.yearlyData?.let { data ->
                                YearlyCycleView(
                                    data = data,
                                    selectedMonthId = uiState.selectedMonthId,
                                    onMonthSelected = onMonthSelected
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
