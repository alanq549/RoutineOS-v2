package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.today.model.TodayProgress

@Composable
fun TodayHeader(
    dateText: String,
    progress: TodayProgress,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dateText.uppercase(),
            style = RoutineTheme.typography.labelCaps,
            color = RoutineTheme.colors.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tu Progreso",
            style = RoutineTheme.typography.displayLarge.copy(fontSize = 32.sp),
            color = RoutineTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        TodayProgressCircle(progress = progress)
    }
}
