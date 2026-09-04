package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun FocusPieChart(
    modifier: Modifier = Modifier
) {
    // Current policy: N/A while actualDuration is missing in ActivityExecution
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                style = Stroke(width = 20.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "N/A",
                style = RoutineTheme.typography.displayLarge,
                color = RoutineTheme.colors.onSurfaceVariant
            )
            Text(
                "Distribución de tiempo",
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        
        // Information about missing data
        Text(
            "Captura de duración real no disponible",
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
            color = RoutineTheme.colors.primary.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}
