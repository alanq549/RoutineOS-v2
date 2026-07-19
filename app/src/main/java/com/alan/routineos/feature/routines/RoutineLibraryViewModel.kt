package com.alan.routineos.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.feature.routines.data.FakeRoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RoutineLibraryViewModel(
    private val repository: FakeRoutineRepository = FakeRoutineRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineLibraryUiState(isLoading = true))
    val uiState: StateFlow<RoutineLibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getCategories(),
                repository.getMyRoutines(),
                repository.getRecommendedTemplates()
            ) { categories, myRoutines, recommended ->
                RoutineLibraryUiState(
                    isLoading = false,
                    categories = categories,
                    myRoutines = myRoutines,
                    recommendedTemplates = recommended
                )
            }.collect { newState ->
                _uiState.value = newState
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
