package com.ads.admob_sdk.core

import android.content.Context
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.ads.admob_sdk.callback.NativeCallback
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.nativead.NativeAd


interface AdmobNativeFactory {
    fun requestNativeAd(context: Context, adId: String, adCallback: NativeCallback)

    fun populateNativeAdView(
        activity: Context,
        nativeAd: NativeAd,
        @LayoutRes nativeAdViewId: Int,
        adPlaceHolder: FrameLayout,
        containerShimmerLoading: ShimmerFrameLayout?,
        adCallback: NativeCallback
    )

    companion object {
        @JvmStatic
        fun getInstance(): AdmobNativeFactory = AdmobNativeFactoryImpl()
    }
}