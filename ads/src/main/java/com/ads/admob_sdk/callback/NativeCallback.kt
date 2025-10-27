package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd

interface NativeCallback : AdCallback<ContentAd> {
    fun populateNativeAd()
}