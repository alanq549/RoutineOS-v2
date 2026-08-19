package com.alan.routineos.feature.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.LifeSystem
import com.alan.routineos.domain.usecase.GetSystemsWithStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val getSystemsWithStatsUseCase: GetSystemsWithStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemUiState(isLoading = true))
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getSystemsWithStatsUseCase().collect { systemsWithStats ->
                val uiModels = systemsWithStats.mapIndexed { index, item -> item.toUiModel(index) }
                val totalDefs = systemsWithStats.sumOf { it.activityCount }
                val totalInstances = systemsWithStats.sumOf { it.instanceCount }
                val totalCompletions = systemsWithStats.sumOf { it.completionCount }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        lifeAreas = uiModels,
                        summary = com.alan.routineos.feature.system.model.SystemSummary(
                            systemsCount = systemsWithStats.size,
                            routinesCount = totalDefs,
                            activitiesCount = totalInstances,
                            exceptionsCount = totalCompletions
                        )
                    )
                }
            }
        }
    }

    private fun com.alan.routineos.domain.model.SystemWithStats.toUiModel(index: Int): com.alan.routineos.feature.system.model.LifeArea {
        val status = if (system.isArchived) com.alan.routineos.feature.system.model.LifeAreaStatus.ARCHIVED 
                     else com.alan.routineos.feature.system.model.LifeAreaStatus.ACTIVE

        return if (index == 0) {
            com.alan.routineos.feature.system.model.LifeArea.Large(
                id = system.id,
                title = system.title,
                iconName = system.iconKey,
                status = status,
                routinesCount = activityCount,
                subjectsCount = 0, // Placeholder
                nextExecution = "Mañana"
            )
        } else {
            com.alan.routineos.feature.system.model.LifeArea.Medium(
                id = system.id,
                title = system.title,
                iconName = system.iconKey,
                status = status,
                sessionsCount = instanceCount,
                exercisesCount = activityCount,
                templatesCount = 0,
                nextExecution = "Hoy"
            )
        }
    }
}
