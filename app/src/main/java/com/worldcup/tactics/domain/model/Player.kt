package com.worldcup.tactics.domain.model

data class Player(
    val id: Int,
    val name: String,
    val number: Int?,
    val position: String,
    val nationality: String,
    val photoUrl: String?,
    val clubName: String?,
    // Enriched from TheSportsDB
    val dateOfBirth: String? = null,
    val height: String? = null,
    val weight: String? = null
)
