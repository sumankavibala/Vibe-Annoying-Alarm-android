package com.example.annoyingalarm.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private const val TAG = "InterstitialAdManager"

    fun loadAd(context: Context, adUnitId: String = "ca-app-pub-6588353673153309/8184535322") {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    Log.d(TAG, "Interstitial Ad Loaded successfully.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    Log.d(TAG, "Interstitial Ad failed to load: ${adError.message}")
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        val ad = mInterstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed full screen content.")
                    mInterstitialAd = null
                    loadAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.d(TAG, "Ad failed to show full screen content: ${adError.message}")
                    mInterstitialAd = null
                    loadAd(activity)
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad wasn't ready yet.")
            onAdDismissed()
        }
    }
}
