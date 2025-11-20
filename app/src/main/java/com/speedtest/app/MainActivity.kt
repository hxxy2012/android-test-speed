package com.speedtest.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var initializeServersUseCase: InitializeServersUseCase

    private var isInitialized = false

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
        permissions.entries.forEach { entry ->
            Log.d(TAG, "Permission ${entry.key}: ${if (entry.value) "granted" else "denied"}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate started")
        super.onCreate(savedInstanceState)

        try {
            // Request permissions early
            requestPermissionsIfNeeded()

            // Set content with error boundary
            setContent {
                var appReady by remember { mutableStateOf(false) }
                var initError by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    try {
                        // Give Hilt time to inject dependencies
                        delay(100)

                        // Initialize app in background
                        initializeApp()

                        appReady = true
                    } catch (e: Exception) {
                        Log.e(TAG, "Initialization failed", e)
                        initError = e.message ?: "Unknown error"
                        // Still set appReady to show UI even if init failed
                        appReady = true
                    }
                }

                if (!appReady) {
                    // Show loading screen while initializing
                    LoadingScreen()
                } else {
                    // Show main app UI
                    MainAppContent(initError)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate", e)
            // Show error screen
            showErrorScreen(e)
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    private fun MainAppContent(initError: String?) {
        try {
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
                    if (initError != null) {
                        // Show error but still allow using the app
                        Log.w(TAG, "App started with initialization warning: $initError")
                    }
                    SpeedTestNavigation()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in MainAppContent", e)
            // Fallback to simple UI
            SpeedTestTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error loading app: ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun initializeApp() {
        try {
            Log.d(TAG, "Initializing app...")

            // Check if first launch
            val isFirstLaunch = try {
                preferencesManager.isFirstLaunch.first()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check first launch status", e)
                true // Assume first launch if we can't read it
            }

            if (isFirstLaunch) {
                Log.d(TAG, "First launch detected, initializing servers...")
                try {
                    initializeServersUseCase()
                    Log.d(TAG, "Servers initialized successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize servers (non-fatal)", e)
                    // Don't fail - servers can be loaded later
                }

                try {
                    preferencesManager.setFirstLaunchComplete()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save first launch complete", e)
                }
            }

            // Setup auto test if enabled
            try {
                setupAutoTest()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to setup auto test (non-fatal)", e)
            }

            isInitialized = true
            Log.d(TAG, "App initialization complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            throw e
        }
    }

    private fun requestPermissionsIfNeeded() {
        try {
            val deniedPermissions = PermissionUtils.getDeniedPermissions(
                this,
                PermissionUtils.REQUIRED_PERMISSIONS
            )

            if (deniedPermissions.isNotEmpty()) {
                Log.d(TAG, "Requesting permissions: ${deniedPermissions.joinToString()}")
                permissionLauncher.launch(deniedPermissions.toTypedArray())
            } else {
                Log.d(TAG, "All required permissions already granted")
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
                Log.d(TAG, "Auto test enabled, scheduling with interval: $autoTestInterval hours")
                WorkManagerScheduler.scheduleAutoSpeedTest(this, autoTestInterval)
            } else {
                Log.d(TAG, "Auto test disabled, cancelling any scheduled tests")
                WorkManagerScheduler.cancelAutoSpeedTest(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up auto test", e)
            throw e
        }
    }

    private fun showErrorScreen(error: Exception) {
        try {
            setContent {
                SpeedTestTheme(darkTheme = false) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Failed to start app: ${error.message}\n\nPlease check logcat for details.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show error screen", e)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
