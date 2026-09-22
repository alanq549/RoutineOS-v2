package com.alan.routineos.feature.planning.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenu
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenuItem
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.feature.today.components.drawThreadLine
import com.alan.routineos.feature.today.model.*
import com.alan.routineos.feature.planning.PlanningAccents

@Composable
fun PlanningTimeBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColor = when (item.itemType) {
        PlanningItemType.ACTIVITY -> RoutineTheme.colors.roleEvent
        PlanningItemType.TASK -> RoutineTheme.colors.roleTask
        PlanningItemType.REMINDER -> RoutineTheme.colors.roleReminder
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Vibrant Layered Spine (36dp axis)
                val x = 36.dp.toPx()
                
                // Layer 1: Atmospheric Glow (wider and softer)
                drawLine(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.2f to semanticColor.copy(alpha = 0.05f),
                        0.5f to semanticColor.copy(alpha = 0.15f),
                        0.8f to semanticColor.copy(alpha = 0.05f),
                        1.0f to Color.Transparent
                    ),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 8.dp.toPx()
                )

                // Layer 2: Core Signal (Solid technical line)
                drawLine(
                    brush = Brush.verticalGradient(
                        0.0f to semanticColor.copy(alpha = 0.2f),
                        0.3f to semanticColor.copy(alpha = 0.6f),
                        0.5f to semanticColor.copy(alpha = 0.9f),
                        0.7f to semanticColor.copy(alpha = 0.6f),
                        1.0f to semanticColor.copy(alpha = 0.2f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        // Inner Column handles the actual visual spacing
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            when (item.itemType) {
                PlanningItemType.ACTIVITY -> FullActivityBlock(item, onAction, onExpandClick)
                PlanningItemType.TASK -> CompactTaskBlock(item, onAction)
                PlanningItemType.REMINDER -> TimelineReminderBlock(item, onAction)
            }
        }
    }
}

@Composable
private fun TimelineReminderBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val amberAccent = RoutineTheme.colors.roleReminder

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onAction(item.id, "EDIT_SPONTANEOUS") },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time & Marker (Double Ring with dot)
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.startTimeMinutes?.let { formatMinutes(it) } ?: "",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (isOmitted) RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f) else amberAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.drawBehind {
                    // STITCH BLOOM EFFECT (Circular Diffusion)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(amberAccent.copy(alpha = 0.2f), Color.Transparent),
                            center = center,
                            radius = 50f
                        ),
                        radius = 50f
                    )
                }
            ) {
                Surface(
                    shape = CircleShape,
                    color = RoutineTheme.colors.background,
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (isOmitted) RoutineTheme.colors.border else amberAccent),
                    modifier = Modifier.size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(if (isOmitted) RoutineTheme.colors.onSurfaceVariant else amberAccent, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // REMINDER CARD
        PlanningReminderCard(item = item, onAction = onAction, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FullActivityBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val semanticColor = RoutineTheme.colors.roleEvent

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Time Axis & Marker
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeRangeText,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (isOmitted) RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f) else semanticColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.drawBehind {
                    // STITCH BLOOM EFFECT (Circular Diffusion)
                    // We use drawCircle to ensure no square edges.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(semanticColor.copy(alpha = 0.25f), Color.Transparent),
                            center = center,
                            radius = 60f
                        ),
                        radius = 60f
                    )
                }
            ) {
                // Stitch Activity Marker (Double Ring)
                Surface(
                    shape = CircleShape,
                    color = RoutineTheme.colors.background,
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (isOmitted) RoutineTheme.colors.border else semanticColor),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isOmitted) RoutineTheme.colors.onSurfaceVariant else semanticColor, CircleShape)
                        )
                    }
                }
            }
            
            Text(
                text = "Bloque",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ACTIVITY CARD (Stitch Fidelity 1:1)
        RoutineCard(
            modifier = Modifier.weight(1f).alpha(if (isOmitted) 0.6f else 1f),
            containerColor = Color(0xFF121A24),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isOmitted) RoutineTheme.colors.border else semanticColor.copy(alpha = 0.3f)
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Stitch Glow effect (Atmospheric Blur, not a square)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(140.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(semanticColor.copy(alpha = 0.08f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(x = 400f, y = 0f),
                                radius = 400f
                            )
                        )
                )

                Column {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = semanticColor.copy(alpha = 0.12f), 
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, semanticColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(11.dp), tint = semanticColor)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "RECURRENTE",
                                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                            color = semanticColor
                                        )
                                    }
                                }

                                if (item.isExpandable) {
                                    val rotation by animateFloatAsState(if (item.isExpanded) 180f else 0f, label = "")
                                    IconButton(onClick = { onExpandClick(item.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ExpandMore, null, Modifier.rotate(rotation), RoutineTheme.colors.onSurfaceVariant)
                                    }
                                }
                            }

                            ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.title,
                            style = RoutineTheme.typography.bodyBase.copy(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, letterSpacing = (-0.5).sp),
                            color = Color.White
                        )

                        // Hierarchy Expansion (Indented with Line)
                        AnimatedVisibility(visible = item.isExpanded) {
                            if (item.subNodes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Column(modifier = Modifier.padding(start = 4.dp).drawThreadLine(PlanningAccents.SpineMedium.copy(alpha = 0.4f)).padding(start = 20.dp)) {
                                    item.subNodes.forEach { sub ->
                                        PlanningSubNodeRow(sub)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // STITCH BOTTOM STATUS BAR
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(PlanningAccents.SpineMedium.copy(alpha = 0.6f)))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(4.dp).background(RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${item.totalSubNodesCount} hábitos encadenados",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
                            )
                        }
                        Text(
                            text = "0% completado hoy",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTaskBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val indigoAccent = RoutineTheme.colors.roleTask

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onAction(item.id, "EDIT_SPONTANEOUS") },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time & Marker (Double Ring with dot)
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.startTimeMinutes?.let { formatMinutes(it) } ?: "",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (isOmitted) RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f) else indigoAccent
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.drawBehind {
                    // STITCH BLOOM EFFECT (Circular Diffusion)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(indigoAccent.copy(alpha = 0.2f), Color.Transparent),
                            center = center,
                            radius = 60f
                        ),
                        radius = 60f
                    )
                }
            ) {
                Surface(
                    shape = CircleShape,
                    color = RoutineTheme.colors.background,
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (isOmitted) RoutineTheme.colors.border else indigoAccent),
                    modifier = Modifier.size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(if (isOmitted) RoutineTheme.colors.onSurfaceVariant else indigoAccent, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // TASK CARD (Stitch Indigo)
        RoutineCard(
            modifier = Modifier.weight(1f).alpha(if (isOmitted) 0.7f else 1f),
            containerColor = Color(0xFF121620),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isOmitted) RoutineTheme.colors.border else indigoAccent.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stitch Checkbox Area (Rounded Square)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(1.5.dp, indigoAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .background(indigoAccent.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = indigoAccent)
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.bodyBase.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Puntual de hoy",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, color = indigoAccent, fontWeight = FontWeight.SemiBold)
                    )
                    
                    // Note inside Task (Specialized Compact UI)
                    item.context?.note?.let { note ->
                        Spacer(modifier = Modifier.height(8.dp))
                        var isExpanded by remember { mutableStateOf(false) }
                        val textLayoutResult = remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                        val isOverflowing = textLayoutResult.value?.hasVisualOverflow ?: false

                        Surface(
                            color = RoutineTheme.colors.background.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PlanningAccents.SpineMedium.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isOverflowing) { isExpanded = !isExpanded }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.StickyNote2, 
                                    null, 
                                    modifier = Modifier.size(13.dp).padding(top = 2.dp), 
                                    tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note.content,
                                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis,
                                    onTextLayout = { textLayoutResult.value = it },
                                    modifier = Modifier.weight(1f)
                                )
                                if (isOverflowing || isExpanded) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).padding(start = 4.dp),
                                        tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
                ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
            }
        }
    }
}

