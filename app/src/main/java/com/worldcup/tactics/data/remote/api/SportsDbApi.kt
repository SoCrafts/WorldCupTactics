package com.worldcup.tactics.data.remote.api

import com.worldcup.tactics.data.remote.dto.SportsDbPlayersResponse
import com.worldcup.tactics.data.remote.dto.SportsDbTeamsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * TheSportsDB v1 REST API.
 *
 * Base URL is constructed dynamically in NetworkModule to embed the API key:
 *   https://www.thesportsdb.com/api/v1/json/{key}/
 *
 * Free public demo key: "3"  (rate-limited, suitable for development)
 * Paid Patreon key:      set SPORTS_DB_KEY in local.properties
 */
interface SportsDbApi {

    /**
     * Returns all national teams registered under a given league/competition name.
     * Use l=FIFA+World+Cup to get all World Cup participating nations.
     */
    @GET("search_all_teams.php")
    suspend fun getWorldCupTeams(
        @Query("l") league: String = "FIFA World Cup"
    ): SportsDbTeamsResponse

    /**
     * Returns the full squad (all players) for a given TheSportsDB team ID.
     * Players include position, jersey number, photo URLs, height, weight, DOB.
     */
    @GET("lookup_all_players.php")
    suspend fun getPlayers(
        @Query("id") teamId: String
    ): SportsDbPlayersResponse

    /**
     * Searches for teams by name — useful for resolving a single team's idTeam.
     */
    @GET("searchteams.php")
    suspend fun searchTeam(
        @Query("t") teamName: String
    ): SportsDbTeamsResponse

    /**
     * Looks up a single team by its TheSportsDB ID.
     */
    @GET("lookupteam.php")
    suspend fun lookupTeam(
        @Query("id") teamId: String
    ): SportsDbTeamsResponse
}
