package com.worldcup.tactics.domain.usecase

import com.worldcup.tactics.domain.model.Team
import com.worldcup.tactics.domain.repository.TeamRepository
import javax.inject.Inject

class GetTeamsUseCase @Inject constructor(
    private val repository: TeamRepository
) {
    suspend operator fun invoke(): Result<List<Team>> = repository.getTeams()
}
