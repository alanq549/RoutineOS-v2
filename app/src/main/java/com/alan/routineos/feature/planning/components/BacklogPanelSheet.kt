package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.BacklogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacklogPanelSheet(
    openBacklogItems: List<BacklogItem>,
    onAssignToDay: (BacklogItem) -> Unit,
    onCreateBacklogItem: (String) -> Unit,
    onDeleteBacklogItem: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newTitle by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RoutineTheme.colors.background,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RoutineTheme.spacing.md)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = RoutineTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BOLSA DE PENDIENTES",
                        style = RoutineTheme.typography.labelCaps.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RoutineTheme.colors.onSurface
                    )
                }

                Surface(
                    color = RoutineTheme.colors.surface2,
                    shape = RoutineTheme.shapes.small
                ) {
                    Text(
                        text = "${openBacklogItems.size} ABIERTOS",
                        style = RoutineTheme.typography.labelCaps.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = RoutineTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Create Input
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Nuevo pendiente sin fecha...",
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                trailingIcon = {
                    if (newTitle.isNotBlank()) {
                        IconButton(onClick = {
                            onCreateBacklogItem(newTitle.trim())
                            newTitle = ""
                        }) {
                            Icon(
                                Icons.Default.Add,
                                "Crear pendiente",
                                tint = RoutineTheme.colors.primary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newTitle.isNotBlank()) {
                        onCreateBacklogItem(newTitle.trim())
                        newTitle = ""
                    }
                }),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoutineTheme.colors.primary,
                    unfocusedBorderColor = RoutineTheme.colors.border,
                    focusedContainerColor = RoutineTheme.colors.surface2,
                    unfocusedContainerColor = RoutineTheme.colors.surface1
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Backlog Items List
            if (openBacklogItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay pendientes en el backlog",
                        style = RoutineTheme.typography.bodyBase,
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(openBacklogItems, key = { it.id }) { item ->
                        BacklogItemRow(
                            item = item,
                            onAssign = {
                                onAssignToDay(item)
                                onDismiss()
                            },
                            onDelete = { onDeleteBacklogItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BacklogItemRow(
    item: BacklogItem,
    onAssign: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = RoutineTheme.colors.surface2,
        shape = RoutineTheme.shapes.small,
        border = BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.title,
                style = RoutineTheme.typography.bodyBase.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = RoutineTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button: ASIGNAR A HOY
            Button(
                onClick = onAssign,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutineTheme.colors.primary.copy(alpha = 0.15f),
                    contentColor = RoutineTheme.colors.primary
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    null,
                    modifier = Modifier.size(12.dp),
                    tint = RoutineTheme.colors.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "PLANIFICAR",
                    style = RoutineTheme.typography.labelCaps.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Delete,
                    "Borrar",
                    tint = RoutineTheme.colors.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
