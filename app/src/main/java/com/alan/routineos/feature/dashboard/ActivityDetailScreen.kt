package com.alan.routineos.feature.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.components.MetadataEditorSheet
import com.alan.routineos.feature.dashboard.components.SchedulingEditorSheet
import com.alan.routineos.feature.dashboard.model.ActivityNodeUiProjection
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
        onUndoDeleteClick = viewModel::onRestoreBranch,
        onAddSubStep = viewModel::onAddChild,
        onDeleteBranch = viewModel::onDeleteBranch,
        onUpdateNode = viewModel::onUpdateNode,
        onNodeClick = viewModel::setEditingNode,
        onOpenScheduling = viewModel::onOpenScheduling,
        onCloseScheduling = viewModel::onCloseScheduling,
        onUpsertRule = viewModel::onUpsertRule,
        onDeleteRule = viewModel::onDeleteRule,
        onOpenMetadata = viewModel::onOpenMetadata,
        onCloseMetadata = viewModel::onCloseMetadata,
        onUpsertMetadataSchema = viewModel::onUpsertMetadataSchema,
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
    onUndoDeleteClick: (List<String>) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onCloseScheduling: () -> Unit,
    onUpsertRule: (com.alan.routineos.domain.model.ScheduleRule) -> Unit,
    onDeleteRule: (com.alan.routineos.domain.model.ScheduleRule) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onCloseMetadata: () -> Unit,
    onUpsertMetadataSchema: (com.alan.routineos.domain.model.MetadataSchema) -> Unit,
    uiEvent: SharedFlow<ActivityDetailUiEvent>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        uiEvent.collect { event ->
            when (event) {
                is ActivityDetailUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        when {
                            event.nodeId != null -> onCompleteNodeClick(event.nodeId)
                            event.deletedBatch != null -> onUndoDeleteClick(event.deletedBatch)
                            event.ruleToRestore != null -> onUpsertRule(event.ruleToRestore)
                        }
                    }
                }
                is ActivityDetailUiEvent.SchedulingUpsertSuccess -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
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
            onExpandClick = onExpandClick,
            onAddSubStep = onAddSubStep,
            onDeleteBranch = onDeleteBranch,
            onUpdateNode = onUpdateNode,
            onNodeClick = onNodeClick,
            onOpenScheduling = onOpenScheduling,
            onOpenMetadata = onOpenMetadata
        )

        if (uiState.isSchedulingSheetOpen && uiState.schedulingTarget != null) {
            SchedulingEditorSheet(
                target = uiState.schedulingTarget,
                rules = uiState.targetRules,
                errorMessage = uiState.schedulingErrorMessage,
                uiEvent = uiEvent,
                onUpsertRule = onUpsertRule,
                onDeleteRule = onDeleteRule,
                onDismiss = onCloseScheduling
            )
        }

        if (uiState.isMetadataSheetOpen && uiState.metadataTarget != null) {
            MetadataEditorSheet(
                target = uiState.metadataTarget,
                currentSchema = uiState.targetMetadataSchema,
                errorMessage = uiState.metadataErrorMessage,
                onUpsertSchema = onUpsertMetadataSchema,
                onDismiss = onCloseMetadata
            )
        }
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
    onExpandClick: (String) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NodesList(
            uiState,
            paddingValues,
            onNewNodeTitleChanged,
            onAddNodeClick,
            onCompleteNodeClick,
            onExpandClick,
            onAddSubStep,
            onDeleteBranch,
            onUpdateNode,
            onNodeClick,
            onOpenScheduling,
            onOpenMetadata
        )
    }
}

