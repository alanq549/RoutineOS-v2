package com.alan.routineos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.LifeSystem
import com.alan.routineos.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ActivityCreationUiState(
    val title: String = "",
    val description: String = "",
    val allSystems: List<LifeSystem> = emptyList(),
    val selectedSystemId: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ActivityCreationViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityCreationUiState())
    val uiState: StateFlow<ActivityCreationUiState> = _uiState.asStateFlow()

    init {
        loadSystems()
    }

    private fun loadSystems() {
        viewModelScope.launch {
            repository.getAllSystems().collect { systems ->
                _uiState.update { it.copy(allSystems = systems) }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onSystemSelected(systemId: String?) {
        _uiState.update { 
            it.copy(selectedSystemId = if (it.selectedSystemId == systemId) null else systemId)
        }
    }

    fun saveActivity() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val newActivity = ActivityDefinition(
                id = UUID.randomUUID().toString(),
                title = currentState.title,
                description = currentState.description,
                systemId = currentState.selectedSystemId
            )
            
            repository.upsertActivityDefinition(newActivity)
            
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}
