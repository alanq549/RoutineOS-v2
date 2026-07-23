package com.alan.routineos.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alan.routineos.core.designsystem.component.RoutineBottomBar
import com.alan.routineos.core.designsystem.component.RoutineCard
import com.alan.routineos.core.designsystem.component.RoutineChip
import com.alan.routineos.core.designsystem.component.RoutineOutlinedButton
import com.alan.routineos.core.designsystem.component.RoutinePrimaryButton
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.component.RoutineSectionHeader
import com.alan.routineos.core.designsystem.component.RoutineTopBar
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@Composable
fun RoutineDesignSystemCatalog() {
    RoutineTheme {
        RoutineScaffold(
            topBar = {
                RoutineTopBar(
                    title = {
                        Text(
                            "DESIGN SYSTEM CATALOG",
                            style = RoutineTheme.typography.headlineMedium,
                            color = RoutineTheme.colors.onSurface
                        )
                    }
                )
            },
            bottomBar = {
                RoutineBottomBar {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "REFINED FOUNDATIONS — V2",
                            style = RoutineTheme.typography.labelCaps,
                            color = RoutineTheme.colors.onSurfaceVariant
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(RoutineTheme.spacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.lg)
            ) {
                // Colors
                Column(verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                    RoutineSectionHeader(title = "CORE COLORS")
                    Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)) {
                        ColorSwatch(name = "Primary", color = RoutineTheme.colors.primary)
                        ColorSwatch(name = "Secondary", color = RoutineTheme.colors.secondary)
                        ColorSwatch(name = "Tertiary", color = RoutineTheme.colors.tertiary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.md)) {
                        ColorSwatch(name = "Border", color = RoutineTheme.colors.border)
                        ColorSwatch(name = "Surface1", color = RoutineTheme.colors.surface1)
                        ColorSwatch(name = "Surface2", color = RoutineTheme.colors.surface2)
                    }
                }

                // Typography
                Column(verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                    RoutineSectionHeader(title = "TYPOGRAPHY")
                    Text("Display Large — 32sp", style = RoutineTheme.typography.displayLarge)
                    Text("Headline Medium — 20sp", style = RoutineTheme.typography.headlineMedium)
                    Text("Body Base — 16sp", style = RoutineTheme.typography.bodyBase)
                    Text("Data Large (Mono) — 18sp", style = RoutineTheme.typography.dataLarge)
                    Text("LABEL CAPS — 12sp", style = RoutineTheme.typography.labelCaps)
                }

                // Buttons
                Column(verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                    RoutineSectionHeader(title = "BUTTONS")
                    Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                        RoutinePrimaryButton(onClick = {}) {
                            Text("Primary Action")
                        }
                        RoutinePrimaryButton(onClick = {}, enabled = false) {
                            Text("Disabled")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                        RoutineOutlinedButton(onClick = {}) {
                            Text("Outlined")
                        }
                        RoutineOutlinedButton(onClick = {}, enabled = false) {
                            Text("Disabled")
                        }
                    }
                }

                // Cards & Chips
                Column(verticalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.sm)) {
                    RoutineSectionHeader(title = "CARDS & CHIPS")
                    RoutineCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(RoutineTheme.spacing.md)) {
                            Text("Technical Premium Card", style = RoutineTheme.typography.headlineMedium)
                            Text(
                                "Cards now feature a 16dp radius and a subtle hairline border.",
                                style = RoutineTheme.typography.bodyBase
                            )
                            Row(
                                modifier = Modifier.padding(top = RoutineTheme.spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(RoutineTheme.spacing.xs)
                            ) {
                                RoutineChip(label = "SYSTEM")
                                RoutineChip(label = "REFINED")
                                RoutineChip(label = "V2")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(name: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = name,
            style = RoutineTheme.typography.labelCaps,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
fun RoutineDesignSystemCatalogPreview() {
    RoutineDesignSystemCatalog()
}
