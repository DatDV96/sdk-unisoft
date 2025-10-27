package com.ads.admob_sdk.core

import android.content.Context
import com.ads.admob_sdk.utils.BannerInlineStyle
import com.ads.admob_sdk.api.config.BannerCollapsibleConfig
import com.ads.admob_sdk.callback.BannerCallBack

interface AdmobBannerFactory {

    fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleGravity: String? = null,
        bannerInlineStyle: Int = BannerInlineStyle.Companion.SMALL_STYLE,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    )

    fun requestBannerAd(
        context: Context,
        adId: String,
        bannerCollapsibleConfig: BannerCollapsibleConfig? = null,
        bannerInlineStyle: Int = BannerInlineStyle.Companion.SMALL_STYLE,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    )

    companion object {
        @JvmStatic
        fun getInstance(): AdmobBannerFactory = AdmobBannerFactoryImpl()
    }
}