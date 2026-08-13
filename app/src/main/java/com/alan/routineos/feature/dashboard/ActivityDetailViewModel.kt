package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.ActivityNodeTree
import com.alan.routineos.domain.usecase.GetActivityTreeUseCase
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
        val nodeId: String? = null
    ) : ActivityDetailUiEvent()
}

data class ActivityNodeWithExecution(
    val node: ActivityNode,
    val isCompleted: Boolean,
    val lastCompletionTimestamp: Long? = null,
)

data class ActivityDetailUiState(
    val activity: ActivityDefinition? = null,
    val nodes: List<ActivityNodeTreeWithExecution> = emptyList(),
    val isLoading: Boolean = true,
    val newNodeTitle: String = ""
)

data class ActivityNodeTreeWithExecution(
    val treeNode: ActivityNodeTree,
    val isCompleted: Boolean,
    val lastCompletionTimestamp: Long? = null,
    val isExpanded: Boolean = true
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getActivityTreeUseCase: GetActivityTreeUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])
    private val referenceDate: Long = LocalDate.now().toEpochDay()

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ActivityDetailUiEvent>()
    val uiEvent: SharedFlow<ActivityDetailUiEvent> = _uiEvent.asSharedFlow()

    private val processingNodeIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        loadActivity()
    }

@OptIn(ExperimentalCoroutinesApi::class)
    private fun loadActivity() {
        viewModelScope.launch {
            val activityFlow = flow { emit(repository.getActivityDefinitionById(activityId)) }
            val treeFlow = getActivityTreeUseCase(activityId)

            treeFlow.flatMapLatest { treeNodes ->
                getTreeWithExecutionsFlow(treeNodes)
            }.combine(activityFlow) { nodesWithExecution, activity ->
                ActivityDetailUiState(
                    activity = activity,
                    nodes = nodesWithExecution,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun getTreeWithExecutionsFlow(treeNodes: List<ActivityNodeTree>): Flow<List<ActivityNodeTreeWithExecution>> {
        if (treeNodes.isEmpty()) return flowOf(emptyList())

        // Flatten the tree for execution status check, but keep hierarchy info
        val flatTree = flattenTree(treeNodes)

        val executionFlows = flatTree.map { treeNode ->
            repository.getExecutionsForNodeOnDate(treeNode.node.id, referenceDate).map { executions ->
                ActivityNodeTreeWithExecution(
                    treeNode = treeNode,
                    isCompleted = executions.isNotEmpty(), // Simplified: leaf completion
                    lastCompletionTimestamp = executions.firstOrNull()?.completedAt
                )
            }
        }

        return combine(executionFlows) { it.toList() }
    }

    private fun flattenTree(tree: List<ActivityNodeTree>): List<ActivityNodeTree> {
        val result = mutableListOf<ActivityNodeTree>()
        tree.forEach { item ->
            result.add(item)
            result.addAll(flattenTree(item.children))
        }
        return result
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
                title = title
            )
            repository.upsertNode(newNode)
            _uiState.update { it.copy(newNodeTitle = "") }
        }
    }

    fun toggleNodeCompletion(nodeId: String) {
        if (processingNodeIds.value.contains(nodeId)) return

        processingNodeIds.update { it + nodeId }

        viewModelScope.launch {
            try {
                val nodeWithExecution = _uiState.value.nodes.find { it.treeNode.node.id == nodeId }
                if (nodeWithExecution?.isCompleted == true) {
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
}
