package com.ads.admob_sdk.api

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.admob_sdk.core.AdmobManager
import com.ads.admob_sdk.utils.LoadingAdsDialog
import com.ads.admob_sdk.api.model.AdState
import com.ads.admob_sdk.callback.RewardCallBack
import com.ads.admob_sdk.api.config.RewardConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.AdShowCallBack
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RewardAdControl private constructor(private val adPlacement: String) {
    private var isCancelRequestAndShowAllAds = false

    private var rewardConfig: RewardConfig =
        RewardConfig(id = "", isShow = true)

    fun setRewardAdConfig(config: RewardConfig) {
        rewardConfig = config
    }

    private var adRewardState = if (canRequestAds()) AdState.None else AdState.Fail
    var rewardAdValue: ContentAd? = null
        private set

    private var loadingAdsDialog: LoadingAdsDialog? = null
    private var requestShowCount = 0
    private var loadingJob: Job? = null
    private fun cancelLoadingJob() {
        loadingJob?.cancel()
        loadingJob = null
    }
    fun cancelRequestAndShowAllAds(isPurchased: Boolean) {
        adRewardState.emit(AdState.None)
        rewardAdValue = null
        isCancelRequestAndShowAllAds = isPurchased
    }


    private fun canRequestAds(): Boolean {
        return rewardConfig.isShow && !isCancelRequestAndShowAllAds
    }

    fun AdState.emit(state: AdState) = apply {
        adRewardState = state
    }

    private fun requestAdsAlternate(
        activity: Context,
        idAdPriority: String,
        idAdNormal: String,
        rewardAdRequestCallBack: AdRequestCallBack
    ) {
        Log.d(TAG, "requestAdsAlternate: Priority ")
        AdSdkManager.INSTANCE
            .requestRewardAd(
                activity,
                idAdPriority,
                object : RewardCallBack {
                    override fun onAdClose() {
                    }

                    override fun onUserEarnedReward(rewardItem: RewardItem?) {
                    }

                    override fun onRewardShow() {
                        adRewardState.emit(AdState.Showed)
                    }

                    override fun onAdLoaded(data: ContentAd) {
                        rewardAdValue = data
                        adRewardState.emit(AdState.Loaded(data))
                        rewardAdRequestCallBack.onAdLoaded(data)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "onAdFailedToLoad: Priority  ${loadAdError.message}", )
                        Log.e(TAG, "requestAdsAlternate: Normal", )
                        AdSdkManager.INSTANCE
                            .requestRewardAd(
                                activity,
                                idAdNormal,
                                object : RewardCallBack {
                                    override fun onAdClose() {
                                    }

                                    override fun onUserEarnedReward(rewardItem: RewardItem?) {
                                    }

                                    override fun onRewardShow() {
                                        adRewardState.emit(AdState.Showed)
                                    }

                                    override fun onAdLoaded(data: ContentAd) {
                                        rewardAdValue = data
                                        adRewardState.emit(AdState.Loaded(data))
                                        rewardAdRequestCallBack.onAdLoaded(data)
                                    }

                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                        adRewardState.emit(AdState.Fail)
                                        rewardAdRequestCallBack.onAdFailedToLoad(loadAdError)
                                    }

                                    override fun onAdClicked() {
                                    }

                                    override fun onAdImpression() {
                                        adRewardState.emit(AdState.Showed)
                                    }

                                    override fun onAdFailedToShow(adError: AdError) {
                                    }

                                }
                            )
                    }

                    override fun onAdClicked() {
                    }

                    override fun onAdImpression() {
                        adRewardState.emit(AdState.Showed)
                    }

                    override fun onAdFailedToShow(adError: AdError) {
                    }

                }
            )
    }
    fun requestRewardAds(
        context: Context,
        rewardAdRequestCallBack: AdRequestCallBack
    ) {
        if (canRequestAds()) {
            if (requestValid()) {
                Log.e(TAG, "requestRewardAds: $adPlacement")
                adRewardState.emit(AdState.Loading)
                if (rewardConfig.idHigh != null) {
                    requestAdsAlternate(
                        context,
                        rewardConfig.idHigh!!,
                        rewardConfig.id,
                        rewardAdRequestCallBack
                    )
                } else {
                    Log.d(TAG, "requestRewardAds: Normal", )
                    AdSdkManager.INSTANCE
                        .requestRewardAd(
                            context,
                            rewardConfig.id,
                            object : RewardCallBack {
                                override fun onAdClose() {
                                }

                                override fun onUserEarnedReward(rewardItem: RewardItem?) {
                                }

                                override fun onRewardShow() {
                                    adRewardState.emit(AdState.Showed)
                                }

                                override fun onAdLoaded(data: ContentAd) {
                                    rewardAdValue = data
                                    adRewardState.emit(AdState.Loaded(data))
                                    rewardAdRequestCallBack.onAdLoaded(data)
                                }

                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                    adRewardState.emit(AdState.Fail)
                                    rewardAdRequestCallBack.onAdFailedToLoad(loadAdError)
                                }

                                override fun onAdClicked() {
                                }

                                override fun onAdImpression() {
                                    adRewardState.emit(AdState.Showed)
                                }

                                override fun onAdFailedToShow(adError: AdError) {
                                }

                            }
                        )
                }
            } else {
                rewardAdValue?.let {
                    if (adRewardState is AdState.Loaded || adRewardState is AdState.ShowFail) {
                        adRewardState.emit(AdState.Loaded(it))
                        rewardAdRequestCallBack.onAdLoaded(it)
                    } else {
                        adRewardState.emit(AdState.Fail)
                        rewardAdRequestCallBack.onAdFailedToLoad(
                            LoadAdError(
                                99,
                                "request Invalid",
                                "",
                                null,
                                null
                            )
                        )
                    }
                } ?: run {
                    adRewardState.emit(AdState.Fail)
                    rewardAdRequestCallBack.onAdFailedToLoad(
                        LoadAdError(
                            99,
                            "request Invalid",
                            "",
                            null,
                            null
                        )
                    )
                }
            }
        } else {
            Log.e(TAG, "requestInterAds $adPlacement: canRequestAds = false")
            adRewardState.emit(AdState.Fail)
            rewardAdRequestCallBack.onAdFailedToLoad(
                LoadAdError(
                    99,
                    "can request = false",
                    "",
                    null,
                    null
                )
            )
        }
    }

    fun forceShowRewardAd(
        context: Activity,
        lifecycleOwner: LifecycleOwner,
        rewardAdShowCallBack: AdShowCallBack
    ) {
        if (isCancelRequestAndShowAllAds ) {
            Log.e(TAG, "$adPlacement forceShowRewardAd:Cancel Request And Show All Ads ")
            rewardAdShowCallBack.onAdClose()
            return
        }
        if (rewardAdValue != null && adRewardState is AdState.Loaded) {
            try {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    AdmobManager.adsShowFullScreen()
                    showDialogLoading(context)
                    delay(500)
                    AdSdkManager.INSTANCE
                        .showRewardAd(
                            context,
                            rewardAdValue!!,
                            object : RewardCallBack {

                                override fun onAdClose() {
                                    AdmobManager.adsFullScreenDismiss()
                                    rewardAdShowCallBack.onAdClose()
                                    cancelLoadingJob()
                                    dismissDialog()
                                    adRewardState.emit(AdState.Showed)
                                }

                                override fun onUserEarnedReward(rewardItem: RewardItem?) {
                                }

                                override fun onRewardShow() {
                                    cancelLoadingJob()
                                    dismissDialog()
                                    rewardAdShowCallBack.onAdImpression()
                                    adRewardState.emit(AdState.Showed)
                                }


                                override fun onAdLoaded(data: ContentAd) {
                                }

                                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                }

                                override fun onAdClicked() {

                                }

                                override fun onAdImpression() {
                                    cancelLoadingJob()
                                    dismissDialog()
                                    adRewardState.emit(AdState.Showed)
                                }

                                override fun onAdFailedToShow(adError: AdError) {
                                    Log.e(TAG, "onAdFailedToShow: ${adError.message}", )
                                    cancelLoadingJob()
                                    dismissDialog()
                                    AdmobManager.adsFullScreenDismiss()
                                    rewardAdShowCallBack.onAdFailedToShow(adError)
                                    adRewardState.emit(AdState.ShowFail)

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
                rewardAdShowCallBack.onAdFailedToShow(
                    AdError(
                        1999,
                        "reward show Exception : ${ex.message}",
                        ""
                    )
                )
            }
        } else if (adRewardState != AdState.Loading) {
            AdmobManager.adsFullScreenDismiss()
            rewardAdShowCallBack.onAdClose()
        } else {
            rewardAdShowCallBack.onAdFailedToShow(AdError(1999, "ads requesting", ""))
        }
    }


    private fun dismissDialog() {
        try {
            Log.e(TAG, "dismissDialog: wqe", )
            val activity = loadingAdsDialog?.ownerActivity
            if (activity != null && !activity.isDestroyed && activity.windowManager != null) {
                Log.e(TAG, "dismissDialog: ffkkk", )
                loadingAdsDialog?.dismiss()
                loadingAdsDialog = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "dismissDialog: ${e.message}", )
        }
    }
    private fun showDialogLoading(context: Activity) {
        if (loadingAdsDialog == null) {
            loadingAdsDialog = LoadingAdsDialog(context)
        }
        if (loadingAdsDialog?.isShowing == true) {
            loadingAdsDialog?.dismiss()
        }
        loadingAdsDialog?.setOwnerActivity(context)
        loadingAdsDialog?.show()
    }

    private fun requestValid(): Boolean {
        val valueValid =
            ((adRewardState != AdState.Loading && adRewardState !is AdState.Loaded)
                    )
                    || adRewardState == AdState.Showed
        return canRequestAds() && valueValid
    }

    fun clearRequest(){
        rewardAdValue = null
        adRewardState.emit(AdState.None)
    }
    companion object {
        private val TAG = RewardAdControl::class.simpleName

        private val instances = mutableMapOf<String, RewardAdControl>()

        @Synchronized
        fun getInstance(adPlacement: String): RewardAdControl {
            return instances.getOrPut(adPlacement) { RewardAdControl(adPlacement) }
        }
    }
}