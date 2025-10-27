package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.LoadAdError

interface AdRequestCallBack {
     fun onAdLoaded(data: ContentAd)
    fun onAdFailedToLoad(loadAdError: LoadAdError)
}