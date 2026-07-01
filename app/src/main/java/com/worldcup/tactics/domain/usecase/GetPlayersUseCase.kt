package com.worldcup.tactics.domain.usecase

import com.worldcup.tactics.domain.model.Player
import com.worldcup.tactics.domain.repository.PlayerRepository
import javax.inject.Inject

class GetPlayersUseCase @Inject constructor(
    private val repository: PlayerRepository
) {
    suspend operator fun invoke(teamId: Int): Result<List<Player>> = repository.getPlayers(teamId)
}
