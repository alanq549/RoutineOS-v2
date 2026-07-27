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
                    categories = listOf(
                        ActivityCategory("1", "Todas", isSelected = true),
                        ActivityCategory("2", "Estudio"),
                        ActivityCategory("3", "Salud"),
                        ActivityCategory("4", "Trabajo"),
                        ActivityCategory("5", "Personal"),
                        ActivityCategory("6", "Descanso")
                    ),
                    myActivities = activities.map { activity ->
                        ActivityCardModel(
                            id = activity.id,
                            title = activity.title,
                            iconName = "school",
                            frequency = "Frecuencia",
                            durationText = "Activo",
                            subtitle = activity.description,
                            summaryItems = emptyList()
                        )
                    },
                    recommendedTemplates = listOf(
                        ActivityTemplateModel(
                            id = "1",
                            title = "Developer Deep Work",
                            description = "Estructura para bloques de 4h de programación sin interrupciones.",
                            iconName = "terminal"
                        ),
                        ActivityTemplateModel(
                            id = "2",
                            title = "Higiene del Sueño",
                            description = "Optimiza tu descanso con este protocolo de 90 min antes de dormir.",
                            iconName = "bedtime"
                        )
                    )
                )
            }
        }
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            categories = _uiState.value.categories.map { it.copy(isSelected = it.id == categoryId) }
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQueries = query)
    }
}
