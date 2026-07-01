package com.worldcup.tactics.domain.model

data class Team(
    val id: Int,
    val name: String,
    val logoUrl: String?,       // Football-Data SVG crest (always fetched)
    val badgeUrl: String?,      // SportsDB strBadge 512×512 PNG (requires USE_REMOTE_TEAM_IMAGES)
    val bannerUrl: String?,     // SportsDB strBanner (requires USE_REMOTE_TEAM_IMAGES)
    val country: String?
) {
    /** Unicode flag emoji for this team, derived from country name. Falls back to ⚽. */
    val flagEmoji: String
        get() = COUNTRY_FLAGS[name] ?: COUNTRY_FLAGS[country] ?: "⚽"
}

/**
 * Maps team/country names (as returned by Football-Data) to Unicode flag emoji.
 * Keys are tried against [Team.name] first, then [Team.country].
 */
private val COUNTRY_FLAGS: Map<String, String> = mapOf(
    // A
    "Algeria" to "🇩🇿",
    "Argentina" to "🇦🇷",
    "Australia" to "🇦🇺",
    "Austria" to "🇦🇹",
    // B
    "Belgium" to "🇧🇪",
    "Bosnia-Herzegovina" to "🇧🇦",
    "Bosnia and Herzegovina" to "🇧🇦",
    "Brazil" to "🇧🇷",
    // C
    "Canada" to "🇨🇦",
    "Cape Verde" to "🇨🇻",
    "Colombia" to "🇨🇴",
    "Croatia" to "🇭🇷",
    "Curacao" to "🇨🇼",
    "Côte d'Ivoire" to "🇨🇮",
    "Ivory Coast" to "🇨🇮",
    "Czechia" to "🇨🇿",
    "Czech Republic" to "🇨🇿",
    // D
    "DR Congo" to "🇨🇩",
    "Democratic Republic of Congo" to "🇨🇩",
    // E
    "Ecuador" to "🇪🇨",
    "Egypt" to "🇪🇬",
    "England" to "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
    // F
    "France" to "🇫🇷",
    // G
    "Germany" to "🇩🇪",
    "Ghana" to "🇬🇭",
    // H
    "Haiti" to "🇭🇹",
    // I
    "Iran" to "🇮🇷",
    "Iraq" to "🇮🇶",
    // J
    "Japan" to "🇯🇵",
    "Jordan" to "🇯🇴",
    // M
    "Mexico" to "🇲🇽",
    "Morocco" to "🇲🇦",
    // N
    "Netherlands" to "🇳🇱",
    "New Zealand" to "🇳🇿",
    "Norway" to "🇳🇴",
    // P
    "Panama" to "🇵🇦",
    "Paraguay" to "🇵🇾",
    "Portugal" to "🇵🇹",
    // Q
    "Qatar" to "🇶🇦",
    // S
    "Saudi Arabia" to "🇸🇦",
    "Scotland" to "🏴󠁧󠁢󠁳󠁣󠁴󠁿",
    "Senegal" to "🇸🇳",
    "South Africa" to "🇿🇦",
    "South Korea" to "🇰🇷",
    "Republic of Korea" to "🇰🇷",
    "Spain" to "🇪🇸",
    "Sweden" to "🇸🇪",
    "Switzerland" to "🇨🇭",
    // T
    "Tunisia" to "🇹🇳",
    "Turkey" to "🇹🇷",
    "Türkiye" to "🇹🇷",
    // U
    "United States" to "🇺🇸",
    "USA" to "🇺🇸",
    "Uruguay" to "🇺🇾",
    "Uzbekistan" to "🇺🇿",
)
