package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.LifeSystem

private val DarkSurface = Color(0xFF0D1017)
private val DarkCard = Color(0xFF171B26)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeSystemEditorSheet(
    system: LifeSystem,
    isCreationMode: Boolean,
    onUpdateFields: (String, String, String) -> Unit,
    onSave: () -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val iconList = listOf(
        "fitness_center" to Icons.Default.FitnessCenter,
        "school" to Icons.Default.School,
        "work" to Icons.Default.Work,
        "favorite" to Icons.Default.Favorite,
        "bedtime" to Icons.Default.Bedtime,
        "rocket" to Icons.Default.RocketLaunch,
        "account_tree" to Icons.Default.AccountTree,
        "science" to Icons.Default.Science,
        "psychology" to Icons.Default.Psychology,
        "palette" to Icons.Default.Palette
    )

    val colorList = listOf(
        "#34D399", // Emerald
        "#818CF8", // Indigo
        "#FBBF24", // Amber
        "#F87171", // Red/Coral
        "#38BDF8", // Sky Blue
        "#A855F7", // Purple
        "#FB7185"  // Rose
    )

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar Sistema?", style = RoutineTheme.typography.headlineMedium) },
            text = { Text("Las actividades vinculadas no se borrarán, solo quedarán sin sistema.") },
            confirmButton = {
                TextButton(onClick = { onDelete(system.id); showDeleteConfirmation = false }) {
                    Text("Eliminar", color = RoutineTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancelar") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RoutineTheme.colors.surface1,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RoutineTheme.spacing.lg)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCreationMode) "NUEVO SISTEMA" else "EDITAR SISTEMA",
                    style = RoutineTheme.typography.labelCaps.copy(letterSpacing = 1.2.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )

                if (!isCreationMode) {
                    IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(RoutineTheme.colors.surface2)
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = RoutineTheme.colors.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TITLE INPUT
            val currentSemanticColor = try { Color(android.graphics.Color.parseColor(system.colorHex)) } catch (e: Exception) { RoutineTheme.colors.primary }
            
            TextField(
                value = system.title,
                onValueChange = { onUpdateFields(it, system.iconKey, system.colorHex) },
                placeholder = { Text("Nombre del sistema...", color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, currentSemanticColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ICON SELECTOR
            SectionHeader("Seleccionar Icono")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                iconList.forEach { (key, icon) ->
                    val isSelected = system.iconKey == key
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) currentSemanticColor.copy(alpha = 0.2f) else DarkSurface)
                            .border(1.dp, if (isSelected) currentSemanticColor else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { onUpdateFields(system.title, key, system.colorHex) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) currentSemanticColor else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // COLOR SELECTOR
            SectionHeader("Color de Identidad")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                colorList.forEach { hex ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = system.colorHex == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = Color.White.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                            .clickable { onUpdateFields(system.title, system.iconKey, hex) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // SAVE BUTTON
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentSemanticColor,
                    contentColor = Color.Black
                ),
                enabled = system.title.isNotBlank()
            ) {
                Text(
                    text = if (isCreationMode) "CREAR SISTEMA" else "GUARDAR CAMBIOS",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 12.dp)
    )
}
