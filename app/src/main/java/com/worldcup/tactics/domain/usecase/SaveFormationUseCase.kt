package com.worldcup.tactics.domain.usecase

import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.repository.FormationRepository
import javax.inject.Inject

class SaveFormationUseCase @Inject constructor(
    private val repository: FormationRepository
) {
    suspend operator fun invoke(teamId: Int, players: List<FormationPlayer>) {
        repository.saveFormation(teamId, players)
    }
}
