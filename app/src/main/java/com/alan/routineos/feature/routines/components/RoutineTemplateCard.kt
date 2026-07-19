package com.alan.routineos.feature.routines.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.routines.model.RoutineTemplateModel

@Composable
fun RoutineTemplateCard(
    template: RoutineTemplateModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoutineTheme.shapes.medium)
            .background(RoutineTheme.colors.surface1)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image/Icon Placeholder
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoutineTheme.shapes.small)
                .background(RoutineTheme.colors.surface2),
            contentAlignment = Alignment.Center
        ) {
            val (icon, color) = when(template.iconName) {
                "terminal" -> Icons.Default.Terminal to RoutineTheme.colors.primary
                "bedtime" -> Icons.Default.Bedtime to RoutineTheme.colors.tertiary
                else -> Icons.Default.Terminal to RoutineTheme.colors.primary
            }
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color.copy(alpha = 0.1f))
            )
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.title,
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = template.description,
                style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
                color = RoutineTheme.colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { }
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = RoutineTheme.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "USAR PLANTILLA",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
