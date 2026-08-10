package com.alan.routineos.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.components.TodayHeader
import com.alan.routineos.feature.today.components.TodayNextActivityCard
import com.alan.routineos.feature.today.components.TodayTimeline

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    RoutineScaffold(
        modifier = modifier,
        bottomBar = bottomBar
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding() // Start below status bar
                    .padding(bottom = paddingValues.calculateBottomPadding()) // Account for bottom bar
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RoutineTheme.spacing.marginMobile),
                verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
            ) {
                // Header handles its own top padding (lg)
                TodayHeader(
                    dateText = uiState.dateText,
                    progress = uiState.progress
                )

                TodayNextActivityCard(activity = uiState.nextActivity)

                PendingChipsSection()

                TodayTimeline(items = uiState.timelineItems)
                
                // Minimal bottom margin after last item
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))
            }

            TodayFAB(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(RoutineTheme.spacing.lg)
                    .padding(bottom = paddingValues.calculateBottomPadding()) // Stay above bottom nav
            )
        }
    }
}

@Composable
private fun PendingChipsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)
    ) {
        ActivityChipWithCount(count = "2", label = "NODOS")
        ActivityChipWithCount(count = "1", label = "NOTA")
        ActivityChipWithCount(count = "1", label = "RECORDATORIO")
    }
}

@Composable
private fun ActivityChipWithCount(count: String, label: String) {
    Row(
        modifier = Modifier
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.pill)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.pill)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = count,
            style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
            color = RoutineTheme.colors.primary,
            modifier = Modifier
                .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = RoutineTheme.colors.onSurface
        )
    }
}

@Composable
private fun TodayFAB(modifier: Modifier = Modifier) {
    // Disabled FAB as per EC-010 until EC-012 defines the action
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = RoutineTheme.colors.onSurface.copy(alpha = 0.12f),
                shape = RoutineTheme.shapes.pill
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = RoutineTheme.colors.onSurface.copy(alpha = 0.38f)
        )
    }
}
