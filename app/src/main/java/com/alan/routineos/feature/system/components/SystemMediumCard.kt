package com.alan.routineos.feature.system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.alan.routineos.feature.system.model.LifeArea
import com.alan.routineos.feature.system.model.LifeAreaStatus

@Composable
fun SystemMediumCard(
    area: LifeArea.Medium,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface3
    ) {
        Box {
            // Background Icon Decoration
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(80.dp)
                    .alpha(0.05f),
                tint = RoutineTheme.colors.onSurface
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        StatusChip(status = area.status)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = area.title,
                            style = RoutineTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RoutineTheme.colors.onSurface
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InfoText(label = "${area.sessionsCount} sesiones")
                        InfoText(label = "${area.exercisesCount} ejercicios")
                        InfoText(label = "${area.templatesCount} plantillas")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = RoutineTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Próxima ejecución: ${area.nextExecution}",
                            style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                            color = RoutineTheme.colors.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = RoutineTheme.colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: LifeAreaStatus) {
    Row(
        modifier = Modifier
            .background(RoutineTheme.colors.primary.copy(alpha = 0.1f), RoutineTheme.shapes.pill)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = RoutineTheme.colors.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Activo",
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.primary
        )
    }
}

@Composable
private fun InfoText(label: String) {
    Text(
        text = label,
        style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
        color = RoutineTheme.colors.onSurfaceVariant
    )
}
