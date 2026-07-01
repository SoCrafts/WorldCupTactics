package com.worldcup.tactics.domain.model

enum class PositionGroup {
    ALL,
    GK,
    DEF,
    MID,
    FWD
}

fun Player.positionGroup(): PositionGroup {
    val normalized = position.lowercase()
    return when {
        normalized.contains("goal") || normalized == "gk" -> PositionGroup.GK
        normalized.contains("def") || normalized in setOf("cb", "lb", "rb", "wb") -> PositionGroup.DEF
        normalized.contains("mid") || normalized in setOf("cm", "dm", "am", "lm", "rm") -> PositionGroup.MID
        normalized.contains("attack") || normalized.contains("forward") ||
            normalized in setOf("fw", "st", "cf", "lw", "rw") -> PositionGroup.FWD
        else -> PositionGroup.MID
    }
}

fun PositionGroup.sectionTitle(): String = when (this) {
    PositionGroup.GK -> "GOALKEEPERS"
    PositionGroup.DEF -> "DEFENDERS"
    PositionGroup.MID -> "MIDFIELDERS"
    PositionGroup.FWD -> "FORWARDS"
    PositionGroup.ALL -> "ALL PLAYERS"
}
