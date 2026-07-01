package com.worldcup.tactics.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.tactics.domain.model.PositionGroup
import com.worldcup.tactics.domain.repository.FormationRepository
import com.worldcup.tactics.domain.usecase.GetPlayersUseCase
import com.worldcup.tactics.domain.usecase.GetTeamsUseCase
import com.worldcup.tactics.ui.state.TeamDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTeamsUseCase: GetTeamsUseCase,
    private val getPlayersUseCase: GetPlayersUseCase,
    private val formationRepository: FormationRepository
) : ViewModel() {

    private val teamId: Int = checkNotNull(savedStateHandle["teamId"])

    private val _uiState = MutableStateFlow(TeamDetailUiState())
    val uiState: StateFlow<TeamDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val teamResult = getTeamsUseCase()
            val playersResult = getPlayersUseCase(teamId)
            val formationLabel = formationRepository.getFormationLabel(teamId) ?: "Custom"

            teamResult
                .onSuccess { teams ->
                    val team = teams.find { it.id == teamId }
                    playersResult
                        .onSuccess { players ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    team = team,
                                    players = players,
                                    formationLabel = formationLabel
                                )
                            }
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    team = team,
                                    error = error.message ?: "Failed to load squad"
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load team"
                        )
                    }
                }
        }
    }

    fun setPositionFilter(filter: PositionGroup) {
        _uiState.update { it.copy(positionFilter = filter) }
    }
}
