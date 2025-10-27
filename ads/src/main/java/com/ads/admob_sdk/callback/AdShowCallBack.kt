package com.ads.admob_sdk.callback

import com.google.android.gms.ads.AdError

interface AdShowCallBack {
    fun onAdClose()
    fun onAdClicked()
    fun onAdImpression()
    fun onAdFailedToShow(adError: AdError)
}