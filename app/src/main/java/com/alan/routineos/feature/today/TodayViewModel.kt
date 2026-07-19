package com.alan.routineos.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.today.data.FakeTodayRepository
import com.alan.routineos.feature.today.model.TimelineItemStatus
import com.alan.routineos.feature.today.model.TodayTimelineItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodayViewModel(
    private val repository: FakeTodayRepository = FakeTodayRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState(isLoading = true))
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getTimelineItems(),
                repository.getTodayProgress()
            ) { items, progress ->
                TodayUiState(
                    isLoading = false,
                    dateText = "MARTES, 24 OCTUBRE", // Mock date as in Stitch
                    progress = progress,
                    timelineItems = items,
                    nextActivity = findNextActivity(items)
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun findNextActivity(items: List<TodayTimelineItem>): TodayTimelineItem? {
        // Simple logic: first item with PENDING or ACTIVE status that isn't the current spontaneous one
        return items.firstOrNull { it.status == TimelineItemStatus.PENDING || it.status == TimelineItemStatus.ACTIVE }
    }

    fun onTaskToggled(taskId: String) {
        // Not implementing business logic beyond presentation state yet
    }
}
