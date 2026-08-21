package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.MetadataField
import com.alan.routineos.feature.today.model.TodaySubNodeUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

@Composable
fun TimelineItemCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.status == DailyInstanceStatus.COMPLETED
    val cardAlpha = if (item.status == DailyInstanceStatus.OMITTED || isCompleted) 0.7f else 1f
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    
    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        containerColor = when {
            isCompleted -> RoutineTheme.colors.surface1
            isModified -> RoutineTheme.colors.surface2
            else -> RoutineTheme.colors.surface1
        },
        border = when {
            isCompleted -> androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
            isModified -> androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.secondary)
            else -> androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
        }
    ) {
        Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.isAdHoc) {
                        Surface(
                            color = RoutineTheme.colors.primary.copy(alpha = 0.2f),
                            shape = RoutineTheme.shapes.small,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "ESPONTÁNEA",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                                color = RoutineTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (item.timeRangeText.isNotBlank()) {
                        Text(
                            text = item.timeRangeText,
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                            color = when {
                                isCompleted -> RoutineTheme.colors.primary
                                isModified -> RoutineTheme.colors.secondary
                                else -> RoutineTheme.colors.primary
                            }
                        )
                    }
                    
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.bodyBase.copy(
                            fontWeight = if (isModified) FontWeight.Bold else FontWeight.SemiBold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (isCompleted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (item.hasConflict) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = RoutineTheme.colors.error.copy(alpha = 0.1f),
                            shape = RoutineTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.error.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = RoutineTheme.colors.error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Conflicto detectado",
                                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                                    color = RoutineTheme.colors.error
                                )
                            }
                        }
                    }
                    
                    // Display Metadata
                    if (item.contextMetadata.isNotEmpty() || item.operationalMetadata.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item.contextMetadata.forEach { (name, value) ->
                                MetadataLabel(name = name, value = value, isContext = true)
                            }
                            item.operationalMetadata.forEach { (name, value) ->
                                MetadataLabel(name = name, value = value, isContext = false)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isExpandable) {
                        val rotation by animateFloatAsState(if (item.isExpanded) 180f else 0f)
                        IconButton(onClick = { onExpandClick(item.id) }) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Expand",
                                modifier = Modifier.rotate(rotation),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (item.status == DailyInstanceStatus.PLANNED || (item.isAdHoc && item.status == DailyInstanceStatus.MODIFIED)) {
                        IconButton(onClick = { onAction(item.id, "MOVE_REQUEST") }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Reschedule", tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onAction(item.id, "SKIP") }) {
                            Icon(Icons.Default.Block, contentDescription = "Skip", tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = { onAction(item.id, "COMPLETE") }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = RoutineTheme.colors.primary)
                        }
                    } else {
                        StatusIcon(status = item.status)
                    }
                }
            }

            // Sub-nodes Section (with expansion support)
            AnimatedVisibility(visible = item.isExpanded || !item.isExpandable) {
                if (item.subNodes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
                    Column(
                        modifier = Modifier
                            .padding(start = RoutineTheme.spacing.sm)
                            .drawThreadLine(RoutineTheme.colors.border)
                            .padding(start = RoutineTheme.spacing.md)
                    ) {
                        item.subNodes.forEachIndexed { index, subNode ->
                            SubNodeRow(subNode, onAction)
                            if (index != item.subNodes.lastIndex) {
                                Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubNodeRow(
    subNode: TodaySubNodeUiModel,
    onAction: (String, String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (subNode.timeText.isNotBlank()) {
                Text(
                    text = subNode.timeText,
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )
            }
            Text(
                text = subNode.title,
                style = RoutineTheme.typography.bodyBase.copy(
                    fontSize = 14.sp,
                    textDecoration = if (subNode.status == DailyInstanceStatus.OMITTED || subNode.status == DailyInstanceStatus.COMPLETED) TextDecoration.LineThrough else null
                ),
                color = if (subNode.status == DailyInstanceStatus.OMITTED || subNode.status == DailyInstanceStatus.COMPLETED) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subNode.contextMetadata.isNotEmpty() || subNode.operationalMetadata.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subNode.contextMetadata.forEach { (name, value) ->
                        MetadataLabel(name = name, value = value, isContext = true)
                    }
                    subNode.operationalMetadata.forEach { (name, value) ->
                        MetadataLabel(name = name, value = value, isContext = false)
                    }
                }
            }
        }
        
        if (subNode.status == DailyInstanceStatus.PLANNED) {
            Row {
                IconButton(onClick = { onAction(subNode.id, "SKIP") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Block, contentDescription = "Skip", tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { onAction(subNode.id, "COMPLETE") }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Complete", tint = RoutineTheme.colors.primary, modifier = Modifier.size(20.dp))
                }
            }
        } else {
            StatusIcon(status = subNode.status, size = 16.dp)
        }
    }
}

@Composable
private fun StatusIcon(status: DailyInstanceStatus, size: androidx.compose.ui.unit.Dp = 24.dp) {
    val (icon, color) = when (status) {
        DailyInstanceStatus.PLANNED -> Icons.Default.Schedule to RoutineTheme.colors.onSurfaceVariant
        DailyInstanceStatus.MODIFIED -> Icons.Default.Sync to RoutineTheme.colors.secondary
        DailyInstanceStatus.COMPLETED -> Icons.Default.CheckCircle to RoutineTheme.colors.primary
        DailyInstanceStatus.OMITTED -> Icons.Default.Block to RoutineTheme.colors.onSurfaceVariant
    }
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun MetadataLabel(name: String, value: String, isContext: Boolean) {
    Text(
        text = if (isContext) "$name: $value" else "$name ($value)",
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
        color = if (isContext) RoutineTheme.colors.primary.copy(alpha = 0.8f) 
                else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
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
