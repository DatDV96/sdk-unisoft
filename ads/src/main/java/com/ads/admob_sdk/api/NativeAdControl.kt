package com.ads.admob_sdk.api

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.admob_sdk.api.config.NativeConfig
import com.ads.admob_sdk.api.model.AdState
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.NativeCallback
import com.ads.admob_sdk.utils.AdOptionVisibility
import com.ads.admob_sdk.utils.AdsHelper

import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ViO on 16/03/2024.
 */
class NativeAdControl(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val config: NativeConfig
) : AdsHelper<NativeConfig>(activity, lifecycleOwner, config) {
    private val TAG = NativeAdControl::class.simpleName
    private val adNativeState: MutableStateFlow<AdState> =
        MutableStateFlow(if (canRequestAds()) AdState.None else AdState.Fail)
    private val resumeCount: AtomicInteger = AtomicInteger(0)
    private val listAdCallback: CopyOnWriteArrayList<NativeCallback> = CopyOnWriteArrayList()
    private var flagEnableReload = true
    private var shimmerLayoutView: ShimmerFrameLayout? = null
    private var nativeContentView: FrameLayout? = null
    private var isRequestValid = true
    var adVisibility: AdOptionVisibility = AdOptionVisibility.GONE
    private var timesRequestAd = 0
    private fun increaseTimesRequestAd() {
        timesRequestAd++
    }

    private fun isFirstRequestAds(): Boolean {
        return timesRequestAd == 1
    }

    private var timeCallShowAd = 0L

    var nativeAd: ContentAd? = null
        private set

    init {
        registerAdListener(getDefaultCallback())
        lifecycleEventState.onEach {
            if (it == Lifecycle.Event.ON_CREATE) {
                if (!canRequestAds()) {
                    nativeContentView?.checkAdVisibility(false)
                    shimmerLayoutView?.checkAdVisibility(false)
                }
            }
            if (it == Lifecycle.Event.ON_RESUME) {
                if (!isShowAd() && isActiveState()) {
                    cancel()
                }
            }
        }.launchIn(lifecycleOwner.lifecycleScope)
        //Request when resume
        lifecycleEventState.debounce(300).onEach { event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                resumeCount.incrementAndGet()
                logZ("Resume repeat ${resumeCount.get()} times")
            }
            if (event == Lifecycle.Event.ON_RESUME && resumeCount.get() > 1 && nativeAd != null && canRequestAds() && canReloadAd() && isActiveState()) {
                if (!isRequestValid) {
                    isRequestValid = true
                    return@onEach
                }
                requestAds()
            }
        }.launchIn(lifecycleOwner.lifecycleScope)
        //for action resume or init
        adNativeState
            .onEach { logZ("adNativeState(${it::class.java.simpleName})") }
            .launchIn(lifecycleOwner.lifecycleScope)
        adNativeState.onEach { adsParam ->
            handleShowAds(adsParam)
        }.launchIn(lifecycleOwner.lifecycleScope)
    }

    fun setShimmerLayoutView(shimmerLayoutView: ShimmerFrameLayout?) = apply {
        runCatching {
            this.shimmerLayoutView = shimmerLayoutView
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    shimmerLayoutView?.checkAdVisibility(false)
                }
            }
        }
    }

    fun setNativeContainer(container: FrameLayout?) = apply {
        runCatching {
            this.nativeContentView = container
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    container?.checkAdVisibility(false)
                }
            }
        }
    }

    @Deprecated("replace with flagEnableReload")
    fun setEnableReload(isEnable: Boolean) {
        flagEnableReload = isEnable
    }

    private fun handleShowAds(adsParam: AdState) {
        nativeContentView?.checkAdVisibility(adsParam !is AdState.Cancel && isShowAd())
        shimmerLayoutView?.checkAdVisibility(adsParam is AdState.Loading)
        when (adsParam) {
            is AdState.Loaded -> {
                if (nativeContentView != null && shimmerLayoutView != null) {
                    AdSdkManager.INSTANCE.populateNativeAdView(
                        activity,
                        adsParam.adContent,
                        config.layoutId,
                        nativeContentView!!,
                        shimmerLayoutView,
                        invokeListenerAdCallback()
                    )
                }
            }

            else -> Unit
        }
    }

    @Deprecated("Using cancel()")
    fun resetState() {
        logZ("resetState()")
        cancel()
    }

    fun getAdState(): Flow<AdState> {
        return adNativeState.asStateFlow()
    }

    private fun createNativeAds(activity: Activity) {
        if (canRequestAds()) {
            if (config.idHigh != null) {
                requestAdsAlternate(
                    activity,
                    config.idHigh,
                    config.id,
                    invokeListenerAdCallback()
                )
            } else {
                AdSdkManager.INSTANCE
                    .requestNativeAd(
                        context = activity,
                        config.id,
                        invokeListenerAdCallback()
                    )
            }
        }
    }

    private fun requestAdsAlternate(
        activity: Context,
        idAdPriority: String,
        idAdNormal: String,
        nativeAdCallback: NativeCallback
    ) {
        AdSdkManager.INSTANCE
            .requestNativeAd(
                activity,
                idAdPriority,
                object : NativeCallback {
                    override fun populateNativeAd() {
                        nativeAdCallback.populateNativeAd()
                    }

                    override fun onAdLoaded(data: ContentAd) {
                        Log.d(TAG, "requestAdsAlternate onAdLoaded: Priority")
                        nativeAdCallback.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.d(
                            TAG,
                            "requestAdsAlternate: onAdFailedToLoad Priority ${loadAdError.message}",
                        )
                        AdSdkManager.INSTANCE
                            .requestNativeAd(
                                activity,
                                idAdNormal,
                                object : NativeCallback {
                                    override fun populateNativeAd() {
                                        nativeAdCallback.populateNativeAd()
                                    }

                                    override fun onAdLoaded(data: ContentAd) {
                                        Log.d(TAG, "requestAdsAlternate onAdLoaded: Normal")
                                        nativeAdCallback.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        Log.d(
                                            TAG,
                                            "requestAdsAlternate: onAdFailedToLoad Normal ${loadAdError.message}",
                                        )
                                        nativeAdCallback.onAdFailedToLoad(loadAdError)
                                    }

                                    override fun onAdClicked() {
                                        nativeAdCallback.onAdClicked()
                                    }

                                    override fun onAdImpression() {
                                        nativeAdCallback.onAdImpression()
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                        nativeAdCallback.onAdFailedToShow(adError)
                                    }

                                })
                    }

                    override fun onAdClicked() {
                        nativeAdCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        nativeAdCallback.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        nativeAdCallback.onAdFailedToShow(adError)
                    }

                })
    }


    private fun getDefaultCallback(): NativeCallback {
        return object : NativeCallback {
            override fun populateNativeAd() {
            }

            override fun onAdLoaded(data: ContentAd) {
                if (isActiveState()) {
                    this@NativeAdControl.nativeAd = data
                    lifecycleOwner.lifecycleScope.launch {
                        adNativeState.emit(AdState.Loaded(data))
                    }
                    logZ("onNativeAdLoaded")
                } else {
                    logInterruptExecute("onNativeAdLoaded")
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (isActiveState()) {
                    if (nativeAd == null) {
                        lifecycleOwner.lifecycleScope.launch {
                            adNativeState.emit(AdState.Fail)
                        }
                    }
                    logZ("onAdFailedToLoad")
                } else {
                    logInterruptExecute("onAdFailedToLoad")
                }
            }

            override fun onAdClicked() {
                logZ("Native onAdClick")
            }

            override fun onAdImpression() {
                isRequestValid = lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED
                logZ("Native onAdImpression")
            }

            override fun onAdFailedToShow(adError: AdError) {
                logZ("Native onAdFailedToShow")
            }
        }
    }

    fun showAd(nativeAd: ContentAd){
        if (canRequestAds()){
            lifecycleOwner.lifecycleScope.launch {
                flagActive.compareAndSet(false, true)
                this@NativeAdControl.nativeAd = nativeAd
                adNativeState.emit(AdState.Loaded(nativeAd))
            }
        } else {
            if (!isOnline() && this.nativeAd == null) {
                cancel()
            }
        }
    }

    fun requestAds() {
        lifecycleOwner.lifecycleScope.launch {
            if (canRequestAds()) {
                if (adNativeState.value == AdState.Loading) return@launch
                flagActive.compareAndSet(false, true)
                if (nativeAd == null) {
                    adNativeState.emit(AdState.Loading)
                }
                createNativeAds(activity)
            } else {
                if (!isOnline() && nativeAd == null) {
                    cancel()
                }
            }
        }
    }

    override fun cancel() {
        logZ("cancel() called")
        flagActive.compareAndSet(true, false)
        nativeContentView?.isVisible = false
        lifecycleOwner.lifecycleScope.launch {
            adNativeState.emit(AdState.Cancel)
        }
    }

    fun registerAdListener(adCallback: NativeCallback) {
        this.listAdCallback.add(adCallback)
    }

    fun unregisterAdListener(adCallback: NativeCallback) {
        this.listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        this.listAdCallback.clear()
    }

    private fun invokeAdListener(action: (adCallback: NativeCallback) -> Unit) {
        listAdCallback.forEach(action)
    }

    private fun invokeListenerAdCallback(): NativeCallback {
        return object : NativeCallback {
            override fun populateNativeAd() {
                invokeAdListener { it.populateNativeAd() }
            }

            override fun onAdLoaded(data: ContentAd) {
                invokeAdListener {
                    it.onAdLoaded(data)
                }
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
                invokeAdListener { it.onAdFailedToShow(adError) }
            }

        }
    }

    /**
     * Adjusts the visibility of the [View] based on the provided visibility state and
     * the configured ad visibility option.
     *
     * @param isVisible A boolean indicating whether the view should be set to visible.
     * @see AdOptionVisibility
     */
    private fun View.checkAdVisibility(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE
        else when (adVisibility) {
            AdOptionVisibility.GONE -> View.GONE
            AdOptionVisibility.INVISIBLE -> View.INVISIBLE
        }
    }

}