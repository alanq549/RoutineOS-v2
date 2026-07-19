package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayTimelineItem

@Composable
fun TodayTimeline(
    items: List<TodayTimelineItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            TimelineRow(
                item = item,
                isLast = index == items.lastIndex
            )
            if (index != items.lastIndex) {
                Spacer(modifier = Modifier.height(RoutineTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun TimelineRow(
    item: TodayTimelineItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Vertical Axis Column
        Column(
            modifier = Modifier
                .padding(horizontal = RoutineTheme.spacing.md)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimelineNode(status = item.status, modifier = Modifier.padding(top = RoutineTheme.spacing.md))
            if (!isLast) {
                TimelineConnector(modifier = Modifier.weight(1f))
            }
        }

        // Card Column
        Box(modifier = Modifier.weight(1f).padding(bottom = RoutineTheme.spacing.sm)) {
            TimelineItemCard(item = item)
        }
    }
}
