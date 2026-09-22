package com.alan.routineos.feature.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.components.MetadataEditorSheet
import com.alan.routineos.feature.dashboard.components.SchedulingEditorSheet
import com.alan.routineos.feature.dashboard.components.getTechnicalIcon
import com.alan.routineos.feature.dashboard.model.ActivityNodeUiProjection
import kotlinx.coroutines.flow.SharedFlow

private val DarkSurface = Color(0xFF0D1017)
private val StitchGray = Color(0xFF1F2C3F)

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
        onDeleteMetadataSchema = viewModel::onDeleteMetadataSchema,
        onAssignToSystem = viewModel::onAssignToSystem,
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
    onDeleteMetadataSchema: (String, String) -> Unit,
    onAssignToSystem: (String?) -> Unit,
    uiEvent: SharedFlow<ActivityDetailUiEvent>
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val currentSystem = uiState.allSystems.find { it.id == uiState.activity?.systemId }
    val semanticColor = try { 
        Color(android.graphics.Color.parseColor(currentSystem?.colorHex ?: "")) 
    } catch (e: Exception) { 
        RoutineTheme.colors.roleEvent 
    }

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
        topBar = { 
            EngineeringTopBar(
                title = uiState.activity?.title ?: "MOLDE",
                onBackClick = onBackClick,
                onSaveClick = { /* No-op or sync signal */ }
            ) 
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        ActivityDetailContent(
            uiState = uiState,
            paddingValues = paddingValues,
            semanticColor = semanticColor,
            onNewNodeTitleChanged = onNewNodeTitleChanged,
            onAddNodeClick = onAddNodeClick,
            onCompleteNodeClick = onCompleteNodeClick,
            onExpandClick = onExpandClick,
            onAddSubStep = onAddSubStep,
            onDeleteBranch = onDeleteBranch,
            onUpdateNode = onUpdateNode,
            onNodeClick = onNodeClick,
            onOpenScheduling = onOpenScheduling,
            onOpenMetadata = onOpenMetadata,
            onAssignToSystem = onAssignToSystem
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
                onDeleteSchema = {
                    val target = uiState.metadataTarget
                    if (target != null) {
                        val (id, type) = when (target) {
                            is com.alan.routineos.domain.model.ScheduleTarget.Definition -> target.id to "DEFINITION"
                            is com.alan.routineos.domain.model.ScheduleTarget.Node -> target.id to "NODE"
                        }
                        onDeleteMetadataSchema(id, type)
                        onCloseMetadata()
                    }
                },
                onDismiss = onCloseMetadata
            )
        }
    }
}

@Composable
private fun EngineeringTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        color = RoutineTheme.colors.background,
        modifier = Modifier.fillMaxWidth().statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DETALLE DE ACTIVIDAD",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                            color = Color.White
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Save, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "GUARDAR", 
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                            color = Color.Black
                        )
                    }
                }
            }
            HorizontalDivider(color = RoutineTheme.colors.border.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun ActivityDetailContent(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    semanticColor: Color,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onAssignToSystem: (String?) -> Unit
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NodesList(
            uiState = uiState,
            paddingValues = paddingValues,
            semanticColor = semanticColor,
            onNewNodeTitleChanged = onNewNodeTitleChanged,
            onAddNodeClick = onAddNodeClick,
            onCompleteNodeClick = onCompleteNodeClick,
            onExpandClick = onExpandClick,
            onAddSubStep = onAddSubStep,
            onDeleteBranch = onDeleteBranch,
            onUpdateNode = onUpdateNode,
            onNodeClick = onNodeClick,
            onOpenScheduling = onOpenScheduling,
            onOpenMetadata = onOpenMetadata,
            onAssignToSystem = onAssignToSystem
        )
    }
}

