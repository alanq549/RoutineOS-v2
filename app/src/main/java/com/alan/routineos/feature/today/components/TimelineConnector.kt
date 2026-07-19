package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun TimelineConnector(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .background(RoutineTheme.colors.border)
    )
}
