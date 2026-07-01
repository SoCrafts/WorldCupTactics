package com.worldcup.tactics.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.worldcup.tactics.navigation.BottomNavTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor() : ViewModel() {

    private val _selectedTeamId = MutableStateFlow<Int?>(null)
    val selectedTeamId: StateFlow<Int?> = _selectedTeamId.asStateFlow()

    private val _scrollToTopEvent = MutableSharedFlow<BottomNavTab>(extraBufferCapacity = 1)
    val scrollToTopEvent: SharedFlow<BottomNavTab> = _scrollToTopEvent.asSharedFlow()

    fun selectTeam(teamId: Int) {
        _selectedTeamId.update { teamId }
    }

    fun onTabReselected(tab: BottomNavTab) {
        _scrollToTopEvent.tryEmit(tab)
    }
}
