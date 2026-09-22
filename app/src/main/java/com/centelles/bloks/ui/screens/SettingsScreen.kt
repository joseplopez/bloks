package com.centelles.bloks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import com.centelles.bloks.R
import com.centelles.bloks.ui.components.BloomCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            BloomCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggle(
                        label = stringResource(R.string.settings_sound_effects),
                        checked = soundEnabled,
                        onCheckedChange = { viewModel.toggleSound(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.background)
                    SettingToggle(
                        label = stringResource(R.string.settings_background_music),
                        checked = musicEnabled,
                        onCheckedChange = { viewModel.toggleMusic(it) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.background)
                    SettingToggle(
                        label = stringResource(R.string.settings_vibration),
                        checked = vibrationEnabled,
                        onCheckedChange = { viewModel.toggleVibration(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_feedback_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            BloomCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItem(
                        title = stringResource(R.string.settings_send_suggestions),
                        subtitle = stringResource(R.string.settings_send_suggestions_desc),
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("joseplcatz@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Block Bloom")
                            }
                            context.startActivity(intent)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.background)
                    SettingItem(
                        title = stringResource(R.string.settings_rate_us),
                        subtitle = stringResource(R.string.settings_rate_us_desc),
                        onClick = {
                            val packageName = context.packageName
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("market://details?id=$packageName")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.settings_version_format, "1.0.0"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
