package com.alan.routineos.domain.model

/**
 * Defines how an instance behaves when facing temporal intersections.
 * Decoupled from the scheduling type (Fixed/Range).
 */
enum class TemporalMobility {
    /**
     * Cannot be moved automatically. Represents a rigid constraint (e.g., University schedule).
     */
    IMMOBILE,

    /**
     * Can be moved or adjusted. Represents a flexible task (e.g., Gym, Personal reading).
     */
    FLEXIBLE
}
