package com.speedtest.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.speedtest.app.data.local.datastore.SpeedUnit
import com.speedtest.app.data.local.datastore.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Test Settings Section
            SettingsSection(title = "Test Settings") {
                SettingItem(
                    icon = Icons.Default.Speed,
                    title = "Speed Unit",
                    subtitle = uiState.speedUnit.name,
                    onClick = { /* Show speed unit selector */ }
                )

                SettingItem(
                    icon = Icons.Default.Timer,
                    title = "Test Duration",
                    subtitle = "${uiState.testDuration} seconds",
                    onClick = { /* Show duration selector */ }
                )

                SettingItem(
                    icon = Icons.Default.Grain,
                    title = "Thread Count",
                    subtitle = if (uiState.threadCount == 0) "Auto" else "${uiState.threadCount}",
                    onClick = { /* Show thread count selector */ }
                )
            }

            Divider()

            // Display Settings Section
            SettingsSection(title = "Display") {
                var showThemeDialog by remember { mutableStateOf(false) }

                SettingItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = uiState.themeMode.name,
                    onClick = { showThemeDialog = true }
                )

                if (showThemeDialog) {
                    ThemeSelectorDialog(
                        currentTheme = uiState.themeMode,
                        onThemeSelected = { theme ->
                            viewModel.setThemeMode(theme)
                            showThemeDialog = false
                        },
                        onDismiss = { showThemeDialog = false }
                    )
                }
            }

            Divider()

            // Auto Test Section
            SettingsSection(title = "Automated Testing") {
                SwitchSettingItem(
                    icon = Icons.Default.Schedule,
                    title = "Enable Auto Test",
                    subtitle = "Run tests automatically",
                    checked = uiState.autoTestEnabled,
                    onCheckedChange = { viewModel.setAutoTestEnabled(it) }
                )

                if (uiState.autoTestEnabled) {
                    SettingItem(
                        icon = Icons.Default.AccessTime,
                        title = "Test Interval",
                        subtitle = "Every ${uiState.autoTestInterval} hours",
                        onClick = { /* Show interval selector */ }
                    )
                }
            }

            Divider()

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SwitchSettingItem(
                    icon = Icons.Default.Notifications,
                    title = "Enable Notifications",
                    subtitle = "Get notified when tests complete",
                    checked = uiState.notificationEnabled,
                    onCheckedChange = { viewModel.setNotificationEnabled(it) }
                )
            }

            Divider()

            // Data Management Section
            SettingsSection(title = "Data") {
                SettingItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all test history",
                    onClick = { /* Show confirmation dialog */ }
                )
            }

            Divider()

            // About Section
            SettingsSection(title = "About") {
                SettingItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )

                SettingItem(
                    icon = Icons.Default.Description,
                    title = "Privacy Policy",
                    subtitle = "View privacy policy",
                    onClick = { }
                )

                SettingItem(
                    icon = Icons.Default.Code,
                    title = "Open Source Licenses",
                    subtitle = "View licenses",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        content()
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SwitchSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
fun ThemeSelectorDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = theme == currentTheme,
                            onClick = { onThemeSelected(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(theme.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
