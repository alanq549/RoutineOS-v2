package com.alan.routineos.core.designsystem.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

@Immutable
data class RoutineShapes(
    val small: CornerBasedShape = RoundedCornerShape(8.dp),
    val medium: CornerBasedShape = RoundedCornerShape(16.dp),
    val large: CornerBasedShape = RoundedCornerShape(24.dp),
    val pill: CornerBasedShape = RoundedCornerShape(999.dp)
)

val LocalRoutineShapes = staticCompositionLocalOf {
    RoutineShapes()
}
