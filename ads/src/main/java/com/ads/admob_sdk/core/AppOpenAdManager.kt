package com.ads.admob_sdk.core

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ads.admob_sdk.callback.AOACallBack
import com.ads.admob_sdk.api.config.AOAConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager {
    companion object {
        val TAG = AppOpenAdManager::class.simpleName
    }

    private var appOpenAd: ContentAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    private var adUnitId = ""
    private var AOAConfig: AOAConfig? = null
    fun setAppResumeConfig(adConfig: AOAConfig) {
        AOAConfig = adConfig
    }

    private var AOACallBack: AOACallBack? = null
    fun setAdUnitId(id: String) {
        this.adUnitId = id
    }

    fun registerLister(AOACallBack: AOACallBack) {
        this.AOACallBack = AOACallBack
    }

    private var isCancelRequestAndShowAllAds = false
    fun cancelRequestAndShowAllAds(isPurchased: Boolean) {
        isCancelRequestAndShowAllAds = isPurchased
    }

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    /**
     * Load an ad.
     *
     * @param context the context of the activity that loads the ad
     */
    fun loadAd(context: Context) {
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable() || AOAConfig?.isShow == false) {
            Log.e(TAG, "loadAd: invalid")
            return
        }
        Log.d(TAG, "request AOA: ")
        isLoadingAd = true
        requestAoaAdmob(context)
    }
    private fun requestAoaAdmob(context: Context) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    AOACallBack?.onAdLoaded(ContentAd.AdmobAd.ApAppResumeAd(ad))
                    appOpenAd = ContentAd.AdmobAd.ApAppOpenAd(ad)
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.e(TAG, "onAdLoaded: ")
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    AOACallBack?.onAdFailedToLoad(loadAdError)
                    Log.d(TAG, "onAdFailedToLoad: " + loadAdError.message)
                }
            }
        )
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    fun isAdAvailable(): Boolean {
        // Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
        // https://support.google.com/admob/answer/9341964?hl=en
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4) && !isCancelRequestAndShowAllAds
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, adCallback: AOACallBack) {
        Log.e(TAG, "showAdIfAvailable: ")
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback.
        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            loadAd(activity)
            return
        }
        Log.d(TAG, "Will show ad.")
        when (appOpenAd) {
            is ContentAd.AdmobAd.ApAppOpenAd -> {
                (appOpenAd as ContentAd.AdmobAd.ApAppOpenAd).appOpenAd.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        /** Called when full screen content is dismissed. */
                        override fun onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null
                            isShowingAd = false
                            adCallback.onAppOpenAdClose()
                            Log.d(TAG, "onAdDismissedFullScreenContent.")
                            loadAd(activity)
                        }

                        /** Called when fullscreen content failed to show. */
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            appOpenAd = null
                            isShowingAd = false
                            adCallback.onAdFailedToShow(adError)
                            Log.d(TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
                            loadAd(activity)
                        }

                        /** Called when fullscreen content is shown. */
                        override fun onAdShowedFullScreenContent() {
                            adCallback.onAppOpenAdShow()
                            Log.d(TAG, "onAdShowedFullScreenContent.")
                        }

                        override fun onAdClicked() {
                            super.onAdClicked()
                            adCallback.onAdClicked()
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                            adCallback.onAdImpression()
                        }
                    }
                isShowingAd = true
                (appOpenAd as ContentAd.AdmobAd.ApAppOpenAd).appOpenAd.show(activity)
            }
            else -> {
                Log.d(TAG, "Not Show Ads")
            }
        }
    }

    fun clearData() {
        appOpenAd = null
    }
}