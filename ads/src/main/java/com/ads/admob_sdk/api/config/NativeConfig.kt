package com.ads.admob_sdk.api.config

import androidx.annotation.LayoutRes
import com.ads.admob_sdk.utils.AdOptionVisibility
import com.ads.admob_sdk.utils.IAdsConfig

class NativeConfig(
    override val id: String,
    override val idHigh: String? = null,
    override val isShow: Boolean,
    @LayoutRes val layoutId: Int,
    ) : IAdsConfig {
    var adVisibility: AdOptionVisibility = AdOptionVisibility.GONE
}