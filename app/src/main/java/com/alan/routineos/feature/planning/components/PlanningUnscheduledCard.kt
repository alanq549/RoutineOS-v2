package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.model.PlanningUnscheduled

@Composable
fun PlanningUnscheduledCard(
    item: PlanningUnscheduled,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface1,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when(item.icon) {
                    "rocket_launch" -> Icons.Default.RocketLaunch
                    "auto_stories" -> Icons.Default.AutoStories
                    else -> Icons.Default.RocketLaunch
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (item.icon == "rocket_launch") RoutineTheme.colors.primary else RoutineTheme.colors.secondary,
                    modifier = Modifier.size(24.dp)
                )
                
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Drag to schedule",
                    tint = RoutineTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
            
            Text(
                text = item.title,
                style = RoutineTheme.typography.bodyBase,
                color = RoutineTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = item.description,
                style = RoutineTheme.typography.bodyBase.copy(fontSize = 11.sp),
                color = RoutineTheme.colors.onSurfaceVariant
            )
        }
    }
}
