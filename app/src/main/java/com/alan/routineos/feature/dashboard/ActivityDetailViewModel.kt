package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ActivityDetailUiState(
    val activity: ActivityDefinition? = null,
    val nodes: List<ActivityNode> = emptyList(),
    val isLoading: Boolean = true,
    val newNodeTitle: String = ""
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val repository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val activityId: String = checkNotNull(savedStateHandle["activityId"])

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    init {
        loadActivity()
    }

    private fun loadActivity() {
        viewModelScope.launch {
            val activityFlow = flow { emit(repository.getActivityDefinitionById(activityId)) }
            val nodesFlow = repository.getNodesForActivityDefinition(activityId)

            combine(activityFlow, nodesFlow) { activity, nodes ->
                ActivityDetailUiState(
                    activity = activity,
                    nodes = nodes,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
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
}
