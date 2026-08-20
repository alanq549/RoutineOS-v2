package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun PlanningUnscheduledCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.medium)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = RoutineTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag",
                tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = title,
            style = RoutineTheme.typography.bodyBase,
            color = RoutineTheme.colors.onSurface
        )
        Text(
            text = description,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}
