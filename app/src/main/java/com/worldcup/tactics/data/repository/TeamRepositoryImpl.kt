package com.worldcup.tactics.data.repository

import com.worldcup.tactics.config.FeatureFlags
import com.worldcup.tactics.data.mapper.FootballDataTeamMapper
import com.worldcup.tactics.data.mapper.SportsDbTeamMapper
import com.worldcup.tactics.data.remote.TeamIdMapper
import com.worldcup.tactics.data.remote.api.FootballDataApi
import com.worldcup.tactics.data.remote.api.SportsDbApi
import com.worldcup.tactics.domain.model.Team
import com.worldcup.tactics.domain.repository.TeamRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepositoryImpl @Inject constructor(
    private val footballDataApi: FootballDataApi,
    private val sportsDbApi: SportsDbApi
) : TeamRepository {

    override suspend fun getTeams(): Result<List<Team>> = runCatching {
        // 1. Fetch base team list from Football-Data (ids, names, SVG crests)
        val baseTeams = footballDataApi
            .getCompetitionTeams()
            .teams
            .map(FootballDataTeamMapper::toDomain)
            .filter { it.name.isNotBlank() }
            .sortedBy { it.name }

        // 2. Optionally enrich with SportsDB badge/banner — gated by feature flag
        //    to avoid exhausting free-tier rate limits (10 req/min shared with player calls)
        if (!FeatureFlags.USE_REMOTE_TEAM_IMAGES) return@runCatching baseTeams

        coroutineScope {
            baseTeams.map { team ->
                async {
                    val sportsDbId = TeamIdMapper.toSportsDbId(team.id)
                    try {
                        val dto = sportsDbApi
                            .lookupTeam(sportsDbId.toString())
                            .teams
                            ?.firstOrNull()
                        if (dto != null) SportsDbTeamMapper.enrich(team, dto) else team
                    } catch (_: Exception) {
                        team // non-fatal — return base team on any failure
                    }
                }
            }.awaitAll()
        }
    }
}
