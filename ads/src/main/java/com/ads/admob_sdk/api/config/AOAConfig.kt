package com.ads.admob_sdk.api.config

import com.ads.admob_sdk.utils.IAdsConfig


class AOAConfig(
    override val id: String,
    override val idHigh: String? = "",
    override val isShow: Boolean,
    val listClassInValid: MutableList<Class<*>> = arrayListOf(),
) : IAdsConfig