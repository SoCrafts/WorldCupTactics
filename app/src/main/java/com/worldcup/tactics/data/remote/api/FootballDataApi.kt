package com.worldcup.tactics.data.remote.api

import com.worldcup.tactics.data.remote.dto.FdCompetitionTeamsResponse
import com.worldcup.tactics.data.remote.dto.FdMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Football-Data.org v4 REST API.
 *
 * Base URL: https://api.football-data.org/
 * Auth:     X-Auth-Token header (injected via OkHttp interceptor in NetworkModule)
 *
 * Free tier: 10 requests/minute, access to major competitions including World Cup (code=WC).
 * Sign up at: https://www.football-data.org/client/register
 */
interface FootballDataApi {

    /**
     * Returns all teams participating in a competition.
     * For the FIFA World Cup 2026, use competitionCode = "WC".
     */
    @GET("v4/competitions/{code}/teams")
    suspend fun getCompetitionTeams(
        @Path("code") competitionCode: String = "WC",
        @Query("season") season: Int? = null
    ): FdCompetitionTeamsResponse

    /**
     * Returns all matches in a competition.
     * Filter by matchday, status, dateFrom/dateTo via query params.
     */
    @GET("v4/competitions/{code}/matches")
    suspend fun getCompetitionMatches(
        @Path("code") competitionCode: String = "WC",
        @Query("matchday") matchday: Int? = null,
        @Query("status") status: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): FdMatchesResponse
}
