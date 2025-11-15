package com.speedtest.app.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.speedtest.app.data.local.datastore.SpeedUnit
import com.speedtest.app.domain.model.TestPhase
import com.speedtest.app.presentation.navigation.Screen
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message if any
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("SpeedTest") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Server.route) }) {
                        Icon(Icons.Default.Storage, contentDescription = "Select Server")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Test") },
                    label = { Text("Test") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.History.route) },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Network Info Card
            NetworkInfoCard(uiState)

            // Speed Gauge
            SpeedGauge(
                progress = uiState.testProgress,
                speedUnit = uiState.speedUnit,
                onUnitClick = { viewModel.toggleSpeedUnit() }
            )

            // Test Details
            TestDetailsCard(uiState)

            // Start/Stop Button
            StartTestButton(
                isTestRunning = uiState.isTestRunning,
                canStartTest = uiState.canStartTest,
                onStart = { viewModel.startTest() },
                onStop = { viewModel.stopTest() }
            )

            // Server Info
            uiState.selectedServer?.let { server ->
                ServerInfoCard(server)
            }
        }
    }
}

@Composable
fun NetworkInfoCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Network",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.networkInfo.networkType.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Operator",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.networkInfo.operator,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "IP Address",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.networkInfo.ipAddress,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SpeedGauge(
    progress: com.speedtest.app.domain.model.TestProgress,
    speedUnit: SpeedUnit,
    onUnitClick: () -> Unit
) {
    val currentSpeed = when (progress.phase) {
        TestPhase.DOWNLOAD -> progress.currentDownloadSpeed
        TestPhase.UPLOAD -> progress.currentUploadSpeed
        else -> 0.0
    }

    val displaySpeed = if (speedUnit == SpeedUnit.MBYTES) {
        currentSpeed / 8.0
    } else {
        currentSpeed
    }

    val unitText = if (speedUnit == SpeedUnit.MBYTES) "MB/s" else "Mbps"

    Box(
        modifier = Modifier
            .size(280.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Circular progress indicator
        CircularProgressGauge(
            progress = progress.progress,
            phase = progress.phase
        )

        // Speed text in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format("%.1f", displaySpeed),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onUnitClick) {
                Text(text = unitText)
            }
            Text(
                text = progress.phase.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CircularProgressGauge(
    progress: Float,
    phase: TestPhase
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    val color = when (phase) {
        TestPhase.PING -> MaterialTheme.colorScheme.tertiary
        TestPhase.DOWNLOAD -> MaterialTheme.colorScheme.primary
        TestPhase.UPLOAD -> MaterialTheme.colorScheme.secondary
        TestPhase.COMPLETED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 20.dp.toPx()
        val size = size.minDimension - strokeWidth

        // Background arc
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2),
            size = Size(size, size),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            topLeft = Offset((this.size.width - size) / 2, (this.size.height - size) / 2),
            size = Size(size, size),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TestDetailsCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SpeedMetric(
                icon = Icons.Default.Download,
                label = "Download",
                value = String.format("%.1f", uiState.testProgress.avgDownloadSpeed),
                unit = "Mbps"
            )
            SpeedMetric(
                icon = Icons.Default.Upload,
                label = "Upload",
                value = String.format("%.1f", uiState.testProgress.avgUploadSpeed),
                unit = "Mbps"
            )
            SpeedMetric(
                icon = Icons.Default.Timer,
                label = "Ping",
                value = "${uiState.testProgress.avgPing}",
                unit = "ms"
            )
        }
    }
}

@Composable
fun SpeedMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StartTestButton(
    isTestRunning: Boolean,
    canStartTest: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    FloatingActionButton(
        onClick = if (isTestRunning) onStop else onStart,
        modifier = Modifier.size(80.dp),
        containerColor = if (isTestRunning) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }
    ) {
        Icon(
            imageVector = if (isTestRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = if (isTestRunning) "Stop Test" else "Start Test",
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun ServerInfoCard(server: com.speedtest.app.data.local.entity.Server) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Test Server",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${server.city}, ${server.country}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (server.lastPing > 0) {
                Text(
                    text = "Ping: ${server.lastPing} ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
