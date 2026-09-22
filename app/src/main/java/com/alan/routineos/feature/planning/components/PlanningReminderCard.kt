package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.feature.today.model.PlanningItemType
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

/**
 * Card genérica para ítems "flotantes" (sin hora fija): Recordatorio y Tarea.
 * NO usa spine ni gutter de tiempo — standalone, pensada para la sección
 * superior de Planning y para no-agobiar la vista.
 */
@Composable
private fun FloatingItemCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    icon: ImageVector,
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val isOmitted = item.status == DailyInstanceStatus.OMITTED
    val isModified = item.status == DailyInstanceStatus.MODIFIED
    val semanticColor = when (item.itemType) {
        PlanningItemType.ACTIVITY -> RoutineTheme.colors.roleEvent
        PlanningItemType.TASK -> RoutineTheme.colors.roleTask
        PlanningItemType.REMINDER -> RoutineTheme.colors.roleReminder
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF111721), Color(0xFF161F2C))
                )
            )
            .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onAction(item.id, "EDIT_SPONTANEOUS") }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(semanticColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(1.dp, semanticColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = semanticColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = semanticColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            badgeText,
                            style = RoutineTheme.typography.labelCaps.copy(
                                fontSize = 8.sp,
                                color = semanticColor,
                                fontWeight = FontWeight.Black
                            ),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            ActionMenu(item, isOmitted, isModified) { onAction(item.id, it) }
        }
    }
}

@Composable
fun PlanningReminderCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingItemCard(item, onAction, Icons.Default.Notifications, "RECORDATORIO", modifier)
}

@Composable
fun PlanningFloatingTaskCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingItemCard(item, onAction, Icons.Default.Check, "TAREA PINEADA", modifier)
}