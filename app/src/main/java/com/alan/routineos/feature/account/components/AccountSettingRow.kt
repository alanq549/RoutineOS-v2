package com.alan.routineos.feature.account.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.account.model.SettingItem

@Composable
fun AccountSettingRow(
    item: SettingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(RoutineTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
        ) {
            val icon = getIconForName(item.iconName)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RoutineTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    text = item.title,
                    style = RoutineTheme.typography.bodyBase,
                    color = RoutineTheme.colors.onSurface
                )
                item.subtitle?.let {
                    Text(
                        text = it,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = RoutineTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun getIconForName(name: String): ImageVector {
    return when (name) {
        "palette" -> Icons.Default.Palette
        "notifications" -> Icons.Default.Notifications
        "accessibility" -> Icons.Default.Accessibility
        "lock" -> Icons.Default.Lock
        "database" -> Icons.Default.Storage
        "info" -> Icons.Default.Info
        "cloud_upload" -> Icons.Default.CloudUpload
        else -> Icons.Default.Settings
    }
}
