package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutinePrimaryButton
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.model.ActivityCardModel

@Composable
fun ActivityCard(
    activity: ActivityCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = RoutineTheme.colors.surface3,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val (icon, color) = when(activity.iconName) {
                    "school" -> Icons.Default.School to RoutineTheme.colors.secondary
                    "fitness_center" -> Icons.Default.FitnessCenter to RoutineTheme.colors.tertiary
                    "wb_sunny" -> Icons.Default.WbSunny to RoutineTheme.colors.primary
                    else -> Icons.Default.School to RoutineTheme.colors.secondary
                }
                
                BoxWithIcon(icon = icon, color = color)
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = activity.frequency,
                        style = RoutineTheme.typography.labelCaps,
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                    Text(
                        text = activity.durationText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                        color = RoutineTheme.colors.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = activity.title,
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = activity.subtitle,
                style = RoutineTheme.typography.labelCaps,
                color = RoutineTheme.colors.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.statsLine,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoutineTheme.colors.surface1.copy(alpha = 0.5f), RoutineTheme.shapes.small)
                    .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
               activity.summaryItems.forEach { summary ->
                   Column {
                       Row(
                           modifier = Modifier.fillMaxWidth(),
                           horizontalArrangement = Arrangement.SpaceBetween,
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                           Text(
                               text = summary.dayName,
                               style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                               color = RoutineTheme.colors.primary
                           )
                           summary.detailText?.let {
                               Text(
                                   text = it,
                                   style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                                   color = RoutineTheme.colors.onSurfaceVariant
                               )
                           }
                       }
                       
                       if (summary.detailText == null) {
                           summary.activities.forEachIndexed { index, activityName ->
                               Row(
                                   modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                                   verticalAlignment = Alignment.CenterVertically
                               ) {
                                   Text(
                                       text = if (index == summary.activities.lastIndex) "└─" else "├─",
                                       style = RoutineTheme.typography.labelCaps.copy(fontSize = 13.sp),
                                       color = RoutineTheme.colors.border,
                                       modifier = Modifier.padding(top = 2.dp)
                                   )
                                   Spacer(modifier = Modifier.width(8.dp))
                                   Text(
                                       text = activityName,
                                       style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                                       color = RoutineTheme.colors.onSurface.copy(alpha = 0.8f),
                                       maxLines = 1,
                                       overflow = TextOverflow.Ellipsis
                                   )
                               }
                           }
                       }
                   }
               }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoutinePrimaryButton(onClick = onClick) {
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
    Box(
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
