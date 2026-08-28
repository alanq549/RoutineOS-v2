package com.alan.routineos.feature.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.feature.today.model.TimelineTemporalState
import com.alan.routineos.feature.today.model.TodaySubNodeUiModel
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

// Paletas cromáticas para estados temporales y especiales
private val AdHocAccent = Color(0xFFB894E6)
private val OverdueAccent = Color(0xFFFDBA74) // Ámbar sutil para tareas atrasadas

@Composable
fun TimelineItemCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = item.status == DailyInstanceStatus.COMPLETED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isOverdue = item.temporalState == TimelineTemporalState.OVERDUE
    val isStale = item.temporalState == TimelineTemporalState.STALE_PENDING
    val isCurrent = item.temporalState == TimelineTemporalState.CURRENT

    // Dimming: lo completado, omitido, atrasado o estancado se atenúa para "despejar" la vista
    val cardAlpha = if (isOmitted || isCompleted || isOverdue || isStale) 0.65f else 1f

    // Identidad Mesh Glass: Color de acento según tipo y estado temporal
    val accentColor = when {
        isCompleted -> RoutineTheme.colors.primary
        isOverdue -> OverdueAccent
        item.isAdHoc -> AdHocAccent
        isModified -> RoutineTheme.colors.secondary
        else -> RoutineTheme.colors.primary
    }

    // Identidad Mesh Glass: Degradado sutil para tarjetas activas o actuales
    val meshBrush = if (isCurrent || (item.isAdHoc && !isCompleted && !isOmitted)) {
        Brush.radialGradient(
            colors = listOf(
                accentColor.copy(alpha = 0.12f),
                Color.Transparent
            ),
            radius = 600f
        )
    } else {
        SolidColor(RoutineTheme.colors.surface1)
    }

    var showMenu by remember { mutableStateOf(false) }

    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        containerColor = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = when {
                isCompleted -> RoutineTheme.colors.border
                isOverdue -> OverdueAccent.copy(alpha = 0.4f)
                isCurrent -> RoutineTheme.colors.primary.copy(alpha = 0.6f)
                else -> RoutineTheme.colors.border
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth().background(meshBrush)) {
            Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
                // HEADER: Título + Expand + MoreVert
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
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

                    // Botones de control en el Header
                    if (item.isExpandable) {
                        val rotation by animateFloatAsState(if (item.isExpanded) 180f else 0f, label = "rotate")
                        IconButton(onClick = { onExpandClick(item.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Expandir",
                                modifier = Modifier.rotate(rotation),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Acciones secundarias",
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { 
                                    showMenu = false
                                    onAction(item.id, "MOVE_REQUEST") 
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Omitir") },
                                onClick = { 
                                    showMenu = false
                                    onAction(item.id, "SKIP") 
                                },
                                leadingIcon = { Icon(Icons.Default.Block, null) }
                            )
                        }
                    }
                }

                // PROGRESS INDICATOR
                if (item.totalSubNodesCount > 0) {
                    Text(
                        text = "${item.completedSubNodesCount}/${item.totalSubNodesCount} completados",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 26.dp, top = 2.dp)
                    )
                }

                // SUB-HEADER: Hora e Información
                Row(
                    modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = item.timeRangeText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp),
                        color = if (isOverdue) OverdueAccent.copy(alpha = 0.8f) else RoutineTheme.colors.onSurfaceVariant
                    )

                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (isOverdue) {
                        Text(
                            text = "ATRASADA",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = OverdueAccent
                        )
                    }
                }

                // CONFLICT ALERT
                if (item.hasConflict) {
                    Surface(
                        modifier = Modifier.padding(start = 26.dp, top = 8.dp),
                        color = RoutineTheme.colors.error.copy(alpha = 0.1f),
                        shape = RoutineTheme.shapes.small,
                        border = BorderStroke(1.dp, RoutineTheme.colors.error.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, Modifier.size(10.dp), RoutineTheme.colors.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CONFLICTO DETECTADO",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp),
                                color = RoutineTheme.colors.error
                            )
                        }
                    }
                }

                // SUB-NODES SECTION
                AnimatedVisibility(visible = item.isExpanded) {
                    if (item.subNodes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .drawThreadLine(RoutineTheme.colors.border.copy(alpha = 0.3f))
                                .padding(start = 16.dp)
                        ) {
                            item.subNodes.forEach { subNode ->
                                SubNodeRow(subNode, onAction)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                // METADATA SECTION
                if (item.contextMetadata.isNotEmpty() || item.operationalMetadata.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(start = 26.dp, top = 8.dp),
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

                // FOOTER: Acción Principal "COMPLETE"
                if (!isCompleted && !isOmitted) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onAction(item.id, "COMPLETE") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverdue) OverdueAccent.copy(alpha = 0.9f) else accentColor.copy(alpha = 0.9f),
                                contentColor = Color.Black
                            ),
                            shape = RoutineTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                "COMPLETE",
                                style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubNodeRow(subNode: TodaySubNodeUiModel, onAction: (String, String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
            if (subNode.timeText.isNotBlank()) {
                Text(subNode.timeText, style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant)
            }
        }
        if (subNode.status == DailyInstanceStatus.PLANNED) {
            IconButton(onClick = { onAction(subNode.id, "COMPLETE") }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp), RoutineTheme.colors.primary)
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
