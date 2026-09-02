package com.alan.routineos.feature.today.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.TemporalImpact
import com.alan.routineos.domain.model.TemporalRelationship
import com.alan.routineos.feature.today.model.*

fun LazyListScope.todayTimelineItems(
    items: List<TodayTimelineUiModel>,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    itemsIndexed(
        items = items,
        key = { _, item -> item.id }
    ) { index, item ->
        TimelineRow(
            item = item,
            isLast = index == items.lastIndex,
            onAction = onAction,
            onExpandClick = onExpandClick
        )
    }
}

@Composable
fun TimelineRow(
    item: TodayTimelineUiModel,
    isLast: Boolean,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    val isVictim = item.conflict.hasConflict && !item.conflict.isInterrupter && item.conflict.impact == TemporalImpact.WARNING
    
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Vertical Axis Column (Simplified: just the rail and nodes)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Time Markers for Rail
                if (isVictim || item.interception != null) {
                    val markerStyle = RoutineTheme.typography.labelCaps.copy(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    // Start Time (Top)
                    Text(
                        text = item.timeRangeText,
                        style = markerStyle,
                        modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 14.dp)
                    )
                }

                if (!isLast) {
                    val railColor = RoutineTheme.colors.border
                    val isDashed = item.temporalState == TimelineTemporalState.OVERDUE || item.temporalState == TimelineTemporalState.STALE_PENDING
                    val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null

                    Canvas(modifier = Modifier
                        .padding(top = 24.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .align(Alignment.TopCenter)
                    ) {
                        drawLine(
                            color = railColor,
                            start = Offset(0f, 0f),
                            end = Offset(0f, size.height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = pathEffect
                        )
                    }
                }
                
                TimelineNode(
                    status = item.status, 
                    modifier = Modifier.padding(top = 16.dp).align(Alignment.TopCenter)
                )
            }

            // Card Column
            Box(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
                TimelineItemCard(
                    item = item, 
                    onAction = onAction,
                    onExpandClick = onExpandClick
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
