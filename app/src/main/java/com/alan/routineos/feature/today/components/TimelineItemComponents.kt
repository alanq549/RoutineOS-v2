package com.alan.routineos.feature.today.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.*
import com.alan.routineos.feature.today.model.*

@Composable
fun StatusIcon(
    status: DailyInstanceStatus, 
    size: androidx.compose.ui.unit.Dp = 24.dp, 
    isHierarchyCompleted: Boolean = false
) {
    val (icon, color) = when {
        status == DailyInstanceStatus.COMPLETED || isHierarchyCompleted -> Icons.Default.CheckCircle to RoutineTheme.colors.primary
        status == DailyInstanceStatus.MODIFIED -> Icons.Default.Sync to RoutineTheme.colors.secondary
        status == DailyInstanceStatus.OMITTED -> Icons.Default.Block to RoutineTheme.colors.onSurfaceVariant
        else -> Icons.Default.Schedule to RoutineTheme.colors.onSurfaceVariant
    }
    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(size))
}

@Composable
fun MetadataLabel(name: String, value: String, isContext: Boolean) {
    Text(
        text = if (isContext) "$name: $value" else "$name ($value)",
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
        color = if (isContext) RoutineTheme.colors.primary.copy(alpha = 0.8f) else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun RelationshipBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoutineTheme.shapes.pill,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

fun getRelationLabel(detail: ConflictDetailUiModel): String {
    val verb = when {
        detail.isInterruption -> "INTERRUMPE"
        detail.relationship == TemporalRelationship.CONTAINS -> "DENTRO DE"
        detail.relationship == TemporalRelationship.CONTAINED_BY -> "DENTRO DE"
        detail.relationship == TemporalRelationship.OVERLAP -> "SE CRUZA CON"
        else -> "RELACIÓN"
    }
    return "$verb · ${detail.otherTitle}"
}

fun Modifier.drawThreadLine(color: Color): Modifier = this.drawBehind {
    drawLine(color = color, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 1.dp.toPx())
}
