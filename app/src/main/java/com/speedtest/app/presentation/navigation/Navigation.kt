package com.speedtest.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.speedtest.app.presentation.history.HistoryScreen
import com.speedtest.app.presentation.home.HomeScreen
import com.speedtest.app.presentation.server.ServerScreen
import com.speedtest.app.presentation.settings.SettingsScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object History : Screen("history")
    object Server : Screen("server")
    object Settings : Screen("settings")
}

/**
 * Main navigation composable
 */
@Composable
fun SpeedTestNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(Screen.Server.route) {
            ServerScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
