package com.worldcup.tactics.domain.model

data class Fixture(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val date: String
)
