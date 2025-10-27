package com.ads.admob_sdk.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.annotation.IntDef
import com.ads.admob_sdk.api.AdSdkManager
import com.ads.admob_sdk.api.config.AdSdkConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.api.config.BannerCollapsibleConfig
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.AdShowCallBack
import com.ads.admob_sdk.callback.BannerCallBack
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.callback.NativeCallback
import com.ads.admob_sdk.callback.RewardCallBack
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.rewarded.RewardItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdSdkManagerImpl : AdSdkManager {
    private lateinit var adSdkConfig: AdSdkConfig
    private val TAG = AdSdkManagerImpl::class.simpleName
    private var isCancelRequestAndShowAllAds = false

    override fun initAdmob(
        context: Application,
        adSdkConfig: AdSdkConfig
    ) {
        this.adSdkConfig = adSdkConfig
        CoroutineScope(Dispatchers.IO).launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = Application.getProcessName()
                val packageName = context.packageName
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            }
            MobileAds.initialize(context) { initializationStatus: InitializationStatus ->
                try {
                    val statusMap = initializationStatus.adapterStatusMap
                    for (adapterClass in statusMap.keys) {
                        val status = statusMap[adapterClass]

                        Log.d(
                            TAG, String.format(
                                "Adapter name: %s, Description: %s, Latency: %d",
                                adapterClass, status?.description, status?.latency
                            )
                        )
                    }
                } catch (_: Exception) {
                }
            }
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(adSdkConfig.listDevices)
                    .build()
            )
        }
    }

    override fun cancelRequestAndShowAllAds() {
        isCancelRequestAndShowAllAds = true
    }

    override fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleGravity: String?,
        bannerInlineStyle: Int,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "BannerAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }
        AdmobBannerFactory.getInstance()
            .requestBannerAd(
                context,
                adId,
                collapsibleGravity,
                bannerInlineStyle,
                useInlineAdaptive,
                object : BannerCallBack {
                    override fun onAdLoaded(data: ContentAd) {
                        adCallback.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        adCallback.onAdFailedToLoad(loadAdError)
                    }

                    override fun onAdClicked() {
                        adCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        adCallback.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        adCallback.onAdFailedToShow(adError)
                    }

                }
            )

    }

    override fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleConfig: BannerCollapsibleConfig?,
        bannerInlineStyle: Int,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "BannerAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }

        AdmobBannerFactory.getInstance()
            .requestBannerAd(
                context,
                adId,
                collapsibleConfig,
                bannerInlineStyle,
                useInlineAdaptive,
                object : BannerCallBack {
                    override fun onAdLoaded(data: ContentAd) {
                        adCallback.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        adCallback.onAdFailedToLoad(loadAdError)
                    }

                    override fun onAdClicked() {
                        adCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        adCallback.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        adCallback.onAdFailedToShow(adError)
                    }

                }
            )
    }

    override fun requestNativeAd(
        context: Context,
        adId: String,
        adCallback: NativeCallback
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "ApNativeAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }
        AdmobNativeFactory.getInstance()
            .requestNativeAd(
                context,
                adId,
                object : NativeCallback {
                    override fun populateNativeAd() {
                        adCallback.populateNativeAd()
                    }

                    override fun onAdLoaded(data: ContentAd) {
                        adCallback.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        adCallback.onAdFailedToLoad(loadAdError)
                    }

                    override fun onAdClicked() {
                        adCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        adCallback.onAdImpression()
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        adCallback.onAdFailedToShow(adError)
                    }

                })
    }

    override fun populateNativeAdView(
        context: Context,
        nativeAd: ContentAd,
        nativeAdViewId: Int,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout?,
        adCallback: NativeCallback
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "ApNativeAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }
        when (nativeAd) {
            is ContentAd.AdmobAd.ApNativeAd -> {
                AdmobNativeFactory.getInstance().populateNativeAdView(
                    context,
                    nativeAd.nativeAd,
                    nativeAdViewId,
                    adPlaceHolder,
                    containerShimmerLoading,
                    adCallback
                )
            }

            else -> {
                adCallback.onAdFailedToShow(AdError(1999, "Ad Not support", ""))
            }
        }

    }

    override fun requestInterstitialAds(
        context: Context,
        adId: String,
        adCallback: InterstitialCallback
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "ApInterstitialAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }
        AdmobInterstitialAdFactory.getInstance()
            .requestInterstitialAd(
                context,
                adId,
                object : InterstitialCallback {

                    override fun onAdClose() {
                        adCallback.onAdClose()
                    }

                    override fun onInterstitialShow() {
                        adCallback.onInterstitialShow()
                    }

                    override fun onAdLoaded(data: ContentAd) {
                        if (data is ContentAd.AdmobAd.ApInterstitialAd) {
                            Log.e("FacebookTrackingManager", "onAdLoaded: adsd")
                            adCallback.onAdLoaded(data)
                        }
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        adCallback.onAdFailedToLoad(loadAdError)
                    }

                    override fun onAdClicked() {
                        adCallback.onAdClicked()
                    }

                    override fun onAdImpression() {
                        adCallback.onAdImpression()

                    }

                    override fun onAdFailedToShow(adError: AdError) {
                        adCallback.onAdFailedToShow(adError)
                    }

                })
    }

    override fun showInterstitial(
        context: Context,
        interstitialAd: ContentAd?,
        adCallback: InterstitialCallback
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToShow(
                AdError(
                    99,
                    "ApInterstitialAd cancel Request And Show All Ads",
                    "",
                    null
                )
            )
            return
        }
        when (interstitialAd) {
            is ContentAd.AdmobAd.ApInterstitialAd -> {
                AdmobInterstitialAdFactory.getInstance()
                    .showInterstitial(
                        context,
                        interstitialAd.interstitialAd,
                        object : InterstitialCallback {

                            override fun onAdClose() {
                                adCallback.onAdClose()
                            }

                            override fun onInterstitialShow() {
                                adCallback.onInterstitialShow()
                            }

                            override fun onAdLoaded(data: ContentAd) {
                                adCallback.onAdLoaded(data)
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                adCallback.onAdFailedToLoad(loadAdError)
                            }

                            override fun onAdClicked() {
                                adCallback.onAdClicked()
                            }

                            override fun onAdImpression() {
                                adCallback.onAdImpression()
                            }

                            override fun onAdFailedToShow(adError: AdError) {
                                adCallback.onAdFailedToShow(adError)
                            }

                        })
            }

            else -> {
                adCallback.onAdFailedToShow(AdError(1999, "Ad Not support", ""))
            }
        }

    }

    override fun requestRewardAd(
        context: Context,
        adId: String,
        adCallback: RewardCallBack
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "ApRewardAd cancel Request And Show All Ads",
                    "",
                    null,
                    null
                )
            )
            return
        }
        AdmobRewardAdFactory.getInstance()
            .requestRewardAd(context, adId, object : RewardCallBack {
                override fun onAdClose() {
                    adCallback.onAdClose()
                }

                override fun onUserEarnedReward(rewardItem: RewardItem?) {
                    adCallback.onUserEarnedReward(rewardItem)
                }

                override fun onRewardShow() {
                    adCallback.onRewardShow()
                }

                override fun onAdLoaded(data: ContentAd) {
                    adCallback.onAdLoaded(data)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailedToLoad(loadAdError)
                }

                override fun onAdClicked() {
                    adCallback.onAdClicked()
                }

                override fun onAdImpression() {
                    adCallback.onAdImpression()
                }

                override fun onAdFailedToShow(adError: AdError) {
                    adCallback.onAdFailedToShow(adError)
                }
            })
    }

    override fun showRewardAd(
        activity: Activity,
        rewardedAd: ContentAd,
        adCallback: RewardCallBack
    ) {
        if (isCancelRequestAndShowAllAds) {
            adCallback.onAdFailedToShow(
                AdError(
                    99,
                    "ApRewardAd cancel Request And Show All Ads",
                    "",
                    null
                )
            )
            return
        }
        when (rewardedAd) {
            is ContentAd.AdmobAd.ApRewardAd -> {
                AdmobRewardAdFactory.getInstance()
                    .showRewardAd(activity, rewardedAd.rewardAd, object : RewardCallBack {
                        override fun onAdClose() {
                            adCallback.onAdClose()
                        }

                        override fun onUserEarnedReward(rewardItem: RewardItem?) {
                            adCallback.onUserEarnedReward(rewardItem)
                        }

                        override fun onRewardShow() {
                            adCallback.onRewardShow()
                        }

                        override fun onAdLoaded(data: ContentAd) {
                            adCallback.onAdLoaded(data)
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            adCallback.onAdFailedToLoad(loadAdError)
                        }

                        override fun onAdClicked() {
                            adCallback.onAdClicked()
                        }

                        override fun onAdImpression() {
                            adCallback.onAdImpression()
                        }

                        override fun onAdFailedToShow(adError: AdError) {
                            adCallback.onAdFailedToShow(adError)
                        }
                    })

            }

            else -> {
                adCallback.onAdFailedToShow(AdError(1999, "Ad Not support", ""))
            }
        }
    }

    override fun requestAppOpenAd(
        context: Context,
        adId: String,
        adCallBack: AdRequestCallBack
    ) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    adCallBack.onAdLoaded(ContentAd.AdmobAd.ApAppOpenAd(ad))
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallBack.onAdFailedToLoad(loadAdError)
                }
            }
        )
    }

    override fun showAppOpenAd(
        activity: Activity,
        appOpenAd: ContentAd?,
        adCallback: AdShowCallBack
    ) {
        Log.e(TAG, "showAppOpenAd: qwew")
        when (appOpenAd) {
            is ContentAd.AdmobAd.ApAppOpenAd -> {
                appOpenAd.appOpenAd.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            adCallback.onAdClose()
                            Log.d(AppOpenAdManager.TAG, "onAdDismissedFullScreenContent.")
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            adCallback.onAdFailedToShow(adError)
                        }

                        override fun onAdShowedFullScreenContent() {
                            adCallback.onAdImpression()
                            Log.d(AppOpenAdManager.TAG, "onAdShowedFullScreenContent.")
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            adCallback.onAdClicked()
                        }

                        override fun onAdImpression() {
                            adCallback.onAdImpression()
                        }
                    }
                appOpenAd.appOpenAd.show(activity)
            }

            else -> {
                adCallback.onAdFailedToShow(AdError(99, "Ad not support", ""))
            }
        }
    }

    override fun getAdmobConfig(): AdSdkConfig? {
        return adSdkConfig
    }

    companion object {
        private val TAG = AdSdkManagerImpl::class.simpleName
    }
}

@IntDef(BannerInlineStyle.SMALL_STYLE, BannerInlineStyle.LARGE_STYLE)
annotation class BannerInlineStyle {
    companion object {
        const val SMALL_STYLE = 0
        const val LARGE_STYLE = 1
    }
}