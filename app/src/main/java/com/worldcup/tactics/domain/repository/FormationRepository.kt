package com.worldcup.tactics.domain.repository

import com.worldcup.tactics.domain.model.FormationPlayer

interface FormationRepository {
    suspend fun saveFormation(teamId: Int, players: List<FormationPlayer>)
    suspend fun loadFormation(teamId: Int): List<FormationPlayer>?
    suspend fun getFormationLabel(teamId: Int): String?
    suspend fun saveFormationLabel(teamId: Int, label: String)
}
