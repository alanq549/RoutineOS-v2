package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
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
    val nodes: List<ActivityNodeWithExecution> = emptyList(),
    val isLoading: Boolean = true,
    val newNodeTitle: String = ""
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val repository: ActivityRepository,
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
            val nodesFlow = repository.getNodesForActivityDefinition(activityId)

            nodesFlow.flatMapLatest { nodes ->
                getNodesWithExecutionsFlow(nodes)
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

    private fun getNodesWithExecutionsFlow(nodes: List<ActivityNode>): Flow<List<ActivityNodeWithExecution>> {
        val executionFlows = nodes.map { node ->
            repository.getExecutionsForNodeOnDate(node.id, referenceDate).map { executions ->
                ActivityNodeWithExecution(
                    node = node,
                    isCompleted = executions.isNotEmpty(),
                    lastCompletionTimestamp = executions.firstOrNull()?.completedAt
                )
            }
        }

        return if (executionFlows.isEmpty()) {
            flowOf(emptyList())
        } else {
            combine(executionFlows) { it.toList() }
        }
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
                val nodeWithExecution = _uiState.value.nodes.find { it.node.id == nodeId }
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
