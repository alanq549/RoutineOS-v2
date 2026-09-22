package com.alan.routineos.core.navigation

sealed class AppRoutes(val route: String) {
    object Today : AppRoutes("today")
    
    // Planning Workspace
    object Planning : AppRoutes("planning_graph")
    
    object Stats : AppRoutes("stats")
    object Account : AppRoutes("account")
    
    // Creation & Detail Flows
    object ActivityCreation : AppRoutes("activity_creation")
    object ActivityDetail : AppRoutes("activity_detail/{activityId}") {
        fun createRoute(activityId: String) = "activity_detail/$activityId"
    }
}
