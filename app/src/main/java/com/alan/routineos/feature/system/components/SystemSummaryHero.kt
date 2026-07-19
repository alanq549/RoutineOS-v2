package com.alan.routineos.feature.system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.model.SystemSummary

@Composable
fun SystemSummaryHero(
    summary: SystemSummary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Sistemas",
                    style = RoutineTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoutineTheme.colors.onSurface
                )
                Text(
                    text = "Tus áreas activas",
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
        ) {
            RoutineCard(
                modifier = Modifier.weight(1f),
                containerColor = RoutineTheme.colors.surface3
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryItem(count = summary.systemsCount.toString(), label = "SISTEMAS")
                    Divider()
                    SummaryItem(count = summary.routinesCount.toString(), label = "RUTINAS")
                    Divider()
                    SummaryItem(count = summary.activitiesCount.toString(), label = "ACTS")
                }
            }

            Button(
                onClick = { },
                modifier = Modifier.height(56.dp),
                shape = RoutineTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoutineTheme.colors.primary,
                    contentColor = RoutineTheme.colors.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun SummaryItem(count: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = count,
            style = RoutineTheme.typography.dataLarge,
            color = RoutineTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .size(width = 1.dp, height = 12.dp)
            .background(RoutineTheme.colors.border)
    )
}
