package com.worldcup.tactics.ui.state

import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.PositionGroup
import com.worldcup.tactics.domain.model.Team
import com.worldcup.tactics.domain.model.positionGroup

data class TeamUiState(
    val isLoading: Boolean = false,
    val teams: List<Team> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
) {
    val filteredTeams: List<Team>
        get() = if (searchQuery.isBlank()) {
            teams
        } else {
            val query = searchQuery.trim().lowercase()
            teams.filter {
                it.name.lowercase().contains(query) ||
                    (it.country?.lowercase()?.contains(query) == true)
            }
        }
}

data class PlayerSection(
    val group: PositionGroup,
    val players: List<Player>
)

data class TeamDetailUiState(
    val isLoading: Boolean = false,
    val team: Team? = null,
    val players: List<Player> = emptyList(),
    val positionFilter: PositionGroup = PositionGroup.ALL,
    val formationLabel: String = "Custom",
    val error: String? = null
) {
    val groupedSections: List<PlayerSection>
        get() {
            val filtered = when (positionFilter) {
                PositionGroup.ALL -> players
                else -> players.filter { player ->
                    player.positionGroup() == positionFilter
                }
            }
            return listOf(
                PositionGroup.GK,
                PositionGroup.DEF,
                PositionGroup.MID,
                PositionGroup.FWD
            ).mapNotNull { group ->
                val groupPlayers = filtered.filter { it.positionGroup() == group }
                if (groupPlayers.isEmpty()) null else PlayerSection(group, groupPlayers)
            }
        }

    val positionsCount: Int
        get() = players.map { it.positionGroup() }.distinct().size
}

data class FieldUiState(
    val isLoading: Boolean = false,
    val teamName: String = "",
    val players: List<FormationPlayer> = emptyList(),
    val benchPlayers: List<Player> = emptyList(),
    val selectedPlayerId: Int? = null,
    val formationLabel: String = "4-4-2",
    val isSaved: Boolean = false,
    val error: String? = null
)
