package com.ads.admob_sdk.core

import android.content.Context
import com.ads.admob_sdk.callback.InterstitialCallback
import com.google.android.gms.ads.interstitial.InterstitialAd


interface AdmobInterstitialAdFactory {
    fun requestInterstitialAd(context: Context, adId: String, adCallback: InterstitialCallback)
    fun showInterstitial(
        context: Context,
        interstitialAd: InterstitialAd?,
        adCallback: InterstitialCallback
    )

    companion object {
        @JvmStatic
        fun getInstance(): AdmobInterstitialAdFactory = AdmobInterstitialAdFactoryImpl()
    }
}