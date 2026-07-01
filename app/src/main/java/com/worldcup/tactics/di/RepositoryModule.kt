package com.worldcup.tactics.di

import com.worldcup.tactics.data.local.FormationRepositoryImpl
import com.worldcup.tactics.data.repository.PlayerRepositoryImpl
import com.worldcup.tactics.data.repository.TeamRepositoryImpl
import com.worldcup.tactics.domain.repository.FormationRepository
import com.worldcup.tactics.domain.repository.PlayerRepository
import com.worldcup.tactics.domain.repository.TeamRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindFormationRepository(impl: FormationRepositoryImpl): FormationRepository
}
