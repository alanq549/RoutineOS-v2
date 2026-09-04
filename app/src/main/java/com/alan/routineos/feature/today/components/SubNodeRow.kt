package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenu
import com.alan.routineos.core.designsystem.component.RoutineDropdownMenuItem
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.*
import com.alan.routineos.feature.today.model.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubNodeRow(subNode: TodaySubNodeUiModel, onAction: (String, String) -> Unit) {
    val isContainer = subNode.children.isNotEmpty()
    val isCompleted = subNode.completion == HierarchyCompletion.COMPLETED
    val isOmitted = subNode.status == DailyInstanceStatus.OMITTED
    var showMenu by remember { mutableStateOf(false) }
    
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subNode.title,
                    style = RoutineTheme.typography.bodyBase.copy(
                        fontSize = 14.sp,
                        textDecoration = if (isOmitted || isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (isOmitted || isCompleted) RoutineTheme.colors.onSurfaceVariant else RoutineTheme.colors.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subNode.timeText.isNotBlank()) Text(subNode.timeText, style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant)
                    if (isContainer) {
                        if (subNode.timeText.isNotBlank()) Spacer(modifier = Modifier.width(8.dp))
                        Text("${subNode.completedCount}/${subNode.totalCount}", style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = if (isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f))
                    }
                }
                
                // Sub-node Metadata
                if (subNode.contextMetadata.isNotEmpty() || subNode.operationalMetadata.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subNode.contextMetadata.forEach { (name, value) ->
                            MetadataLabel(name, value, isContext = true)
                        }
                        subNode.operationalMetadata.forEach { (name, value) ->
                            MetadataLabel(name, value, isContext = false)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isCompleted && !isOmitted && !isContainer) {
                    IconButton(onClick = { onAction(subNode.id, "COMPLETE") }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.RadioButtonUnchecked, null, Modifier.size(18.dp), RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                } else {
                    StatusIcon(status = subNode.status, size = 16.dp, isHierarchyCompleted = isCompleted)
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }
                    RoutineDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (!isCompleted && !isOmitted) {
                            RoutineDropdownMenuItem(
                                text = "Editar",
                                onClick = { showMenu = false; onAction(subNode.id, "MOVE_REQUEST") },
                                icon = Icons.Default.Edit
                            )
                            RoutineDropdownMenuItem(
                                text = "Omitir",
                                onClick = { showMenu = false; onAction(subNode.id, "SKIP") },
                                icon = Icons.Default.Block,
                                iconColor = RoutineTheme.colors.onSurfaceVariant
                            )
                        } else {
                            RoutineDropdownMenuItem(
                                text = "Desmarcar",
                                onClick = { showMenu = false; onAction(subNode.id, "RESET") },
                                icon = Icons.AutoMirrored.Filled.Undo,
                                iconColor = RoutineTheme.colors.secondary
                            )
                        }
                    }
                }
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
