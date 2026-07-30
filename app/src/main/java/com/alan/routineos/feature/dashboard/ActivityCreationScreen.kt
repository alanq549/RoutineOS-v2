package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivityFormFields(
                title = uiState.title,
                onTitleChanged = onTitleChanged,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChanged,
                enabled = !uiState.isSaving
            )
            Spacer(modifier = Modifier.weight(1f))
            ActivitySaveButton(
                onClick = onSaveClick,
                isSaving = uiState.isSaving,
                enabled = uiState.title.isNotBlank()
            )
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChanged,
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = enabled
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = enabled
        )
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
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isSaving,
        shape = RoutineTheme.shapes.medium
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Guardar Actividad")
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
