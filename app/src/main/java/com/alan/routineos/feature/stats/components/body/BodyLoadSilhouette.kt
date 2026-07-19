package com.alan.routineos.feature.stats.components.body

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.model.BodyView
import com.alan.routineos.feature.stats.model.BodyZone

@Composable
fun BodyLoadSilhouette(
    view: BodyView,
    highlightedZones: Set<BodyZone>,
    modifier: Modifier = Modifier
) {
    val baseColor = RoutineTheme.colors.surface2
    val highlightColor = RoutineTheme.colors.primary

    Box(
        modifier = modifier
            .size(width = 120.dp, height = 240.dp)
            .aspectRatio(0.5f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBody(view, baseColor, highlightedZones, highlightColor)
        }
    }
}

private fun DrawScope.drawBody(
    view: BodyView,
    baseColor: Color,
    highlightedZones: Set<BodyZone>,
    highlightColor: Color
) {
    val width = size.width
    val height = size.height

    // Head
    drawZone(
        color = if (BodyZone.HEAD in highlightedZones) highlightColor else baseColor,
        topLeft = Offset(width * 0.35f, 0f),
        size = Size(width * 0.3f, height * 0.1f),
        cornerRadius = 100f
    )

    // Torso (Chest/Abs or Back Upper/Lower)
    if (view == BodyView.FRONT) {
        // Chest
        drawZone(
            color = if (BodyZone.CHEST in highlightedZones) highlightColor else baseColor,
            topLeft = Offset(width * 0.25f, height * 0.12f),
            size = Size(width * 0.5f, height * 0.15f),
            cornerRadius = 8f
        )
        // Abs
        drawZone(
            color = if (BodyZone.ABS in highlightedZones) highlightColor else baseColor,
            topLeft = Offset(width * 0.3f, height * 0.28f),
            size = Size(width * 0.4f, height * 0.15f),
            cornerRadius = 8f
        )
    } else {
        // Back Upper
        drawZone(
            color = if (BodyZone.BACK_UPPER in highlightedZones) highlightColor else baseColor,
            topLeft = Offset(width * 0.25f, height * 0.12f),
            size = Size(width * 0.5f, height * 0.15f),
            cornerRadius = 8f
        )
        // Back Lower / Glutes
        drawZone(
            color = if (BodyZone.BACK_LOWER in highlightedZones || BodyZone.GLUTES in highlightedZones) highlightColor else baseColor,
            topLeft = Offset(width * 0.3f, height * 0.28f),
            size = Size(width * 0.4f, height * 0.15f),
            cornerRadius = 8f
        )
    }

    // Arms
    val armWidth = width * 0.15f
    val armHeight = height * 0.3f
    val armsHighlighted = if (view == BodyView.FRONT) BodyZone.ARMS_FRONT in highlightedZones else BodyZone.ARMS_BACK in highlightedZones
    val armColor = if (armsHighlighted) highlightColor else baseColor

    // Left Arm
    drawZone(armColor, Offset(width * 0.05f, height * 0.12f), Size(armWidth, armHeight), 100f)
    // Right Arm
    drawZone(armColor, Offset(width * 0.8f, height * 0.12f), Size(armWidth, armHeight), 100f)

    // Legs
    val legWidth = width * 0.2f
    val legHeight = height * 0.45f
    val legsHighlighted = if (view == BodyView.FRONT) BodyZone.LEGS_FRONT in highlightedZones else BodyZone.LEGS_BACK in highlightedZones
    val legColor = if (legsHighlighted) highlightColor else baseColor

    // Left Leg
    drawZone(legColor, Offset(width * 0.25f, height * 0.45f), Size(legWidth, legHeight), 100f)
    // Right Leg
    drawZone(legColor, Offset(width * 0.55f, height * 0.45f), Size(legWidth, legHeight), 100f)
}

private fun DrawScope.drawZone(
    color: Color,
    topLeft: Offset,
    size: Size,
    cornerRadius: Float
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
}
