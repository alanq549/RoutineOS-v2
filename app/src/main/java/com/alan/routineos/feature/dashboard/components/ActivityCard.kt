package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.dashboard.model.ActivityCardModel

@Composable
fun ActivityCard(
    activity: ActivityCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColor = Color(android.graphics.Color.parseColor(activity.iconColorHex))

    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = Color(0xFF111721), // Stitch Surface
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = semanticColor.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // TOP HEADER: Icon + Identity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(semanticColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, semanticColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getTechnicalIcon(activity.iconName),
                        contentDescription = null,
                        tint = semanticColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = activity.frequency.uppercase(),
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = activity.durationText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = semanticColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = activity.title,
                style = RoutineTheme.typography.bodyBase.copy(
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White
            )
            
            if (activity.subtitle.isNotBlank()) {
                Text(
                    text = activity.subtitle,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = activity.statsLine.uppercase(),
                style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Black),
                color = semanticColor.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // SUMMARY TREE (Technical Stitch Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B0E14), RoundedCornerShape(14.dp))
                    .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
               activity.summaryItems.forEach { summary ->
                   Column {
                       Text(
                           text = summary.dayName,
                           style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                           color = semanticColor
                       )
                       
                       summary.activities.forEachIndexed { index, activityName ->
                           Row(
                               modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               // Technical Tree Connector
                               Box(
                                   modifier = Modifier
                                       .width(1.5.dp)
                                       .height(14.dp)
                                       .background(RoutineTheme.colors.border.copy(alpha = 0.4f))
                               )
                               Box(
                                   modifier = Modifier
                                       .width(8.dp)
                                       .height(1.5.dp)
                                       .background(RoutineTheme.colors.border.copy(alpha = 0.4f))
                               )
                               Spacer(modifier = Modifier.width(8.dp))
                               Text(
                                   text = activityName,
                                   style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                                   color = RoutineTheme.colors.onSurface.copy(alpha = 0.85f),
                                   maxLines = 1,
                                   overflow = TextOverflow.Ellipsis
                               )
                           }
                       }
                   }
               }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onClick,
                    color = semanticColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, semanticColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "GESTIONAR",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, color = semanticColor, fontWeight = FontWeight.Black)
                    )
                }
                
                IconButton(onClick = { /* Menu */ }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

private fun getTechnicalIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "fitness_center" -> Icons.Default.FitnessCenter
        "school" -> Icons.Default.School
        "work" -> Icons.Default.Work
        "favorite" -> Icons.Default.Favorite
        "bedtime" -> Icons.Default.Bedtime
        "rocket" -> Icons.Default.RocketLaunch
        "account_tree" -> Icons.Default.AccountTree
        "science" -> Icons.Default.Science
        "psychology" -> Icons.Default.Psychology
        "palette" -> Icons.Default.Palette
        else -> Icons.Default.AccountTree
    }
}
