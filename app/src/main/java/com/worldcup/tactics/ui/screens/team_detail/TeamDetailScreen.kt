package com.worldcup.tactics.ui.screens.team_detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.tactics.config.FeatureFlags
import com.worldcup.tactics.domain.model.sectionTitle
import com.worldcup.tactics.ui.components.PositionFilterTabs
import com.worldcup.tactics.ui.components.SectionHeader
import com.worldcup.tactics.ui.components.SquadPlayerCard
import com.worldcup.tactics.ui.components.StatCard
import com.worldcup.tactics.ui.components.WctBackHeader
import com.worldcup.tactics.ui.state.TeamDetailUiState
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgPrimary
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary

@Composable
fun TeamDetailScreen(
    uiState: TeamDetailUiState,
    onBack: () -> Unit,
    onFilterSelected: (com.worldcup.tactics.domain.model.PositionGroup) -> Unit,
    onOpenTactics: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WctBackHeader(label = "Back to Teams", onBack = onBack)

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }

            uiState.error != null && uiState.team == null -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.error, color = TextSecondary)
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = AccentGreen)
                    }
                }
            }

            else -> {
                uiState.team?.let { team ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (FeatureFlags.USE_REMOTE_TEAM_IMAGES) {
                            AsyncImage(
                                model = team.badgeUrl ?: team.logoUrl,
                                contentDescription = team.name,
                                modifier = Modifier
                                    .size(width = 80.dp, height = 60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = team.flagEmoji,
                                fontSize = 52.sp,
                                modifier = Modifier.size(width = 80.dp, height = 60.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = team.name,
                                color = TextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append(team.country ?: "National Team")
                                    append(" · ")
                                    withStyle(SpanStyle(color = AccentGreen, fontWeight = FontWeight.SemiBold)) {
                                        append("${uiState.players.size} Players")
                                    }
                                },
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    /*
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            value = uiState.players.size.toString(),
                            label = "Squad Size",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = uiState.formationLabel,
                            label = "Formation",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            value = uiState.positionsCount.toString(),
                            label = "Positions",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp)) */

                    PositionFilterTabs(
                        selected = uiState.positionFilter,
                        onSelected = onFilterSelected
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.groupedSections.forEach { section ->
                            item(key = "header_${section.group}") {
                                SectionHeader(title = section.group.sectionTitle())
                            }
                            items(section.players, key = { it.id }) { player ->
                                SquadPlayerCard(player = player)
                            }
                            item(key = "spacer_${section.group}") {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    Button(
                        onClick = onOpenTactics,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = BgPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Open Tactics Board",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
