package com.ads.admob_sdk.core

import android.app.Activity
import android.content.Context
import com.ads.admob_sdk.utils.getAdRequest
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class AdmobInterstitialAdFactoryImpl : AdmobInterstitialAdFactory {
    override fun requestInterstitialAd(
        context: Context,
        adId: String,
        adCallback: InterstitialCallback
    ) {
        InterstitialAd.load(
            context,
            adId,
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adCallback.onAdFailedToLoad(adError)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    adCallback.onAdLoaded(ContentAd.AdmobAd.ApInterstitialAd(ad))
                }
            }
        )
    }

    override fun showInterstitial(
        context: Context,
        interstitialAd: InterstitialAd?,
        adCallback: InterstitialCallback
    ) {
        if (interstitialAd == null) {
            adCallback.onAdClose()
            return
        }

        interstitialAd.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    adCallback.onAdClose()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    adCallback.onAdFailedToShow(adError)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when ad is dismissed.
                    adCallback.onInterstitialShow()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdmobManager.adsClicked()
                    adCallback.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }
            }
        interstitialAd.show(context as Activity)
    }
}