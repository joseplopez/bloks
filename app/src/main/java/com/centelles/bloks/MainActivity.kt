package com.centelles.bloks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.centelles.bloks.audio.SoundManager
import com.centelles.bloks.monetization.AdsManager
import com.centelles.bloks.ui.navigation.NavGraph
import com.centelles.bloks.ui.theme.BlockBloomTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var adsManager: AdsManager

    @Inject
    lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize UMP and Ads
        adsManager.initConsentAndAds(this) {
            // Consent flow finished or failed, ads might be ready
        }

        setContent {
            BlockBloomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        soundManager.onActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        soundManager.onActivityPaused()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
