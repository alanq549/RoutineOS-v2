package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TimelineTemporalState
import com.alan.routineos.feature.today.model.TodayTimelineUiModel
import java.time.LocalTime

@Composable
fun TodayNextActivityCard(
    activity: TodayTimelineUiModel?,
    modifier: Modifier = Modifier
) {
    if (activity == null) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (activity.temporalState == TimelineTemporalState.CURRENT) "AHORA MISMO" else "SIGUIENTE",
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = RoutineTheme.spacing.md)
        )

        RoutineCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = RoutineTheme.colors.surface3
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Radial Gradient Decoration
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(120.dp)
                        .offset(x = 40.dp, y = (-40).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    RoutineTheme.colors.primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .padding(RoutineTheme.spacing.md)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val now = LocalTime.now()
                        val currentMinutes = now.hour * 60 + now.minute
                        
                        val startMinutes = activity.timeRangeText.split(":").let { 
                            if (it.size == 2) it[0].toInt() * 60 + it[1].toInt() else 0 
                        }

                        val badgeText = when {
                            activity.temporalState == TimelineTemporalState.CURRENT -> "EN CURSO"
                            startMinutes > currentMinutes -> {
                                val diff = startMinutes - currentMinutes
                                if (diff < 60) "EN $diff MIN" else "EN ${diff / 60}H ${diff % 60}M"
                            }
                            else -> "AHORA"
                        }

                        Surface(
                            color = RoutineTheme.colors.primary.copy(alpha = 0.1f),
                            shape = RoutineTheme.shapes.small
                        ) {
                            Text(
                                text = badgeText,
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                                color = RoutineTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = activity.title,
                            style = RoutineTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoutineTheme.colors.onSurface
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (activity.subNodes.isNotEmpty()) {
                                    "${activity.subNodes.first().title} • ${activity.timeRangeText}"
                                } else activity.timeRangeText,
                                style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                                color = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    // Play Button
                    Surface(
                        onClick = { /* TODO: Start */ },
                        color = RoutineTheme.colors.primary,
                        contentColor = RoutineTheme.colors.onPrimary,
                        shape = RoutineTheme.shapes.medium,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }
            }
        }
    }
}
