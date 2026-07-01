package com.worldcup.tactics.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────
//  TheSportsDB — Teams
// ─────────────────────────────────────────────

@Serializable
data class SportsDbTeamsResponse(
    val teams: List<SportsDbTeamDto>? = null
)

@Serializable
data class SportsDbTeamDto(
    val idTeam: String,
    val strTeam: String,
    val strTeamShort: String? = null,
    val strTeamAlternate: String? = null,
    val strCountry: String? = null,
    val strBadge: String? = null,
    val strLogo: String? = null,
    val strBanner: String? = null,
    val strEquipment: String? = null,
    val strColour1: String? = null,
    val strColour2: String? = null,
    val strDescriptionEN: String? = null,
    val intFormedYear: String? = null,
    val strStadium: String? = null,
    val intStadiumCapacity: String? = null,
    val strWebsite: String? = null,
    val idLeague: String? = null
)

// ─────────────────────────────────────────────
//  TheSportsDB — Players
// ─────────────────────────────────────────────

@Serializable
data class SportsDbPlayersResponse(
    val player: List<SportsDbPlayerDto>? = null
)

@Serializable
data class SportsDbPlayerDto(
    val idPlayer: String,
    val idTeam: String,
    val idTeam2: String? = null,
    val strPlayer: String,
    val strLastName: String? = null,
    val strNationality: String? = null,
    val strPosition: String? = null,
    val strNumber: String? = null,
    val dateBorn: String? = null,
    val strHeight: String? = null,
    val strWeight: String? = null,
    val strStatus: String? = null,
    @SerialName("strThumb") val strThumb: String? = null,
    @SerialName("strCutout") val strCutout: String? = null,
    @SerialName("strRender") val strRender: String? = null,
    @SerialName("strCartoon") val strCartoon: String? = null,
    val strDescriptionEN: String? = null,
    val strTeam: String? = null,
    val strTeam2: String? = null,
    val strSide: String? = null,
    val idAPIfootball: String? = null
)
