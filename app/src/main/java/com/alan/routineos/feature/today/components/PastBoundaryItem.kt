package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun PastBoundaryItem(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(end = 12.dp)
        )
        
        Text(
            text = "↑ $count actividades anteriores",
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(start = 12.dp)
        )
    }
}
