package com.ads.admob_sdk.core

import android.app.Activity
import android.content.Context
import com.ads.admob_sdk.utils.getAdRequest
import com.ads.admob_sdk.callback.RewardCallBack
import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdmobRewardAdFactoryImpl : AdmobRewardAdFactory {
    override fun requestRewardAd(
        context: Context,
        adId: String,
        adCallback: RewardCallBack
    ) {
        RewardedAd.load(context, adId, getAdRequest(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                adCallback.onAdLoaded(ContentAd.AdmobAd.ApRewardAd(rewardedAd))
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                adCallback.onAdFailedToLoad(loadAdError)
            }
        })
    }

    override fun showRewardAd(
        activity: Activity,
        rewardedAd: RewardedAd,
        adCallback: RewardCallBack
    ) {

        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                adCallback.onAdClose()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                adCallback.onAdFailedToShow(adError)
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                adCallback.onRewardShow()
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
        rewardedAd.show(activity) { rewardItem ->
            adCallback.onUserEarnedReward(rewardItem)
        }
    }
}