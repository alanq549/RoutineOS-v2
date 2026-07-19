package com.alan.routineos.feature.account

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.component.RoutineScaffold
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.account.components.AccountProfileCard
import com.alan.routineos.feature.account.components.AccountSection

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {}
) {
    RoutineScaffold(
        modifier = modifier,
        bottomBar = bottomBar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // Main Header
            Text(
                text = "Cuenta",
                style = RoutineTheme.typography.displayLarge,
                color = RoutineTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(horizontal = RoutineTheme.spacing.marginMobile)
                    .padding(top = RoutineTheme.spacing.lg)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RoutineTheme.spacing.marginMobile)
                    .padding(bottom = RoutineTheme.spacing.xl)
            ) {
                uiState.profile?.let {
                    AccountProfileCard(profile = it)
                }

                Spacer(modifier = Modifier.height(RoutineTheme.spacing.xl))

                uiState.sections.forEach { section ->
                    AccountSection(section = section)
                }

                Spacer(modifier = Modifier.height(RoutineTheme.spacing.lg))

                // Logout Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            RoutineTheme.colors.error.copy(alpha = 0.3f),
                            RoutineTheme.shapes.medium
                        )
                        .clickable(onClick = onLogout)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cerrar sesión",
                        style = RoutineTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                        color = RoutineTheme.colors.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Version Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RoutineOS · Versión ${uiState.version}",
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}
