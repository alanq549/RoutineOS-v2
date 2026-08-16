package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.ScheduleRule
import com.alan.routineos.domain.model.ScheduleRuleType
import com.alan.routineos.domain.model.ScheduleTarget
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingEditorSheet(
    target: ScheduleTarget,
    rules: List<ScheduleRule>,
    errorMessage: String? = null,
    uiEvent: kotlinx.coroutines.flow.SharedFlow<com.alan.routineos.feature.dashboard.ActivityDetailUiEvent>,
    onUpsertRule: (ScheduleRule) -> Unit,
    onDeleteRule: (ScheduleRule) -> Unit,
    onDismiss: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTimeMinutes by remember { mutableIntStateOf(480) } // 08:00
    var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5)) }
    var editingRuleId by remember { mutableStateOf<String?>(null) }
    
    val isDuplicate by remember(selectedTimeMinutes, selectedDays, rules, editingRuleId) {
        derivedStateOf {
            rules.any { existing ->
                existing.id != editingRuleId && 
                existing.startTime == selectedTimeMinutes && 
                existing.daysOfWeek.size == selectedDays.size &&
                existing.daysOfWeek.containsAll(selectedDays)
            }
        }
    }

    val resetForm = {
        selectedTimeMinutes = 480
        selectedDays = setOf(1, 2, 3, 4, 5)
        editingRuleId = null
    }

    LaunchedEffect(uiEvent) {
        uiEvent.collect { event ->
            if (event is com.alan.routineos.feature.dashboard.ActivityDetailUiEvent.SchedulingUpsertSuccess) {
                resetForm()
            }
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTimeMinutes / 60,
            initialMinute = selectedTimeMinutes % 60
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTimeMinutes = timePickerState.hour * 60 + timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RoutineTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Horarios",
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(rules) { rule ->
                    RuleItem(
                        rule = rule,
                        onEdit = {
                            selectedTimeMinutes = it.startTime ?: 480
                            selectedDays = it.daysOfWeek
                            editingRuleId = it.id
                        },
                        onDelete = onDeleteRule
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Text(
                    text = if (editingRuleId == null) "Nuevo Horario" else "Editar Horario",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true }
                        .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.small)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora de inicio", style = RoutineTheme.typography.bodyBase)
                    Text(
                        text = formatTime(selectedTimeMinutes),
                        style = RoutineTheme.typography.dataLarge,
                        color = RoutineTheme.colors.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..7).forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.surface2,
                                    shape = RoutineTheme.shapes.small
                                )
                                .clickable {
                                    selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = listOf("L", "M", "M", "J", "V", "S", "D")[day - 1],
                                color = if (isSelected) RoutineTheme.colors.onPrimary else RoutineTheme.colors.onSurfaceVariant,
                                style = RoutineTheme.typography.labelCaps
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editingRuleId != null) {
                        OutlinedButton(
                            onClick = { resetForm() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        val finalError = errorMessage ?: if (isDuplicate) "Horario duplicado" else null
                        if (finalError != null) {
                            Text(
                                text = finalError,
                                style = RoutineTheme.typography.labelCaps,
                                color = RoutineTheme.colors.error,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (selectedDays.isNotEmpty() && !isDuplicate) {
                                    onUpsertRule(
                                        ScheduleRule(
                                            id = editingRuleId ?: UUID.randomUUID().toString(),
                                            target = target,
                                            type = ScheduleRuleType.FIXED_DAYS,
                                            daysOfWeek = selectedDays,
                                            startTime = selectedTimeMinutes
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDuplicate || errorMessage != null) RoutineTheme.colors.error.copy(alpha = 0.5f) else RoutineTheme.colors.primary
                            ),
                            enabled = selectedDays.isNotEmpty() && !isDuplicate
                        ) {
                            Icon(if (editingRuleId == null) Icons.Default.Add else Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (editingRuleId == null) "Añadir" else "Guardar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    rule: ScheduleRule,
    onEdit: (ScheduleRule) -> Unit,
    onDelete: (ScheduleRule) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(rule) },
        colors = CardDefaults.cardColors(containerColor = RoutineTheme.colors.surface2)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTime(rule.startTime ?: 0),
                    style = RoutineTheme.typography.dataLarge,
                    color = RoutineTheme.colors.primary
                )
                Text(
                    text = formatDays(rule.daysOfWeek),
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDelete(rule) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoutineTheme.colors.error)
            }
        }
    }
}

private fun formatTime(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}

private fun formatDays(days: Set<Int>): String {
    if (days.size == 7) return "Todos los días"
    val names = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    // LocalDate DayOfWeek is 1-7 (Mon-Sun)
    // Calendar is 1-7 (Sun-Sat)
    // Our model uses 1-7 (Mon-Sun) as defined in GetActivityTreeUseCase or similar
    // Let's assume 1=Mon, 7=Sun
    return days.sorted().joinToString(", ") { names[it - 1] }
}
