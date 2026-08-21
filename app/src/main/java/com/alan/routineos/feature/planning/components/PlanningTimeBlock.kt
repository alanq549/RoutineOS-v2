package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

@Composable
fun PlanningTimeBlock(
    item: TodayTimelineUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Axis (Stitch V3 style: Left 80dp)
        Column(
            modifier = Modifier.width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.timeRangeText,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                color = RoutineTheme.colors.primary
            )
            
            // Vertical Line (The "Axis")
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(1.dp)
                    .height(40.dp)
                    .background(RoutineTheme.colors.border)
            )

            Icon(
                imageVector = Icons.Default.Bedtime,
                contentDescription = null,
                tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content Card (Glass effect style)
        RoutineCard(
            modifier = Modifier.weight(1f),
            containerColor = RoutineTheme.colors.surface3,
            border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge: Flexible vs Exact (Simplified logic for now)
                    Surface(
                        color = RoutineTheme.colors.primary.copy(alpha = 0.1f),
                        shape = RoutineTheme.shapes.pill
                    ) {
                        Text(
                            text = "FLEXIBLE",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                            color = RoutineTheme.colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.title,
                    style = RoutineTheme.typography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = RoutineTheme.colors.onSurface
                )
                
                if (item.hasConflict) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Conflicto detectado",
                        style = RoutineTheme.typography.bodyBase.copy(fontSize = 11.sp),
                        color = RoutineTheme.colors.error
                    )
                }

                when (item.status) {
                    DailyInstanceStatus.OMITTED -> {
                        Text(
                            text = "SALTADO",
                            style = RoutineTheme.typography.labelCaps,
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    DailyInstanceStatus.COMPLETED -> {
                        Text(
                            text = "COMPLETADO",
                            style = RoutineTheme.typography.labelCaps,
                            color = RoutineTheme.colors.primary.copy(alpha = 0.7f)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
