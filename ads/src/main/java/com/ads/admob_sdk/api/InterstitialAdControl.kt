package com.ads.admob_sdk.api

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.admob_sdk.core.AdmobManager
import com.ads.admob_sdk.utils.LoadingAdsDialog
import com.ads.admob_sdk.api.model.AdState
import com.ads.admob_sdk.api.config.InterstitialConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.AdShowCallBack
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InterstitialAdControl private constructor(private val adPlacement: String) {
    private var interstitialConfig: InterstitialConfig =
        InterstitialConfig(
            id = "",
            isShow = true,
            )

    fun setInterstitialAdConfig(config: InterstitialConfig) {
        interstitialConfig = config
    }

    private var adInterstitialState: AdState =
        if (canRequestAds()) AdState.None else AdState.Fail

    fun AdState.emit(state: AdState) = apply {
        adInterstitialState = state
    }
    private var loadingJob: Job? = null
    var interstitialAdValue: ContentAd? = null
        private set
    private var requestShowCount = 0
    private var loadingAdsDialog: LoadingAdsDialog? = null
    private var isCancelRequestAndShowAllAds = false

    fun cancelRequestAndShowAllAds(isPurchased: Boolean){
        adInterstitialState.emit(AdState.None)
        interstitialAdValue = null
        isCancelRequestAndShowAllAds = isPurchased
    }

    private fun requestAdsAlternate(
        activity: Context,
        idAdPriority: String,
        idAdNormal: String,
        interstitialCallback: InterstitialCallback
    ) {
        Log.d(TAG, "requestAdsAlternate: ", )
        Log.d(TAG, "requestAdsAlternate: ad priority")
        AdSdkManager.INSTANCE
            .requestInterstitialAds(
                activity,
                idAdPriority,
                object : InterstitialCallback {

                    override fun onAdClose() {
                        interstitialCallback.onAdClose()
                    }

                    override fun onInterstitialShow() {
                        interstitialCallback.onInterstitialShow()
                    }

                    override fun onAdLoaded(data: ContentAd) {
                        Log.d(TAG, "requestAdsAlternate onAdLoaded: Priority")
                        interstitialCallback.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(TAG, "requestAdsAlternate: onAdFailedToLoad Priority ${loadAdError.message}", )
                        Log.d(TAG, "requestAdsAlternate: ad normal")
                        AdSdkManager.INSTANCE
                            .requestInterstitialAds(
                                activity,
                                idAdNormal,
                                object : InterstitialCallback {

                                    override fun onAdClose() {
                                        interstitialCallback.onAdClose()
                                    }

                                    override fun onInterstitialShow() {
                                        interstitialCallback.onInterstitialShow()
                                    }

                                    override fun onAdLoaded(data: ContentAd) {
                                        Log.d(TAG, "requestAdsAlternate onAdLoaded: Normal")
                                        interstitialCallback.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        Log.d(TAG, "requestAdsAlternate: onAdFailedToLoad Normal ${loadAdError.message}", )
                                        interstitialCallback.onAdFailedToLoad(loadAdError)
                                    }

                                    override fun onAdClicked() {
                                        interstitialCallback.onAdClicked()
                                    }

                                    override fun onAdImpression() {
                                        interstitialCallback.onAdImpression()
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                        interstitialCallback.onAdFailedToShow(adError)
                                    }

                                })
                    }

                    override fun onAdClicked() {
                        interstitialCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        interstitialCallback.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        interstitialCallback.onAdFailedToShow(adError)
                    }

                })
    }

    fun requestInterAds(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        adRequestCallBack: AdRequestCallBack
    ) {
        if (canRequestAds()) {
            if (requestValid()) {
                lifecycleOwner.lifecycleScope.launch {
                    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && requestValid()) {
                        Log.e(TAG, "requestInterAds: $adPlacement Loading")
                        adInterstitialState.emit(AdState.Loading)
                        if (interstitialConfig.idHigh != null) {
                            requestAdsAlternate(context,
                                interstitialConfig.idHigh!!,
                                interstitialConfig.id,
                                object : InterstitialCallback {
                                    override fun onAdClose() {
                                    }

                                    override fun onInterstitialShow() {
                                        adInterstitialState.emit(AdState.Showed)
                                    }

                                    override fun onAdLoaded(data: ContentAd) {
                                        interstitialAdValue = data
                                        adInterstitialState.emit(AdState.Loaded(data))
                                        adRequestCallBack.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        adInterstitialState.emit(AdState.Fail)
                                        adRequestCallBack.onAdFailedToLoad(
                                            loadAdError
                                        )
                                    }

                                    override fun onAdClicked() {
                                    }

                                    override fun onAdImpression() {
                                        adInterstitialState.emit(AdState.Showed)
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                    }

                                })
                        } else {
                            Log.d(TAG, "requestAdsAlternate: ad normal")
                            AdSdkManager.INSTANCE
                                .requestInterstitialAds(
                                    context,
                                    interstitialConfig.id,
                                    object : InterstitialCallback {
                                        override fun onAdClose() {
                                        }

                                        override fun onInterstitialShow() {
                                            adInterstitialState.emit(AdState.Showed)
                                        }

                                        override fun onAdLoaded(data: ContentAd) {
                                            interstitialAdValue = data
                                            adInterstitialState.emit(AdState.Loaded(data))
                                            adRequestCallBack.onAdLoaded(data)
                                        }

                                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                            adInterstitialState.emit(AdState.Fail)
                                            adRequestCallBack.onAdFailedToLoad(
                                                loadAdError
                                            )
                                        }

                                        override fun onAdClicked() {
                                        }

                                        override fun onAdImpression() {
                                            adInterstitialState.emit(AdState.Showed)
                                        }

                                        override fun onAdFailedToShow(adError: AdError) {
                                        }

                                    }
                                )
                        }
                    }
                }

            } else {
                Log.e(TAG, "requestInterAds $adPlacement: Invalid")
            }
        } else {
            Log.e(TAG, "requestInterAds $adPlacement: canRequestAds = false")
            adInterstitialState.emit(AdState.Fail)
            adRequestCallBack.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "Request Invalid",
                    "",
                    null,
                    null
                )
            )
        }
    }

    fun forceShowInterstitial(
        context: Activity,
        lifecycleOwner: LifecycleOwner,
        adShowCallBack: AdShowCallBack
    ) {
        if (isCancelRequestAndShowAllAds) {
            Log.e(TAG, "forceShowInterstitial: InValid ")
            adShowCallBack.onAdClose()
            adShowCallBack.onAdFailedToShow(
                AdError(
                    1999,
                    "Show ads InValid isCancelRequestAndShowAllAds",
                    ""
                )
            )
            return
        }

        if (interstitialAdValue != null && adInterstitialState is AdState.Loaded) {
            try {
                lifecycleOwner.lifecycleScope.launch {
                    Log.e(TAG, "forceShowInterstitial: dwqdwq")
                    AdmobManager.adsShowFullScreen()
                    showDialogLoading(context)
                    delay(500)
                    AdSdkManager.INSTANCE
                        .showInterstitial(
                            context,
                            interstitialAdValue,
                            object : InterstitialCallback {

                                override fun onAdClose() {
                                    AdmobManager.adsFullScreenDismiss()
                                    adShowCallBack.onAdClose()
                                    cancelLoadingJob()
                                    dismissDialog()
                                }

                                override fun onInterstitialShow() {
                                    adShowCallBack.onAdImpression()
                                    adInterstitialState.emit(AdState.Showed)
                                }

                                override fun onAdLoaded(data: ContentAd) {
                                }

                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                }

                                override fun onAdClicked() {

                                }

                                override fun onAdImpression() {
                                    adInterstitialState.emit(AdState.Showed)
                                }

                                override fun onAdFailedToShow(adError: AdError) {
                                    cancelLoadingJob()
                                    dismissDialog()
                                    AdmobManager.adsFullScreenDismiss()
                                    adShowCallBack.onAdFailedToShow(adError)
                                    adShowCallBack.onAdClose()
                                    adInterstitialState.emit(AdState.ShowFail)

                                }

                            })
                    loadingJob = lifecycleOwner.lifecycleScope.launch {
                        delay(1000)
                        dismissDialog()
                    }
                }
            } catch (ex: Exception) {
                cancelLoadingJob()
                dismissDialog()
                AdmobManager.adsFullScreenDismiss()
                Log.e(TAG, "forceShowInterstitial: Exception ${ex.message}")
                adShowCallBack.onAdClose()
                adShowCallBack.onAdFailedToShow(AdError(1999, "${ex.message}", ""))
            }

        } else if (adInterstitialState != AdState.Loading
        ) {
            AdmobManager.adsFullScreenDismiss()
            Log.e(TAG, "forceShowInterstitial: InValidewqe")
            adShowCallBack.onAdClose()
            adShowCallBack.onAdFailedToShow(AdError(1999, "Show ads InValid", ""))
        } else {
            AdmobManager.adsFullScreenDismiss()
            Log.e(TAG, "forceShowInterstitial: InValidlkojorjwq")
            adShowCallBack.onAdClose()
            adShowCallBack.onAdFailedToShow(AdError(1999, "Show ads InValid", ""))
        }
    }

    private fun cancelLoadingJob() {
        loadingJob?.cancel()
        loadingJob = null
    }
    private fun dismissDialog() {
        try {
            val activity = loadingAdsDialog?.ownerActivity
            if (activity != null && !activity.isDestroyed && activity.windowManager != null) {
                loadingAdsDialog?.dismiss()
                loadingAdsDialog = null
            }
        } catch (_: Exception) {
        }
    }

    private fun showDialogLoading(context: Activity) {
        try {
            if (loadingAdsDialog == null) {
                loadingAdsDialog = LoadingAdsDialog(context)
            }
            if (loadingAdsDialog?.isShowing == true) {
                loadingAdsDialog?.dismiss()
            }
            loadingAdsDialog?.setOwnerActivity(context)
            loadingAdsDialog?.show()
        } catch (_: Exception) {
        }

    }

    private fun requestValid(): Boolean {
        val valueValid =
            (interstitialAdValue == null
                    && (adInterstitialState != AdState.Loading && adInterstitialState !is AdState.Loaded)
                    )
                    || adInterstitialState == AdState.Showed
        return canRequestAds()  && valueValid
    }

    private fun canRequestAds(): Boolean {
        return interstitialConfig.isShow && !isCancelRequestAndShowAllAds
    }

    companion object {
        private val TAG = InterstitialAdControl::class.simpleName

        // Map to hold singleton instances with their associated IDs
        private val instances = mutableMapOf<String, InterstitialAdControl>()

        // Method to get or create singleton instances
        @Synchronized
        fun getInstance(adPlacement: String): InterstitialAdControl {
            return instances.getOrPut(adPlacement) { InterstitialAdControl(adPlacement) }
        }
    }
}