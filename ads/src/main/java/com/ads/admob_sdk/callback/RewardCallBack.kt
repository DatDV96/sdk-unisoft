package com.ads.admob_sdk.callback

import com.ads.admob_sdk.api.model.ContentAd
import com.google.android.gms.ads.rewarded.RewardItem

interface RewardCallBack : AdCallback<ContentAd> {
    fun onAdClose()
    fun onUserEarnedReward(rewardItem: RewardItem?)
    fun onRewardShow()
}