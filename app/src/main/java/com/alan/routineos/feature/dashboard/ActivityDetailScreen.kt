package com.alan.routineos.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun ActivityDetailRoute(
    onBack: () -> Unit,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ActivityDetailScreen(
        uiState = uiState,
        onBackClick = onBack,
        onNewNodeTitleChanged = viewModel::onNewNodeTitleChanged,
        onAddNodeClick = viewModel::addNode
    )
}

@Composable
fun ActivityDetailScreen(
    uiState: ActivityDetailUiState,
    onBackClick: () -> Unit,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit
) {
    RoutineScaffold(
        topBar = { ActivityDetailTopBar(uiState.activity?.title, onBackClick) }
    ) { paddingValues ->
        ActivityDetailContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onNewNodeTitleChanged = onNewNodeTitleChanged,
            onAddNodeClick = onAddNodeClick
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
    onAddNodeClick: () -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NodesList(uiState, paddingValues, onNewNodeTitleChanged, onAddNodeClick)
    }
}

@Composable
private fun NodesList(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit
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
        NodesContent(uiState.nodes)
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

private fun LazyListScope.NodesContent(nodes: List<com.alan.routineos.domain.model.ActivityNode>) {
    if (nodes.isEmpty()) {
        item { EmptyNodesMessage() }
    } else {
        items(nodes) { node -> NodeItem(title = node.title) }
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
private fun NodeItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            style = RoutineTheme.typography.headlineMedium,
            color = RoutineTheme.colors.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = title,
            style = RoutineTheme.typography.bodyBase,
            color = RoutineTheme.colors.onSurface
        )
    }
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
            onAddNodeClick = {}
        )
    }
}
