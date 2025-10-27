package com.ads.admob_sdk.api

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.ads.admob_sdk.utils.BannerInlineStyle
import com.ads.admob_sdk.api.config.AdSdkConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.api.config.BannerCollapsibleConfig
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.AdShowCallBack
import com.ads.admob_sdk.callback.BannerCallBack
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.callback.NativeCallback
import com.ads.admob_sdk.callback.RewardCallBack
import com.ads.admob_sdk.core.AdSdkManagerImpl
import com.facebook.shimmer.ShimmerFrameLayout


interface AdSdkManager {
    fun initAdmob(context: Application, adSdkConfig: AdSdkConfig)
    fun cancelRequestAndShowAllAds()

    fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleGravity: String? = null,
        bannerInlineStyle: Int = BannerInlineStyle.Companion.SMALL_STYLE,
        useInlineAdaptive: Boolean = false,
        adCallback: BannerCallBack
    )

    fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleConfig: BannerCollapsibleConfig? = null,
        bannerInlineStyle: Int = BannerInlineStyle.Companion.SMALL_STYLE,
        useInlineAdaptive: Boolean = false,
        adCallback: BannerCallBack
    )

    fun requestNativeAd(context: Context, adId: String, adCallback: NativeCallback)

    fun populateNativeAdView(
        activity: Context,
        nativeAd: ContentAd,
        @LayoutRes nativeAdViewId: Int,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout?,
        adCallback: NativeCallback
    )

    fun requestInterstitialAds(context: Context, adId: String, adCallback: InterstitialCallback)

    fun showInterstitial(
        context: Context,
        interstitialAd: ContentAd?,
        adCallback: InterstitialCallback
    )

    fun requestRewardAd(context: Context, adId: String, adCallback: RewardCallBack)
    fun showRewardAd(
        activity: Activity,
        rewardedAd: ContentAd,
        adCallback: RewardCallBack
    )

    fun requestAppOpenAd(context: Context, adId: String, adCallBack: AdRequestCallBack)
    fun showAppOpenAd(
        activity: Activity,
        appOpenAd: ContentAd?,
        adCallback: AdShowCallBack
    )

    fun getAdmobConfig(): AdSdkConfig?
    companion object {
        val INSTANCE: AdSdkManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AdSdkManagerImpl() }
    }
}