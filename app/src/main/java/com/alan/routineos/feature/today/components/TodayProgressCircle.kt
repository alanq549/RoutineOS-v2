package com.alan.routineos.feature.today.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayProgress

@Composable
fun TodayProgressCircle(
    progress: TodayProgress,
    modifier: Modifier = Modifier
) {
    val sweepAngle = if (progress.total > 0) {
        (progress.completed.toFloat() / progress.total.toFloat()) * 360f
    } else 0f

    val strokeWidth = 8.dp
    val primaryColor = RoutineTheme.colors.primary
    val trackColor = RoutineTheme.colors.surface2

    Box(
        modifier = modifier.size(132.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(116.dp)) {
            // Track
            drawCircle(
                color = trackColor,
                radius = size.minDimension / 2,
                style = Stroke(width = strokeWidth.toPx())
            )
            // Progress
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${progress.completed}/${progress.total}",
                style = RoutineTheme.typography.dataLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = RoutineTheme.colors.onSurface
            )
            Text(
                text = "Completadas",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                color = RoutineTheme.colors.onSurfaceVariant
            )
        }
    }
}
