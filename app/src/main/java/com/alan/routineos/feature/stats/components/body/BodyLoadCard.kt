package com.alan.routineos.feature.stats.components.body

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.stats.model.BodyLoadLevel
import com.alan.routineos.feature.stats.model.BodyLoadUiModel
import com.alan.routineos.feature.stats.model.BodyView

@Composable
fun BodyLoadCard(
    state: BodyLoadUiModel,
    modifier: Modifier = Modifier
) {
    var selectedView by remember { mutableStateOf(BodyView.FRONT) }

    RoutineCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            HeaderSection(state = state)

            Spacer(modifier = Modifier.height(24.dp))

            ViewSelector(
                selectedView = selectedView,
                onViewSelected = { selectedView = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isEmpty) {
                EmptyStateSection()
            } else {
                MainContent(state = state, view = selectedView)
            }
        }
    }
}

@Composable
private fun HeaderSection(state: BodyLoadUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "CARGA CORPORAL",
                style = RoutineTheme.typography.labelCaps,
                color = RoutineTheme.colors.onSurfaceVariant
            )
            Text(
                text = state.contextLabel,
                style = RoutineTheme.typography.headlineMedium,
                color = RoutineTheme.colors.onSurface
            )
        }
        
        if (state.isWarning) {
            Box(
                modifier = Modifier
                    .background(RoutineTheme.colors.error.copy(alpha = 0.1f), RoutineTheme.shapes.small)
                    .border(1.dp, RoutineTheme.colors.error.copy(alpha = 0.2f), RoutineTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RoutineTheme.colors.error, modifier = Modifier.size(14.dp))
                    Text(
                        text = "RECUPERACIÓN NECESARIA",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                        color = RoutineTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewSelector(
    selectedView: BodyView,
    onViewSelected: (BodyView) -> Unit
) {
    Row(
        modifier = Modifier
            .width(160.dp)
            .background(RoutineTheme.colors.surface2, RoutineTheme.shapes.pill)
            .padding(2.dp)
    ) {
        ViewButton(
            title = "Frente",
            isSelected = selectedView == BodyView.FRONT,
            onClick = { onViewSelected(BodyView.FRONT) },
            modifier = Modifier.weight(1f)
        )
        ViewButton(
            title = "Espalda",
            isSelected = selectedView == BodyView.BACK,
            onClick = { onViewSelected(BodyView.BACK) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ViewButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoutineTheme.shapes.pill)
            .background(if (isSelected) RoutineTheme.colors.surface3 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
            color = if (isSelected) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun MainContent(state: BodyLoadUiModel, view: BodyView) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Silhouette
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            BodyLoadSilhouette(view = view, highlightedZones = state.highlightedZones)
        }

        // Right: Metrics
        Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricHighlight(label = "ZONA DOMINANTE", value = state.dominantZone)
            MetricHighlight(label = "ZONAS ACTIVAS", value = "${state.activeZonesCount} áreas")
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("RANKING DE CARGA", style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.rankedZones.forEach { zone ->
                    ZoneRankRow(zone = zone)
                }
            }
            
            if (state.recoveryInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.recoveryInfo,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 12.sp),
                    color = if (state.isWarning) RoutineTheme.colors.error else RoutineTheme.colors.primary
                )
            }
        }
    }
}

@Composable
private fun MetricHighlight(label: String, value: String) {
    Column {
        Text(text = label, style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = RoutineTheme.colors.onSurfaceVariant)
        Text(text = value, style = RoutineTheme.typography.dataLarge.copy(fontSize = 14.sp), color = RoutineTheme.colors.primary)
    }
}

@Composable
private fun ZoneRankRow(zone: com.alan.routineos.feature.stats.model.ZoneRankItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = zone.name, style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.width(60.dp).height(4.dp).clip(RoutineTheme.shapes.pill).background(RoutineTheme.colors.surface2)) {
                Box(modifier = Modifier.fillMaxWidth(zone.percentage / 100f).fillMaxHeight().background(RoutineTheme.colors.primary))
            }
            Text(text = "${zone.percentage}%", style = RoutineTheme.typography.dataLarge.copy(fontSize = 11.sp), color = RoutineTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyStateSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No se registró carga corporal en este periodo.",
            style = RoutineTheme.typography.bodyBase,
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}
