package com.alan.routineos.feature.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.*
import com.alan.routineos.feature.today.model.*

private val AdHocAccent = Color(0xFFB894E6)
private val OverdueAccent = Color(0xFFFDBA74)

@Composable
fun TimelineItemCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isContainer = item.subNodes.isNotEmpty()
    val isCompleted = item.completion == HierarchyCompletion.COMPLETED
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isOverdue = item.temporalState == TimelineTemporalState.OVERDUE
    val isStale = item.temporalState == TimelineTemporalState.STALE_PENDING
    val isCurrent = item.temporalState == TimelineTemporalState.CURRENT
    val isInterrupter = item.conflict.isInterrupter

    val cardAlpha = if (isOmitted || isCompleted || isOverdue || isStale) 0.65f else 1f

    val accentColor = when {
        isCompleted -> RoutineTheme.colors.primary
        isOverdue -> OverdueAccent
        item.isAdHoc -> AdHocAccent
        item.status == DailyInstanceStatus.MODIFIED -> RoutineTheme.colors.secondary
        else -> RoutineTheme.colors.primary
    }

    val impactColor = when (item.conflict.impact) {
        TemporalImpact.WARNING -> RoutineTheme.colors.error
        TemporalImpact.INFO -> RoutineTheme.colors.secondary
        else -> null
    }

    val meshBrush = if (!isCompleted && (isCurrent || (item.isAdHoc && !isOmitted))) {
        Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.12f), Color.Transparent),
            radius = 600f
        )
    } else {
        SolidColor(RoutineTheme.colors.surface1)
    }

    var showMenu by remember { mutableStateOf(false) }

    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = if (isInterrupter) 24.dp else 0.dp)
            .alpha(cardAlpha),
        containerColor = if (isInterrupter) RoutineTheme.colors.background.copy(alpha = 0.5f) else Color.Transparent,
        border = BorderStroke(
            width = if (isInterrupter) 1.5.dp else 1.dp,
            color = when {
                isCompleted -> RoutineTheme.colors.border
                isInterrupter && impactColor != null -> impactColor
                else -> accentColor.copy(alpha = 0.4f)
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(meshBrush)) {
            // Minimal Impact Side Bar (removed for Interrupters to focus on the Inset look)
            if (impactColor != null && !isCompleted && !isInterrupter && item.conflict.details.any { it.impact == TemporalImpact.WARNING }) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(impactColor)
                )
            }

            Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                isCompleted -> Icons.Default.CheckCircle
                                isOverdue -> Icons.Default.History
                                item.isAdHoc -> Icons.Default.FlashOn
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = if (isOverdue) OverdueAccent else accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.title,
                            style = RoutineTheme.typography.headlineMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                            ),
                            color = if (isCompleted || isOverdue || isStale) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (item.isExpandable) {
                        val rotation by animateFloatAsState(if (item.isExpanded) 180f else 0f, label = "rotate")
                        IconButton(onClick = { onExpandClick(item.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ExpandMore, "Expandir", Modifier.rotate(rotation), RoutineTheme.colors.onSurfaceVariant)
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, "Acciones", tint = RoutineTheme.colors.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { showMenu = false; onAction(item.id, "MOVE_REQUEST") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Omitir") },
                                onClick = { showMenu = false; onAction(item.id, "SKIP") },
                                leadingIcon = { Icon(Icons.Default.Block, null) }
                            )
                        }
                    }
                }

                // PROGRESS
                if (isContainer) {
                    Row(modifier = Modifier.padding(start = 26.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.completedSubNodesCount}/${item.totalSubNodesCount} completados",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = if (isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Check, null, Modifier.size(10.dp), RoutineTheme.colors.primary)
                        }
                    }
                }

                // TIME
                Row(
                    modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time (shown inside card only if NOT an interrupter, to avoid rail marker double-up)
                    if (!isInterrupter) {
                        Text(
                            item.timeRangeText, 
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), 
                            color = if (isOverdue) OverdueAccent.copy(alpha = 0.8f) else RoutineTheme.colors.onSurfaceVariant
                        )
                    } else {
                        // For interrupters, show a small specific badge
                        Text(
                            item.timeRangeText,
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = impactColor ?: RoutineTheme.colors.primary
                        )
                    }
                    
                    if (isOverdue) {
                        Text("ATRASADA", style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = OverdueAccent)
                    }

                    // RELATIONSHIP SUMMARY (Internal hidden for interrupters - moved to Rail labels)
                    if (item.conflict.hasConflict && !isCompleted && !isInterrupter && impactColor == RoutineTheme.colors.secondary) {
                        RelationshipBadge(
                            text = getRelationLabel(item.conflict.details.first()),
                            color = RoutineTheme.colors.secondary
                        )
                    }
                }

                // SUGGESTIONS 
                if (item.isExpanded && item.conflict.hasConflict && !isCompleted) {
                    Column(modifier = Modifier.padding(start = 26.dp, top = 8.dp)) {
                        item.conflict.suggestions.forEach { suggestion ->
                            Surface(
                                onClick = { 
                                    if (suggestion.type == SuggestionType.MOVE) {
                                        onAction(item.id, "MOVE_TO:${suggestion.newStartTimeMinutes}")
                                    }
                                },
                                color = RoutineTheme.colors.surface2,
                                shape = RoutineTheme.shapes.small,
                                border = BorderStroke(1.dp, RoutineTheme.colors.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, null, Modifier.size(12.dp), RoutineTheme.colors.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = suggestion.message,
                                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                                        color = RoutineTheme.colors.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // SUB-NODES
                AnimatedVisibility(visible = item.isExpanded) {
                    if (item.subNodes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(modifier = Modifier.padding(start = 6.dp).drawThreadLine(RoutineTheme.colors.border.copy(alpha = 0.3f)).padding(start = 16.dp)) {
                            item.subNodes.forEach { subNode ->
                                SubNodeRow(subNode, onAction)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // METADATA
                if (item.contextMetadata.isNotEmpty() || item.operationalMetadata.isNotEmpty()) {
                    FlowRow(modifier = Modifier.padding(start = 26.dp, top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item.contextMetadata.forEach { (name, value) -> MetadataLabel(name = name, value = value, isContext = true) }
                        item.operationalMetadata.forEach { (name, value) -> MetadataLabel(name = name, value = value, isContext = false) }
                    }
                }

                // FOOTER: Action "COMPLETE"
                if (!isCompleted && !isOmitted && !isContainer) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { onAction(item.id, "COMPLETE") },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.9f), contentColor = Color.Black),
                            shape = RoutineTheme.shapes.small,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("COMPLETE", style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationshipBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoutineTheme.shapes.pill,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

private fun getRelationLabel(detail: ConflictDetailUiModel): String {
    val verb = when {
        detail.isInterruption -> "INTERRUMPE"
        detail.relationship == TemporalRelationship.CONTAINS -> "DENTRO DE"
        detail.relationship == TemporalRelationship.CONTAINED_BY -> "DENTRO DE"
        detail.relationship == TemporalRelationship.OVERLAP -> "SE CRUZA CON"
        else -> "RELACIÓN"
    }
    return "$verb · ${detail.otherTitle}"
}

@Composable
private fun SubNodeRow(subNode: TodaySubNodeUiModel, onAction: (String, String) -> Unit) {
    val isContainer = subNode.children.isNotEmpty()
    val isCompleted = subNode.completion == HierarchyCompletion.COMPLETED
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subNode.title,
                    style = RoutineTheme.typography.bodyBase.copy(
                        fontSize = 14.sp,
                        textDecoration = if (subNode.status == DailyInstanceStatus.OMITTED || isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (subNode.status == DailyInstanceStatus.OMITTED || isCompleted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subNode.timeText.isNotBlank()) Text(subNode.timeText, style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant)
                    if (isContainer) {
                        if (subNode.timeText.isNotBlank()) Spacer(modifier = Modifier.width(8.dp))
                        Text("${subNode.completedCount}/${subNode.totalCount}", style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = if (isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
            }

            if (!isCompleted && subNode.status != DailyInstanceStatus.OMITTED && !isContainer) {
                IconButton(onClick = { onAction(subNode.id, "COMPLETE") }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Outlined.RadioButtonUnchecked, null, Modifier.size(18.dp), RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f))
                }
            } else {
                StatusIcon(status = subNode.status, size = 16.dp, isHierarchyCompleted = isCompleted)
            }
        }
        
        if (subNode.children.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 12.dp).drawThreadLine(RoutineTheme.colors.border.copy(alpha = 0.2f)).padding(start = 12.dp)) {
                subNode.children.forEach { child ->
                    SubNodeRow(child, onAction)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusIcon(status: DailyInstanceStatus, size: androidx.compose.ui.unit.Dp = 24.dp, isHierarchyCompleted: Boolean = false) {
    val (icon, color) = when {
        status == DailyInstanceStatus.COMPLETED || isHierarchyCompleted -> Icons.Default.CheckCircle to RoutineTheme.colors.primary
        status == DailyInstanceStatus.MODIFIED -> Icons.Default.Sync to RoutineTheme.colors.secondary
        status == DailyInstanceStatus.OMITTED -> Icons.Default.Block to RoutineTheme.colors.onSurfaceVariant
        else -> Icons.Default.Schedule to RoutineTheme.colors.onSurfaceVariant
    }
    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(size))
}

@Composable
private fun MetadataLabel(name: String, value: String, isContext: Boolean) {
    Text(
        text = if (isContext) "$name: $value" else "$name ($value)",
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
        color = if (isContext) RoutineTheme.colors.primary.copy(alpha = 0.8f) else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

private fun Modifier.drawThreadLine(color: Color): Modifier = this.drawBehind {
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 1.dp.toPx())
}
