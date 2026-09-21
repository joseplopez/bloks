package com.centelles.bloks.ui.navigation

sealed class Screen(val route: String) {
    object Menu : Screen("menu")
    object Game : Screen("game")
    object GameOver : Screen("game_over")
    object Settings : Screen("settings")
    object Shop : Screen("shop")
    object DailyChallenge : Screen("daily_challenge")
}
