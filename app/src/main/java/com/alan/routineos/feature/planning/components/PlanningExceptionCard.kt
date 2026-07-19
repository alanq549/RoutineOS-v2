package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
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
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.model.PlanningException

@Composable
fun PlanningExceptionCard(
    exception: PlanningException,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface3,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.tertiary.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopEnd)
                    .alpha(0.1f),
                tint = RoutineTheme.colors.onSurface
            )

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = exception.dateText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.size(RoutineTheme.spacing.sm))
                    Text(
                        text = exception.label,
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                        color = RoutineTheme.colors.secondary,
                        modifier = Modifier
                            .background(
                                RoutineTheme.colors.secondary.copy(alpha = 0.1f),
                                RoutineTheme.shapes.pill
                            )
                            .border(
                                1.dp,
                                RoutineTheme.colors.secondary.copy(alpha = 0.2f),
                                RoutineTheme.shapes.pill
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                Text(
                    text = exception.title,
                    style = RoutineTheme.typography.headlineMedium.copy(fontSize = 16.sp),
                    color = RoutineTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = exception.timeRange,
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                    color = RoutineTheme.colors.primary
                )
            }
        }
    }
}
