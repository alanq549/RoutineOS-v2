package com.alan.routineos.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ScheduleTarget
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.feature.dashboard.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() {
        viewModelScope.launch {
            repository.getActivityDefinitions().flatMapLatest { definitions ->
                if (definitions.isEmpty()) return@flatMapLatest flowOf(emptyList<ActivityCardModel>())
                
                val cardFlows = definitions.map { def ->
                    combine(
                        repository.getNodesForActivityDefinition(def.id),
                        repository.getRulesForActivityTree(def.id)
                    ) { nodes, rules ->
                        mapToCardModel(def, nodes, rules)
                    }
                }
                combine(cardFlows) { it.toList() }
            }.collect { cards ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    myActivities = cards
                )
            }
        }
    }

    private fun mapToCardModel(
        def: ActivityDefinition,
        nodes: List<com.alan.routineos.domain.model.ActivityNode>,
        rules: List<com.alan.routineos.domain.model.ScheduleRule>
    ): ActivityCardModel {
        val uniqueDays = rules.flatMap { it.daysOfWeek }.distinct().size
        val containers = nodes.filter { it.parentId == null }.size
        val leaves = nodes.filter { node -> nodes.none { it.parentId == node.id } }.size
        
        val statsLine = buildString {
            if (uniqueDays > 0) append("$uniqueDays sesiones")
            if (containers > 0) {
                if (isNotEmpty()) append(" · ")
                append("$containers categorías")
            }
            if (leaves > 0) {
                if (isNotEmpty()) append(" · ")
                append("$leaves actividades")
            }
        }

        val dayNames = listOf("", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val summaryItems = (1..7).mapNotNull { day ->
            val dayRules = rules.filter { it.daysOfWeek.contains(day) }
            if (dayRules.isEmpty()) return@mapNotNull null
            
            // Collect titles of the targets AND their immediate children if they are containers
            val previewItems = mutableListOf<String>()
            
            dayRules.forEach { rule ->
                val targetId = when(val target = rule.target) {
                    is ScheduleTarget.Definition -> null
                    is ScheduleTarget.Node -> target.id
                }
                
                if (targetId == null) {
                    // Definition scheduled: show its top-level nodes
                    previewItems.addAll(nodes.filter { it.parentId == null }.map { it.title })
                } else {
                    val targetNode = nodes.find { it.id == targetId }
                    val children = nodes.filter { it.parentId == targetId }
                    
                    if (children.isNotEmpty()) {
                        // Container scheduled: show its children
                        previewItems.addAll(children.map { it.title })
                    } else {
                        // Leaf scheduled: show itself
                        targetNode?.let { previewItems.add(it.title) }
                    }
                }
            }

            if (previewItems.isEmpty()) return@mapNotNull null

            ActivitySummaryDay(
                dayName = dayNames[day],
                activities = previewItems.distinct(),
                detailText = null // We'll show the actual list now
            )
        }

        return ActivityCardModel(
            id = def.id,
            title = def.title,
            iconName = if (def.title.contains("Gym", ignoreCase = true)) "fitness_center" else "school",
            frequency = if (uniqueDays == 7) "Lun - Dom" else if (uniqueDays >= 5) "Lun - Vie" else "Frecuencia",
            durationText = if (uniqueDays > 0) "$uniqueDays sesiones/sem" else "Sin configurar",
            subtitle = def.description,
            statsLine = statsLine,
            summaryItems = summaryItems.take(5) // Increased to show more context
        )
    }

    private fun getDescendants(parentId: String, allNodes: List<com.alan.routineos.domain.model.ActivityNode>): List<com.alan.routineos.domain.model.ActivityNode> {
        val children = allNodes.filter { it.parentId == parentId }
        return children + children.flatMap { getDescendants(it.id, allNodes) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQueries = query) }
    }
}
