package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme

private val DarkSurface = Color(0xFF0D1017)

@Composable
fun ActivityCreationRoute(
    onBack: () -> Unit,
    viewModel: ActivityCreationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack()
        }
    }

    ActivityCreationScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onSaveClick = viewModel::saveActivity,
        onBackClick = onBack
    )
}

@Composable
fun ActivityCreationScreen(
    uiState: ActivityCreationUiState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    RoutineScaffold(
        topBar = { ActivityCreationTopBar(onBackClick) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = RoutineTheme.spacing.md)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PROTOCOLO DE CONSTRUCCIÓN",
                    style = RoutineTheme.typography.labelCaps.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Black,
                        color = RoutineTheme.colors.primary.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "V2.4",
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
            Text(
                text = "DEFINICIÓN TÉCNICA DE MOLDE",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            ActivityFormFields(
                title = uiState.title,
                onTitleChanged = onTitleChanged,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChanged,
                enabled = !uiState.isSaving
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            ActivitySaveButton(
                onClick = onSaveClick,
                isSaving = uiState.isSaving,
                enabled = uiState.title.isNotBlank()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "GUARDAR COMO BORRADOR SILENCIOSO",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ActivityCreationTopBar(onBackClick: () -> Unit) {
    Box(modifier = Modifier.statusBarsPadding()) {
        RoutineTopBar(
            title = {
                Text(
                    text = "Nueva Actividad",
                    style = RoutineTheme.typography.headlineMedium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }
}

@Composable
fun ActivityFormFields(
    title: String,
    onTitleChanged: (String) -> Unit,
    description: String,
    onDescriptionChanged: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // 01 // SISTEMA
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "01 //",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, color = RoutineTheme.colors.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SISTEMA / DOMINIO PRIMARIO",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mock System Grid (Technical Tiles)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SystemTile("Carrera", "Deep Work", RoutineTheme.colors.secondary, Modifier.weight(1f))
                SystemTile("Salud", "Bio-regulación", RoutineTheme.colors.roleEvent, Modifier.weight(1f))
            }
        }

        // 02 // TÍTULO
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "02 //",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, color = RoutineTheme.colors.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TÍTULO DE LA PLANTILLA",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = title,
                onValueChange = onTitleChanged,
                placeholder = { Text("Gimnasio Hipertrofia // Bloque A", fontSize = 16.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                singleLine = true,
                enabled = enabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = RoutineTheme.typography.bodyBase.copy(fontWeight = FontWeight.Bold)
            )
        }

        // 03 // PROPÓSITO
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "03 //",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Black, color = RoutineTheme.colors.primary.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PROPÓSITO & FUNCIÓN SISTÉMICA",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextField(
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = { Text("Consolidar adaptación neuromuscular...", fontSize = 14.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.3f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                minLines = 3,
                enabled = enabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp)
            )
        }
    }
}

@Composable
private fun SystemTile(title: String, sub: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF141B25),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.School, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = RoutineTheme.typography.labelCaps.copy(fontSize = 12.sp), color = Color.White)
            Text(sub, style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = color.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun ActivitySaveButton(
    onClick: () -> Unit,
    isSaving: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        enabled = enabled && !isSaving,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RoutineTheme.colors.primary,
            contentColor = Color.Black
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "GUARDAR DEFINICIÓN",
                style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ActivityCreationScreenPreview() {
    RoutineTheme {
        ActivityCreationScreen(
            uiState = ActivityCreationUiState(title = "Mi nueva actividad"),
            onTitleChanged = {},
            onDescriptionChanged = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}
