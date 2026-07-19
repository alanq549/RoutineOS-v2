package com.alan.routineos.feature.planning.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.alan.routineos.feature.planning.model.PlanningBlock
import com.alan.routineos.feature.planning.model.PlanningBlockType

@Composable
fun PlanningTimeBlock(
    block: PlanningBlock,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Time Axis
        Column(
            modifier = Modifier.width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val timeColor = if (block.type == PlanningBlockType.EXACT) RoutineTheme.colors.tertiary else RoutineTheme.colors.primary
            
            Text(
                text = block.startTime,
                style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                color = timeColor
            )
            
            if (block.endTime != null) {
                Box(
                    modifier = Modifier
                        .padding(vertical = RoutineTheme.spacing.xs)
                        .width(1.dp)
                        .height(24.dp)
                        .background(RoutineTheme.colors.border)
                )
                Text(
                    text = block.endTime,
                    style = RoutineTheme.typography.dataLarge.copy(fontSize = 13.sp),
                    color = timeColor
                )
            } else {
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.xs))
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = timeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(RoutineTheme.spacing.md))

        // Content Card
        RoutineCard(
            modifier = Modifier.weight(1f),
            containerColor = RoutineTheme.colors.surface3,
            border = if (block.type == PlanningBlockType.EXACT) {
                androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.tertiary.copy(alpha = 0.5f))
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border)
            }
        ) {
            Row(
                modifier = Modifier.padding(RoutineTheme.spacing.md),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val labelBackground = if (block.type == PlanningBlockType.EXACT) {
                        RoutineTheme.colors.tertiary.copy(alpha = 0.1f)
                    } else {
                        RoutineTheme.colors.primary.copy(alpha = 0.1f)
                    }
                    val labelColor = if (block.type == PlanningBlockType.EXACT) {
                        RoutineTheme.colors.tertiary
                    } else {
                        RoutineTheme.colors.primary
                    }

                    Text(
                        text = if (block.type == PlanningBlockType.EXACT) "HORA EXACTA" else "FLEXIBLE",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                        color = labelColor,
                        modifier = Modifier
                            .background(labelBackground, RoutineTheme.shapes.pill)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(RoutineTheme.spacing.xs))
                    Text(
                        text = block.title,
                        style = RoutineTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        color = RoutineTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    block.description?.let {
                        Text(
                            text = it,
                            style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                    }

                    if (block.location != null) {
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(RoutineTheme.colors.surface1, RoutineTheme.shapes.small)
                                .border(1.dp, RoutineTheme.colors.border, RoutineTheme.shapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = RoutineTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = block.location,
                                style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                                color = RoutineTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    if (block.conflictMessage != null) {
                        Spacer(modifier = Modifier.height(RoutineTheme.spacing.sm))
                        Row(
                            modifier = Modifier
                                .background(RoutineTheme.colors.error.copy(alpha = 0.1f), RoutineTheme.shapes.small)
                                .border(1.dp, RoutineTheme.colors.error.copy(alpha = 0.2f), RoutineTheme.shapes.small)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = RoutineTheme.colors.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = block.conflictMessage,
                                style = RoutineTheme.typography.bodyBase.copy(fontSize = 10.sp),
                                color = RoutineTheme.colors.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
