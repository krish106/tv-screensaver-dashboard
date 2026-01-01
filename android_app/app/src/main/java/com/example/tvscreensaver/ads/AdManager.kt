package com.example.tvscreensaver.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.example.tvscreensaver.billing.SubscriptionRepository
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(
    private val context: Context,
    private val subscriptionRepository: SubscriptionRepository
) {

    companion object {
        private const val TAG = "AdManager"
        // Test Ad Unit IDs - Replace with your real Ad Unit IDs before release
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    private var interstitialAd: InterstitialAd? = null
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        
        MobileAds.initialize(context) {
            isInitialized = true
            Log.d(TAG, "AdMob initialized")
        }
    }

    fun shouldShowAds(): Boolean {
        return !subscriptionRepository.isSubscriptionActive()
    }

    fun loadBannerAd(adView: AdView) {
        if (!shouldShowAds()) {
            adView.visibility = View.GONE
            return
        }

        adView.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${error.message}")
                adView.visibility = View.GONE
            }
        }
        
        adView.loadAd(adRequest)
    }

    fun loadInterstitialAd() {
        if (!shouldShowAds()) {
            return
        }

        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.e(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (!shouldShowAds()) {
            onAdClosed()
            return
        }

        interstitialAd?.let { ad ->
            ad.show(activity)
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onAdClosed()
                    // Load next ad
                    loadInterstitialAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdClosed()
                }
            }
        } ?: run {
            onAdClosed()
            // Try to load ad for next time
            loadInterstitialAd()
        }
    }

    fun hideBannerAd(adView: AdView) {
        adView.visibility = View.GONE
        adView.destroy()
    }
}