@Composable
private fun NodesList(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit
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
        nodesContent(
            uiState.nodes,
            uiState.editingNodeId,
            onCompleteNodeClick,
            onExpandClick,
            onAddSubStep,
            onDeleteBranch,
            onUpdateNode,
            onNodeClick,
            onOpenScheduling,
            onOpenMetadata
        )
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
    editingNodeId: String?,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit
) {
    if (nodes.isEmpty()) {
        item { EmptyNodesMessage() }
    } else {
        items(nodes, key = { it.id }) { nodeProjection ->
            val isEditing = nodeProjection.id == editingNodeId
            NodeItem(
                projection = nodeProjection,
                isEditing = isEditing,
                onCompleteClick = { onCompleteNodeClick(nodeProjection.id) },
                onExpandClick = { onExpandClick(nodeProjection.id) },
                onNodeClick = { onNodeClick(if (isEditing) null else nodeProjection.id) },
                onAddSubStep = { title -> onAddSubStep(nodeProjection.id, title) },
                onDeleteClick = { onDeleteBranch(nodeProjection.id) },
                onTitleChange = { newTitle -> onUpdateNode(nodeProjection.id, newTitle) },
                onOpenScheduling = { onOpenScheduling(com.alan.routineos.domain.model.ScheduleTarget.Node(nodeProjection.id)) },
                onOpenMetadata = { onOpenMetadata(com.alan.routineos.domain.model.ScheduleTarget.Node(nodeProjection.id)) }
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
    isEditing: Boolean,
    onCompleteClick: () -> Unit,
    onExpandClick: () -> Unit,
    onNodeClick: () -> Unit,
    onAddSubStep: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onOpenScheduling: () -> Unit,
    onOpenMetadata: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (projection.depth * 24).dp)
            .background(
                color = if (isEditing) RoutineTheme.colors.surface2 else RoutineTheme.colors.surface1,
                shape = RoutineTheme.shapes.small
            )
            .border(
                width = if (isEditing) 2.dp else 1.dp,
                color = if (isEditing) RoutineTheme.colors.primary else RoutineTheme.colors.border,
                shape = RoutineTheme.shapes.small
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNodeClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompletionIcon(
                isCompleted = projection.status == com.alan.routineos.domain.model.NodeStatus.COMPLETED,
                isLeaf = projection.isLeaf,
                onClick = onCompleteClick
            )
            
            if (isEditing) {
                var localTitle by remember { mutableStateOf(projection.title) }
                TextField(
                    value = localTitle,
                    onValueChange = { localTitle = it },
                    modifier = Modifier.weight(1f),
                    textStyle = RoutineTheme.typography.bodyBase,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = { onTitleChange(localTitle) }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = projection.title,
                        style = RoutineTheme.typography.bodyBase,
                        color = RoutineTheme.colors.onSurface
                    )
                    if (projection.rules.isNotEmpty()) {
                        Text(
                            text = formatSchedulesSummary(projection.rules),
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.primary
                        )
                    }
                }
            }

            if (!projection.isLeaf) {
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = if (projection.isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                        contentDescription = "Expand",
                        tint = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        if (isEditing) {
            NodeInspectorShell(onAddSubStep, onDeleteClick, onOpenScheduling, onOpenMetadata)
        }
    }
}

@Composable
private fun NodeInspectorShell(
    onAddSubStep: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onOpenScheduling: () -> Unit,
    onOpenMetadata: () -> Unit
) {
    var newSubStepTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        HorizontalDivider(color = RoutineTheme.colors.border)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InspectorActionChip(Icons.Outlined.Schedule, "Scheduling", onOpenScheduling)
            InspectorActionChip(Icons.Outlined.Info, "Metadata", onOpenMetadata)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = RoutineTheme.colors.error)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubStepTitle,
                onValueChange = { newSubStepTitle = it },
                placeholder = { Text("Añadir sub-paso...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(
                onClick = {
                    onAddSubStep(newSubStepTitle)
                    newSubStepTitle = ""
                },
                enabled = newSubStepTitle.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add sub-step")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectorActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { CustomIcon(icon, contentDescription = null, size = 16.dp) }
    )
}

@Composable
private fun CustomIcon(imageVector: ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size)
    )
}

@Composable
private fun CompletionIcon(
    isCompleted: Boolean,
    isLeaf: Boolean,
    onClick: () -> Unit
) {
    val icon = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val tint = if (isCompleted) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
    
    val alpha = if (isLeaf) 1f else 0.5f

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

private fun formatSchedulesSummary(rules: List<com.alan.routineos.domain.model.ScheduleRule>): String {
    return rules.joinToString(" | ") { rule ->
        val time = rule.startTime?.let { minutes ->
            "%02d:%02d".format(minutes / 60, minutes % 60)
        } ?: "Sin hora"
        
        val days = if (rule.daysOfWeek.size == 7) "Daily" else {
            val names = listOf("L", "M", "M", "J", "V", "S", "D")
            rule.daysOfWeek.sorted().joinToString("") { names[it - 1] }
        }
        
        "$days $time"
    }
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
            onUndoDeleteClick = {},
            onAddSubStep = { _, _ -> },
            onDeleteBranch = {},
            onUpdateNode = { _, _ -> },
            onNodeClick = {},
            onOpenScheduling = {},
            onCloseScheduling = {},
            onUpsertRule = {},
            onDeleteRule = {},
            onOpenMetadata = {},
            onCloseMetadata = {},
            onUpsertMetadataSchema = {},
            uiEvent = kotlinx.coroutines.flow.MutableSharedFlow<ActivityDetailUiEvent>()
        )
    }
}
