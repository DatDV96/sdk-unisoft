package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd

interface InterstitialCallback : AdCallback<ContentAd> {
    fun onAdClose()
    fun onInterstitialShow()
}