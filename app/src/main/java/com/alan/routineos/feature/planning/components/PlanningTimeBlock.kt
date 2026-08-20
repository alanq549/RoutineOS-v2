package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import com.alan.routineos.domain.model.DailyInstanceStatus

@Composable
fun PlanningTimeBlock(
    item: TodayTimelineUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Axis
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeRangeText,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                color = if (item.status == DailyInstanceStatus.MODIFIED) RoutineTheme.colors.secondary else RoutineTheme.colors.primary
            )
        }

        Spacer(modifier = Modifier.width(RoutineTheme.spacing.md))

        // Content Card
        RoutineCard(
            modifier = Modifier.weight(1f),
            containerColor = RoutineTheme.colors.surface3,
            border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
        ) {
            Row(
                modifier = Modifier.padding(RoutineTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        color = RoutineTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (item.hasConflict) {
                        Text(
                            text = "Conflicto de horario",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.error
                        )
                    }
                }

                if (item.status == DailyInstanceStatus.OMITTED) {
                    Text(
                        text = "SALTADO",
                        style = RoutineTheme.typography.labelCaps,
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
