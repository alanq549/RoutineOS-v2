package com.alan.routineos.feature.today.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenu
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenuItem
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.HierarchyCompletion
import com.alan.routineos.domain.model.TemporalImpact
import com.alan.routineos.feature.today.model.*

@Composable
fun InterceptionContainer(
    victim: TodayTimelineUiModel,
    interception: InterceptionUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val impactColor = when (victim.conflict.impact) {
        TemporalImpact.WARNING -> RoutineTheme.colors.error
        TemporalImpact.INFO -> RoutineTheme.colors.secondary
        else -> RoutineTheme.colors.primary
    }

    // Combined and sorted list of elements
    val combinedContent = remember(victim.subNodes, interception.interrupter) {
        val list = mutableListOf<Any>()
        list.addAll(victim.subNodes)
        list.add(interception.interrupter)
        list.sortedBy { 
            when (it) {
                is TodaySubNodeUiModel -> it.startTimeMinutes ?: 0
                is TodayTimelineUiModel -> it.startTimeMinutes ?: 0
                else -> 0
            }
        }
    }

    val isCompleted = victim.completion == HierarchyCompletion.COMPLETED
    val isOmitted = victim.status == DailyInstanceStatus.OMITTED
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(impactColor.copy(alpha = 0.03f), RoutineTheme.shapes.medium)
            .padding(top = 12.dp, bottom = 12.dp, end = 8.dp)
            .drawBehind {
                val railX = 22.dp.toPx()
                drawLine(
                    color = impactColor.copy(alpha = 0.4f),
                    start = Offset(railX, 38.dp.toPx()),
                    end = Offset(railX, size.height - 30.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }
    ) {
        InterceptionHeader(impactColor)
        Spacer(modifier = Modifier.height(12.dp))
        
        // ROOT HEADER (Victim)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(32.dp)
                    .background(impactColor.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACTIVIDAD PROGRAMADA",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                    color = impactColor
                )
                Text(
                    text = victim.title,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    color = RoutineTheme.colors.onSurface
                )
                if (victim.timeRangeText.isNotBlank()) {
                    Text(
                        text = victim.timeRangeText,
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
            if (victim.isExpandable) {
                val rotation by animateFloatAsState(if (victim.isExpanded) 180f else 0f, label = "victimExpand")
                IconButton(onClick = { onExpandClick(victim.id) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ExpandMore, "Mostrar subpasos", Modifier.rotate(rotation), RoutineTheme.colors.onSurfaceVariant)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (!isCompleted && !isOmitted) {
                        RoutineDropdownMenuItem(
                            text = "Editar",
                            onClick = { showMenu = false; onAction(victim.id, "MOVE_REQUEST") },
                            icon = Icons.Default.Edit
                        )
                        RoutineDropdownMenuItem(
                            text = "Omitir",
                            onClick = { showMenu = false; onAction(victim.id, "SKIP") },
                            icon = Icons.Default.Block,
                            iconColor = RoutineTheme.colors.onSurfaceVariant
                        )
                    } else {
                        RoutineDropdownMenuItem(
                            text = "Desmarcar",
                            onClick = { showMenu = false; onAction(victim.id, "RESET") },
                            icon = Icons.AutoMirrored.Filled.Undo,
                            iconColor = RoutineTheme.colors.secondary
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = victim.isExpanded) {
            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .drawThreadLine(impactColor.copy(alpha = 0.35f))
                    .padding(start = 14.dp)
            ) {
                combinedContent.forEach { item ->
                    when (item) {
                        is TodaySubNodeUiModel -> {
                            SubNodeRow(item, onAction)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        is TodayTimelineUiModel -> {
                            InterceptionTransition("EVENTO SIMULTÁNEO", impactColor)
                            NormalTimelineCard(
                                item = item,
                                onAction = onAction,
                                onExpandClick = onExpandClick,
                                style = TimelineCardStyle.MINIMAL
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InterceptionHeader(color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EVENTO SUPERPUESTO",
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tu actividad programada sigue intacta",
            style = RoutineTheme.typography.bodyBase.copy(fontSize = 11.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun InterceptionTransition(label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(0.65f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
