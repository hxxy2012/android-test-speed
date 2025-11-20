package com.speedtest.app.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    try {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                ErrorBoundary {
                    HomeScreen(navController = navController)
                }
            }

            composable(Screen.History.route) {
                ErrorBoundary {
                    HistoryScreen(navController = navController)
                }
            }

            composable(Screen.Server.route) {
                ErrorBoundary {
                    ServerScreen(navController = navController)
                }
            }

            composable(Screen.Settings.route) {
                ErrorBoundary {
                    SettingsScreen(navController = navController)
                }
            }
        }
    } catch (e: Exception) {
        Log.e("SpeedTestNavigation", "Navigation error", e)
        ErrorScreen(error = e.message ?: "Navigation error")
    }
}

@Composable
private fun ErrorBoundary(content: @Composable () -> Unit) {
    try {
        content()
    } catch (e: Exception) {
        Log.e("ErrorBoundary", "Screen rendering error", e)
        ErrorScreen(error = e.message ?: "Unknown error")
    }
}

@Composable
private fun ErrorScreen(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $error",
            color = MaterialTheme.colorScheme.error
        )
    }
}
