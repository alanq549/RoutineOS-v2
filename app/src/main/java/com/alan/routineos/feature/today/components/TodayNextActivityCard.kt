package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
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

@Composable
fun TodayNextActivityCard(
    activity: TodayTimelineUiModel?,
    modifier: Modifier = Modifier
) {
    if (activity == null) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SIGUIENTE",
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = RoutineTheme.spacing.sm)
        )

        RoutineCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = RoutineTheme.colors.surface3
        ) {
            Row(
                modifier = Modifier
                    .padding(RoutineTheme.spacing.md)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Countdown Badge
                    Surface(
                        color = RoutineTheme.colors.primary.copy(alpha = 0.15f),
                        shape = RoutineTheme.shapes.small
                    ) {
                        Text(
                            text = "EN 15 MIN", // TODO: Real calculation
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                    
                    Text(
                        text = activity.title,
                        style = RoutineTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = RoutineTheme.colors.onSurface
                    )
                    
                    if (activity.subNodes.isNotEmpty()) {
                        val firstChild = activity.subNodes.first()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${firstChild.title} • ${firstChild.timeText}",
                                style = RoutineTheme.typography.bodyBase.copy(fontSize = 12.sp),
                                color = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }

                // Play Button
                Surface(
                    onClick = { /* TODO: Start execution */ },
                    modifier = Modifier.size(48.dp),
                    shape = RoutineTheme.shapes.medium,
                    color = RoutineTheme.colors.primary,
                    contentColor = RoutineTheme.colors.onPrimary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
