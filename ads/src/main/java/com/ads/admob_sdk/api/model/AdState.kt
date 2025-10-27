package com.ads.admob_sdk.api.model

sealed class AdState {
    object None : AdState()
    object Fail : AdState()
    object Loading : AdState()
    object Cancel : AdState()
    object Showed: AdState()
    object ShowFail: AdState()
    data class Loaded(val adContent: ContentAd) : AdState()
}