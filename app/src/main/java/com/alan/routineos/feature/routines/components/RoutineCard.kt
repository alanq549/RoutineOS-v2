package com.alan.routineos.feature.routines.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutinePrimaryButton
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.routines.model.RoutineCardModel

@Composable
fun RoutineBentoCard(
    routine: RoutineCardModel,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface3,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val (icon, color) = when(routine.iconName) {
                    "school" -> Icons.Default.School to RoutineTheme.colors.secondary
                    "fitness_center" -> Icons.Default.FitnessCenter to RoutineTheme.colors.tertiary
                    "wb_sunny" -> Icons.Default.WbSunny to RoutineTheme.colors.primary
                    else -> Icons.Default.School to RoutineTheme.colors.secondary
                }
                
                BoxWithIcon(icon = icon, color = color)
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = routine.frequency,
                        style = RoutineTheme.typography.labelCaps,
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = routine.durationText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                        color = RoutineTheme.colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = routine.title,
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = routine.subtitle,
                style = RoutineTheme.typography.labelCaps,
                color = RoutineTheme.colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoutineTheme.colors.surface1.copy(alpha = 0.5f), RoutineTheme.shapes.small)
                    .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
               routine.summaryItems.forEach { summary ->
                   if (summary.dayName.isNotEmpty()) {
                       Text(
                           text = summary.dayName,
                           style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                           color = RoutineTheme.colors.primary
                       )
                   }
                   summary.activities.forEach { activity ->
                       Text(
                           text = "├ $activity",
                           style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                           color = RoutineTheme.colors.onSurface,
                           modifier = Modifier.alpha(0.8f)
                       )
                   }
               }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoutinePrimaryButton(onClick = { }) {
                    Text("ABRIR", style = RoutineTheme.typography.labelCaps)
                }
                
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = RoutineTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun BoxWithIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(48.dp)
            .background(color.copy(alpha = 0.2f), RoutineTheme.shapes.medium),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(30.dp)
        )
    }
}
