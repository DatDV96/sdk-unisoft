package com.ads.admob_sdk.api

import android.app.Activity
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ads.admob_sdk.api.config.AdSplashConfig
import com.ads.admob_sdk.api.model.AdState
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.AdShowCallBack
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.core.AdmobManager
import com.ads.admob_sdk.utils.AdsHelper
import com.ads.admob_sdk.utils.LoadingAdsDialog
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class AdSplashControl(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val config: AdSplashConfig
) : AdsHelper<AdSplashConfig>(activity, lifecycleOwner, config) {
    private val dialogLoading by lazy {
        val dialog = LoadingAdsDialog(activity)
        dialog.setOwnerActivity(activity)
        dialog
    }

    private val listAdCallback: CopyOnWriteArrayList<InterstitialCallback> =
        CopyOnWriteArrayList()
    private val adInterstitialState: MutableStateFlow<AdState> =
        MutableStateFlow(if (canRequestAds()) AdState.None else AdState.Fail)
    var interstitialAdValue: ContentAd? = null
        private set

    private var requestTimeOutJob: Job? = null
    private var requestDelayJob: Job? = null
    private var showValid = false
    private var loadingJob: Job? = null

    fun showAd(interstitialAd: ContentAd? = null) {
        lifecycleOwner.lifecycleScope.launch {
            if (interstitialAd == null) {
                invokeAdListener { it.onAdClose() }
            } else {
                flagActive.compareAndSet(false, true)
                interstitialAdValue = interstitialAd
                adInterstitialState.emit(AdState.Loaded(interstitialAd))
                showInterAds(activity)
            }
        }
    }

    fun requestAds() {
        lifecycleOwner.lifecycleScope.launch {
            if (canRequestAds()) {
                flagActive.compareAndSet(false, true)
                if (interstitialAdValue == null) {
                    adInterstitialState.emit(AdState.Loading)
                }
                createAdSplash()
            } else {
                invokeAdListener {
                    it.onAdFailedToLoad(
                        LoadAdError(
                            0,
                            "request condition = false",
                            "",
                            null,
                            null
                        )
                    )
                }
            }
        }
    }

    private fun createAdSplash() {
        if (config.isShowInter) {
            createInterAds()
        } else {
            createOpenAd()
        }
    }

    private fun createOpenAd() {
        Log.d(TAG, "requestOpenAd: ")
        requestTimeOutJob = lifecycleOwner.lifecycleScope.launch {
            config.idAdOpenHigh?.let {
                requestOpenAdAlternate(it, config.idAdOpen ?: "")
            } ?: run {
                config.idAdOpen?.let {
                    requestOpenAd(it)
                } ?: run {
                    createInterAds()
                }
            }
            delay(config.timeOut)
            if (interstitialAdValue != null) {
                showInterAds(activity)
            } else {
                Log.e(TAG, "createOpenAd: ", )
                invokeAdListener { it.onAdClose() }
                requestTimeOutJob?.cancel()
            }
        }
    }

    private fun requestOpenAdAlternate(idAdPriority: String, idAdNormal: String) {
        Log.d(TAG, "requestOpenAd: priority ")
        AdSdkManager.INSTANCE.requestAppOpenAd(
            activity,
            idAdPriority,
            object : AdRequestCallBack {
                override fun onAdLoaded(data: ContentAd) {
                    Log.e(TAG, "onAdLoaded: OpenAd priority")
                    interstitialAdValue = data
                    lifecycleOwner.lifecycleScope.launch {
                        adInterstitialState.emit(AdState.Loaded(data))
                    }
                    invokeAdListener { it.onAdLoaded(data) }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "onAdFailedToLoad:OpenAd priority ${loadAdError.message} ")
                    Log.d(TAG, "requestOpenAd: normal ")
                    requestOpenAd(idAdNormal)
                }

            })
    }


    private fun requestOpenAd(idAdNormal: String) {
        Log.d(TAG, "requestOpenAd: ")
        AdSdkManager.INSTANCE.requestAppOpenAd(
            activity,
            idAdNormal,
            object : AdRequestCallBack {
                override fun onAdLoaded(data: ContentAd) {
                    Log.e(TAG, "onAdLoaded: OpenAd")
                    interstitialAdValue = data
                    lifecycleOwner.lifecycleScope.launch {
                        adInterstitialState.emit(AdState.Loaded(data))
                    }
                    invokeAdListener { it.onAdLoaded(data) }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "onAdFailedToLoad: ${loadAdError.message}")
                    invokeAdListener { it.onAdFailedToLoad(loadAdError) }
                }

            })
    }

    private fun showInterAds(activity: Activity) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (adInterstitialState.value is AdState.Loaded || adInterstitialState.value is AdState.ShowFail) {
                    requestDelayJob?.cancel()
                    AdmobManager.adsShowFullScreen()
                    showDialogLoading()
                    delay(500)
                    when (interstitialAdValue) {
                        is ContentAd.AdmobAd.ApInterstitialAd -> {
                            AdSdkManager.INSTANCE
                                .showInterstitial(
                                    activity,
                                    interstitialAdValue,
                                    invokeListenerAdCallback()
                                )
                        }

                        is ContentAd.AdmobAd.ApAppOpenAd -> {
                            AdSdkManager.INSTANCE.showAppOpenAd(
                                activity,
                                interstitialAdValue,
                                object : AdShowCallBack {

                                    override fun onAdClose() {
                                        dismissDialog()
                                        cancelLoadingJob()
                                        invokeAdListener { it.onAdClose() }
                                        AdmobManager.adsFullScreenDismiss()
                                    }


                                    override fun onAdClicked() {
                                        invokeAdListener { it.onAdClicked() }
                                    }

                                    override fun onAdImpression() {
                                        invokeAdListener { it.onAdImpression() }
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                        Log.e(TAG, " ApAppOpenAd onAdFailedToShow: ${adError.message}", )
                                        AdmobManager.adsFullScreenDismiss()
                                        dismissDialog()
                                        cancelLoadingJob()
                                        lifecycleOwner.lifecycleScope.launch {
                                            adInterstitialState.emit(AdState.ShowFail)
                                        }
                                        invokeAdListener { it.onAdFailedToShow(adError) }
                                        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                            invokeAdListener { it.onAdClose() }
                                        }
                                    }

                                })
                        }

                        else -> {
                            if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                invokeAdListener { it.onAdClose() }
                            }
                        }
                    }

                    loadingJob = lifecycleOwner.lifecycleScope.launch {
                        delay(2000)
                        dismissDialog()
                    }
                }
            }
        }
    }

    private fun showDialogLoading() {
        try {
            cancelLoadingJob()
            dialogLoading.show()
        } catch (_: Exception) {
        }
    }

    private fun createInterAds() {
        requestTimeOutJob = lifecycleOwner.lifecycleScope.launch {
            if (config.idHigh != null) {
                requestAdsAlternate(
                    activity,
                    config.idHigh,
                    config.id,
                    invokeListenerAdCallback()
                )
            } else {
                Log.d(TAG, "requestAdsAlternate: ad normal")
                AdSdkManager.INSTANCE
                    .requestInterstitialAds(
                        activity,
                        config.id,
                        invokeListenerAdCallback()
                    )
            }
            delay(config.timeOut)
            if (interstitialAdValue != null) {
                showInterAds(activity)
            } else {
                invokeAdListener { it.onAdClose() }
                requestTimeOutJob?.cancel()
            }
        }
    }

    private fun requestAdsAlternate(
        activity: Activity,
        idAdPriority: String,
        idAdNormal: String,
        interstitialCallback: InterstitialCallback
    ) {
        Log.d(TAG, "requestAdsAlternate: ")
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
                        Log.d(
                            TAG,
                            "requestAdsAlternate: onAdFailedToLoad Priority ${loadAdError.message}",
                        )
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
                                        Log.d(
                                            TAG,
                                            "requestAdsAlternate: onAdFailedToLoad Normal ${loadAdError.message}",
                                        )
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

    override fun cancel() {
    }

    fun registerAdListener(adCallback: InterstitialCallback) {
        this.listAdCallback.add(adCallback)
    }

    fun unregisterAdListener(adCallback: InterstitialCallback) {
        this.listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        this.listAdCallback.clear()
    }

    private fun invokeAdListener(action: (adCallback: InterstitialCallback) -> Unit) {
        listAdCallback.forEach(action)
    }

    private fun invokeListenerAdCallback(): InterstitialCallback {
        return object : InterstitialCallback {

            override fun onAdClose() {
                dismissDialog()
                cancelLoadingJob()
                invokeAdListener { it.onAdClose() }
                AdmobManager.adsFullScreenDismiss()
            }

            override fun onInterstitialShow() {
                AdmobManager.adsShowFullScreen()
                lifecycleOwner.lifecycleScope.launch {
                    adInterstitialState.emit(AdState.Showed)
                }
                requestTimeOutJob?.cancel()
            }

            override fun onAdLoaded(data: ContentAd) {
                interstitialAdValue = data
                lifecycleOwner.lifecycleScope.launch {
                    adInterstitialState.emit(AdState.Loaded(data))
                }
                invokeAdListener { it.onAdLoaded(data) }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                invokeAdListener { it.onAdFailedToLoad(loadAdError) }
            }

            override fun onAdClicked() {
                invokeAdListener { it.onAdClicked() }
            }

            override fun onAdImpression() {
                invokeAdListener { it.onAdImpression() }
            }

            override fun onAdFailedToShow(adError: AdError) {
                Log.e(TAG, " Inter onAdFailedToShow: ${adError.message}", )

                AdmobManager.adsFullScreenDismiss()
                dismissDialog()
                cancelLoadingJob()
                lifecycleOwner.lifecycleScope.launch {
                    adInterstitialState.emit(AdState.ShowFail)
                }
                invokeAdListener { it.onAdFailedToShow(adError) }
            }

        }
    }

    private fun dismissDialog() {
        try {
            val activity = dialogLoading.ownerActivity
            if (activity != null && !activity.isDestroyed && activity.windowManager != null) {
                dialogLoading.dismiss()
            }
        } catch (_: Exception) {
        }
    }

    private fun cancelLoadingJob() {
        loadingJob?.cancel()
        loadingJob = null
    }

    companion object {
        private val TAG = AdSplashControl::class.simpleName
    }
}