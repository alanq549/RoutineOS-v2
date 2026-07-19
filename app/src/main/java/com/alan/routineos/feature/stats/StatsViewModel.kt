package com.alan.routineos.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.stats.data.FakeStatsRepository
import com.alan.routineos.feature.stats.model.StatsPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatsViewModel(
    private val repository: FakeStatsRepository = FakeStatsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getWeeklyRhythm(),
                repository.getMonthlyCycle(),
                repository.getYearlyCycle()
            ) { weekly, monthly, yearly ->
                _uiState.value.copy(
                    isLoading = false,
                    weeklyData = weekly,
                    monthlyData = monthly,
                    yearlyData = yearly
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun onPeriodSelected(period: StatsPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    fun onDaySelected(dayId: String) {
        _uiState.value = _uiState.value.copy(selectedDayId = dayId)
    }

    fun onWeekSelected(weekId: String) {
        _uiState.value = _uiState.value.copy(selectedWeekId = weekId)
    }

    fun onMonthSelected(monthId: String) {
        _uiState.value = _uiState.value.copy(selectedMonthId = monthId)
    }
}
