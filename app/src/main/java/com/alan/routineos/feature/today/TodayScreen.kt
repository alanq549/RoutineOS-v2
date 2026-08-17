package com.alan.routineos.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.ActivityDetailUiEvent
import com.alan.routineos.feature.today.components.CaptureMetadataSheet
import com.alan.routineos.feature.today.components.TodayHeader
import com.alan.routineos.feature.today.components.TodayNextActivityCard
import com.alan.routineos.feature.today.components.TodayTimeline
import kotlinx.coroutines.flow.SharedFlow
import android.widget.Toast

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onAction: (String, String) -> Unit,
    onMetadataCaptured: (String, String) -> Unit,
    onCloseCapture: () -> Unit,
    onAddAdHoc: (String) -> Unit,
    uiEvent: SharedFlow<ActivityDetailUiEvent>,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAdHocDialog by remember { mutableStateOf(false) }

    if (showAdHocDialog) {
        AdHocDialog(
            onConfirm = { 
                onAddAdHoc(it)
                showAdHocDialog = false
            },
            onDismiss = { showAdHocDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            if (event is ActivityDetailUiEvent.SchedulingUpsertSuccess) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    RoutineScaffold(
        modifier = modifier,
        topBar = { /* Header handles title */ },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RoutineTheme.spacing.marginMobile),
                verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
            ) {
                TodayHeader(
                    dateText = uiState.dateText,
                    progress = uiState.progress
                )

                TodayNextActivityCard(activity = uiState.nextActivity)

                TodayTimeline(items = uiState.timelineItems, onAction = onAction)
                
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))
            }

            FloatingActionButton(
                onClick = { showAdHocDialog = true },
                containerColor = RoutineTheme.colors.primary,
                contentColor = RoutineTheme.colors.onPrimary,
                shape = RoutineTheme.shapes.pill,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(RoutineTheme.spacing.lg)
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add")
            }
        }

        if (uiState.captureSchema != null && uiState.captureTargetId != null) {
            CaptureMetadataSheet(
                schema = uiState.captureSchema,
                onCaptured = { json -> onMetadataCaptured(uiState.captureTargetId, json) },
                onDismiss = onCloseCapture
            )
        }
    }
}

@Composable
private fun AdHocDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Qué quieres hacer ahora?") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Ej: Leer artículo, Meditar...") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
