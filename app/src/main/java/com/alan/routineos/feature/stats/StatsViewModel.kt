package com.alan.routineos.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.usecase.GetHistoryAnalyticsUseCase
import com.alan.routineos.feature.stats.model.StatsPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getHistoryAnalyticsUseCase: GetHistoryAnalyticsUseCase
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(StatsPeriod.WEEK)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState> = _selectedPeriod.flatMapLatest { period ->
        val (start, end) = calculateRange(period)
        getHistoryAnalyticsUseCase.invoke(start, end).map { snapshot ->
            StatsUiState(
                isLoading = false,
                selectedPeriod = period,
                historySnapshot = snapshot
            )
        }.catch { e ->
            emit(
                StatsUiState(
                    isLoading = false,
                    selectedPeriod = period,
                    errorMessage = e.message ?: "Error al cargar estadísticas"
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StatsUiState(isLoading = true)
    )

    fun onPeriodSelected(period: StatsPeriod) {
        if (_selectedPeriod.value == period) return
        _selectedPeriod.value = period
    }

    private fun calculateRange(period: StatsPeriod): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        return when (period) {
            StatsPeriod.DAY -> now to now
            StatsPeriod.WEEK -> {
                val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                monday to now
            }
            StatsPeriod.MONTH -> {
                val firstOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
                firstOfMonth to now
            }
            StatsPeriod.YEAR -> {
                val firstOfYear = now.with(TemporalAdjusters.firstDayOfYear())
                firstOfYear to now
            }
        }
    }
}
