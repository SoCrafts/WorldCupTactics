package com.worldcup.tactics.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.worldcup.tactics.domain.usecase.GetTeamsUseCase
import com.worldcup.tactics.ui.state.TeamUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamSelectionViewModel @Inject constructor(
    private val getTeamsUseCase: GetTeamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeamUiState())
    val uiState: StateFlow<TeamUiState> = _uiState.asStateFlow()

    init {
        loadTeams()
    }

    fun loadTeams() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getTeamsUseCase()
                .onSuccess { teams ->
                    _uiState.update { it.copy(isLoading = false, teams = teams) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message ?: "Failed to load teams")
                    }
                }
        }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
