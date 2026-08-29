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
    val isInterrupter = item.conflict.isInterrupter
    val isVictim = item.conflict.hasConflict && !isInterrupter && item.conflict.impact == TemporalImpact.WARNING
    
    val impactColor = when (item.conflict.impact) {
        TemporalImpact.WARNING -> RoutineTheme.colors.error
        TemporalImpact.INFO -> RoutineTheme.colors.secondary
        else -> null
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .drawBehind {
                    val railX = 32.dp.toPx() // Moved rail to make room for time labels
                    val nodeY = 22.dp.toPx()

                    if (isInterrupter && impactColor != null) {
                        // Technical Bracket (Elbow)
                        val cardX = (32 + 24 + 24).dp.toPx() 
                        
                        // Horizontal line from rail to card start
                        drawLine(
                            color = impactColor.copy(alpha = 0.6f),
                            start = Offset(railX, nodeY),
                            end = Offset(cardX - 4.dp.toPx(), nodeY),
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        // Vertical "connector" line up towards the previous item
                        drawLine(
                            color = impactColor.copy(alpha = 0.6f),
                            start = Offset(railX, nodeY),
                            end = Offset(railX, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
        ) {
            // Vertical Axis Column (Extended for time markers)
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Time Markers for Dashed Rail
                if (isVictim) {
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
                    
                    // End Time (Bottom)
                    item.endTimeMinutes?.let { end ->
                        Text(
                            text = formatMinutes(end),
                            style = markerStyle,
                            modifier = Modifier.align(Alignment.BottomStart).padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                if (!isLast) {
                    val railColor = if (isVictim) RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f) else RoutineTheme.colors.border
                    val pathEffect = if (isVictim) PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f) else null

                    Canvas(modifier = Modifier
                        .padding(top = 24.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .align(Alignment.TopCenter)
                        .offset(x = 0.dp) // Aligned with the node center
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

        // Floating Technical Label
        if (isInterrupter && impactColor != null) {
            val labelVerb = when {
                item.conflict.details.any { it.isInterruption } -> "INTERRUMPE"
                item.conflict.details.firstOrNull()?.relationship == TemporalRelationship.CONTAINS -> "DENTRO DE"
                else -> "SE CRUZA"
            }
            
            Text(
                text = "→ $labelVerb",
                style = RoutineTheme.typography.labelCaps.copy(
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                ),
                color = impactColor,
                modifier = Modifier
                    .padding(start = 70.dp, top = 11.dp)
                    .background(RoutineTheme.colors.background)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
