package com.alan.routineos.domain.model

/**
 * Defines the nature of a temporal intersection impact.
 */
enum class TemporalImpact {
    NONE,
    
    /**
     * Purely informational (e.g., structural nesting: child inside parent).
     */
    INFO,

    /**
     * Potential issue or overlap between independent flexible tasks.
     */
    WARNING
}
