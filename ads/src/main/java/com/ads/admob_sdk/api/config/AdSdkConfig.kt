package com.ads.admob_sdk.api.config

import android.app.Application

data class AdSdkConfig (
    val application: Application? = null,
    val listDevices: List<String> = arrayListOf(),
    val intervalBetweenInterstitial: Long = 10_000,
)