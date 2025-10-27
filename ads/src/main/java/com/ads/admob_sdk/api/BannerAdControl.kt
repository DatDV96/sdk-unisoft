package com.ads.admob_sdk.api

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.admob.R
import com.ads.admob_sdk.utils.AdsHelper
import com.ads.admob_sdk.api.model.AdState
import com.ads.admob_sdk.callback.BannerCallBack
import com.ads.admob_sdk.api.config.BannerAdConfig
import com.ads.admob_sdk.api.config.BannerCollapsibleConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.core.BannerInlineStyle
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

class BannerAdControl(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val config: BannerAdConfig,
) : AdsHelper<BannerAdConfig>(activity, lifecycleOwner, config) {
    private val TAG = BannerAdControl::class.simpleName
    private val adBannerState: MutableStateFlow<AdState> =
        MutableStateFlow(if (canRequestAds()) AdState.None else AdState.Fail)
    private var timeShowAdImpression: Long = 0
    private val listAdCallback: CopyOnWriteArrayList<BannerCallBack> = CopyOnWriteArrayList()
    private val resumeCount: AtomicInteger = AtomicInteger(0)
    private var shimmer: ShimmerFrameLayout? = null
    private var container: FrameLayout? = null
    private var isRequestValid = true

    var bannerAdView: ContentAd? = null
        private set

    init {
        registerAdListener(getDefaultCallback())
        lifecycleEventState.onEach {
            if (it == Lifecycle.Event.ON_CREATE) {
                if (!canRequestAds()) {
                    container?.isVisible = false
                    shimmer?.isVisible = false
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
                if (!isActiveState()) {
                    logInterruptExecute("Request when resume")
                }
            }
            if (event == Lifecycle.Event.ON_RESUME && resumeCount.get() > 1 && bannerAdView != null && canRequestAds() && canReloadAd() && isActiveState()) {
                if (!isRequestValid) {
                    isRequestValid = true
                    return@onEach
                }
                logZ("requestAds on resume")
                requestAds()
            }
        }.launchIn(lifecycleOwner.lifecycleScope)
        //for action resume or init
        adBannerState.onEach { adsParam ->
            logZ("dsadsadr24")
            handleShowAds(adsParam)
        }.launchIn(lifecycleOwner.lifecycleScope)
    }

    fun getBannerState(): Flow<AdState> {
        return adBannerState.asStateFlow()
    }

    private fun handleShowAds(adsParam: AdState) {
        container?.isGone = adsParam is AdState.Cancel || !isShowAd()
        shimmer?.isVisible = adsParam is AdState.Loading
        when (adsParam) {
            is AdState.Loaded -> {
                val bannerContentView = container
                val shimmerLayoutView = shimmer
                if (bannerContentView != null && shimmerLayoutView != null) {
                    bannerContentView.setBackgroundColor(Color.WHITE)
                    val view = View(bannerContentView.context)
                    val oldHeight = bannerContentView.height
                    bannerContentView.let {
                        it.removeAllViews()
                        when (adsParam.adContent) {
                            is ContentAd.AdmobAd.ApBannerAd -> {
                                if (!config.useInlineAdaptive && config.bannerInlineStyle == BannerInlineStyle.Companion.SMALL_STYLE) {
                                    it.addView(view, 0, oldHeight)
                                }
                                it.addView(adsParam.adContent.adView)
                            }

                            else -> {
                                invokeAdListener {
                                    it.onAdFailedToShow(AdError(1999, "Ad not support", ""))
                                }
                            }
                        }
                    }
                }
            }

            else -> Unit
        }
    }

    fun requestAds(bannerAd: ContentAd.AdmobAd.ApBannerAd? = null) {
        if (canRequestAds()) {
            lifecycleOwner.lifecycleScope.launch {
                if (bannerAd == null){
                    flagActive.compareAndSet(false, true)
                    if (bannerAdView == null) {
                        adBannerState.emit(AdState.Loading)
                    }
                    loadBannerAd()
                } else {
                    flagActive.compareAndSet(false, true)
                    adBannerState.emit(AdState.Loaded(bannerAd))
                }
            }
        } else {
            if (!isOnline() && bannerAdView == null) {
                cancel()
            }
        }
    }

    override fun cancel() {
        logZ("cancel() called")
        flagActive.compareAndSet(true, false)
        bannerAdView = null
        container?.isVisible = false
        lifecycleOwner.lifecycleScope.launch { adBannerState.emit(AdState.Cancel) }
    }

    private fun loadBannerAd() {
        if (canRequestAds()) {
            config.idHigh?.let { idAdsPriority ->
                config.bannerCollapsibleConfig?.let { bannerCollapsibleConfig ->
                    requestAdsAlternate(
                        activity,
                        idAdsPriority,
                        config.id,
                        bannerCollapsibleConfig,
                        invokeListenerAdCallback()
                    )
                } ?: run {
                    requestAdsAlternate(
                        activity,
                        idAdsPriority,
                        config.id,
                        invokeListenerAdCallback()
                    )
                }
            } ?: run {
                config.bannerCollapsibleConfig?.let { bannerCollapsibleConfig ->
                    AdSdkManager.INSTANCE
                        .requestBannerAd(
                            activity,
                            config.id,
                            bannerCollapsibleConfig,
                            config.bannerInlineStyle,
                            config.useInlineAdaptive,
                            invokeListenerAdCallback()
                        )
                } ?: run {
                    AdSdkManager.INSTANCE
                        .requestBannerAd(
                            activity,
                            config.id,
                            config.collapsibleGravity,
                            config.bannerInlineStyle,
                            config.useInlineAdaptive,
                            invokeListenerAdCallback()
                        )
                }
            }
        }
    }

    private fun requestAdsAlternate(
        activity: Context,
        idAdPriority: String,
        idAdNormal: String,
        bannerCallBack: BannerCallBack
    ) {
        Log.e(TAG, "requestAdsAlternate: ")
        AdSdkManager.INSTANCE
            .requestBannerAd(
                activity,
                idAdPriority,
                config.collapsibleGravity,
                config.bannerInlineStyle,
                config.useInlineAdaptive,
                object : BannerCallBack {
                    override fun onAdLoaded(data: ContentAd) {
                        Log.e(TAG, " requestAdsAlternate onAdLoaded: Priority ")
                        bannerCallBack.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, " requestAdsAlternate onAdFailedToLoad: Priority ")
                        AdSdkManager.INSTANCE
                            .requestBannerAd(
                                activity,
                                idAdNormal,
                                config.collapsibleGravity,
                                config.bannerInlineStyle,
                                config.useInlineAdaptive,
                                object : BannerCallBack {
                                    override fun onAdLoaded(data: ContentAd) {
                                        Log.e(TAG, " requestAdsAlternate onAdLoaded: normal ")
                                        bannerCallBack.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        Log.e(
                                            TAG,
                                            " requestAdsAlternate onAdFailedToLoad: normal ",
                                        )
                                        bannerCallBack.onAdFailedToLoad(loadAdError)
                                    }

                                    override fun onAdClicked() {
                                        bannerCallBack.onAdClicked()
                                    }

                                    override fun onAdImpression() {
                                        bannerCallBack.onAdImpression()
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                        bannerCallBack.onAdFailedToShow(adError)
                                    }

                                }
                            )
                    }

                    override fun onAdClicked() {
                        bannerCallBack.onAdClicked()
                    }

                    override fun onAdImpression() {
                        bannerCallBack.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        bannerCallBack.onAdFailedToShow(adError)
                    }

                }
            )
    }

    private fun requestAdsAlternate(
        activity: Context,
        idAdPriority: String,
        idAdNormal: String,
        bannerCollapsibleConfig: BannerCollapsibleConfig,
        bannerCallBack: BannerCallBack
    ) {
        Log.e(TAG, "requestAdsAlternate: ")
        AdSdkManager.INSTANCE
            .requestBannerAd(
                activity,
                idAdPriority,
                bannerCollapsibleConfig,
                config.bannerInlineStyle,
                config.useInlineAdaptive,
                object : BannerCallBack {
                    override fun onAdLoaded(data: ContentAd) {
                        Log.e(TAG, " requestAdsAlternate onAdLoaded: Priority ")
                        bannerCallBack.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, " requestAdsAlternate onAdFailedToLoad: Priority ")
                        AdSdkManager.INSTANCE
                            .requestBannerAd(
                                activity,
                                idAdNormal,
                                bannerCollapsibleConfig,
                                config.bannerInlineStyle,
                                config.useInlineAdaptive,
                                object : BannerCallBack {
                                    override fun onAdLoaded(data: ContentAd) {
                                        Log.e(TAG, " requestAdsAlternate onAdLoaded: normal ")
                                        bannerCallBack.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        Log.e(
                                            TAG,
                                            " requestAdsAlternate onAdFailedToLoad: normal ",
                                        )
                                        bannerCallBack.onAdFailedToLoad(loadAdError)
                                    }

                                    override fun onAdClicked() {
                                        bannerCallBack.onAdClicked()
                                    }

                                    override fun onAdImpression() {
                                        bannerCallBack.onAdImpression()
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                        bannerCallBack.onAdFailedToShow(adError)
                                    }

                                }
                            )
                    }

                    override fun onAdClicked() {
                        bannerCallBack.onAdClicked()
                    }

                    override fun onAdImpression() {
                        bannerCallBack.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        bannerCallBack.onAdFailedToShow(adError)
                    }

                }
            )
    }

    fun setShimmerLayoutView(shimmerLayoutView: ShimmerFrameLayout) = apply {
        runCatching {
            this.shimmer = shimmerLayoutView
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    shimmerLayoutView.isVisible = false
                }
            }
        }
    }

    fun setContainer(container: FrameLayout) = apply {
        runCatching {
            this.container = container
            this.shimmer =
                container.findViewById(R.id.shimmer_container_banner)
            if (lifecycleOwner.lifecycle.currentState in Lifecycle.State.CREATED..Lifecycle.State.RESUMED) {
                if (!canRequestAds()) {
                    container.isVisible = false
                    shimmer?.isVisible = false
                }
            }
        }
    }

    private fun getDefaultCallback(): BannerCallBack {
        return object : BannerCallBack {
            override fun onAdLoaded(data: ContentAd) {
                if (isActiveState()) {
                    lifecycleOwner.lifecycleScope.launch {
                        bannerAdView = data
                        adBannerState.emit(AdState.Loaded(data))
                    }
                    logZ("onBannerLoaded()")
                } else {
                    logInterruptExecute("onBannerLoaded")
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (isActiveState()) {
                    lifecycleOwner.lifecycleScope.launch {
                        adBannerState.emit(AdState.Fail)
                    }
                    logZ("onAdFailedToLoad()")
                } else {
                    logInterruptExecute("onAdFailedToLoad")
                }
            }

            override fun onAdClicked() {

            }

            override fun onAdImpression() {
            }

            override fun onAdFailedToShow(adError: AdError) {
            }

        }
    }

    fun registerAdListener(adCallback: BannerCallBack) {
        this.listAdCallback.add(adCallback)
    }

    fun unregisterAdListener(adCallback: BannerCallBack) {
        this.listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        this.listAdCallback.clear()
    }

    private fun invokeListenerAdCallback(): BannerCallBack {
        return object : BannerCallBack {
            override fun onAdLoaded(data: ContentAd) {
                invokeAdListener { it.onAdLoaded(data) }
                logZ("onAdLoaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                invokeAdListener { it.onAdFailedToLoad(loadAdError) }
                logZ("onAdFailedToLoad: ${loadAdError.message}")
            }

            override fun onAdClicked() {
                invokeAdListener { it.onAdClicked() }
                logZ("onAdClicked")
            }

            override fun onAdImpression() {
                invokeAdListener { it.onAdImpression() }
                logZ("onAdImpression")
            }

            override fun onAdFailedToShow(adError: AdError) {
                invokeAdListener { it.onAdFailedToShow(adError) }
                logZ("onAdFailedToShow: ${adError.message}")
            }
        }
    }

    private fun invokeAdListener(action: (adCallback: BannerCallBack) -> Unit) {
        listAdCallback.forEach(action)
    }
}