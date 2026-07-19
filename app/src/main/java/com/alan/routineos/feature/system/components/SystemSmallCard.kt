package com.alan.routineos.feature.system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.model.LifeArea
import com.alan.routineos.feature.system.model.LifeAreaStatus

@Composable
fun SystemSmallCard(
    area: LifeArea.Small,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.height(IntrinsicSize.Min),
        containerColor = RoutineTheme.colors.surface1
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when(area.iconName) {
                    "work" -> Icons.Default.Work
                    "favorite" -> Icons.Default.Favorite
                    "rocket_launch" -> Icons.Default.RocketLaunch
                    "bedtime" -> Icons.Default.Bedtime
                    else -> Icons.Default.Work
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RoutineTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                StatusDot(status = area.status)
            }

            Column {
                Text(
                    text = area.title,
                    style = RoutineTheme.typography.bodyBase,
                    fontWeight = FontWeight.Bold,
                    color = RoutineTheme.colors.onSurface,
                    modifier = Modifier.alpha(if (area.status == LifeAreaStatus.PAUSED) 0.6f else 1f)
                )
                Text(
                    text = area.description,
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusDot(status: LifeAreaStatus) {
    val color = when(status) {
        LifeAreaStatus.ACTIVE -> RoutineTheme.colors.primary
        LifeAreaStatus.PAUSED -> RoutineTheme.colors.onSurfaceVariant
        LifeAreaStatus.TEMPORARY -> RoutineTheme.colors.secondary
        LifeAreaStatus.ARCHIVED -> RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, RoutineTheme.shapes.pill)
    )
}
