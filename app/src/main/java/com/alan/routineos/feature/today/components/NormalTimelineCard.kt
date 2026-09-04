package com.alan.routineos.feature.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenu
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenuItem
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.*
import com.alan.routineos.feature.today.model.*

private val AdHocAccent = Color(0xFFB894E6)
private val OverdueAccent = Color(0xFFFDBA74)

enum class TimelineCardStyle {
    FULL,
    MINIMAL
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NormalTimelineCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TimelineCardStyle = TimelineCardStyle.FULL
) {
    if (style == TimelineCardStyle.MINIMAL) {
        MinimalSpontaneousRow(item, onAction, modifier)
        return
    }

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
            .padding(start = if (isInterrupter) 12.dp else 0.dp)
            .alpha(cardAlpha),
        containerColor = if (isInterrupter) RoutineTheme.colors.background.copy(alpha = 0.5f) else Color.Transparent,
        border = BorderStroke(
            width = if (isInterrupter) 1.5.dp else 1.dp,
            color = when {
                isCompleted -> RoutineTheme.colors.border
                item.isAdHoc -> AdHocAccent.copy(alpha = 0.75f)
                isInterrupter && impactColor != null -> impactColor
                else -> accentColor.copy(alpha = 0.4f)
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(meshBrush)) {
            // Minimal Impact Side Bar
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
                        RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (!isCompleted && !isOmitted) {
                                RoutineDropdownMenuItem(
                                    text = "Editar",
                                    onClick = { showMenu = false; onAction(item.id, "MOVE_REQUEST") },
                                    icon = Icons.Default.Edit
                                )
                                RoutineDropdownMenuItem(
                                    text = "Omitir",
                                    onClick = { showMenu = false; onAction(item.id, "SKIP") },
                                    icon = Icons.Default.Block,
                                    iconColor = RoutineTheme.colors.onSurfaceVariant
                                )
                            } else {
                                RoutineDropdownMenuItem(
                                    text = "Desmarcar",
                                    onClick = { showMenu = false; onAction(item.id, "RESET") },
                                    icon = Icons.AutoMirrored.Filled.Undo,
                                    iconColor = RoutineTheme.colors.secondary
                                )
                            }
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
                    if (!isInterrupter) {
                        Text(
                            item.timeRangeText, 
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), 
                            color = if (isOverdue) OverdueAccent.copy(alpha = 0.8f) else RoutineTheme.colors.onSurfaceVariant
                        )
                    } else {
                        Text(
                            item.timeRangeText,
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = impactColor ?: RoutineTheme.colors.primary
                        )
                    }
                    
                    if (isOverdue) {
                        Text("ATRASADA", style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = OverdueAccent)
                    }

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
private fun MinimalSpontaneousRow(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.status == DailyInstanceStatus.COMPLETED
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val color = AdHocAccent
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = color.copy(alpha = 0.08f),
        shape = RoutineTheme.shapes.small,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted || isOmitted) TextDecoration.LineThrough else null
                    ),
                    color = if (isOmitted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface
                )
                if (item.timeRangeText.isNotBlank()) {
                    Text(
                        text = item.timeRangeText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                        color = if (isOmitted) color.copy(alpha = 0.5f) else color
                    )
                }
            }

            if (!isCompleted && !isOmitted) {
                IconButton(
                    onClick = { onAction(item.id, "COMPLETE") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = "Complete",
                        tint = color.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                StatusIcon(status = item.status, size = 18.dp)
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (!isCompleted && !isOmitted) {
                        RoutineDropdownMenuItem(
                            text = "Editar",
                            onClick = { showMenu = false; onAction(item.id, "MOVE_REQUEST") },
                            icon = Icons.Default.Edit
                        )
                        RoutineDropdownMenuItem(
                            text = "Omitir",
                            onClick = { showMenu = false; onAction(item.id, "SKIP") },
                            icon = Icons.Default.Block,
                            iconColor = RoutineTheme.colors.onSurfaceVariant
                        )
                    } else {
                        RoutineDropdownMenuItem(
                            text = "Desmarcar",
                            onClick = { showMenu = false; onAction(item.id, "RESET") },
                            icon = Icons.AutoMirrored.Filled.Undo,
                            iconColor = RoutineTheme.colors.secondary
                        )
                    }
                }
            }
        }
    }
}
