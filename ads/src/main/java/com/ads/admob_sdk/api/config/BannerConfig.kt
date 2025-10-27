package com.ads.admob_sdk.api.config

import com.ads.admob_sdk.utils.BannerInlineStyle
import com.ads.admob_sdk.utils.IAdsConfig


data class BannerAdConfig(
    override val id: String,
    override val idHigh: String? = null,
    override val isShow: Boolean,
    val bannerInlineStyle: Int = BannerInlineStyle.SMALL_STYLE,
    val useInlineAdaptive: Boolean = false,
) : IAdsConfig {
    @Deprecated("replace to BannerCollapsibleConfig: adRequestRate")
    var collapsibleGravity: String? = null
    var bannerCollapsibleConfig: BannerCollapsibleConfig? = null
}

data class BannerCollapsibleConfig(
    val collapsibleGravity: String? = null,
    val adRequestRate: Int? = 100
)