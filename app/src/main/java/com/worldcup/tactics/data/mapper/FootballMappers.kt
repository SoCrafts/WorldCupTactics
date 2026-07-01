package com.worldcup.tactics.data.mapper

import com.worldcup.tactics.data.remote.dto.FdTeamDto
import com.worldcup.tactics.data.remote.dto.SportsDbPlayerDto
import com.worldcup.tactics.data.remote.dto.SportsDbTeamDto
import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.model.Team

// ─────────────────────────────────────────────────────────────────────────────
//  Team mappers — TheSportsDB / Football-Data.org → Domain
// ─────────────────────────────────────────────────────────────────────────────

object SportsDbTeamMapper {
    fun toDomain(dto: SportsDbTeamDto): Team = Team(
        id = dto.idTeam.toIntOrNull() ?: 0,
        name = dto.strTeam,
        logoUrl = dto.strBadge ?: dto.strLogo,
        badgeUrl = dto.strBadge,
        bannerUrl = dto.strBanner,
        country = dto.strCountry
    )

    /** Enriches an existing Team (sourced from Football-Data) with SportsDB image URLs. */
    fun enrich(team: Team, dto: SportsDbTeamDto): Team = team.copy(
        badgeUrl = dto.strBadge?.takeIf { it.isNotBlank() },
        bannerUrl = dto.strBanner?.takeIf { it.isNotBlank() }
    )
}

object FootballDataTeamMapper {
    fun toDomain(dto: FdTeamDto): Team = Team(
        id = dto.id,
        name = dto.name,
        logoUrl = dto.crest,
        badgeUrl = null,
        bannerUrl = null,
        country = dto.area?.name
    )
}


// ─────────────────────────────────────────────────────────────────────────────
//  Player mapper — TheSportsDB → Domain
// ─────────────────────────────────────────────────────────────────────────────

object SportsDbPlayerMapper {

    /**
     * Position strings that identify non-playing staff (managers, coaches, physios, etc.).
     * Any DTO whose strPosition matches one of these is silently dropped.
     */
    private val STAFF_POSITIONS = setOf(
        "manager", "head coach", "coach", "assistant manager", "assistant coach",
        "goalkeeper coach", "fitness coach", "physio", "physiotherapist",
        "director", "president", "staff", "trainer", "scout"
    )

    /**
     * Maps a TheSportsDB player DTO to the domain [Player] model.
     * Returns null when the player has no recognisable position (e.g. staff entries).
     */
    fun toDomain(dto: SportsDbPlayerDto): Player? {
        val rawPosition = dto.strPosition?.takeIf { it.isNotBlank() } ?: return null

        // Drop coaching/management staff — TheSportsDB sometimes mixes them into squad lists
        if (STAFF_POSITIONS.any { rawPosition.contains(it, ignoreCase = true) }) return null

        val position = normalisePosition(rawPosition)

        // Prefer high-quality cutout (transparent background) over thumb
        val photoUrl = dto.strCutout?.takeIf { it.isNotBlank() }
            ?: dto.strThumb?.takeIf { it.isNotBlank() }
            ?: dto.strRender?.takeIf { it.isNotBlank() }

        return Player(
            id = dto.idPlayer.toIntOrNull() ?: 0,
            name = dto.strPlayer,
            number = dto.strNumber?.toIntOrNull(),
            position = position,
            nationality = dto.strNationality.orEmpty(),
            photoUrl = photoUrl,
            // strTeam = club name, strTeam2 = national team
            clubName = dto.strTeam?.takeIf { it.isNotBlank() },
            dateOfBirth = dto.dateBorn,
            height = dto.strHeight,
            weight = dto.strWeight
        )
    }

    /**
     * Normalises TheSportsDB's verbose position strings to four canonical buckets:
     * Goalkeeper | Defender | Midfielder | Forward
     */
    private fun normalisePosition(raw: String): String = when {
        raw.contains("Goalkeeper", ignoreCase = true)  -> "Goalkeeper"
        raw.contains("Back", ignoreCase = true)
            || raw.contains("Defender", ignoreCase = true)
            || raw.contains("Sweeper", ignoreCase = true) -> "Defender"
        raw.contains("Midfield", ignoreCase = true)
            || raw.contains("Midfielder", ignoreCase = true)
            || raw.contains("Defensive Mid", ignoreCase = true) -> "Midfielder"
        raw.contains("Forward", ignoreCase = true)
            || raw.contains("Winger", ignoreCase = true)
            || raw.contains("Striker", ignoreCase = true)
            || raw.contains("Attacker", ignoreCase = true)
            || raw.contains("Centre-Forward", ignoreCase = true) -> "Forward"
        else -> raw // preserve unknown positions as-is
    }
}
