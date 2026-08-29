package com.alan.routineos.feature.today

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.ActivityDetailUiEvent
import com.alan.routineos.feature.today.components.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    onMetadataCaptured: (String, String) -> Unit,
    onCloseCapture: () -> Unit,
    onAddAdHoc: (String, Int?) -> Unit,
    uiEvent: SharedFlow<ActivityDetailUiEvent>,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showQuickAdd by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var userScrollDetected by remember { mutableStateOf(false) }
    var lastScrolledFocusId by remember { mutableStateOf<String?>(null) }
    
    var moveTargetId by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState()

    // Auto-scroll logic: only on first load of a focus item or explicit "Now" click
    LaunchedEffect(uiState.focusItemId) {
        if (!userScrollDetected && uiState.focusItemId != null && uiState.focusItemId != lastScrolledFocusId) {
            val index = uiState.timelineItems.indexOfFirst { it.id == uiState.focusItemId }
            if (index != -1) {
                lastScrolledFocusId = uiState.focusItemId
                // Scroll with offset to account for the fading edge and next activity card
                listState.animateScrollToItem(index + 2)
            }
        }
    }

    LaunchedEffect(uiState.dateText) {
        userScrollDetected = false
        lastScrolledFocusId = null
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) userScrollDetected = true
    }

    val showNowButton by remember {
        derivedStateOf {
            val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
            val targetIndex = uiState.timelineItems.indexOfFirst { it.id == uiState.focusItemId } + 2
            userScrollDetected && targetIndex != -1 && targetIndex !in visibleIndices
        }
    }

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            if (event is ActivityDetailUiEvent.SchedulingUpsertSuccess) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showQuickAdd) {
        QuickAddDialog(
            onConfirm = { title, time -> 
                onAddAdHoc(title, time)
                showQuickAdd = false
            },
            onDismiss = { showQuickAdd = false }
        )
    }

    if (moveTargetId != null) {
        AlertDialog(
            onDismissRequest = { moveTargetId = null },
            title = { Text("Reprogramar actividad") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = timePickerState.hour * 60 + timePickerState.minute
                    onAction(moveTargetId!!, "MOVE_CONFIRM:$minutes")
                    moveTargetId = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { moveTargetId = null }) { Text("Cancelar") }
            }
        )
    }

    val fadeEdgeHeight = 80.dp

    RoutineScaffold(
        modifier = modifier,
        topBar = { 
            TodayHeader(
                dateText = uiState.dateText,
                progress = uiState.progress
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = bottomBar
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Viewport with Alpha Masking for a premium translucency effect
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        // Smooth Alpha Mask to fade items before they reach the header
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black),
                                startY = 0f,
                                endY = fadeEdgeHeight.toPx()
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
                    .padding(horizontal = RoutineTheme.spacing.marginMobile),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
            ) {
                item { TodayNextActivityCard(activity = uiState.nextActivity) }

                val pastCount = uiState.focusItemId?.let { focusId ->
                    uiState.timelineItems.takeWhile { it.id != focusId }.size
                } ?: 0

                item { PastBoundaryItem(count = pastCount) }

                todayTimelineItems(
                    items = uiState.timelineItems,
                    onAction = { id, type ->
                        if (type == "MOVE_REQUEST") moveTargetId = id
                        else onAction(id, type)
                    },
                    onExpandClick = onExpandClick
                )
            }

            // NOW button (Stays visible on top of the mask)
            if (showNowButton) {
                SmallFloatingActionButton(
                    onClick = {
                        userScrollDetected = false
                        coroutineScope.launch {
                            val targetIndex = uiState.timelineItems.indexOfFirst { it.id == uiState.focusItemId } + 2
                            if (targetIndex != -1) listState.animateScrollToItem(targetIndex)
                        }
                    },
                    containerColor = RoutineTheme.colors.surface2,
                    contentColor = RoutineTheme.colors.primary,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 32.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AHORA", style = RoutineTheme.typography.labelCaps)
                    }
                }
            }

            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = RoutineTheme.colors.primary,
                contentColor = RoutineTheme.colors.onPrimary,
                shape = RoutineTheme.shapes.pill,
                modifier = Modifier.align(Alignment.BottomEnd).padding(RoutineTheme.spacing.lg)
            ) {
                Icon(Icons.Default.Add, "Quick Add")
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
