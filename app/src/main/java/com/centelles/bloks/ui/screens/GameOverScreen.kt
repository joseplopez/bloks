package com.centelles.bloks.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.centelles.bloks.R
import com.centelles.bloks.ui.components.BloomButton
import com.centelles.bloks.ui.components.BloomCard

@Composable
fun GameOverScreen(
    score: Int,
    onRestart: () -> Unit,
    onMenu: () -> Unit,
    onContinue: () -> Unit,
    viewModel: ShopViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coins by viewModel.coins.collectAsState()
    val bestScore by viewModel.highScore.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.game_over_title),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "🪙 $coins",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        BloomCard(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.game_over_score_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (score > bestScore && score > 0) {
                    Text(
                        text = stringResource(R.string.game_over_new_record),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.game_over_personal_best), color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = bestScore.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        BloomButton(
            text = stringResource(R.string.game_over_continue_video),
            onClick = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.watchAdToContinue(activity) {
                        onContinue()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BloomButton(
            text = stringResource(R.string.game_over_continue_coins),
            enabled = coins >= 200,
            onClick = {
                viewModel.continueWithCoins {
                    onContinue()
                }
            },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        BloomButton(
            text = stringResource(R.string.game_over_retry),
            onClick = {
                viewModel.restartGame()
                onRestart()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BloomButton(
            text = stringResource(R.string.game_over_menu),
            onClick = onMenu,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}
