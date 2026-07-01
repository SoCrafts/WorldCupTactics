package com.worldcup.tactics.navigation

enum class BottomNavTab(val route: String, val label: String) {
    Teams("teams", "Teams"),
    Tactics("tactics", "Tactics"),
    Profile("profile", "Profile")
}

object Routes {
    const val TEAMS = "teams"
    const val TEAMS_PROMPT = "teams?prompt=true"
    const val TEAM_DETAIL = "team_detail/{teamId}"
    const val FIELD = "field/{teamId}?showBack={showBack}"
    const val PROFILE = "profile"

    fun teamDetail(teamId: Int) = "team_detail/$teamId"
    fun field(teamId: Int, showBack: Boolean = false) = "field/$teamId?showBack=$showBack"
}
