package com.worldcup.tactics.ui.screens.team_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.worldcup.tactics.config.FeatureFlags
import com.worldcup.tactics.domain.model.Team
import com.worldcup.tactics.navigation.BottomNavTab
import com.worldcup.tactics.ui.state.TeamUiState
import com.worldcup.tactics.ui.theme.AccentGreen
import com.worldcup.tactics.ui.theme.BgCard
import com.worldcup.tactics.ui.theme.BorderColor
import com.worldcup.tactics.ui.theme.TextPrimary
import com.worldcup.tactics.ui.theme.TextSecondary
import com.worldcup.tactics.ui.theme.wctCard
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun TeamSelectionScreen(
    uiState: TeamUiState,
    onSearch: (String) -> Unit,
    onRetry: () -> Unit,
    onTeamClick: (Team) -> Unit,
    showSelectTeamPrompt: Boolean = false,
    scrollToTopEvent: SharedFlow<BottomNavTab>? = null,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    // Scroll to top when the Teams tab is re-tapped
    LaunchedEffect(scrollToTopEvent) {
        scrollToTopEvent?.collect { tab ->
            if (tab == BottomNavTab.Teams) {
                gridState.animateScrollToItem(0)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "World Cup 2026",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a national team",
            color = TextSecondary,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Inline prompt banner (non-blocking) — shown when arriving from Tactics tab with no team set
        if (showSelectTeamPrompt) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Select a team first to open the Tactics board",
                    color = AccentGreen,
                    fontSize = 13.sp
                )
            }
        }

        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search teams...", color = TextSecondary) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = BorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentGreen
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGreen)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    TextButton(onClick = onRetry) {
                        Text("Retry", color = AccentGreen)
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTeams, key = { it.id }) { team ->
                        TeamGridCard(team = team, onClick = { onTeamClick(team) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamGridCard(
    team: Team,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wctCard(cornerRadius = 16.dp)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (FeatureFlags.USE_REMOTE_TEAM_IMAGES) {
            AsyncImage(
                model = team.badgeUrl ?: team.logoUrl,
                contentDescription = team.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = team.flagEmoji,
                fontSize = 48.sp,
                modifier = Modifier.size(64.dp),
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = team.name,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        team.country?.let { country ->
            Text(
                text = country,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
