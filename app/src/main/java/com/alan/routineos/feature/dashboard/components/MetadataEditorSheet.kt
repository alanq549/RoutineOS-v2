package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
            Text(
                text = "Métricas",
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface
            )

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
                Text("Nueva Métrica", style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                
                var newFieldName by remember { mutableStateOf("") }
                var newFieldType by remember { mutableStateOf(MetadataFieldType.NUMBER) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newFieldName,
                        onValueChange = { newFieldName = it },
                        placeholder = { Text("Nombre...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    FieldTypeSelector(
                        selected = newFieldType,
                        onSelected = { newFieldType = it }
                    )

                    IconButton(
                        onClick = {
                            if (newFieldName.isNotBlank()) {
                                localFields = localFields + MetadataField(newFieldName, newFieldType)
                                newFieldName = ""
                            }
                        },
                        enabled = newFieldName.isNotBlank() && localFields.none { it.name == newFieldName }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
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
                            fields = localFields
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary)
            ) {
                Text("Guardar Métricas")
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
                Text(text = field.type.name, style = RoutineTheme.typography.labelCaps, color = RoutineTheme.colors.onSurfaceVariant)
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
    onSelected: (MetadataFieldType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MetadataFieldType.values().forEach { type ->
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
