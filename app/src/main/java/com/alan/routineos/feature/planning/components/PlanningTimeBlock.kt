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
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.TemporalImpact
import com.alan.routineos.feature.today.components.drawThreadLine
import com.alan.routineos.feature.today.model.TodaySubNodeUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

@Composable
fun PlanningTimeBlock(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    var showMenu by remember { mutableStateOf(false) }

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
        RoutineCard(
            modifier = Modifier.weight(1f).alpha(if (isOmitted) 0.6f else 1f),
            containerColor = RoutineTheme.colors.surface3,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = when {
                    isOmitted -> RoutineTheme.colors.border
                    isModified -> RoutineTheme.colors.secondary.copy(alpha = 0.5f)
                    item.isAdHoc -> Color(0xFFB894E6).copy(alpha = 0.5f)
                    else -> RoutineTheme.colors.border
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
                            else -> RoutineTheme.colors.primary.copy(alpha = 0.1f)
                        }
                        val textColor = when {
                            isOmitted -> RoutineTheme.colors.onSurfaceVariant
                            isModified -> RoutineTheme.colors.secondary
                            else -> RoutineTheme.colors.primary
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

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant)
                        }
                        RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            if (!isOmitted) {
                                RoutineDropdownMenuItem(
                                    text = "Mover",
                                    onClick = { showMenu = false; onAction(item.id, "MOVE_REQUEST") },
                                    icon = Icons.Default.Edit
                                )
                                RoutineDropdownMenuItem(
                                    text = "Omitir",
                                    onClick = { showMenu = false; onAction(item.id, "SKIP") },
                                    icon = Icons.Default.Block
                                )
                                if (item.isAdHoc) {
                                    RoutineDropdownMenuItem(
                                        text = "Editar Estructura",
                                        onClick = { showMenu = false; onAction(item.id, "EDIT_SPONTANEOUS") },
                                        icon = Icons.Default.FlashOn
                                    )
                                    RoutineDropdownMenuItem(
                                        text = "Eliminar",
                                        onClick = { showMenu = false; onAction(item.id, "DELETE_INSTANCE") },
                                        icon = Icons.Default.Delete,
                                        iconColor = RoutineTheme.colors.error
                                    )
                                }
                            }
                            if (isOmitted || isModified) {
                                RoutineDropdownMenuItem(
                                    text = "Revertir",
                                    onClick = { showMenu = false; onAction(item.id, "RESET") },
                                    icon = Icons.AutoMirrored.Filled.Undo,
                                    iconColor = RoutineTheme.colors.secondary
                                )
                            }
                        }
                    }
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
