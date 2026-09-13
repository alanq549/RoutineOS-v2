package com.alan.routineos.feature.planning.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenu
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenuItem
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.ActionProtocol
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.TemporalImpact
import com.alan.routineos.feature.today.components.drawThreadLine
import com.alan.routineos.feature.today.model.TodaySubNodeUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

import com.alan.routineos.domain.model.*
import com.alan.routineos.feature.today.components.drawThreadLine
import com.alan.routineos.feature.today.model.*

@Composable
fun PlanningTimeBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (item.itemType) {
        PlanningItemType.ACTIVITY -> FullActivityBlock(item, onAction, onExpandClick, modifier)
        PlanningItemType.TASK -> CompactTaskBlock(item, onAction, modifier)
        PlanningItemType.REMINDER -> LightweightReminderBlock(item, onAction, modifier)
    }
}

@Composable
private fun FullActivityBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Axis
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeRangeText,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                color = if (isOmitted) RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f) else RoutineTheme.colors.primary
            )
            
            val isBlock = item.endTimeMinutes != null
            
            if (isBlock) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(1.dp)
                        .height(48.dp)
                        .background(RoutineTheme.colors.primary.copy(alpha = 0.3f))
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            Icon(
                imageVector = if (item.isAdHoc) Icons.Default.FlashOn else if (isBlock) Icons.Default.Inventory2 else Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = if (isBlock) RoutineTheme.colors.primary else RoutineTheme.colors.secondary,
                modifier = Modifier.size(if (isBlock) 14.dp else 12.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Content Card
        val protocolColor = RoutineTheme.colors.roleEvent

        RoutineCard(
            modifier = Modifier.weight(1f).alpha(if (isOmitted) 0.6f else 1f),
            containerColor = RoutineTheme.colors.surface3,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when {
                    isOmitted -> RoutineTheme.colors.border
                    isModified -> RoutineTheme.colors.secondary.copy(alpha = 0.5f)
                    else -> protocolColor.copy(alpha = 0.4f)
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val badgeColor = when {
                            isOmitted -> RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.2f)
                            isModified -> RoutineTheme.colors.secondary.copy(alpha = 0.1f)
                            else -> protocolColor.copy(alpha = 0.15f)
                        }
                        val textColor = when {
                            isOmitted -> RoutineTheme.colors.onSurfaceVariant
                            isModified -> RoutineTheme.colors.secondary
                            else -> protocolColor
                        }

                        Surface(color = badgeColor, shape = RoutineTheme.shapes.pill) {
                            Text(
                                text = if (isOmitted) "SALTADO" else if (isModified) "AJUSTADO" else "RECURRENTE",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (item.isExpandable) {
                            val rotation by animateFloatAsState(if (item.isExpanded) 180f else 0f, label = "")
                            IconButton(onClick = { onExpandClick(item.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.ExpandMore, null, Modifier.rotate(rotation), RoutineTheme.colors.onSurfaceVariant)
                            }
                        }
                    }

                    ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase.copy(fontWeight = FontWeight.Bold),
                    color = RoutineTheme.colors.onSurface
                )
                
                if (item.conflict.hasConflict && !isOmitted) {
                    Text(
                        text = "Conflicto detectado",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                        color = RoutineTheme.colors.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // CONTEXT INDICATORS
                if (item.context != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ContextIndicators(item)
                }

                AnimatedVisibility(visible = item.isExpanded) {
                    if (item.subNodes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 4.dp).drawThreadLine(RoutineTheme.colors.border.copy(alpha = 0.2f)).padding(start = 12.dp)) {
                            item.subNodes.forEach { sub ->
                                PlanningSubNodeRow(sub)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactTaskBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val accentColor = RoutineTheme.colors.roleTask

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Axis (Mini)
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.startTimeMinutes?.let { formatMinutes(it) } ?: "",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        RoutineCard(
            modifier = Modifier.weight(1f),
            containerColor = RoutineTheme.colors.surface2.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.TaskAlt, null, modifier = Modifier.size(16.dp), tint = accentColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                        color = RoutineTheme.colors.onSurface
                    )
                }
                ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
            }
        }
    }
}

@Composable
private fun LightweightReminderBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val accentColor = RoutineTheme.colors.roleReminder

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.startTimeMinutes?.let { formatMinutes(it) } ?: "",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                color = accentColor.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier.weight(1f),
            color = accentColor.copy(alpha = 0.05f),
            shape = RoutineTheme.shapes.small,
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(14.dp), tint = accentColor)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                    color = RoutineTheme.colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
            }
        }
    }
}

@Composable
private fun ActionMenu(
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (item.context?.tasks?.isNotEmpty() == true) {
            ContextIndicator(
                icon = Icons.Default.CheckBox,
                text = "${item.context.tasks.size}"
            )
        }
        if (item.context?.reminder != null) {
            ContextIndicator(
                icon = Icons.Default.Notifications,
                text = ""
            )
        }
        if (item.context?.note != null) {
            ContextIndicator(
                icon = Icons.Default.Description,
                text = ""
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    return "%02d:%02d".format(minutes / 60, minutes % 60)
}

@Composable
private fun ContextIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
        )
        if (text.isNotBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun PlanningSubNodeRow(sub: TodaySubNodeUiModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.RadioButtonUnchecked, null, Modifier.size(10.dp), RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = sub.title,
            style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
        if (sub.timeText.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = sub.timeText,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                color = RoutineTheme.colors.primary.copy(alpha = 0.6f)
            )
        }
    }
}