@Composable
private fun NodesList(
    uiState: ActivityDetailUiState,
    paddingValues: PaddingValues,
    semanticColor: Color,
    onNewNodeTitleChanged: (String) -> Unit,
    onAddNodeClick: () -> Unit,
    onCompleteNodeClick: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    onAddSubStep: (String, String) -> Unit,
    onDeleteBranch: (String) -> Unit,
    onUpdateNode: (String, String) -> Unit,
    onNodeClick: (String?) -> Unit,
    onOpenScheduling: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onOpenMetadata: (com.alan.routineos.domain.model.ScheduleTarget) -> Unit,
    onAssignToSystem: (String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = RoutineTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { EngineeringStatsRow(uiState) }

        // 01 // CONTEXTO
        item {
            EngineeringSection(
                number = "01",
                title = "Contexto",
                subtitle = "Identidad del molde"
            ) {
                DetailHeader(
                    description = uiState.activity?.description ?: "",
                    currentSystemId = uiState.activity?.systemId,
                    allSystems = uiState.allSystems,
                    onAssignToSystem = onAssignToSystem
                )
            }
        }

        // 02 // HORARIO
        item {
            EngineeringSection(
                number = "02",
                title = "Horario",
                subtitle = "Programación de sesiones"
            ) {
                SchedulingSummarySection(
                    rules = uiState.allActivityRules,
                    onManageClick = { onOpenScheduling(com.alan.routineos.domain.model.ScheduleTarget.Definition(uiState.activity?.id ?: "")) }
                )
            }
        }

        // 03 // ESTRUCTURA
        item {
            EngineeringSection(
                number = "03",
                title = "Estructura",
                subtitle = "Jerarquía de pasos"
            ) {
                Column {
                    QuickAddNodeForm(
                        title = uiState.newNodeTitle,
                        onTitleChanged = onNewNodeTitleChanged,
                        onAddClick = onAddNodeClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        nodesContent(
            uiState.nodes,
            uiState.editingNodeId,
            semanticColor,
            onCompleteNodeClick,
            onExpandClick,
            onAddSubStep,
            onDeleteBranch,
            onUpdateNode,
            onNodeClick,
            onOpenScheduling,
            onOpenMetadata
        )
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun EngineeringSection(
    number: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = "$number //",
                style = RoutineTheme.typography.labelCaps.copy(
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Black,
                    color = RoutineTheme.colors.primary.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title.uppercase(),
                style = RoutineTheme.typography.labelCaps.copy(
                    fontSize = 11.sp, 
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
        Text(
            text = subtitle.uppercase(),
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        content()
    }
}

@Composable
private fun SchedulingSummarySection(
    rules: List<com.alan.routineos.domain.model.ScheduleRule>,
    onManageClick: () -> Unit
) {
    val cadence = if (rules.isEmpty()) 0 else rules.flatMap { it.daysOfWeek }.distinct().size
    val frequency = if (rules.isEmpty()) "Sin programar" else "$cadence DÍAS / SEM"

    Surface(
        color = Color(0xFF111721),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable { onManageClick() }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = frequency,
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color.White
                )
                if (rules.isNotEmpty()) {
                    Text(
                        text = formatSchedulesSummary(rules).uppercase(),
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        text = "Vínculo flotante sin horario fijo",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
            
            Surface(
                color = RoutineTheme.colors.surface2,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp),
                border = BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, null, tint = RoutineTheme.colors.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    description: String,
    currentSystemId: String?,
    allSystems: List<com.alan.routineos.domain.model.LifeSystem>,
    onAssignToSystem: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111721), RoundedCornerShape(16.dp))
            .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // SUBSISTEMA SECTION
        Text(
            text = "SISTEMA",
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        SystemAssignmentRow(
            currentSystemId = currentSystemId,
            allSystems = allSystems,
            onAssign = onAssignToSystem
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // PROPÓSITO SECTION
        Text(
            text = "PROPÓSITO",
            style = RoutineTheme.typography.labelCaps.copy(
                fontSize = 9.sp, 
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Color(0xFF0D121A),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = description.ifBlank { "Sin descripción registrada." },
                style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp, lineHeight = 18.sp),
                color = RoutineTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun SystemAssignmentRow(
    currentSystemId: String?,
    allSystems: List<com.alan.routineos.domain.model.LifeSystem>,
    onAssign: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSystem = allSystems.find { it.id == currentSystemId }
    val accentColor = try { Color(android.graphics.Color.parseColor(currentSystem?.colorHex ?: "")) } catch(e: Exception) { RoutineTheme.colors.primary }

    Surface(
        color = Color(0xFF0D121A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (currentSystem != null) getTechnicalIcon(currentSystem.iconKey) else Icons.Default.Category,
                contentDescription = null,
                tint = if (currentSystem != null) accentColor else RoutineTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = currentSystem?.title ?: "SIN SISTEMA",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = if (currentSystem != null) Color.White else RoutineTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.ArrowDropDown, 
                null, 
                tint = RoutineTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            DropdownMenu(
                expanded = expanded, 
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(RoutineTheme.colors.surface2)
            ) {
                DropdownMenuItem(
                    text = { Text("Ninguno (Sin organizar)", style = RoutineTheme.typography.labelCaps) },
                    onClick = {
                        onAssign(null)
                        expanded = false
                    }
                )
                allSystems.forEach { system ->
                    val sysColor = try { Color(android.graphics.Color.parseColor(system.colorHex)) } catch(e: Exception) { RoutineTheme.colors.primary }
                    DropdownMenuItem(
                        text = { Text(system.title, style = RoutineTheme.typography.labelCaps, color = sysColor) },
                        leadingIcon = { Icon(getTechnicalIcon(system.iconKey), null, Modifier.size(16.dp), sysColor) },
                        onClick = {
                            onAssign(system.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun LazyListScope.nodesContent(
    nodes: List<ActivityNodeUiProjection>,
    editingNodeId: String?,
    semanticColor: Color,
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
                semanticColor = semanticColor,
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
    semanticColor: Color,
    onCompleteClick: () -> Unit,
    onExpandClick: () -> Unit,
    onNodeClick: () -> Unit,
    onAddSubStep: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onOpenScheduling: () -> Unit,
    onOpenMetadata: () -> Unit
) {
    val nodeNumber = (projection.depth + 1).toString().padStart(2, '0')

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (projection.depth * 20).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isEditing) RoutineTheme.colors.surface2 else Color(0xFF111721))
                .border(
                    width = 1.dp,
                    color = if (isEditing) semanticColor else RoutineTheme.colors.border.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .combinedClickable(onClick = onNodeClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbering (Stitch Style)
            Text(
                text = "$nodeNumber.",
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (isEditing) semanticColor else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 12.dp)
            )

            if (isEditing) {
                var localTitle by remember { mutableStateOf(projection.title) }
                TextField(
                    value = localTitle,
                    onValueChange = { localTitle = it },
                    modifier = Modifier.weight(1f),
                    textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = { onTitleChange(localTitle) }) {
                            Icon(Icons.Default.Save, null, tint = semanticColor, modifier = Modifier.size(18.dp))
                        }
                    }
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = projection.title,
                        style = RoutineTheme.typography.bodyBase.copy(
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (projection.rules.isNotEmpty()) {
                            Text(
                                text = formatSchedulesSummary(projection.rules).uppercase(),
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                                color = semanticColor.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        if (!projection.isLeaf) {
                            Surface(
                                color = RoutineTheme.colors.surface2,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(16.dp)
                            ) {
                                Text(
                                    text = "DIRECTO",
                                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 7.sp, fontWeight = FontWeight.Black),
                                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!projection.isLeaf) {
                val rotation by animateFloatAsState(if (projection.isExpanded) 180f else 0f, label = "")
                IconButton(onClick = onExpandClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ExpandMore, null, Modifier.rotate(rotation), RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
            
            IconButton(onClick = { /* More Menu */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
            }
        }

        if (isEditing) {
            NodeInspectorShell(onAddSubStep, onDeleteClick, onOpenScheduling, onOpenMetadata, semanticColor)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun NodeInspectorShell(
    onAddSubStep: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onOpenScheduling: () -> Unit,
    onOpenMetadata: () -> Unit,
    semanticColor: Color
) {
    var newSubStepTitle by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF0B0E14), RoundedCornerShape(12.dp))
            .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InspectorActionChip(Icons.Outlined.Schedule, "Horario", onOpenScheduling)
            InspectorActionChip(Icons.Outlined.Info, "Métricas", onOpenMetadata)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = RoutineTheme.colors.error.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubStepTitle,
                onValueChange = { newSubStepTitle = it },
                placeholder = { Text("Añadir sub-paso...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = semanticColor.copy(alpha = 0.5f),
                    unfocusedBorderColor = RoutineTheme.colors.border.copy(alpha = 0.3f)
                ),
                textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp)
            )
            IconButton(
                onClick = {
                    onAddSubStep(newSubStepTitle)
                    newSubStepTitle = ""
                },
                enabled = newSubStepTitle.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add sub-step", tint = semanticColor)
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
    semanticColor: Color,
    onClick: () -> Unit
) {
    val icon = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked
    val tint = if (isCompleted) RoutineTheme.colors.primary else semanticColor
    
    val alpha = if (isLeaf) 1f else 0.4f

    Icon(
        imageVector = icon,
        contentDescription = "Complete",
        tint = tint.copy(alpha = alpha),
        modifier = Modifier
            .padding(end = 12.dp)
            .size(22.dp)
            .clickable(enabled = isLeaf, onClick = onClick)
    )
}

@Composable
private fun EngineeringStatsRow(uiState: ActivityDetailUiState) {
    val cadence = uiState.allActivityRules.flatMap { it.daysOfWeek }.distinct().size
    val blocks = uiState.nodes.size

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TechnicalStatBadge("CADENCIA", "$cadence DÍAS / SEM", Modifier.weight(1f))
        TechnicalStatBadge("BLOQUES", "$blocks NODOS", Modifier.weight(1f))
        TechnicalStatBadge("ESTADO", "SINCRONIZADO", Modifier.weight(1f), isHighlight = true)
    }
}

@Composable
private fun TechnicalStatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Surface(
        color = Color(0xFF111721),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
            )
            Text(
                text = value,
                style = RoutineTheme.typography.labelCaps.copy(
                    fontSize = 10.sp, 
                    fontWeight = FontWeight.Black,
                    color = if (isHighlight) RoutineTheme.colors.secondary else Color.White
                )
            )
        }
    }
}

@Composable
private fun EmptyNodesMessage() {
    Text(
        text = "Este molde no tiene estructura definida aún.",
        style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
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
            onDeleteMetadataSchema = { _, _ -> },
            onAssignToSystem = {},
            uiEvent = kotlinx.coroutines.flow.MutableSharedFlow<ActivityDetailUiEvent>()
        )
    }
}
