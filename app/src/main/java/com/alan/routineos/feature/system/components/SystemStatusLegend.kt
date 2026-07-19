package com.alan.routineos.feature.system.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun SystemStatusLegend(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "ESTADOS DE SISTEMA:",
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(icon = Icons.Default.CheckCircle, label = "Activo", color = RoutineTheme.colors.primary)
            LegendItem(icon = Icons.Default.PauseCircle, label = "Pausado", color = RoutineTheme.colors.onSurfaceVariant)
            LegendItem(icon = Icons.Default.Schedule, label = "Temporal", color = RoutineTheme.colors.secondary)
            LegendItem(icon = Icons.Default.Archive, label = "Archivado", color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun LegendItem(icon: ImageVector, label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = RoutineTheme.typography.bodyBase.copy(fontSize = 12.sp),
            color = RoutineTheme.colors.onSurface
        )
    }
}
