package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.ActivityNodeSnapshot
import com.alan.routineos.feature.today.model.TimelineItemStatus
import com.alan.routineos.feature.today.model.TodayTimelineItem

@Composable
fun TimelineItemCard(
    item: TodayTimelineItem,
    modifier: Modifier = Modifier
) {
    val cardAlpha = if (item.status == TimelineItemStatus.SKIPPED) 0.4f else 1f
    
    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        containerColor = if (item.status == TimelineItemStatus.ACTIVE) {
            RoutineTheme.colors.surface2
        } else {
            RoutineTheme.colors.surface1
        },
        border = if (item.status == TimelineItemStatus.ACTIVE) {
            androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
        }
    ) {
        Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.startTime + if (item.endTime != null) " – ${item.endTime}" else "",
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                        color = if (item.status == TimelineItemStatus.ACTIVE) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
                    )
                    
                    val title = when (item) {
                        is TodayTimelineItem.Activity -> item.title
                        is TodayTimelineItem.Flexible -> item.title
                        is TodayTimelineItem.Spontaneous -> item.title
                    }
                    
                    Text(
                        text = title,
                        style = RoutineTheme.typography.bodyBase.copy(
                            fontWeight = if (item.status == TimelineItemStatus.ACTIVE) FontWeight.Bold else FontWeight.SemiBold,
                            textDecoration = if (item.status == TimelineItemStatus.COMPLETED) TextDecoration.LineThrough else null
                        ),
                        color = RoutineTheme.colors.onSurface
                    )
                    
                    if (item is TodayTimelineItem.Flexible) {
                       Text(
                           text = item.activity,
                           style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
                           color = RoutineTheme.colors.primary
                       )
                       item.description?.let {
                           Text(
                               text = it,
                               style = RoutineTheme.typography.bodyBase.copy(fontSize = 11.sp),
                               color = RoutineTheme.colors.onSurfaceVariant
                           )
                       }
                    }
                }

                if (item is TodayTimelineItem.Flexible && item.progress != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.progress,
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                            color = RoutineTheme.colors.primary
                        )
                        Text(
                            text = "GRUPOS",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                    }
                } else {
                    StatusIcon(status = item.status)
                }
            }

            if (item is TodayTimelineItem.Activity && item.nodes.isNotEmpty() && item.status == TimelineItemStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
                Column(
                    modifier = Modifier
                        .padding(start = RoutineTheme.spacing.sm)
                        .drawThreadLine(RoutineTheme.colors.border)
                        .padding(start = RoutineTheme.spacing.md)
                ) {
                    item.nodes.forEach { node ->
                        NodeRow(node)
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                    }
                }
            }
            
            if (item is TodayTimelineItem.Spontaneous) {
                 item.interruptionInfo?.let {
                     Text(
                         text = it,
                         style = RoutineTheme.typography.bodyBase.copy(fontSize = 10.sp),
                         color = RoutineTheme.colors.onSurfaceVariant
                     )
                 }
            }
        }
    }
}

@Composable
private fun NodeRow(node: ActivityNodeSnapshot) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "${node.startTime} – ${node.endTime}",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                color = if (node.status == TimelineItemStatus.ACTIVE) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
            )
            Text(
                text = node.title,
                style = RoutineTheme.typography.bodyBase.copy(
                    fontSize = 14.sp,
                    textDecoration = if (node.status == TimelineItemStatus.COMPLETED) TextDecoration.LineThrough else null
                ),
                color = if (node.status == TimelineItemStatus.ACTIVE) RoutineTheme.colors.onSurface else RoutineTheme.colors.onSurfaceVariant
            )
        }
        StatusIcon(status = node.status, size = 16.dp)
    }
}

@Composable
private fun StatusIcon(status: TimelineItemStatus, size: androidx.compose.ui.unit.Dp = 24.dp) {
    val (icon, color) = when (status) {
        TimelineItemStatus.COMPLETED -> Icons.Default.CheckCircle to RoutineTheme.colors.primary
        TimelineItemStatus.ACTIVE -> Icons.Default.Sync to RoutineTheme.colors.primary
        TimelineItemStatus.PENDING -> Icons.Default.Schedule to RoutineTheme.colors.onSurfaceVariant
        TimelineItemStatus.SKIPPED -> Icons.Default.Block to RoutineTheme.colors.onSurfaceVariant
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(size)
    )
}

private fun Modifier.drawThreadLine(color: Color): Modifier = this.drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(0f, size.height),
        strokeWidth = 1.dp.toPx()
    )
}
