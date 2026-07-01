package com.worldcup.tactics.domain.usecase

import com.worldcup.tactics.domain.model.FormationPlayer
import com.worldcup.tactics.domain.repository.FormationRepository
import javax.inject.Inject

class LoadFormationUseCase @Inject constructor(
    private val repository: FormationRepository
) {
    suspend operator fun invoke(teamId: Int): List<FormationPlayer>? =
        repository.loadFormation(teamId)
}
