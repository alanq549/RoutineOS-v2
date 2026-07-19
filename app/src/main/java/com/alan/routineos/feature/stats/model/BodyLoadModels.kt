package com.alan.routineos.feature.stats.model

import androidx.compose.runtime.Immutable

enum class BodyView {
    FRONT, BACK
}

enum class BodyLoadLevel {
    NONE, LOW, MEDIUM, HIGH, RECOVERY_REQUIRED
}

enum class BodyZone {
    // Front
    HEAD, CHEST, ARMS_FRONT, ABS, LEGS_FRONT,
    // Back
    BACK_UPPER, BACK_LOWER, ARMS_BACK, GLUTES, LEGS_BACK
}

@Immutable
data class BodyLoadUiModel(
    val contextLabel: String,
    val dominantZone: String,
    val activeZonesCount: Int,
    val loadLevel: BodyLoadLevel,
    val highlightedZones: Set<BodyZone>,
    val rankedZones: List<ZoneRankItem>,
    val recoveryInfo: String? = null,
    val isWarning: Boolean = false,
    val isEmpty: Boolean = false
)

data class ZoneRankItem(
    val name: String,
    val percentage: Int,
    val level: BodyLoadLevel
)
