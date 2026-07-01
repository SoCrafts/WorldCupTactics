package com.worldcup.tactics.domain.repository

import com.worldcup.tactics.domain.model.Team

interface TeamRepository {
    suspend fun getTeams(): Result<List<Team>>
}
