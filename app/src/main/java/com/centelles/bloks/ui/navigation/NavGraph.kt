package com.centelles.bloks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.centelles.bloks.ui.game.GameScreen
import com.centelles.bloks.ui.screens.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Menu.route
    ) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onPlayClick = { navController.navigate(Screen.Game.route) },
                onDailyChallengeClick = { navController.navigate(Screen.DailyChallenge.route) },
                onShopClick = { navController.navigate(Screen.Shop.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        
        composable(Screen.Game.route) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val activity = context as? android.app.Activity
            val adsManager = (activity as? com.centelles.bloks.MainActivity)?.adsManager

            GameScreen(
                onGameOver = { score ->
                    if (activity != null && adsManager != null) {
                        adsManager.showInterstitial(activity) {
                            navController.navigate("${Screen.GameOver.route}/$score") {
                                popUpTo(Screen.Menu.route)
                            }
                        }
                    } else {
                        navController.navigate("${Screen.GameOver.route}/$score") {
                            popUpTo(Screen.Menu.route)
                        }
                    }
                }
            )
        }
        
        composable(
            route = "${Screen.GameOver.route}/{score}",
            arguments = listOf(navArgument("score") { type = NavType.IntType })
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            GameOverScreen(
                score = score,
                onRestart = { navController.navigate(Screen.Game.route) { popUpTo(Screen.Menu.route) } },
                onMenu = { navController.navigate(Screen.Menu.route) { popUpTo(Screen.Menu.route) } },
                onContinue = {
                    // We navigate to game and the ViewModel will handle the "continue" state 
                    // because the GameEngine is a singleton and we'll tell it to continue
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.Shop.route) {
            ShopScreen(onBack = { navController.popBackStack() })
        }
        
        composable(Screen.DailyChallenge.route) {
            DailyChallengeScreen(
                onBack = { navController.popBackStack() },
                onStartChallenge = { 
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }
    }
}
