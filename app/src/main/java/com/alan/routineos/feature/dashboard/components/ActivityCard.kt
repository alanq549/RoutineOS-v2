package com.alan.routineos.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
    val semanticColor = try { Color(android.graphics.Color.parseColor(activity.iconColorHex)) } catch (e: Exception) { RoutineTheme.colors.primary }

    RoutineCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = Color(0xFF0B0E14), // Deep background for layering
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = semanticColor.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // TOP HEADER: Icon + Identity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(semanticColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, semanticColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getTechnicalIcon(activity.iconName),
                            contentDescription = null,
                            tint = semanticColor.copy(alpha = 0.9f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = activity.systemTitle ?: "SIN SISTEMA",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, color = semanticColor, fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = activity.title,
                            style = RoutineTheme.typography.headlineMedium.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = activity.frequency.uppercase(),
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = activity.durationText,
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = semanticColor
                    )
                }
            }
            
            if (activity.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = activity.subtitle,
                    style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 58.dp)
                )
            }
            
            if (activity.statsLine.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = activity.statsLine.uppercase(),
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 0.5.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 58.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // SUMMARY TREE (Compact Technical Layout)
            if (activity.summaryItems.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF070A0F), RoundedCornerShape(12.dp))
                        .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                   activity.summaryItems.forEach { summary ->
                       Column {
                           Row(
                               modifier = Modifier.fillMaxWidth(),
                               horizontalArrangement = Arrangement.SpaceBetween,
                               verticalAlignment = Alignment.CenterVertically
                           ) {
                               Text(
                                   text = summary.dayName.uppercase(),
                                   style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                   color = semanticColor.copy(alpha = 0.9f)
                               )
                           }
                           
                           summary.activities.forEach { activityName ->
                               Row(
                                   modifier = Modifier.padding(start = 8.dp, top = 6.dp),
                                   verticalAlignment = Alignment.CenterVertically
                               ) {
                                   // Technical Tree Connector (Circuit Style)
                                   Box(
                                       modifier = Modifier
                                           .width(1.5.dp)
                                           .height(14.dp)
                                           .background(RoutineTheme.colors.border.copy(alpha = 0.3f))
                                   )
                                   Box(
                                       modifier = Modifier
                                           .width(8.dp)
                                           .height(1.5.dp)
                                           .background(RoutineTheme.colors.border.copy(alpha = 0.3f))
                                   )
                                   Spacer(modifier = Modifier.width(10.dp))
                                   
                                   // CIRCLE INDICATOR (Stitch style)
                                   Box(
                                       modifier = Modifier.size(6.dp).background(semanticColor.copy(alpha = 0.5f), CircleShape)
                                   )
                                   Spacer(modifier = Modifier.width(10.dp))

                                   Text(
                                       text = activityName,
                                       style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                                       color = RoutineTheme.colors.onSurface.copy(alpha = 0.8f),
                                       maxLines = 1,
                                       overflow = TextOverflow.Ellipsis,
                                       modifier = Modifier.weight(1f)
                                   )
                               }
                           }
                       }
                   }

                   if (activity.moreDaysCount > 0) {
                       Row(
                           verticalAlignment = Alignment.CenterVertically,
                           modifier = Modifier.padding(top = 4.dp)
                       ) {
                           Icon(
                               imageVector = Icons.Default.ChevronRight,
                               contentDescription = null,
                               tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
                               modifier = Modifier.size(14.dp)
                           )
                           Spacer(modifier = Modifier.width(8.dp))
                           Text(
                               text = "+ ${activity.moreDaysCount} DÍAS",
                               style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                               color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                           )
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
                // Technical Control: ABRIR
                Surface(
                    onClick = onClick,
                    color = RoutineTheme.colors.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ABRIR",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 12.sp, fontWeight = FontWeight.Black),
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getTechnicalIcon(name: String): ImageVector {
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
