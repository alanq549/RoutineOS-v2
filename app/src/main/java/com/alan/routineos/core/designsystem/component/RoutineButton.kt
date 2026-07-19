package com.alan.routineos.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutinePrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoutineTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = RoutineTheme.colors.primary,
            contentColor = RoutineTheme.colors.onPrimary
        ),
        content = content
    )
}

@Composable
fun RoutineOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoutineTheme.shapes.small,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RoutineTheme.colors.onSurface
        ),
        border = BorderStroke(1.dp, RoutineTheme.colors.border),
        content = content
    )
}
