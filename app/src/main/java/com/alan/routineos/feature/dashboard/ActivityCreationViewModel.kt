package com.alan.routineos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ActivityCreationUiState(
    val title: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class ActivityCreationViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityCreationUiState())
    val uiState: StateFlow<ActivityCreationUiState> = _uiState.asStateFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun saveActivity() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val newActivity = ActivityDefinition(
                id = UUID.randomUUID().toString(),
                title = currentState.title,
                description = currentState.description
            )
            
            repository.upsertActivityDefinition(newActivity)
            
            _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }
}
