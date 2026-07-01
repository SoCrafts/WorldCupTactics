package com.worldcup.tactics.ui.screens.field

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.ui.components.FootballField
import com.worldcup.tactics.ui.components.WctBackHeader
import com.worldcup.tactics.ui.state.FieldUiState
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.BgPrimary
import com.worldcup.tactics.ui.theme.BorderColor
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary

@Composable
fun FieldScreen(
    uiState: FieldUiState,
    onBack: (() -> Unit)? = null,
    onPlayerTap: (Int) -> Unit,
    onPlayerDrag: (Int, Float, Float) -> Unit,
    onPlayerDragStart: (Int) -> Unit,
    onPlayerDragEnd: (Int, Float, Float, Float) -> Unit,
    onSelectedFormation: (String) -> Unit,
    onDismissSheet: () -> Unit,
    onSubstitutePlayer: (fieldPlayerId: Int, benchPlayer: Player) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            WctBackHeader(label = if (onBack != null) "Back to Squad" else "Formation", onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.teamName.ifBlank { "Formation" },
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (uiState.isSaved) "Formation saved" else "Drag players to build your lineup",
                color = if (uiState.isSaved) AccentGreen else TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Text("Reset", color = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = BgPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FormationTabs(
                selected = uiState.formationLabel,
                onSelected = onSelectedFormation,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AccentGreen)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error, color = TextSecondary)
                            TextButton(onClick = onRetry) {
                                Text("Retry", color = AccentGreen)
                            }
                        }
                    }
                }

                else -> {
                    val density = LocalDensity.current
                    val swapThresholdPx = with(density) { 45.dp.toPx() }

                    FootballField(
                        players = uiState.players,
                        selectedPlayerId = uiState.selectedPlayerId,
                        onPlayerTap = onPlayerTap,
                        onClearSelection = onDismissSheet,
                        onPlayerDrag = onPlayerDrag,
                        onPlayerDragStart = onPlayerDragStart,
                        onPlayerDragEnd = { id, w, h ->
                            onPlayerDragEnd(id, w, h, swapThresholdPx)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }

        // Substitution sheet — shown when a player is selected
        uiState.selectedPlayerId?.let { selectedId ->
            uiState.players.find { it.player.id == selectedId }?.let { selectedFP ->
                SubstitutionSheet(
                    selectedPlayer = selectedFP,
                    benchPlayers = uiState.benchPlayers,
                    onSubstitute = { benchPlayer ->
                        onSubstitutePlayer(selectedId, benchPlayer)
                    },
                    onDismiss = onDismissSheet
                )
            }
        }
    }
}

@Composable
private fun FormationTabs(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val formations = listOf(
        "4-4-2", "4-3-3", "4-2-3-1", "3-5-2", "3-4-3", "5-3-2", "5-4-1", "4-5-1", "Custom"
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        formations.forEach { label ->
            val isSelected = selected == label
            Surface(
                onClick = { onSelected(label) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) AccentGreen else BgCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) AccentGreen else BorderColor
                )
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) BgPrimary else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
