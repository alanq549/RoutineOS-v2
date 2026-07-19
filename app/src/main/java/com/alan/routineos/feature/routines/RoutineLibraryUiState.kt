package com.alan.routineos.feature.routines

import com.alan.routineos.feature.routines.model.*

data class RoutineLibraryUiState(
    val isLoading: Boolean = false,
    val searchQueries: String = "",
    val categories: List<RoutineCategory> = emptyList(),
    val myRoutines: List<RoutineCardModel> = emptyList(),
    val recommendedTemplates: List<RoutineTemplateModel> = emptyList()
)