@Composable
internal fun ActionMenu(
    item: TodayTimelineUiModel,
    isOmitted: Boolean,
    isModified: Boolean,
    onAction: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant)
        }
        RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            if (!isOmitted) {
                RoutineDropdownMenuItem(
                    text = "Mover",
                    onClick = { showMenu = false; onAction("MOVE_REQUEST") },
                    icon = Icons.Default.Edit
                )
                RoutineDropdownMenuItem(
                    text = "Omitir",
                    onClick = { showMenu = false; onAction("SKIP") },
                    icon = Icons.Default.Block
                )
                if (item.isAdHoc) {
                    RoutineDropdownMenuItem(
                        text = "Editar Estructura",
                        onClick = { showMenu = false; onAction("EDIT_SPONTANEOUS") },
                        icon = Icons.Default.FlashOn
                    )
                    RoutineDropdownMenuItem(
                        text = "Eliminar",
                        onClick = { showMenu = false; onAction("DELETE_INSTANCE") },
                        icon = Icons.Default.Delete,
                        iconColor = RoutineTheme.colors.error
                    )
                }
            }
            if (isOmitted || isModified) {
                RoutineDropdownMenuItem(
                    text = "Revertir",
                    onClick = { showMenu = false; onAction("RESET") },
                    icon = Icons.AutoMirrored.Filled.Undo,
                    iconColor = RoutineTheme.colors.secondary
                )
            }
        }
    }
}

@Composable
private fun ContextIndicators(item: TodayTimelineUiModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (item.context?.tasks?.isNotEmpty() == true) {
            ContextIndicator(icon = Icons.Default.CheckBox, text = "${item.context.tasks.size}")
        }
        if (item.context?.reminder != null) {
            ContextIndicator(icon = Icons.Default.Notifications, text = "")
        }
    }
}

@Composable
private fun ContextIndicator(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(12.dp), tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun PlanningSubNodeRow(sub: TodaySubNodeUiModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.RadioButtonUnchecked, null, Modifier.size(11.dp), RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.width(12.dp))
        Text(sub.title, style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp), color = RoutineTheme.colors.onSurfaceVariant)
        if (sub.timeText.isNotBlank()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(sub.timeText, style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.primary.copy(alpha = 0.7f))
        }
    }
}

internal fun formatMinutes(minutes: Int): String {
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}
