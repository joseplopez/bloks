package com.centelles.bloks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centelles.bloks.R
import com.centelles.bloks.ui.components.BloomButton
import com.centelles.bloks.ui.components.BloomCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(onBack: () -> Unit, onStartChallenge: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.daily_challenge_title), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📅", fontSize = 80.sp)
            Text(
                text = stringResource(R.string.daily_challenge_today),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            BloomCard {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.daily_challenge_objective_label), style = MaterialTheme.typography.labelLarge)
                    Text(
                        stringResource(R.string.daily_challenge_objective_desc),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(stringResource(R.string.daily_challenge_reward_label), style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙", fontSize = 24.sp)
                        Text(" 200", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            BloomButton(
                text = stringResource(R.string.daily_challenge_start),
                onClick = onStartChallenge,
                containerColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
