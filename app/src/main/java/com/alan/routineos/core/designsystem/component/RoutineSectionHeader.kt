package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = RoutineTheme.spacing.md)
    ) {
        Text(
            text = title,
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}
