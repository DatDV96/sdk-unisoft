package com.ads.admob_sdk.api.config

import com.ads.admob_sdk.utils.IAdsConfig

class AdSplashConfig(
    override val id: String,
    override val idHigh: String? = null,
    val idAdOpen: String? = null,
    val idAdOpenHigh: String? = null,
    override val isShow: Boolean,
    val timeOut: Long,
    val isShowInter: Boolean = true,
) : IAdsConfig