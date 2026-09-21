package com.centelles.bloks.monetization

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor() {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
    }

    fun logGameStart(mode: String = "classic") {
        val bundle = Bundle().apply {
            putString("game_mode", mode)
        }
        logEvent("game_start", bundle)
    }

    fun logGameOver(score: Int, highscore: Int) {
        val bundle = Bundle().apply {
            putInt("score", score)
            putInt("highscore", highscore)
        }
        logEvent("game_over", bundle)
    }

    fun logAdView(adUnitId: String, adType: String) {
        val bundle = Bundle().apply {
            putString("ad_unit_id", adUnitId)
            putString("ad_type", adType)
        }
        logEvent("ad_view", bundle)
    }
    
    fun logPurchase(productId: String, price: Double, currency: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, productId)
            putDouble(FirebaseAnalytics.Param.VALUE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
        }
        logEvent(FirebaseAnalytics.Event.PURCHASE, bundle)
    }
}
