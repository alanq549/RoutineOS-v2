package com.alan.routineos.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.usecase.GetHistoryAnalyticsUseCase
import com.alan.routineos.feature.stats.model.StatsPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getHistoryAnalyticsUseCase: GetHistoryAnalyticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState(isLoading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val period = _uiState.value.selectedPeriod
            val (start, end) = calculateRange(period)
            
            try {
                val snapshot = getHistoryAnalyticsUseCase.execute(start, end)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    historySnapshot = snapshot
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al cargar estadísticas"
                )
            }
        }
    }

    fun onPeriodSelected(period: StatsPeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    private fun calculateRange(period: StatsPeriod): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        return when (period) {
            StatsPeriod.DAY -> now to now
            StatsPeriod.WEEK -> {
                // This week: Monday to today
                val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                monday to now
            }
            StatsPeriod.MONTH -> {
                // This month: 1st day to today
                val firstOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                firstOfMonth to now
            }
            StatsPeriod.YEAR -> {
                // This year: Jan 1st to today
                val firstOfYear = now.with(TemporalAdjusters.firstDayOfYear())
                firstOfYear to now
            }
        }
    }
}
