package com.worldcup.tactics.data.remote.dto

import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────
//  Football-Data.org v4 — Matches / Competitions
//  (wired up now; actively used when a Fixtures
//   screen is added in the future)
// ─────────────────────────────────────────────

@Serializable
data class FdMatchesResponse(
    val count: Int? = null,
    val competition: FdCompetitionDto? = null,
    val matches: List<FdMatchDto> = emptyList()
)

@Serializable
data class FdCompetitionDto(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null
)

@Serializable
data class FdMatchDto(
    val id: Int,
    val utcDate: String? = null,
    val status: String? = null,
    val matchday: Int? = null,
    val stage: String? = null,
    val group: String? = null,
    val homeTeam: FdMatchTeamDto? = null,
    val awayTeam: FdMatchTeamDto? = null,
    val score: FdScoreDto? = null,
    val venue: String? = null
)

@Serializable
data class FdMatchTeamDto(
    val id: Int? = null,
    val name: String? = null,
    val shortName: String? = null,
    val crest: String? = null
)

@Serializable
data class FdScoreDto(
    val winner: String? = null,
    val duration: String? = null,
    val fullTime: FdGoalsDto? = null,
    val halfTime: FdGoalsDto? = null
)

@Serializable
data class FdGoalsDto(
    val home: Int? = null,
    val away: Int? = null
)

// ─────────────────────────────────────────────
//  Football-Data.org v4 — Competition Teams
// ─────────────────────────────────────────────

@Serializable
data class FdCompetitionTeamsResponse(
    val competition: FdCompetitionDto? = null,
    val season: FdSeasonDto? = null,
    val teams: List<FdTeamDto> = emptyList()
)

@Serializable
data class FdSeasonDto(
    val id: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val currentMatchday: Int? = null
)

@Serializable
data class FdTeamDto(
    val id: Int,
    val name: String,
    val shortName: String? = null,
    val tla: String? = null,
    val crest: String? = null,
    val address: String? = null,
    val website: String? = null,
    val founded: Int? = null,
    val clubColors: String? = null,
    val venue: String? = null,
    val area: FdAreaDto? = null
)

@Serializable
data class FdAreaDto(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null,
    val flag: String? = null
)
