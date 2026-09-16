package com.alan.routineos.feature.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.LifeSystem
import com.alan.routineos.domain.usecase.GetSystemsWithStatsUseCase
import com.alan.routineos.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SystemViewModel @Inject constructor(
    private val repository: ActivityRepository,
    private val getSystemsWithStatsUseCase: GetSystemsWithStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SystemUiState(isLoading = true))
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    private val _editingSystem = MutableStateFlow<LifeSystem?>(null)
    private val _isCreatingNewSystem = MutableStateFlow(false)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val statsFlow = getSystemsWithStatsUseCase()
            
            combine(statsFlow, _editingSystem, _isCreatingNewSystem) { systemsWithStats, editing, creating ->
                val uiModels = systemsWithStats.map { it.toUiModel() }
                val totalDefs = systemsWithStats.sumOf { it.activityCount }
                val totalInstances = systemsWithStats.sumOf { it.scheduledCount }
                val totalCompletions = systemsWithStats.sumOf { it.completedCount }

                SystemUiState(
                    isLoading = false,
                    lifeAreas = uiModels,
                    summary = com.alan.routineos.feature.system.model.SystemSummary(
                        systemsCount = systemsWithStats.size,
                        routinesCount = totalDefs,
                        activitiesCount = totalInstances,
                        exceptionsCount = totalCompletions
                    ),
                    editingSystem = editing,
                    isCreatingNewSystem = creating
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onAddSystemClick() {
        _isCreatingNewSystem.value = true
        _editingSystem.value = LifeSystem(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            iconKey = "account_tree",
            colorHex = "#34D399"
        )
    }

    fun onEditSystemClick(id: String) {
        viewModelScope.launch {
            val system = repository.getSystemById(id)
            if (system != null) {
                _isCreatingNewSystem.value = false
                _editingSystem.value = system
            }
        }
    }

    fun onUpdateSystemFields(title: String, icon: String, color: String) {
        _editingSystem.update { current ->
            current?.copy(title = title, iconKey = icon, colorHex = color)
        }
    }

    fun onSaveSystem() {
        val system = _editingSystem.value ?: return
        if (system.title.isBlank()) return

        viewModelScope.launch {
            repository.upsertSystem(system)
            onDismissEditor()
        }
    }

    fun onDeleteSystem(id: String) {
        viewModelScope.launch {
            val system = repository.getSystemById(id)
            if (system != null) {
                repository.deleteSystem(system)
                onDismissEditor()
            }
        }
    }

    fun onDismissEditor() {
        _editingSystem.value = null
        _isCreatingNewSystem.value = false
    }

    private fun com.alan.routineos.domain.model.SystemWithStats.toUiModel(): com.alan.routineos.feature.system.model.LifeArea {
        val status = if (system.isArchived) com.alan.routineos.feature.system.model.LifeAreaStatus.ARCHIVED 
                     else com.alan.routineos.feature.system.model.LifeAreaStatus.ACTIVE

        return com.alan.routineos.feature.system.model.LifeArea(
            id = system.id,
            title = system.title,
            iconName = system.iconKey,
            status = status,
            colorHex = system.colorHex,
            activityCount = activityCount,
            completedCount = completedCount,
            skippedCount = skippedCount,
            pendingCount = pendingCount,
            successRate = successRate
        )
    }
}
