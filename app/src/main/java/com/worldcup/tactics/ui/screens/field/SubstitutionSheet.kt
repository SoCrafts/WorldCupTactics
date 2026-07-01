package com.worldcup.tactics.ui.screens.field

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.PositionGroup
import com.worldcup.tactics.domain.model.positionGroup
import com.worldcup.tactics.domain.model.sectionTitle
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.BgPrimary
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstitutionSheet(
    selectedPlayer: FormationPlayer,
    benchPlayers: List<Player>,
    onSubstitute: (benchPlayer: Player) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgCard,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            // Header
            Text(
                text = "Substitute ${selectedPlayer.player.name}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = TextSecondary.copy(alpha = 0.2f))

            if (benchPlayers.isEmpty()) {
                Text(
                    text = "No substitutions available",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val selectedGroup = selectedPlayer.player.positionGroup()

                // Group players by position in fixed order: GK, DEF, MID, FWD
                val groups = listOf(PositionGroup.GK, PositionGroup.DEF, PositionGroup.MID, PositionGroup.FWD)
                    .mapNotNull { group ->
                        val groupPlayers = benchPlayers.filter { it.positionGroup() == group }
                        if (groupPlayers.isEmpty()) null else group to groupPlayers
                    }

                LazyColumn {
                    groups.forEach { (group, players) ->
                        item(key = group.name) {
                            Text(
                                text = group.sectionTitle(),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BgPrimary)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        items(players, key = { it.id }) { player ->
                            BenchPlayerRow(
                                player = player,
                                isCompatible = player.positionGroup() == selectedGroup,
                                onClick = { onSubstitute(player) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BenchPlayerRow(
    player: Player,
    isCompatible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isCompatible) AccentGreen.copy(alpha = 0.12f) else BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        player.number?.let { num ->
            Text(
                text = "#$num",
                color = AccentGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = player.position,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
