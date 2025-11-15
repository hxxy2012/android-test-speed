package com.speedtest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.datastore.ThemeMode
import com.speedtest.app.presentation.navigation.SpeedTestNavigation
import com.speedtest.app.presentation.theme.SpeedTestTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            SpeedTestTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SpeedTestNavigation()
                }
            }
        }

        // Initialize default servers on first launch
        lifecycleScope.launch {
            val isFirstLaunch = preferencesManager.isFirstLaunch.first()
            if (isFirstLaunch) {
                // TODO: Initialize default servers
                preferencesManager.setFirstLaunchComplete()
            }
        }
    }
}
