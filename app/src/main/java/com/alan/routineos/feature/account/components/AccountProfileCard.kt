package com.alan.routineos.feature.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.account.model.UserProfile

@Composable
fun AccountProfileCard(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    RoutineCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = RoutineTheme.colors.surface3
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoutineTheme.shapes.pill)
                        .background(RoutineTheme.colors.surface1)
                        .border(
                            2.dp,
                            RoutineTheme.colors.primary,
                            RoutineTheme.shapes.pill
                        )
                )
                
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoutineTheme.shapes.pill)
                        .background(RoutineTheme.colors.primary)
                        .border(
                            2.dp,
                            RoutineTheme.colors.background,
                            RoutineTheme.shapes.pill
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = RoutineTheme.colors.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    style = RoutineTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoutineTheme.colors.onSurface
                )
                Text(
                    text = profile.email,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "EDITAR PERFIL",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp),
                    color = RoutineTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }
}
