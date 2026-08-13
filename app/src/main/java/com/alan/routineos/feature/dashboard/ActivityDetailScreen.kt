package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.model.ActivityNodeUiProjection
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun ActivityDetailRoute(
    onBack: () -> Unit,
    viewModel: ActivityDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ActivityDetailScreen(
        uiState = uiState,
        onBackClick = onBack,
        onNewNodeTitleChanged = viewModel::onNewNodeTitleChanged,
        onAddNodeClick = viewModel::addNode,
        onCompleteNodeClick = viewModel::toggleNodeCompletion,
        onExpandClick = viewModel::toggleExpand,
        uiEvent = viewModel.uiEvent
    )
}

@Composable
fun ActivityDetailScreen(
    uiState: ActivityDetailUiState,
    onBackClick: () -> Unit,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    uiEvent: SharedFlow<ActivityDetailUiEvent>
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is ActivityDetailUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed && event.nodeId != null) {
                        onCompleteNodeClick(event.nodeId)
                    }
                }
            }
        }
    }

    RoutineScaffold(
        topBar = { ActivityDetailTopBar(uiState.activity?.title, onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        ActivityDetailContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onNewNodeTitleChanged = onNewNodeTitleChanged,
            onAddNodeClick = onAddNodeClick,
            onCompleteNodeClick = onCompleteNodeClick,
            onExpandClick = onExpandClick
        )
    }
}

@Composable
private fun ActivityDetailTopBar(title: String?, onBackClick: () -> Unit) {
    Box(modifier = Modifier.statusBarsPadding()) {
        RoutineTopBar(
            title = {
                Text(
                    text = title ?: "Detalle",
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
private fun ActivityDetailContent(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NodesList(uiState, paddingValues, onNewNodeTitleChanged, onAddNodeClick, onCompleteNodeClick, onExpandClick)
    }
}

@Composable
private fun NodesList(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DetailHeader(uiState.activity?.description ?: "") }
        item { NodesSectionTitle() }
        item {
            QuickAddNodeForm(
                title = uiState.newNodeTitle,
                onTitleChanged = onNewNodeTitleChanged,
                onAddClick = onAddNodeClick
            )
        }
        nodesContent(uiState.nodes, onCompleteNodeClick, onExpandClick)
        item { ListBottomSpacer() }
    }
}

@Composable
private fun DetailHeader(description: String) {
    Spacer(modifier = Modifier.height(16.dp))
    ActivityHeaderSection(description = description)
}

@Composable
private fun NodesSectionTitle() {
    Text(
        text = "PASOS / NODOS",
        style = RoutineTheme.typography.labelCaps,
        color = RoutineTheme.colors.primary
    )
}

private fun LazyListScope.nodesContent(
    nodes: List<ActivityNodeUiProjection>,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    if (nodes.isEmpty()) {
        item { EmptyNodesMessage() }
    } else {
        items(nodes, key = { it.id }) { nodeProjection ->
            NodeItem(
                projection = nodeProjection,
                onCompleteClick = { onCompleteNodeClick(nodeProjection.id) },
                onExpandClick = { onExpandClick(nodeProjection.id) }
            )
        }
    }
}

@Composable
private fun ListBottomSpacer() {
    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun ActivityHeaderSection(description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = description.ifBlank { "Sin descripción" },
            style = RoutineTheme.typography.bodyBase,
            color = RoutineTheme.colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun QuickAddNodeForm(
    title: String,
    onTitleChanged: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.medium)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NodeTitleTextField(title, onTitleChanged, Modifier.weight(1f))
        AddNodeIconButton(onAddClick, title.isNotBlank())
    }
}

@Composable
private fun NodeTitleTextField(
    title: String,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = { Text("Nuevo paso...") },
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
        )
    )
}

@Composable
private fun AddNodeIconButton(onClick: () -> Unit, enabled: Boolean) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add node",
            tint = if (enabled) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun NodeItem(
    projection: ActivityNodeUiProjection,
    onCompleteClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (projection.depth * 24).dp)
            .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
            .clickable(onClick = onExpandClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompletionIcon(
            isCompleted = projection.status == com.alan.routineos.domain.model.NodeStatus.COMPLETED,
            isLeaf = projection.isLeaf,
            onClick = onCompleteClick
        )
        Text(
            text = projection.title,
            style = RoutineTheme.typography.bodyBase,
            color = RoutineTheme.colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (!projection.isLeaf) {
            Text(
                text = if (projection.isExpanded) "▼" else "▶",
                style = RoutineTheme.typography.labelCaps,
                color = RoutineTheme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompletionIcon(
    isCompleted: Boolean,
    isLeaf: Boolean,
    onClick: () -> Unit
) {
    val icon = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val tint = if (isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
    
    val alpha = if (isLeaf) 1f else 0.5f // Visual hint that only leaves are primary targets

    Icon(
        imageVector = icon,
        contentDescription = "Complete",
        tint = tint.copy(alpha = alpha),
        modifier = Modifier
            .padding(end = 12.dp)
            .size(24.dp)
            .clickable(enabled = isLeaf, onClick = onClick)
    )
}

@Composable
private fun EmptyNodesMessage() {
    Text(
        text = "No hay pasos definidos aún.",
        style = RoutineTheme.typography.bodyBase,
        color = RoutineTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 32.dp)
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ActivityDetailScreenPreview() {
    RoutineTheme {
        ActivityDetailScreen(
            uiState = ActivityDetailUiState(
                activity = com.alan.routineos.domain.model.ActivityDefinition("1", "Actividad de Prueba", "Descripción"),
                isLoading = false
            ),
            onBackClick = {},
            onNewNodeTitleChanged = {},
            onAddNodeClick = {},
            onCompleteNodeClick = {},
            onExpandClick = {},
            uiEvent = MutableSharedFlow()
        )
    }
}
