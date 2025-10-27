package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

interface AdCallback<T : ContentAd> {
    fun onAdLoaded(data: T)
    fun onAdFailedToLoad(loadAdError: LoadAdError)
    fun onAdClicked()
    fun onAdImpression()
    fun onAdFailedToShow(adError: AdError)
}