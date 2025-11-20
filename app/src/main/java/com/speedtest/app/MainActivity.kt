package com.speedtest.app

import android.os.Bundle
import android.util.Log
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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            Log.d(TAG, "Permission ${entry.key}: ${if (entry.value) "granted" else "denied"}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        try {
            requestPermissionsIfNeeded()
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting permissions", e)
        }

        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in setContent", e)
        }

        // Initialize in background
        lifecycleScope.launch {
            try {
                initializeApp()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error (non-fatal)", e)
            }
        }
    }

    private suspend fun initializeApp() {
        try {
            Log.d(TAG, "Initializing app...")

            val isFirstLaunch = try {
                preferencesManager.isFirstLaunch.first()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check first launch", e)
                false
            }

            if (isFirstLaunch) {
                Log.d(TAG, "First launch, initializing servers...")
                try {
                    initializeServersUseCase()
                    Log.d(TAG, "Servers initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize servers", e)
                }

                try {
                    preferencesManager.setFirstLaunchComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save first launch complete", e)
                }
            }

            try {
                setupAutoTest()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to setup auto test", e)
            }

            Log.d(TAG, "Initialization complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
        }
    }

    private fun requestPermissionsIfNeeded() {
        try {
            val deniedPermissions = PermissionUtils.getDeniedPermissions(
                this,
                PermissionUtils.REQUIRED_PERMISSIONS
            )

            if (deniedPermissions.isNotEmpty()) {
                Log.d(TAG, "Requesting ${deniedPermissions.size} permissions")
                permissionLauncher.launch(deniedPermissions.toTypedArray())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
        }
    }

    private suspend fun setupAutoTest() {
        try {
            val autoTestEnabled = preferencesManager.autoTestEnabled.first()
            val autoTestInterval = preferencesManager.autoTestInterval.first()

            if (autoTestEnabled) {
                WorkManagerScheduler.scheduleAutoSpeedTest(this, autoTestInterval)
            } else {
                WorkManagerScheduler.cancelAutoSpeedTest(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupAutoTest", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
