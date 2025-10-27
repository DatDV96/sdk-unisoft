package com.ads.admob_sdk.core

import android.app.Activity
import android.content.Context
import com.ads.admob_sdk.callback.AdRequestCallBack
import com.ads.admob_sdk.callback.RewardCallBack
import com.google.android.gms.ads.rewarded.RewardedAd

interface AdmobRewardAdFactory {
    fun requestRewardAd(context: Context, adId: String, adCallback: RewardCallBack)
    fun showRewardAd(
        activity: Activity,
        rewardedAd: RewardedAd,
        adCallback: RewardCallBack
    )

    companion object {
        @JvmStatic
        fun getInstance(): AdmobRewardAdFactoryImpl = AdmobRewardAdFactoryImpl()
    }
}