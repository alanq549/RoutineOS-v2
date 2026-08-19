package com.alan.routineos.feature.system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun SystemLargeCard(
    area: LifeArea.Large,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface3
    ) {
        Box {
            // Background Icon Decoration
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(120.dp)
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
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = RoutineTheme.colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoItem(label = "${area.routinesCount} rutinas")
                        InfoItem(label = "${area.subjectsCount} materias")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.EventRepeat,
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
private fun InfoItem(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowForward, // Mocking subdirectory_arrow_right
            contentDescription = null,
            tint = RoutineTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}
