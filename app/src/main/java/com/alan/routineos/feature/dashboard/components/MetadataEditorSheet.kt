package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    onDeleteSchema: () -> Unit,
    onDismiss: () -> Unit
) {
    var localFields by remember(currentSchema) { 
        mutableStateOf(currentSchema?.fields ?: emptyList()) 
    }
    
    // Form State
    var editingFieldId by remember { mutableStateOf<String?>(null) }
    var fieldName by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf(MetadataFieldType.NUMBER) }
    var isRequired by remember { mutableStateOf(false) }
    var isReadOnly by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("") }
    var defaultValue by remember { mutableStateOf("") }

    val resetForm = {
        editingFieldId = null
        fieldName = ""
        fieldType = MetadataFieldType.NUMBER
        isRequired = false
        isReadOnly = false
        unit = ""
        defaultValue = ""
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
                Column {
                    Text(
                        text = "Métricas",
                        style = RoutineTheme.typography.headlineMedium,
                        color = RoutineTheme.colors.onSurface
                    )
                    currentSchema?.let {
                        Text(
                            text = "v${it.schemaVersion}",
                            style = RoutineTheme.typography.labelCaps,
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                    }
                }
                if (currentSchema != null) {
                    IconButton(onClick = onDeleteSchema) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Eliminar todo", tint = RoutineTheme.colors.error)
                    }
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
                        isEditing = field.id == editingFieldId,
                        onEdit = {
                            editingFieldId = it.id
                            fieldName = it.name
                            fieldType = it.type
                            isRequired = it.required
                            isReadOnly = it.isReadOnly
                            unit = it.unit ?: ""
                            defaultValue = it.defaultValue ?: ""
                        },
                        onDelete = { localFields = localFields.filter { it.id != field.id } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Column {
                Text(
                    text = if (editingFieldId == null) "Añadir Campo" else "Editar Campo",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fieldName,
                    onValueChange = { fieldName = it },
                    label = { Text("Nombre del campo") },
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
                        selected = fieldType,
                        onSelected = { fieldType = it },
                        modifier = Modifier.weight(1f)
                    )
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isRequired, onCheckedChange = { isRequired = it })
                            Text("Obligatorio", style = RoutineTheme.typography.labelCaps)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isReadOnly, onCheckedChange = { isReadOnly = it })
                            Text("Contexto", style = RoutineTheme.typography.labelCaps)
                        }
                    }
                }

                if (fieldType == MetadataFieldType.NUMBER || fieldType == MetadataFieldType.TEXT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unidad") },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editingFieldId != null) {
                        OutlinedButton(onClick = resetForm, modifier = Modifier.weight(1f)) {
                            Text("Cancelar")
                        }
                    }
                    Button(
                        onClick = {
                            val newField = MetadataField(
                                id = editingFieldId ?: UUID.randomUUID().toString(),
                                name = fieldName,
                                type = fieldType,
                                required = isRequired,
                                isReadOnly = isReadOnly,
                                unit = unit.ifBlank { null },
                                defaultValue = defaultValue.ifBlank { null }
                            )
                            localFields = if (editingFieldId == null) {
                                localFields + newField
                            } else {
                                localFields.map { if (it.id == editingFieldId) newField else it }
                            }
                            resetForm()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = fieldName.isNotBlank()
                    ) {
                        Icon(if (editingFieldId == null) Icons.Default.Add else Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingFieldId == null) "Añadir" else "Actualizar")
                    }
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
    isEditing: Boolean,
    onEdit: (MetadataField) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(field) },
        colors = CardDefaults.cardColors(
            containerColor = if (isEditing) RoutineTheme.colors.surface3 else RoutineTheme.colors.surface2
        ),
        border = if (isEditing) borderStroke(2.dp, RoutineTheme.colors.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = field.name, style = RoutineTheme.typography.bodyBase, color = RoutineTheme.colors.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = field.type.name, style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.primary)
                    if (field.isReadOnly) {
                        Text(text = "CONTEXTO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.tertiary)
                    }
                    if (field.required) {
                        Text(text = "OBLIGATORIO", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.error)
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
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color) = 
    androidx.compose.foundation.BorderStroke(width, color)

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
