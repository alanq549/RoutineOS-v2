package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.MetadataField
import com.alan.routineos.domain.model.MetadataFieldType
import com.alan.routineos.domain.model.MetadataSchema
import kotlinx.serialization.json.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureMetadataSheet(
    schema: MetadataSchema,
    onCaptured: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val operationalFields = remember(schema) { 
        schema.fields.filter { !it.isReadOnly }
    }
    
    val capturedValues = remember { mutableStateMapOf<String, String>() }
    
    // Initialize with default values
    LaunchedEffect(schema) {
        schema.fields.forEach { field ->
            if (field.defaultValue != null) {
                capturedValues[field.id] = field.defaultValue!!
            }
        }
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
                text = "Registrar Datos",
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface
            )
            Text(
                text = "Carga de ${operationalFields.size} métricas",
                style = RoutineTheme.typography.labelCaps,
                color = RoutineTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Find numeric fields to potentially group them
                val fields = operationalFields
                var i = 0
                while (i < fields.size) {
                    val field = fields[i]
                    if (field.type == MetadataFieldType.NUMBER && i + 1 < fields.size && fields[i + 1].type == MetadataFieldType.NUMBER) {
                        // Group two numeric fields side-by-side
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    DynamicField(field, capturedValues[field.id] ?: "", { capturedValues[field.id] = it })
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    val nextField = fields[i + 1]
                                    DynamicField(nextField, capturedValues[nextField.id] ?: "", { capturedValues[nextField.id] = it })
                                }
                            }
                        }
                        i += 2
                    } else {
                        item {
                            DynamicField(field, capturedValues[field.id] ?: "", { capturedValues[field.id] = it })
                        }
                        i++
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isCompleteEnabled = operationalFields.all { !it.required || capturedValues[it.id]?.isNotBlank() == true }

            Button(
                onClick = {
                    val resultJson = buildJsonObject {
                        put("schemaVersion", schema.schemaVersion)
                        put("values", buildJsonObject {
                            capturedValues.forEach { (id, value) ->
                                put(id, value)
                            }
                        })
                    }.toString()
                    onCaptured(resultJson)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isCompleteEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary)
            ) {
                Text("Confirmar y Completar")
            }
        }
    }
}

@Composable
private fun DynamicField(
    field: MetadataField,
    currentValue: String,
    onValueChanged: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = field.name,
                style = RoutineTheme.typography.bodyBase,
                fontWeight = FontWeight.Bold
            )
            if (field.required) {
                Text(text = " *", color = RoutineTheme.colors.error)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        when (field.type) {
            MetadataFieldType.NUMBER -> {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChanged,
                    modifier = Modifier.fillMaxWidth(),
                    suffix = { field.unit?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            MetadataFieldType.TEXT -> {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
            MetadataFieldType.BOOLEAN -> {
                val checked = currentValue.toBoolean()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = checked, onCheckedChange = { onValueChanged(it.toString()) })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (checked) "Sí" else "No", style = RoutineTheme.typography.bodyBase)
                }
            }
            MetadataFieldType.SELECT -> {
                // Simplified selection: list of chips or buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    field.options?.forEach { option ->
                        val isSelected = currentValue == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { onValueChanged(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }
        }
    }
}
