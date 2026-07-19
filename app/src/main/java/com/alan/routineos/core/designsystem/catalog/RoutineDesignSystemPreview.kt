package com.alan.routineos.core.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineChip
import com.alan.routineos.core.designsystem.component.RoutineOutlinedButton
import com.alan.routineos.core.designsystem.component.RoutinePrimaryButton
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineDesignSystemCatalog() {
    RoutineTheme {
        RoutineScaffold(
            topBar = {
                RoutineSectionHeader(
                    title = "DESIGN SYSTEM CATALOG",
                    modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(RoutineTheme.spacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)
            ) {
                // Typography
                RoutineSectionHeader(title = "TYPOGRAPHY")
                Text("Display Large", style = RoutineTheme.typography.displayLarge)
                Text("Headline Medium", style = RoutineTheme.typography.headlineMedium)
                Text("Body Base", style = RoutineTheme.typography.bodyBase)
                Text("Data Large (Mono)", style = RoutineTheme.typography.dataLarge)
                Text("LABEL CAPS", style = RoutineTheme.typography.labelCaps)

                // Buttons
                RoutineSectionHeader(title = "BUTTONS")
                Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                    RoutinePrimaryButton(onClick = {}) {
                        Text("Primary Action")
                    }
                    RoutineOutlinedButton(onClick = {}) {
                        Text("Secondary")
                    }
                }

                // Cards & Chips
                RoutineSectionHeader(title = "CARDS & CHIPS")
                RoutineCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
                        Text("This is a RoutineCard", style = RoutineTheme.typography.headlineMedium)
                        Text(
                            "Cards use Surface1 and a 1px border by default.",
                            style = RoutineTheme.typography.bodyBase
                        )
                        Row(
                            modifier = Modifier.padding(top = RoutineTheme.spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.xs)
                        ) {
                            RoutineChip(label = "TAG ONE")
                            RoutineChip(label = "TAG TWO")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun RoutineDesignSystemCatalogPreview() {
    RoutineDesignSystemCatalog()
}
