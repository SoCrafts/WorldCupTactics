package com.worldcup.tactics.domain.repository

import com.worldcup.tactics.domain.model.Player

interface PlayerRepository {
    suspend fun getPlayers(teamId: Int): Result<List<Player>>
}
