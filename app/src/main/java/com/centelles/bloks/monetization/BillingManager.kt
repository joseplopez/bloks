package com.centelles.bloks.monetization

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.QueryProductDetailsParams.Product
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady = _isBillingReady.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products = _products.asStateFlow()

    private val _purchaseSuccess = MutableSharedFlow<Purchase>()
    val purchaseSuccess = _purchaseSuccess.asSharedFlow()

    private val _isRemoveAdsPurchased = MutableStateFlow(false)
    val isRemoveAdsPurchased = _isRemoveAdsPurchased.asStateFlow()

    companion object {
        const val PRODUCT_REMOVE_ADS = "remove_ads"
        const val PRODUCT_COINS_500 = "coins_500"
        const val PRODUCT_COINS_1000 = "coins_1000"

        val ALL_PRODUCTS = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_COINS_500)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_COINS_1000)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
    }

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    queryProductDetails()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                _isBillingReady.value = false
            }
        })
    }

    private fun queryProductDetails() {
        scope.launch {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(ALL_PRODUCTS)
                .build()

            val productDetailsResult = billingClient.queryProductDetails(params)
            if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = productDetailsResult.productDetailsList ?: emptyList()
            }
        }
    }

    fun queryPurchases() {
        if (!billingClient.isReady) return

        scope.launch {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            val purchasesResult = billingClient.queryPurchasesAsync(params)
            if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchases(purchasesResult.purchasesList)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (purchase.products.contains(PRODUCT_REMOVE_ADS)) {
                    _isRemoveAdsPurchased.value = true
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    } else {
                        scope.launch {
                            _purchaseSuccess.emit(purchase)
                        }
                    }
                } else {
                    // Consumables: emit immediately so the app can consume them
                    scope.launch {
                        _purchaseSuccess.emit(purchase)
                    }
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch {
                    _purchaseSuccess.emit(purchase)
                }
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    fun consumePurchase(purchase: Purchase, onComplete: (BillingResult) -> Unit) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient.consumeAsync(params) { billingResult, _ ->
            onComplete(billingResult)
        }
    }
}
