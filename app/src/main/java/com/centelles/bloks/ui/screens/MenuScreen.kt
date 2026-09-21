package com.centelles.bloks.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.centelles.bloks.R
import com.centelles.bloks.ui.components.BannerAd
import com.centelles.bloks.ui.components.BloomButton
import com.centelles.bloks.ui.components.BloomSecondaryButton

@Composable
fun MenuScreen(
    onPlayClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    onShopClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.menu_title_block),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.menu_title_bloom),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            BloomButton(
                text = stringResource(R.string.menu_play),
                onClick = onPlayClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BloomButton(
                text = stringResource(R.string.menu_daily_challenge),
                onClick = onDailyChallengeClick,
                containerColor = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            BloomButton(
                text = stringResource(R.string.menu_shop),
                onClick = onShopClick,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            BloomSecondaryButton(
                text = stringResource(R.string.menu_settings),
                onClick = onSettingsClick
            )
        }

        BannerAd(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
