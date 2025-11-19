package com.speedtest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.speedtest.app.data.local.datastore.PreferencesManager
import com.speedtest.app.data.local.datastore.ThemeMode
import com.speedtest.app.domain.usecase.InitializeServersUseCase
import com.speedtest.app.presentation.navigation.SpeedTestNavigation
import com.speedtest.app.presentation.theme.SpeedTestTheme
import com.speedtest.app.utils.NotificationHelper
import com.speedtest.app.utils.PermissionUtils
import com.speedtest.app.worker.WorkManagerScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var initializeServersUseCase: InitializeServersUseCase

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            // Log or handle permission result
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request permissions
        requestPermissionsIfNeeded()

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
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

        // Initialize on first launch
        lifecycleScope.launch {
            try {
                val isFirstLaunch = preferencesManager.isFirstLaunch.first()
                if (isFirstLaunch) {
                    // Initialize default servers (don't fail if this errors)
                    try {
                        initializeServersUseCase()
                    } catch (e: Exception) {
                        // Log error but don't crash - servers can be loaded later
                        android.util.Log.e("MainActivity", "Failed to initialize servers", e)
                    }
                    preferencesManager.setFirstLaunchComplete()
                }

                // Setup auto test if enabled
                setupAutoTest()
            } catch (e: Exception) {
                // Catch any errors during initialization to prevent crashes
                android.util.Log.e("MainActivity", "Initialization error", e)
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val deniedPermissions = PermissionUtils.getDeniedPermissions(
            this,
            PermissionUtils.REQUIRED_PERMISSIONS
        )

        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }

    private suspend fun setupAutoTest() {
        val autoTestEnabled = preferencesManager.autoTestEnabled.first()
        val autoTestInterval = preferencesManager.autoTestInterval.first()

        if (autoTestEnabled) {
            WorkManagerScheduler.scheduleAutoSpeedTest(this, autoTestInterval)
        } else {
            WorkManagerScheduler.cancelAutoSpeedTest(this)
        }
    }
}
