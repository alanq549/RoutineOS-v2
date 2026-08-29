package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed

fun LazyListScope.todayTimelineItems(
    items: List<TodayTimelineUiModel>,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit
) {
    itemsIndexed(
        items = items,
        key = { _, item -> item.id }
    ) { index, item ->
        TimelineRow(
            item = item,
            isLast = index == items.lastIndex,
            onAction = onAction,
            onExpandClick = onExpandClick
        )
    }
}

@Composable
fun TimelineRow(
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
