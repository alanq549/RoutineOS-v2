package com.alan.routineos.feature.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
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

    when (item.itemType) {
        PlanningItemType.ACTIVITY -> FullActivityCard(item, onAction, onExpandClick, modifier)
        PlanningItemType.TASK -> CompactTaskCard(item, onAction, modifier)
        PlanningItemType.REMINDER -> LightweightReminderCard(item, onAction, modifier)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FullActivityCard(
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

    val semanticColor = when (item.itemType) {
        PlanningItemType.ACTIVITY -> RoutineTheme.colors.roleEvent
        PlanningItemType.TASK -> RoutineTheme.colors.roleTask
        PlanningItemType.REMINDER -> RoutineTheme.colors.roleReminder
    }

    val cardAlpha = if (isOmitted || isCompleted || isOverdue || isStale) 0.65f else 1f

    val impactColor = when (item.conflict.impact) {
        TemporalImpact.WARNING -> RoutineTheme.colors.error
        TemporalImpact.INFO -> RoutineTheme.colors.secondary
        else -> null
    }

    val meshBrush = if (!isCompleted && (isCurrent || (item.isAdHoc && !isOmitted))) {
        Brush.radialGradient(
            colors = listOf(semanticColor.copy(alpha = 0.12f), Color.Transparent),
            radius = 600f
        )
    } else {
        SolidColor(RoutineTheme.colors.surface1)
    }

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
                isInterrupter && impactColor != null -> impactColor
                else -> semanticColor.copy(alpha = 0.35f)
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
                            tint = if (isOverdue) OverdueAccent else semanticColor,
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

                    // Quick Delete for Spontaneous (Ad-Hoc)
                    if (item.isAdHoc && !isCompleted && !isOmitted) {
                        IconButton(onClick = { onAction(item.id, "DELETE_INSTANCE") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteOutline, "Borrar", tint = RoutineTheme.colors.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }

                    ActionMenu(item, isCompleted, isOmitted) { id, action -> onAction(id, action) }
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

                // CONTEXT FOOTER (Associated Items)
                if (item.context != null) {
                    ContextFooter(item, isCompleted, isOmitted, onAction)
                }

                // FOOTER: Action "COMPLETE"
                if (!isCompleted && !isOmitted && !isContainer) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { onAction(item.id, "COMPLETE") },
                            colors = ButtonDefaults.buttonColors(containerColor = semanticColor.copy(alpha = 0.9f), contentColor = Color.Black),
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
private fun CompactTaskCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.status == DailyInstanceStatus.COMPLETED
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val indigoAccent = RoutineTheme.colors.roleTask
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Vibrant Layered Spine (Today axis: 22dp)
                val x = 22.dp.toPx() 

                // Layer 1: Atmospheric Glow
                drawLine(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.2f to indigoAccent.copy(alpha = 0.05f),
                        0.5f to indigoAccent.copy(alpha = 0.15f),
                        0.8f to indigoAccent.copy(alpha = 0.05f),
                        1.0f to Color.Transparent
                    ),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 8.dp.toPx()
                )

                // Layer 2: Core Signal
                drawLine(
                    brush = Brush.verticalGradient(
                        0.0f to indigoAccent.copy(alpha = 0.2f),
                        0.3f to indigoAccent.copy(alpha = 0.6f),
                        0.5f to indigoAccent.copy(alpha = 0.9f),
                        0.7f to indigoAccent.copy(alpha = 0.6f),
                        1.0f to indigoAccent.copy(alpha = 0.2f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
    ) {
        RoutineCard(
            modifier = Modifier.fillMaxWidth().padding(start = 40.dp).alpha(if (isOmitted) 0.65f else 1f),
            containerColor = Color.Transparent,
            border = BorderStroke(1.dp, indigoAccent.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onAction(item.id, if (isCompleted) "RESET" else "COMPLETE") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCompleted) RoutineTheme.colors.primary else indigoAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.bodyBase.copy(
                            fontSize = 16.sp, 
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (isCompleted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface
                    )
                    if (item.timeRangeText.isNotBlank()) {
                        Text(
                            text = item.timeRangeText,
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Note inside Task (Specialized Compact UI)
                    item.context?.note?.let { note ->
                        Spacer(modifier = Modifier.height(6.dp))
                        var isExpanded by remember { mutableStateOf(false) }
                        val textLayoutResult = remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                        val isOverflowing = textLayoutResult.value?.hasVisualOverflow ?: false

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isOverflowing) { isExpanded = !isExpanded },
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.StickyNote2, 
                                null, 
                                modifier = Modifier.size(13.dp).padding(top = 2.dp), 
                                tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = note.content,
                                style = RoutineTheme.typography.bodyBase.copy(fontSize = 12.sp),
                                color = RoutineTheme.colors.onSurfaceVariant,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                onTextLayout = { textLayoutResult.value = it },
                                modifier = Modifier.weight(1f)
                            )
                            if (isOverflowing || isExpanded) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
            ActionMenu(item, isCompleted, isOmitted) { id, action -> onAction(id, action) }
        }
    }
}

@Composable
private fun LightweightReminderCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val amberAccent = RoutineTheme.colors.roleReminder

    Surface(
        modifier = modifier.fillMaxWidth().alpha(if (isOmitted) 0.65f else 1f).clickable { onAction(item.id, "EDIT_SPONTANEOUS") },
        color = Color(0xFF141B25),
        shape = RoutineTheme.shapes.small,
        border = BorderStroke(1.dp, if (isOmitted) RoutineTheme.colors.border else amberAccent.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(amberAccent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, amberAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, null, tint = amberAccent, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = amberAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "RECORDATORIO",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, color = amberAccent, fontWeight = FontWeight.Black),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    if (item.timeRangeText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.timeRangeText,
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            ActionMenu(item, false, isOmitted) { id, action -> onAction(id, action) }
        }
    }
}

@Composable
private fun ActionMenu(
    item: TodayTimelineUiModel,
    isCompleted: Boolean,
    isOmitted: Boolean,
    onAction: (String, String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.MoreVert, "Acciones", tint = RoutineTheme.colors.onSurfaceVariant)
        }
        RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            if (!isCompleted && !isOmitted) {
                RoutineDropdownMenuItem(
                    text = "Editar",
                    onClick = { 
                        showMenu = false
                        if (item.isAdHoc) onAction(item.id, "EDIT_SPONTANEOUS")
                        else onAction(item.id, "MOVE_REQUEST") 
                    },
                    icon = Icons.Default.Edit
                )
                if (item.isAdHoc) {
                    RoutineDropdownMenuItem(
                        text = "Borrar",
                        onClick = { showMenu = false; onAction(item.id, "DELETE_INSTANCE") },
                        icon = Icons.Default.Delete,
                        iconColor = RoutineTheme.colors.error
                    )
                }
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

@Composable
private fun ContextFooter(
    item: TodayTimelineUiModel,
    isCompleted: Boolean,
    isOmitted: Boolean,
    onAction: (String, String) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    HorizontalDivider(
        modifier = Modifier.padding(start = 26.dp),
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.15f)
    )
    Spacer(modifier = Modifier.height(12.dp))
    
    Column(modifier = Modifier.padding(start = 26.dp)) {
        // Associated Tasks
        item.context?.tasks?.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(!isCompleted && !isOmitted) { 
                            onAction(task.id, if (task.isCompleted) "RESET" else "COMPLETE") 
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = task.title,
                    style = RoutineTheme.typography.bodyBase.copy(
                        fontSize = 13.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (task.isCompleted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface
                )
            }
        }

        // Reminder Row (Note removed here, only for Tasks)
        if (item.context?.reminder != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                item.context.reminder.let { reminder ->
                    AssistChip(
                        onClick = { },
                        label = { Text(reminder.formattedTime, fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.Notifications, null, Modifier.size(10.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = RoutineTheme.colors.secondary.copy(alpha = 0.1f),
                            labelColor = RoutineTheme.colors.secondary
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )
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
    val color = if (item.itemType == PlanningItemType.REMINDER) RoutineTheme.colors.roleReminder else AdHocAccent
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
                imageVector = if (item.itemType == PlanningItemType.REMINDER) Icons.Default.Notifications else Icons.Default.FlashOn,
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
                            onClick = { 
                                showMenu = false
                                if (item.isAdHoc) onAction(item.id, "EDIT_SPONTANEOUS")
                                else onAction(item.id, "MOVE_REQUEST") 
                            },
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
