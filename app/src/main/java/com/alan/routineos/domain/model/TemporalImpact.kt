package com.alan.routineos.domain.model

/**
 * Defines the nature of a temporal intersection impact.
 */
enum class TemporalImpact {
    /**
     * Purely informational (e.g., structural nesting: child inside parent).
     */
    INFO,

    /**
     * Potential issue or overlap between independent flexible tasks.
     */
    WARNING,

    /**
     * An intersection that suggests a specific move to resolve a conflict.
     */
    MOVE_SUGGESTION,

    /**
     * An interruption occurring within an immobile task window.
     */
    INTERRUPTION_LABEL
}
