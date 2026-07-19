package com.alan.routineos.core.navigation

sealed class RoutineRoutes(val route: String) {
    object Today : RoutineRoutes("today")
    
    // Planning Workspace & Nested Routes
    object Planning : RoutineRoutes("planning_graph")
    object Planner : RoutineRoutes("planner")
    object Routines : RoutineRoutes("routines")
    object Systems : RoutineRoutes("systems")
    
    object Stats : RoutineRoutes("stats")
    object Account : RoutineRoutes("account")
}
