package com.alan.routineos.feature.today.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.TemporalImpact
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
            // Vertical Axis Column (Unified 56.dp rail width)
            Box(
                modifier = Modifier
                    .width(56.dp)
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
                    
                    Text(
                        text = item.timeRangeText,
                        style = markerStyle,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 2.dp, top = 14.dp)
                    )
                }

                // Continuous Spine Rail Line (Seamless connection without gaps)
                val railColor = RoutineTheme.colors.border.copy(alpha = 0.5f)
                val isDashed = item.temporalState == TimelineTemporalState.OVERDUE || item.temporalState == TimelineTemporalState.STALE_PENDING
                val pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null

                Canvas(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .align(Alignment.TopCenter)
                ) {
                    val endY = if (isLast) 20.dp.toPx() else size.height
                    drawLine(
                        color = railColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, endY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
                
                // Timeline Node centered at Y = 20.dp
                TimelineNode(
                    status = item.status, 
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .align(Alignment.TopCenter)
                )
            }

            // Card Column
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) {
                TimelineItemCard(
                    item = item, 
                    onAction = onAction,
                    onExpandClick = onExpandClick
                )
            }
        }
    }
}
