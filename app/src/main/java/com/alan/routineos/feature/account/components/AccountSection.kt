package com.alan.routineos.feature.account.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.feature.account.model.AccountSectionModel

@Composable
fun AccountSection(
    section: AccountSectionModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        RoutineSectionHeader(title = section.title)
        RoutineCard {
            Column {
                section.items.forEach { item ->
                    AccountSettingRow(
                        item = item,
                        onClick = { }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
