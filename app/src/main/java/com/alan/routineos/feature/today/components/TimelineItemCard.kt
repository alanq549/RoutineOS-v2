package com.alan.routineos.feature.today.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alan.routineos.feature.today.model.*

/**
 * Main dispatcher for Timeline items.
 * Decides whether to render a standard card or an interception container.
 */
@Composable
fun TimelineItemCard(
    item: TodayTimelineUiModel,
    onAction: (String, String) -> Unit,
    onExpandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (item.interception != null) {
        InterceptionContainer(
            victim = item,
            interception = item.interception,
            onAction = onAction,
            onExpandClick = onExpandClick,
            modifier = modifier
        )
    } else {
        NormalTimelineCard(
            item = item,
            onAction = onAction,
            onExpandClick = onExpandClick,
            modifier = modifier
        )
    }
}
