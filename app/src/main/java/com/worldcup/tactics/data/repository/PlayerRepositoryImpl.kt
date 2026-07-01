package com.worldcup.tactics.data.repository

import com.worldcup.tactics.data.local.CsvPlayerDataSource
import com.worldcup.tactics.data.mapper.SportsDbPlayerMapper
import com.worldcup.tactics.data.remote.TeamIdMapper
import com.worldcup.tactics.data.remote.api.SportsDbApi
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.repository.PlayerRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepositoryImpl @Inject constructor(
    private val sportsDbApi: SportsDbApi,
    private val csvPlayerDataSource: CsvPlayerDataSource
) : PlayerRepository {

    override suspend fun getPlayers(teamId: Int): Result<List<Player>> = runCatching {

        val sportsDbTeamId = TeamIdMapper.toSportsDbId(teamId)
        val csvTeamName = TeamIdMapper.toCsvTeamName(teamId)

        // ─────────────────────────────────────────────
        // 1. API players (authoritative source)
        // ─────────────────────────────────────────────
        val apiPlayers = sportsDbApi
            .getPlayers(sportsDbTeamId.toString())
            .player
            ?.mapNotNull { SportsDbPlayerMapper.toDomain(it) }
            .orEmpty()

        // ─────────────────────────────────────────────
        // 2. CSV players (fallback / enrichment source)
        // ─────────────────────────────────────────────
        val csvPlayers = if (csvTeamName != null) {
            csvPlayerDataSource.getPlayers(csvTeamName)
        } else {
            emptyList()
        }

        // ─────────────────────────────────────────────
        // 3. Build name-keyed CSV lookup for enrichment
        //    (covers the whole CSV, not just this team)
        // ─────────────────────────────────────────────
        val csvByName: Map<String, Player> = csvPlayerDataSource.getAllPlayersByName()

        // ─────────────────────────────────────────────
        // 4. Merge strategy
        //    API is primary; CSV fills in players whose
        //    names are absent from the API response.
        // ─────────────────────────────────────────────
        val apiKeys = apiPlayers.map { it.name.trim().lowercase() }.toSet()

        val merged = mutableListOf<Player>().apply {
            addAll(apiPlayers)

            addAll(
                csvPlayers.filter { csv ->
                    csv.name.trim().lowercase() !in apiKeys
                }
            )
        }

        // ─────────────────────────────────────────────
        // 5. Jersey-number enrichment pass
        //    For every player (API or CSV) whose number
        //    is null, attempt to fill it from the CSV.
        // ─────────────────────────────────────────────
        val enriched = merged.map { player ->
            if (player.number != null) {
                player
            } else {
                val csvNumber = csvByName[player.name.trim().lowercase()]?.number
                if (csvNumber != null) player.copy(number = csvNumber) else player
            }
        }

        // ─────────────────────────────────────────────
        // 6. Stable ordering (jersey number → name)
        // ─────────────────────────────────────────────
        enriched
            .sortedWith(
                compareBy<Player> { it.number ?: Int.MAX_VALUE }
                    .thenBy { it.name }
            )
    }
}