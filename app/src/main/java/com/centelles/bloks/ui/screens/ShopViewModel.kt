package com.centelles.bloks.ui.screens

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.centelles.bloks.data.GameRepository
import com.centelles.bloks.engine.logic.GameEngine
import com.centelles.bloks.monetization.AdsManager
import com.centelles.bloks.monetization.AnalyticsManager
import com.centelles.bloks.monetization.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val repository: GameRepository,
    private val adsManager: AdsManager,
    private val gameEngine: GameEngine,
    private val billingManager: BillingManager,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val coins: StateFlow<Int> = repository.coinsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val highScore: StateFlow<Int> = repository.highScoreFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val products: StateFlow<List<ProductDetails>> = billingManager.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isRemoveAdsPurchased: StateFlow<Boolean> = repository.removeAdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeThemeId: StateFlow<Int> = repository.activeThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unlockedThemesMask: StateFlow<Int> = repository.unlockedThemesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    init {
        viewModelScope.launch {
            billingManager.purchaseSuccess.collectLatest { purchase ->
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: com.android.billingclient.api.Purchase) {
        viewModelScope.launch {
            val productId = purchase.products.firstOrNull() ?: "unknown"
            analyticsManager.logPurchase(productId, 0.0, "EUR") // Simplified price logging

            if (purchase.products.contains(BillingManager.PRODUCT_REMOVE_ADS)) {
                repository.setRemoveAds(true)
            } else if (purchase.products.contains(BillingManager.PRODUCT_COINS_500)) {
                repository.addCoins(500)
                billingManager.consumePurchase(purchase) { /* Consume callback */ }
            } else if (purchase.products.contains(BillingManager.PRODUCT_COINS_1000)) {
                repository.addCoins(1000)
                billingManager.consumePurchase(purchase) { /* Consume callback */ }
            }
        }
    }

    fun buyProduct(activity: Activity, productDetails: ProductDetails) {
        billingManager.launchBillingFlow(activity, productDetails)
    }

    fun watchAdForCoins(activity: Activity) {
        adsManager.showRewarded(
            activity = activity,
            onRewardEarned = {
                analyticsManager.logAdView("rewarded_coins", "rewarded")
                viewModelScope.launch {
                    repository.addCoins(100)
                }
            },
            onDismissed = {}
        )
    }

    fun watchAdToContinue(activity: Activity, onComplete: () -> Unit) {
        adsManager.showRewarded(
            activity = activity,
            onRewardEarned = {
                analyticsManager.logAdView("rewarded_continue", "rewarded")
                gameEngine.continueAfterGameOver()
                onComplete()
            },
            onDismissed = {}
        )
    }

    fun continueWithCoins(onComplete: () -> Unit) {
        viewModelScope.launch {
            val success = repository.spendCoins(200)
            if (success) {
                gameEngine.continueAfterGameOver()
                onComplete()
            }
        }
    }

    fun restartGame() {
        gameEngine.startNewGame()
    }

    fun selectOrBuyTheme(themeId: Int, cost: Int) {
        viewModelScope.launch {
            val isUnlocked = (unlockedThemesMask.value and (1 shl themeId)) != 0
            if (isUnlocked) {
                repository.setActiveTheme(themeId)
            } else {
                val success = repository.spendCoins(cost)
                if (success) {
                    repository.unlockTheme(themeId)
                    repository.setActiveTheme(themeId)
                }
            }
        }
    }
}
