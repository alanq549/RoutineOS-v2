package com.alan.routineos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.feature.dashboard.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getActivityDefinitions().collect { activities ->
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    myActivities = activities.map { activity ->
                        ActivityCardModel(
                            id = activity.id,
                            title = activity.title,
                            iconName = "activity", // Generic icon key
                            frequency = "Frecuencia",
                            durationText = "Activo",
                            subtitle = activity.description,
                            summaryItems = emptyList()
                        )
                    }
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQueries = query)
    }
}
