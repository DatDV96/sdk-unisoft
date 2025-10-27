package com.ads.admob_sdk.core

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import com.ads.admob_sdk.utils.getAdRequest
import com.ads.admob_sdk.utils.getAdSize
import com.ads.admob_sdk.utils.getCollapsibleAdRequest
import com.ads.admob_sdk.api.config.BannerCollapsibleConfig
import com.ads.admob_sdk.callback.BannerCallBack
import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdapterResponseInfo
import com.google.android.gms.ads.LoadAdError


class AdmobBannerFactoryImpl : AdmobBannerFactory {
    private val TAG = AdmobBannerFactory::class.simpleName
    override fun requestBannerAd(
        context: Context,
        adId: String,
        collapsibleGravity: String?,
        bannerInlineStyle: Int,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    ) {
        try {
            val adView = AdView(context)
            adView.adUnitId = adId
            val adSize = getAdSize(context as Activity, useInlineAdaptive, bannerInlineStyle)
            adView.setAdSize(adSize)
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    adCallback.onAdLoaded(ContentAd.AdmobAd.ApBannerAd(adView))

                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdmobManager.adsClicked()
                    adCallback.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }
            }
            adView.loadAd(
                if (collapsibleGravity == null) {
                    getAdRequest()
                } else {
                    try {
                        getCollapsibleAdRequest(collapsibleGravity)
                    } catch (ex: Exception){
                        getAdRequest()
                    }
                }
            )
        } catch (ex: Exception) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    1991,
                    ex.message.toString(),
                    "",
                    null,
                    null
                )
            )
        }
    }

    override fun requestBannerAd(
        context: Context,
        adId: String,
        bannerCollapsibleConfig: BannerCollapsibleConfig?,
        bannerInlineStyle: Int,
        useInlineAdaptive: Boolean,
        adCallback: BannerCallBack
    ) {
        try {
            val adView = AdView(context)
            adView.adUnitId = adId
            val adSize = getAdSize(context as Activity, useInlineAdaptive, bannerInlineStyle)
            adView.setAdSize(adSize)
            adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            adView.adListener = object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    adCallback.onAdFailedToLoad(loadAdError)
                }

                override fun onAdLoaded() {
                    adCallback.onAdLoaded(ContentAd.AdmobAd.ApBannerAd(adView))

                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdmobManager.adsClicked()
                    adCallback.onAdClicked()
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    adCallback.onAdImpression()
                }
            }

            adView.loadAd(
                if (bannerCollapsibleConfig == null) {
                    getAdRequest()
                } else {
                    try {
                        bannerCollapsibleConfig.adRequestRate?.let { adRequestRate ->
                            val randomRate = (0..100).random()
                            Log.d(
                                TAG,
                                "requestBannerAd: $randomRate  with adRequestRate : $adRequestRate",
                            )
                            if (randomRate <= adRequestRate) {
                                bannerCollapsibleConfig.collapsibleGravity?.let { collapsibleGravity ->
                                    Log.d(TAG, "requestBannerAd: collapsible")
                                    getCollapsibleAdRequest(collapsibleGravity)
                                } ?: run {
                                    getAdRequest()
                                }
                            } else {
                                getAdRequest()
                            }
                        } ?: run {
                            bannerCollapsibleConfig.collapsibleGravity?.let { collapsibleGravity ->
                                Log.d(TAG, "requestBannerAd: collapsible")
                                getCollapsibleAdRequest(collapsibleGravity)
                            } ?: run {
                                getAdRequest()
                            }
                        }
                    } catch (ex: Exception) {
                        getAdRequest()
                    }
                }
            )
        } catch (ex: Exception) {
            adCallback.onAdFailedToLoad(
                LoadAdError(
                    1991,
                    ex.message.toString(),
                    "",
                    null,
                    null
                )
            )
        }
    }
}