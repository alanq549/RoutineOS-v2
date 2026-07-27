package com.alan.routineos.core.navigation

sealed class AppRoutes(val route: String) {
    object Today : AppRoutes("today")
    
    // Planning Workspace & Nested Routes
    object Planning : AppRoutes("planning_graph")
    object Planner : AppRoutes("planner")
    object Activities : AppRoutes("activities")
    object Systems : AppRoutes("systems")
    
    object Stats : AppRoutes("stats")
    object Account : AppRoutes("account")
}
