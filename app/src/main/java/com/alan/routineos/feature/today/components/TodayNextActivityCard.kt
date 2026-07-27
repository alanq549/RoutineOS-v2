package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayTimelineItem

@Composable
fun TodayNextActivityCard(
    activity: TodayTimelineItem?,
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
            containerColor = RoutineTheme.colors.surface3 // Translucent glass effect
        ) {
            Box {
                // Background decoration (gradient)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(120.dp)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EN 15 MIN",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.primary,
                            modifier = Modifier
                                .background(RoutineTheme.colors.primary.copy(alpha = 0.1f), RoutineTheme.shapes.small)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.xs))
                        
                        val title = when(activity) {
                            is TodayTimelineItem.Activity -> activity.title
                            is TodayTimelineItem.Flexible -> activity.activity
                            is TodayTimelineItem.Spontaneous -> activity.title
                        }
                        
                        Text(
                            text = title,
                            style = RoutineTheme.typography.headlineMedium,
                            color = RoutineTheme.colors.onSurface
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = "${activity.startTime} - ${activity.endTime ?: ""}",
                                style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
                                color = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoutineTheme.shapes.small),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = RoutineTheme.colors.primary,
                            contentColor = RoutineTheme.colors.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    }
                }
            }
        }
    }
}
