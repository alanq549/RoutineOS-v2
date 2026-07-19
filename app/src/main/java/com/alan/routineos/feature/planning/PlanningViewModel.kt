package com.alan.routineos.feature.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.planning.data.FakePlanningRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PlanningViewModel(
    private val repository: FakePlanningRepository = FakePlanningRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlanningUiState(isLoading = true))
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getPlanningDays(),
                repository.getPlanningBlocks(),
                repository.getExceptions(),
                repository.getUnscheduled()
            ) { days, blocks, exceptions, unscheduled ->
                PlanningUiState(
                    isLoading = false,
                    days = days,
                    blocks = blocks,
                    exceptions = exceptions,
                    unscheduledItems = unscheduled
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onDaySelected(dayId: String) {
        // Mock selection update
        _uiState.value = _uiState.value.copy(
            days = _uiState.value.days.map { it.copy(isSelected = it.id == dayId) }
        )
    }

    fun onSegmentChanged(segment: PlanningSegment) {
        _uiState.value = _uiState.value.copy(selectedSegment = segment)
    }
}
