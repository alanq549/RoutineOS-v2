package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.MetadataField
import com.alan.routineos.domain.model.MetadataFieldType
import com.alan.routineos.domain.model.MetadataSchema
import com.alan.routineos.domain.model.ScheduleTarget
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataEditorSheet(
    target: ScheduleTarget,
    currentSchema: MetadataSchema?,
    errorMessage: String? = null,
    onUpsertSchema: (MetadataSchema) -> Unit,
    onDismiss: () -> Unit
) {
    var localFields by remember(currentSchema) { 
        mutableStateOf(currentSchema?.fields ?: emptyList()) 
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Métricas",
                    style = RoutineTheme.typography.headlineMedium,
                    color = RoutineTheme.colors.onSurface
                )
                if (currentSchema != null) {
                    Text(
                        text = "v${currentSchema.schemaVersion}",
                        style = RoutineTheme.typography.labelCaps,
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(localFields) { field ->
                    MetadataFieldItem(
                        field = field,
                        onDelete = { localFields = localFields - field }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Column {
                Text("Añadir Campo de Captura", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                var newFieldName by remember { mutableStateOf("") }
                var newFieldType by remember { mutableStateOf(MetadataFieldType.NUMBER) }
                var isRequired by remember { mutableStateOf(false) }
                var unit by remember { mutableStateOf("") }
                var defaultValue by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    label = { Text("Nombre del campo (ej: Peso, Instructor)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FieldTypeSelector(
                        selected = newFieldType,
                        onSelected = { newFieldType = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isRequired, onCheckedChange = { isRequired = it })
                        Text("Obligatorio", style = RoutineTheme.typography.labelCaps)
                    }
                }

                if (newFieldType == MetadataFieldType.NUMBER || newFieldType == MetadataFieldType.TEXT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unidad (ej: kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = defaultValue,
                            onValueChange = { defaultValue = it },
                            label = { Text("Valor inicial") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newFieldName.isNotBlank()) {
                            localFields = localFields + MetadataField(
                                id = UUID.randomUUID().toString(),
                                name = newFieldName,
                                type = newFieldType,
                                required = isRequired,
                                unit = unit.ifBlank { null },
                                defaultValue = defaultValue.ifBlank { null }
                            )
                            // Reset local add form
                            newFieldName = ""
                            unit = ""
                            defaultValue = ""
                            isRequired = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newFieldName.isNotBlank() && localFields.none { it.name.lowercase() == newFieldName.lowercase() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir Campo")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = RoutineTheme.colors.error,
                    style = RoutineTheme.typography.labelCaps,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    onUpsertSchema(
                        MetadataSchema(
                            id = currentSchema?.id ?: UUID.randomUUID().toString(),
                            target = target,
                            fields = localFields,
                            schemaVersion = currentSchema?.schemaVersion ?: 1
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary)
            ) {
                Text("Guardar Plantilla")
            }
        }
    }
}

@Composable
private fun MetadataFieldItem(
    field: MetadataField,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RoutineTheme.colors.surface2)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = field.name, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = field.type.name, style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.primary)
                    if (field.required) {
                        Text(text = "OBLIGATORIO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.error)
                    }
                    field.unit?.let {
                        Text(text = "UNIDAD: $it", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoutineTheme.colors.error)
            }
        }
    }
}

@Composable
private fun FieldTypeSelector(
    selected: MetadataFieldType,
    onSelected: (MetadataFieldType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MetadataFieldType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.name) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
