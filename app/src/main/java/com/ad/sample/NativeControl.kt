package com.ad.sample

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.ads.admob_sdk.api.AdSdkManager
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.NativeCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

internal object NativeControl {
    private val TAG = NativeControl::class.simpleName
    var nativeLFO1: MutableLiveData<ContentAd?> = MutableLiveData()
    var nativeLFO2: MutableLiveData<ContentAd?> = MutableLiveData()
    var nativeOb1: MutableLiveData<ContentAd?> = MutableLiveData()
    var nativeObFullScreen: MutableLiveData<ContentAd?> = MutableLiveData()


    fun requestNativeOnboardingFullScreen23(
        context: Context,
    ) {
        if (ConfigManager.isShowNativeFullScreen == true) {
            Log.d(TAG, "requestNativeOnboardingFull ")
            requestNativeAd(
                context,
                "ca-app-pub-3940256099942544/2247696110",
                onAdLoaded = { nativeAd ->
                    nativeObFullScreen.postValue(nativeAd)
                },
                onFailedToLoad = {
                    nativeObFullScreen.postValue(null)
                },
                onAdImpression = {},
                onClick = {},
            )
        } else {
            if (nativeObFullScreen.value == null) {
                nativeObFullScreen.postValue(null)
            }
            Log.e(TAG, "requestNativeOnboarding3: invalid")
        }
    }

    fun requestNativeOnboarding1(
        context: Context
    ) {
        if (ConfigManager.isShowNativeOnboarding1) {
            Log.d(TAG, "requestNativeOnboarding1 ")
            requestNativeAd(
                context,
                "ca-app-pub-3940256099942544/2247696110",
                onAdLoaded = { nativeAd ->
                    nativeOb1.postValue(nativeAd)
                },
                onFailedToLoad = {
                    nativeOb1.postValue(null)
                },
                onAdImpression = {},
                onClick = {},
            )
        } else {
            nativeOb1.postValue(null)
            Log.e(TAG, "requestNativeOnboarding1: invalid")
        }
    }

    fun requestNativeLFO1(
        context: Context,
    ) {
        if (ConfigManager.isShowNativeLanguage == true) {
            Log.d(TAG, "requestNativeLFO1 ")
            if (ConfigManager.isShowNativeLanguage2f == true) {
                requestNativeAlternate(
                    context,
                    idAdPriorityAd = "ca-app-pub-3940256099942544/1044960115",
                    idAdAllPrice = "ca-app-pub-3940256099942544/2247696110",
                    onAdLoaded = { nativeAd ->
                        nativeLFO1.postValue(nativeAd)
                    },
                    onFailedToLoad = {
                        nativeLFO1.postValue(null)
                    },
                    onAdImpression = {

                    },
                    onClick = {},
                )
            } else {
                requestNativeAd(
                    context,
                    "ca-app-pub-3940256099942544/2247696110",
                    onAdLoaded = { nativeAd ->
                        nativeLFO1.postValue(nativeAd)
                    },
                    onFailedToLoad = {
                        nativeLFO1.postValue(null)
                    },
                    onAdImpression = {},
                    onClick = {},
                )
            }
        } else {
            nativeLFO1.postValue(null)
            Log.e(TAG, "onAdLFO1FailedToLoad: invalid")
        }
    }

    fun requestNativeLFO2(
        context: Context,
    ) {
        if (ConfigManager.isShowNativeLanguageDup == true) {
            Log.d(TAG, "requestNativeLFO2 ")
            if (ConfigManager.isShowNativeLanguageDup2f == true) {
                requestNativeAlternate(
                    context,
                    idAdPriorityAd = "ca-app-pub-3940256099942544/1044960115",
                    idAdAllPrice = "ca-app-pub-3940256099942544/2247696110",
                    onAdLoaded = { nativeAd ->
                        nativeLFO2.postValue(nativeAd)
                    },
                    onFailedToLoad = {
                        nativeLFO2.postValue(null)
                    },
                    onAdImpression = {

                    },
                    onClick = {},
                )
            } else {
                requestNativeAd(
                    context,
                    "ca-app-pub-3940256099942544/2247696110",
                    onAdLoaded = { nativeAd ->
                        nativeLFO2.postValue(nativeAd)
                    },
                    onFailedToLoad = {
                        nativeLFO2.postValue(null)
                    },
                    onAdImpression = {},
                    onClick = {},
                )
            }
        } else {
            nativeLFO2.postValue(null)
            Log.e(TAG, "onAdLFO2FailedToLoad: invalid")
        }
    }


    private fun requestNativeAlternate(
        context: Context,
        idAdPriorityAd: String,
        idAdAllPrice: String,
        onAdLoaded: (ContentAd) -> Unit,
        onFailedToLoad: () -> Unit,
        onAdImpression: () -> Unit = {},
        onClick: () -> Unit,
    ) {
        requestNativeAd(
            context,
            idAdPriorityAd,
            onAdLoaded = { nativeAd ->
                Log.d(TAG, "requestNativeAlternate: Priority Loaded  $idAdPriorityAd")
                onAdLoaded(nativeAd)
            },
            onFailedToLoad = {
                Log.d(TAG, "requestNativeAlternate: Priority Failed To Load ")
                requestNativeAd(
                    context,
                    idAdAllPrice,
                    onAdLoaded = { nativeAd ->
                        Log.d(TAG, "requestNativeAlternate: All price loaded $idAdAllPrice")
                        onAdLoaded(nativeAd)
                    },
                    onFailedToLoad = {
                        Log.d(TAG, "requestNativeAlternate: All price Failed To Load ")
                        onFailedToLoad()
                    },
                    onAdImpression = onAdImpression,
                    onClick = onClick,
                )
            },
            onClick = onClick,
        )
    }

    private fun requestNativeAd(
        context: Context,
        idAd: String,
        onAdLoaded: (ContentAd) -> Unit,
        onFailedToLoad: () -> Unit,
        onAdImpression: () -> Unit = {},
        onClick: () -> Unit,
    ) {
        AdSdkManager.INSTANCE.requestNativeAd(
            context,
            idAd,
            object : NativeCallback {
                override fun populateNativeAd() {

                }

                override fun onAdLoaded(data: ContentAd) {
                    onAdLoaded(data)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    onFailedToLoad()
                }

                override fun onAdClicked() {
                    onClick()
                }

                override fun onAdImpression() {
                    onAdImpression()
                }

                override fun onAdFailedToShow(adError: AdError) {

                }
            })
    }
}