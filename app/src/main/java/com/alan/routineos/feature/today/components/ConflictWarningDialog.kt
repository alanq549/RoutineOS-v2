package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.PendingMove

@Composable
fun ConflictWarningDialog(
    pendingMove: PendingMove,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onChooseOther: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, null, tint = RoutineTheme.colors.error) },
        title = {
            Text(
                "Conflicto Detectado",
                style = RoutineTheme.typography.headlineMedium.copy(fontSize = 20.sp)
            )
        },
        text = {
            Column {
                Text(
                    "El nuevo horario de \"${pendingMove.entry.root.instance.titleSnapshot}\" solapa con otras actividades planificadas.",
                    style = RoutineTheme.typography.bodyBase
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "¿Deseas mantener este horario de todos modos?",
                    style = RoutineTheme.typography.bodyBase.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.error)
            ) {
                Text("Mantener de todos modos")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                TextButton(onClick = onChooseOther) {
                    Text("Cambiar horario")
                }
            }
        }
    )
}
