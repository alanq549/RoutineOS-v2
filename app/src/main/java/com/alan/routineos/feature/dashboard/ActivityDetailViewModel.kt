package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.ScheduleRule
import com.alan.routineos.domain.model.ScheduleTarget
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.dashboard.model.ActivityNodeUiProjection
import com.alan.routineos.feature.dashboard.model.toUiProjection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

sealed class ActivityDetailUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val nodeId: String? = null,
        val deletedBatch: List<String>? = null,
        val ruleToRestore: ScheduleRule? = null
    ) : ActivityDetailUiEvent()
    data class SchedulingUpsertSuccess(val message: String) : ActivityDetailUiEvent()
}

data class ActivityDetailUiState(
    val activity: ActivityDefinition? = null,
    val nodes: List<ActivityNodeUiProjection> = emptyList(),
    val isLoading: Boolean = true,
    val newNodeTitle: String = "",
    val editingNodeId: String? = null,
    val isSchedulingSheetOpen: Boolean = false,
    val schedulingTarget: ScheduleTarget? = null,
    val targetRules: List<ScheduleRule> = emptyList(),
    val schedulingErrorMessage: String? = null
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getActivityTreeUseCase: GetActivityTreeUseCase,
    private val addChildUseCase: AddChildUseCase,
    private val updateNodeUseCase: UpdateNodeUseCase,
    private val deleteBranchUseCase: DeleteBranchUseCase,
    private val restoreBranchUseCase: RestoreBranchUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])
    private val referenceDate: Long = LocalDate.now().toEpochDay()

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ActivityDetailUiEvent>()
    val uiEvent: SharedFlow<ActivityDetailUiEvent> = _uiEvent.asSharedFlow()

    private val processingNodeIds = MutableStateFlow<Set<String>>(emptySet())
    private val expandedNodes = MutableStateFlow<Set<String>>(emptySet())
    private val _schedulingTarget = MutableStateFlow<ScheduleTarget?>(null)

    init {
        loadActivity()
        observeSchedulingRules()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            val activityFlow = flow { emit(repository.getActivityDefinitionById(activityId)) }
            val treeFlow = getActivityTreeUseCase(activityId, referenceDate)

            combine(activityFlow, treeFlow, expandedNodes, _schedulingTarget) { activity, tree, expanded, schTarget ->
                ActivityDetailUiState(
                    activity = activity,
                    nodes = tree.toUiProjection(expandedNodes = expanded),
                    isLoading = false,
                    isSchedulingSheetOpen = schTarget != null,
                    schedulingTarget = schTarget
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSchedulingRules() {
        _schedulingTarget
            .filterNotNull()
            .flatMapLatest { target ->
                when (target) {
                    is ScheduleTarget.Definition -> repository.getRulesForDefinition(target.id)
                    is ScheduleTarget.Node -> repository.getRulesForNode(target.id)
                }
            }
            .onEach { rules ->
                _uiState.update { it.copy(targetRules = rules) }
            }
            .launchIn(viewModelScope)
    }

    fun onNewNodeTitleChanged(title: String) {
        _uiState.update { it.copy(newNodeTitle = title) }
    }

    fun addNode() {
        val title = _uiState.value.newNodeTitle
        if (title.isBlank()) return

        viewModelScope.launch {
            val newNode = ActivityNode(
                id = UUID.randomUUID().toString(),
                activityDefinitionId = activityId,
                parentId = null,
                position = _uiState.value.nodes.filter { it.depth == 0 }.size,
                title = title,
                description = "",
                isDeleted = false
            )
            repository.upsertNode(newNode)
            _uiState.update { it.copy(newNodeTitle = "") }
        }
    }

    fun onAddChild(parentId: String, title: String) {
        viewModelScope.launch {
            try {
                addChildUseCase(parentId, title)
                expandedNodes.update { it + parentId }
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al añadir sub-paso"))
            }
        }
    }

    fun onUpdateNode(nodeId: String, newTitle: String) {
        viewModelScope.launch {
            try {
                updateNodeUseCase(nodeId, newTitle)
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al actualizar paso"))
            }
        }
    }

    fun onDeleteBranch(nodeId: String) {
        viewModelScope.launch {
            try {
                val affectedIds = deleteBranchUseCase(nodeId)
                _uiEvent.emit(
                    ActivityDetailUiEvent.ShowSnackbar(
                        message = "Rama eliminada",
                        actionLabel = "Deshacer",
                        deletedBatch = affectedIds
                    )
                )
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al eliminar rama"))
            }
        }
    }

    fun onRestoreBranch(nodeIds: List<String>) {
        viewModelScope.launch {
            try {
                restoreBranchUseCase(nodeIds)
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al restaurar rama"))
            }
        }
    }

    fun toggleNodeCompletion(nodeId: String) {
        if (processingNodeIds.value.contains(nodeId)) return

        processingNodeIds.update { it + nodeId }

        viewModelScope.launch {
            try {
                val nodeProjection = _uiState.value.nodes.find { it.id == nodeId }
                if (nodeProjection?.isLeaf == false) {
                    _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Los contenedores se completan mediante sus hijos"))
                    return@launch
                }

                if (nodeProjection?.status == com.alan.routineos.domain.model.NodeStatus.COMPLETED) {
                    repository.deleteExecutionsForNodeOnDate(nodeId, referenceDate)
                    _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Paso marcado como pendiente", "Deshacer", nodeId))
                } else {
                    repository.registerExecution(nodeId, referenceDate, "{}")
                    _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Paso completado", "Deshacer", nodeId))
                }
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al actualizar paso"))
            } finally {
                processingNodeIds.update { it - nodeId }
            }
        }
    }

    fun toggleExpand(nodeId: String) {
        expandedNodes.update {
            if (it.contains(nodeId)) it - nodeId else it + nodeId
        }
    }

    fun setEditingNode(nodeId: String?) {
        _uiState.update { it.copy(editingNodeId = nodeId) }
    }

    fun onOpenScheduling(target: ScheduleTarget) {
        _schedulingTarget.value = target
    }

    fun onCloseScheduling() {
        _schedulingTarget.value = null
        _uiState.update { it.copy(targetRules = emptyList()) }
    }

    fun onUpsertRule(rule: ScheduleRule) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(schedulingErrorMessage = null) }
                val isEditing = _uiState.value.targetRules.any { it.id == rule.id }
                repository.upsertRule(rule)
                _uiEvent.emit(
                    ActivityDetailUiEvent.SchedulingUpsertSuccess(
                        if (isEditing) "Horario actualizado" else "Horario añadido"
                    )
                )
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(schedulingErrorMessage = e.message ?: "Error de validación") }
            } catch (e: Exception) {
                _uiState.update { it.copy(schedulingErrorMessage = "Error al guardar horario") }
            }
        }
    }

    fun onDeleteRule(rule: ScheduleRule) {
        viewModelScope.launch {
            try {
                repository.deleteRule(rule)
                _uiEvent.emit(
                    ActivityDetailUiEvent.ShowSnackbar(
                        message = "Horario eliminado",
                        actionLabel = "Deshacer",
                        ruleToRestore = rule
                    )
                )
            } catch (e: Exception) {
                _uiEvent.emit(ActivityDetailUiEvent.ShowSnackbar("Error al eliminar horario"))
            }
        }
    }
}
