package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

@Composable
fun TodayTimeline(
    items: List<TodayTimelineUiModel>,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            TimelineRow(
                item = item,
                isLast = index == items.lastIndex,
                onAction = onAction,
                onExpandClick = onExpandClick
            )
        }
    }
}

@Composable
private fun TimelineRow(
    item: TodayTimelineUiModel,
    isLast: Boolean,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Axis Column (Stitch V3 style)
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(RoutineTheme.colors.border)
                )
            }
            
            TimelineNode(
                status = item.status, 
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Card Column
        Box(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            TimelineItemCard(
                item = item, 
                onAction = onAction,
                onExpandClick = onExpandClick
            )
        }
    }
}
