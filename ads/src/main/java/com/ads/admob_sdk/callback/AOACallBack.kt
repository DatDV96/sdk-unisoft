package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd

interface AOACallBack : AdCallback<ContentAd> {
    fun onAppOpenAdShow()
    fun onAppOpenAdClose()
}