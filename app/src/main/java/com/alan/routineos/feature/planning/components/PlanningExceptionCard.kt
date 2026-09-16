package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.PlanningItemType
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

@Composable
fun PlanningExceptionCard(
    item: TodayTimelineUiModel,
    modifier: Modifier = Modifier
) {
    val accentColor = when (item.itemType) {
        PlanningItemType.ACTIVITY -> RoutineTheme.colors.roleEvent
        PlanningItemType.TASK -> RoutineTheme.colors.roleTask
        PlanningItemType.REMINDER -> RoutineTheme.colors.roleReminder
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF10151D),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Stitch Solid Edge Stripe (Far Left)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.6f))
                        )
                    )
            )

            Row(
                modifier = Modifier.padding(14.dp).padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HOY",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Surface(
                            color = accentColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "MODIFICADO",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp),
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        style = RoutineTheme.typography.bodyBase.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "${item.timeRangeText} (Horario reprogramado)",
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp, color = RoutineTheme.colors.primary.copy(alpha = 0.8f))
                    )
                }

                // Calendar / Diff icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(RoutineTheme.colors.background, RoundedCornerShape(8.dp))
                        .border(1.dp, RoutineTheme.colors.border, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.EventNote, null, modifier = Modifier.size(16.dp), tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f))
                }
            }
        }
    }
}
