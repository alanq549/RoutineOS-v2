package com.alan.routineos.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.TrendSeries

@Composable
fun MetadataLineChart(
    series: TrendSeries,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = "Evolución: ${series.fieldName}",
            style = RoutineTheme.typography.headlineMedium,
            color = RoutineTheme.colors.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (series.values.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin datos numéricos capturados",
                    style = RoutineTheme.typography.bodyBase,
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            val primaryColor = RoutineTheme.colors.primary
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val maxVal = series.values.maxOf { it.second }.toFloat().coerceAtLeast(1f)
                val minVal = series.values.minOf { it.second }.toFloat().coerceAtMost(0f)
                val range = (maxVal - minVal).coerceAtLeast(1f)
                
                val width = size.width
                val height = size.height
                val stepX = width / (series.values.size - 1).coerceAtLeast(1)
                
                val path = Path()
                series.values.forEachIndexed { index, pair ->
                    val x = index * stepX
                    val y = height - ((pair.second.toFloat() - minVal) / range * height)
                    
                    if (index == 0) path.moveTo(x, y)
                    else path.lineTo(x, y)
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
                
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
