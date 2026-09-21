package com.centelles.bloks.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor() {
    private val TAG = "AdsManager"

    // Test IDs
    private val INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private val REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private lateinit var consentInformation: ConsentInformation

    /**
     * Initializes UMP SDK and requests consent.
     * If consent is gathered, it initializes Mobile Ads.
     */
    fun initConsentAndAds(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "${formError.errorCode}: ${formError.message}")
                    }

                    if (consentInformation.canRequestAds()) {
                        initializeMobileAdsSdk(activity)
                    }
                    onComplete()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "${requestConsentError.errorCode}: ${requestConsentError.message}")
                if (consentInformation.canRequestAds()) {
                    initializeMobileAdsSdk(activity)
                }
                onComplete()
            }
        )
        
        // Check if ads can already be requested (e.g. from previous session)
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsSdk(activity)
        }
    }

    private fun initializeMobileAdsSdk(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        MobileAds.initialize(context) {
            loadInterstitial(context)
            loadRewarded(context)
        }
    }

    // --- INTERSTITIAL ---

    fun loadInterstitial(context: Context) {
        if (!consentInformation.canRequestAds()) return
        
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
                Log.d(TAG, "Interstitial failed to load: ${adError.message}")
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial loaded")
            }
        })
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            onDismissed()
        }
    }

    // --- REWARDED ---

    fun loadRewarded(context: Context) {
        if (!consentInformation.canRequestAds()) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
                Log.d(TAG, "Rewarded failed to load: ${adError.message}")
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d(TAG, "Rewarded loaded")
            }
        })
    }

    fun showRewarded(activity: Activity, onRewardEarned: (Int) -> Unit, onDismissed: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    onDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    onDismissed()
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                onRewardEarned(rewardItem.amount)
            }
        } else {
            onDismissed()
        }
    }
}
