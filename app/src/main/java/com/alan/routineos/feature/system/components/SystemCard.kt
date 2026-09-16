package com.alan.routineos.feature.system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.system.model.LifeArea
import com.alan.routineos.feature.system.model.LifeAreaStatus

@Composable
fun SystemCard(
    area: LifeArea,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColor = Color(android.graphics.Color.parseColor(area.colorHex))
    val isArchived = area.status == LifeAreaStatus.ARCHIVED

    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .alpha(if (isArchived) 0.6f else 1f),
        containerColor = Color(0xFF111721), // Stitch Surface
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = semanticColor.copy(alpha = 0.25f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Stitch Technical Glow (Atmospheric)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(semanticColor.copy(alpha = 0.08f), Color.Transparent),
                            radius = 400f
                        )
                    )
            )

            Column(modifier = Modifier.padding(20.dp)) {
                // HEADER: Icon + Title + Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(semanticColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, semanticColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = getTechnicalIcon(area.iconName)
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = semanticColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = area.title,
                            style = RoutineTheme.typography.bodyBase.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${area.activityCount} rutinas vinculadas",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = { /* Menu */ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, null, tint = RoutineTheme.colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // PERFORMANCE: Success Rate Logic
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RENDIMIENTO SEMANAL",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                        
                        Text(
                            text = if (area.successRate != null) "${(area.successRate * 100).toInt()}%" else "SIN DATOS",
                            style = RoutineTheme.typography.dataLarge.copy(
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (area.successRate != null) semanticColor else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Technical Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(RoutineTheme.colors.border.copy(alpha = 0.3f))
                    ) {
                        if (area.successRate != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(area.successRate)
                                    .fillMaxHeight()
                                    .background(semanticColor)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // FOOTER: Stats Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${area.completedCount}",
                            style = RoutineTheme.typography.dataLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = " / ${area.completedCount + area.skippedCount} completadas",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (area.pendingCount > 0) {
                        Surface(
                            color = RoutineTheme.colors.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${area.pendingCount} PENDIENTES",
                                style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                color = RoutineTheme.colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getTechnicalIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "fitness", "gym" -> Icons.Default.FitnessCenter
        "school", "study" -> Icons.Default.School
        "work" -> Icons.Default.Work
        "favorite" -> Icons.Default.Favorite
        "bedtime" -> Icons.Default.Bedtime
        "rocket" -> Icons.Default.RocketLaunch
        else -> Icons.Default.AccountTree
    }
}
