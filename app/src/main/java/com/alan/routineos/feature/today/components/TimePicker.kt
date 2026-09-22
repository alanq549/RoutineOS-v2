package com.alan.routineos.feature.today.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePicker(
    state: TimePickerState
) {
    androidx.compose.material3.TimePicker(state = state)
}
