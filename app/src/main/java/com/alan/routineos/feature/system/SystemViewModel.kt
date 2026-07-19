package com.alan.routineos.feature.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.system.data.FakeSystemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SystemViewModel(
    private val repository: FakeSystemRepository = FakeSystemRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemUiState(isLoading = true))
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getSummary(),
                repository.getLifeAreas()
            ) { summary, lifeAreas ->
                SystemUiState(
                    isLoading = false,
                    summary = summary,
                    lifeAreas = lifeAreas
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
