package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserPreferences
import com.example.model.StringsCatalog
import com.example.ui.ToolzifyViewModel
import com.example.ui.components.LocalAppSettings
import com.example.ui.components.LuxuryCard
import com.example.ui.components.rememberHapticTrigger
import com.example.ui.theme.LocalToolzifyColors
import com.example.ui.theme.ThemeAccent
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    prefs: UserPreferences,
    viewModel: ToolzifyViewModel,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppSettings.current.strings
    val ext = LocalToolzifyColors.current
    val triggerHaptic = rememberHapticTrigger()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var clearedBanner by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = strings.settingsTitle,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Local preferences only — zero accounts or cloud sync",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (clearedBanner) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "All local app data has been cleared from this device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Appearance & Theme Mode
        item {
            LuxuryCard {
                SectionHeader(icon = Icons.Rounded.Palette, title = strings.appearance)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Display Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val selected = prefs.themeMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = {
                                triggerHaptic()
                                viewModel.setThemeMode(mode)
                            },
                            label = { Text(mode.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (mode) {
                                        ThemeMode.LIGHT -> Icons.Rounded.LightMode
                                        ThemeMode.DARK -> Icons.Rounded.DarkMode
                                        ThemeMode.SYSTEM -> Icons.Rounded.Contrast
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_${mode.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Theme Customization",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeAccent.entries.chunked(2).forEach { rowAccents ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowAccents.forEach { accent ->
                                val selected = prefs.themeAccent == accent
                                Surface(
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.setThemeAccent(accent)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else ext.creamSurface,
                                    border = BorderStroke(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else ext.cardBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("theme_accent_${accent.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(accent.primary)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(accent.accent)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = accent.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Language & Feedback
        item {
            LuxuryCard {
                SectionHeader(icon = Icons.Rounded.Tune, title = "${strings.language} & ${strings.feedbackMotion}")
                Spacer(modifier = Modifier.height(10.dp))

                val currentLangName = StringsCatalog.supportedLanguages.find { it.first == prefs.appLanguage }?.second ?: "English"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            triggerHaptic()
                            showLanguageDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("setting_language_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(strings.language, style = MaterialTheme.typography.titleSmall)
                            Text(currentLangName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                }

                HorizontalDivider(color = ext.cardBorder)

                SettingsSwitchRow(
                    icon = Icons.Rounded.Vibration,
                    title = "Haptic Feedback",
                    subtitle = "Gentle tactile pulse on buttons & Tasbeeh",
                    checked = prefs.hapticsEnabled,
                    onCheckedChange = { viewModel.setHapticsEnabled(it) },
                    testTag = "switch_haptics"
                )

                HorizontalDivider(color = ext.cardBorder)

                SettingsSwitchRow(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "Animation Preferences",
                    subtitle = "Smooth transitions, rings & card reveals",
                    checked = prefs.animationsEnabled,
                    onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                    testTag = "switch_animations"
                )

                HorizontalDivider(color = ext.cardBorder)

                SettingsSwitchRow(
                    icon = Icons.Rounded.Mosque,
                    title = "Show Islamic Tools Category",
                    subtitle = "Optional Prayer Times, Qibla, Tasbeeh & Ramadan",
                    checked = prefs.showIslamicTools,
                    onCheckedChange = { viewModel.setShowIslamicTools(it) },
                    testTag = "switch_islamic"
                )
            }
        }

        // Privacy & Data Management
        item {
            LuxuryCard {
                SectionHeader(icon = Icons.Rounded.VerifiedUser, title = strings.privacyData)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            triggerHaptic()
                            onOpenPrivacy()
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("open_privacy_info"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Privacy Information", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Zero data collection, zero tracking, 100% local",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                }

                HorizontalDivider(color = ext.cardBorder)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            triggerHaptic()
                            showClearDialog = true
                        }
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                        .testTag("clear_local_data_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(strings.clearLocalData, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text(
                                "Delete all local notes, tasks, habits & expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // About Toolzify
        item {
            LuxuryCard(containerColor = ext.goldContainer.copy(alpha = 0.45f)) {
                SectionHeader(icon = Icons.Rounded.Info, title = strings.aboutToolzify)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Toolzify v1.0 • Build Offline-First",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Everyday Tools, One Beautiful App. Crafted for daily productivity, precision calculations, and calm utility without ever collecting personal data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.language) },
            text = {
                Column {
                    StringsCatalog.supportedLanguages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setAppLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.appLanguage == code,
                                onClick = {
                                    viewModel.setAppLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear Local App Data?") },
            text = {
                Text("This permanently erases your locally stored tasks, notes, habits, checklists, expenses, water logs, and favorites from this device. Nothing is backed up externally.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllLocalData()
                        showClearDialog = false
                        clearedBanner = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyInfoScreen(onBack: () -> Unit) {
    val ext = LocalToolzifyColors.current
    val guarantees = listOf(
        "No user accounts, sign-ups, or login screens exist anywhere in Toolzify.",
        "Zero personal information (name, email, phone, address, age, gender) is ever requested.",
        "No analytics, advertising, tracking, or device-fingerprinting SDKs are installed.",
        "No internet permission is declared in AndroidManifest.xml — Toolzify cannot upload data.",
        "All notes, tasks, habits, expenses, and preferences live strictly in local SQLite & DataStore.",
        "Android Auto Backup (allowBackup) is explicitly disabled so local data never leaves your phone.",
        "Camera permission is requested only when you tap to open the live QR Scanner (Photo Picker fallback needs zero permissions).",
        "Coarse Location permission is requested only if you manually tap 'Use Device Location' in Prayer Times or Qibla — 30+ global cities work completely offline.",
        "Image Resizer uses Android's zero-permission Photo Picker without requesting broad storage access.",
        "You can erase 100% of local app data at any time from Settings."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Pledge") },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LuxuryCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Zero Data Collection", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Everything stays on your device. Always.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(guarantees.size) { index ->
                LuxuryCard(contentPadding = PaddingValues(14.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = ext.success,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = guarantees[index],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
