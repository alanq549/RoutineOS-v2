package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun PlanningUnscheduledCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoutineTheme.shapes.medium)
            .background(RoutineTheme.colors.surface1)
            .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.medium)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = RoutineTheme.colors.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Posicionar",
                tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
            color = RoutineTheme.colors.onSurface,
            maxLines = 1
        )
        Text(
            text = description,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}
