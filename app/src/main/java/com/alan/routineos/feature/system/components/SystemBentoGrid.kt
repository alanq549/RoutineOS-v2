package com.alan.routineos.feature.system.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.model.LifeArea

@Composable
fun SystemBentoGrid(
    lifeAreas: List<LifeArea>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
    ) {
        val largeAreas = lifeAreas.filterIsInstance<LifeArea.Large>()
        val mediumAreas = lifeAreas.filterIsInstance<LifeArea.Medium>()
        val smallAreas = lifeAreas.filterIsInstance<LifeArea.Small>()

        largeAreas.forEach { area ->
            SystemLargeCard(area = area)
        }

        mediumAreas.forEach { area ->
            SystemMediumCard(area = area)
        }

        // Small cards in a 2-column grid for mobile
        smallAreas.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
            ) {
                pair.forEach { area ->
                    SystemSmallCard(
                        area = area,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
