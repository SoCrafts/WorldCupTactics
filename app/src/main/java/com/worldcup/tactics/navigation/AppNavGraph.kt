package com.worldcup.tactics.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.worldcup.tactics.ui.components.WctBottomNav
import com.worldcup.tactics.ui.screens.field.FieldScreen
import com.worldcup.tactics.ui.screens.profile.ProfileScreen
import com.worldcup.tactics.ui.screens.team_detail.TeamDetailScreen
import com.worldcup.tactics.ui.screens.team_selection.TeamSelectionScreen
import com.worldcup.tactics.ui.theme.BgPrimary
import com.worldcup.tactics.ui.viewmodel.AppNavigationViewModel
import com.worldcup.tactics.ui.viewmodel.FieldViewModel
import com.worldcup.tactics.ui.viewmodel.TeamDetailViewModel
import com.worldcup.tactics.ui.viewmodel.TeamSelectionViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val appNavViewModel: AppNavigationViewModel = hiltViewModel()
    val selectedTeamId by appNavViewModel.selectedTeamId.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedTab = when {
        currentRoute?.startsWith("field") == true       -> BottomNavTab.Tactics
        currentRoute?.startsWith("team_detail") == true -> BottomNavTab.Teams
        currentRoute == Routes.PROFILE                  -> BottomNavTab.Profile
        else                                            -> BottomNavTab.Teams
    }

    // Bottom nav shown on FieldScreen only if not pushed (i.e., no back button)
    val showBottomNav = run {
        if (currentRoute == null || !currentRoute.startsWith("field/")) {
            true
        } else {
            // Check if showBack query parameter is false
            val backStackEntry = navBackStackEntry ?: return@run false
            backStackEntry.arguments?.getBoolean("showBack") != true
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        containerColor = BgPrimary,
        bottomBar = {
            if (showBottomNav) {
                WctBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        // Re-tapping the active tab triggers scroll-to-top instead of navigation
                        if (tab == selectedTab) {
                            appNavViewModel.onTabReselected(tab)
                            return@WctBottomNav
                        }
                        when (tab) {
                            BottomNavTab.Teams -> {
                                navController.navigate(Routes.TEAMS) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }

                            BottomNavTab.Tactics -> {
                                val teamId = selectedTeamId
                                if (teamId != null) {
                                    navController.navigate(Routes.field(teamId, showBack = false)) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(Routes.TEAMS_PROMPT) {
                                        launchSingleTop = true
                                    }
                                }
                            }

                            BottomNavTab.Profile -> {
                                navController.navigate(Routes.PROFILE) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TEAMS,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Teams screen — supports optional ?prompt=true query param
            composable(
                route = "teams?prompt={prompt}",
                arguments = listOf(
                    navArgument("prompt") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val showPrompt = backStackEntry.arguments?.getBoolean("prompt") ?: false
                val viewModel: TeamSelectionViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                TeamSelectionScreen(
                    uiState = uiState,
                    onSearch = viewModel::onSearch,
                    onRetry = viewModel::loadTeams,
                    onTeamClick = { team ->
                        appNavViewModel.selectTeam(team.id)
                        navController.navigate(Routes.teamDetail(team.id))
                    },
                    showSelectTeamPrompt = showPrompt,
                    scrollToTopEvent = appNavViewModel.scrollToTopEvent
                )
            }

            composable(
                route = Routes.TEAM_DETAIL,
                arguments = listOf(navArgument("teamId") { type = NavType.IntType })
            ) { backStackEntry ->
                val teamIdArg = backStackEntry.arguments?.getInt("teamId")
                teamIdArg?.let { appNavViewModel.selectTeam(it) }

                val viewModel: TeamDetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val teamId = teamIdArg ?: selectedTeamId ?: uiState.team?.id

                TeamDetailScreen(
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onFilterSelected = viewModel::setPositionFilter,
                    onOpenTactics = {
                        teamId?.let { id ->
                            // Push FieldScreen onto back stack (no launchSingleTop)
                            navController.navigate(Routes.field(id, showBack = true))
                        }
                    },
                    onRetry = viewModel::load
                )
            }

            composable(
                route = Routes.FIELD,
                arguments = listOf(
                    navArgument("teamId") { type = NavType.IntType },
                    navArgument("showBack") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val showBack = backStackEntry.arguments?.getBoolean("showBack") ?: false
                val viewModel: FieldViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                FieldScreen(
                    uiState = uiState,
                    onBack = if (showBack) {
                        { navController.popBackStack() }
                    } else {
                        null
                    },
                    onPlayerTap = viewModel::selectPlayer,
                    onPlayerDrag = viewModel::movePlayer,
                    onPlayerDragStart = viewModel::startDragging,
                    onPlayerDragEnd = viewModel::endDragging,
                    onSelectedFormation = viewModel::selectFormation,
                    onDismissSheet = { viewModel.selectPlayer(null) },
                    onSubstitutePlayer = viewModel::substitutePlayer,
                    onReset = viewModel::resetFormation,
                    onSave = viewModel::saveFormation,
                    onRetry = viewModel::loadPlayers
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen()
            }
        }
    }
}
