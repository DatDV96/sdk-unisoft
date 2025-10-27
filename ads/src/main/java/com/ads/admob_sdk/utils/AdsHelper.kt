package com.ads.admob_sdk.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ads.admob_sdk.api.UmpManager
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

abstract class AdsHelper<C : IAdsConfig>(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val config: C
) {

    internal var isCancelRequestAndShowAllAds = false
    fun cancelRequestAndShowAllAds(isPurchased: Boolean) {
        isCancelRequestAndShowAllAds = isPurchased
    }

    private var tag: String = context::class.java.simpleName
    internal val flagActive: AtomicBoolean = AtomicBoolean(false)
    internal val lifecycleEventState = MutableStateFlow(Lifecycle.Event.ON_ANY)
    var flagUserEnableReload = true
        set(value) {
            field = value
            logZ("setFlagUserEnableReload($field)")
        }

    init {
        CoroutineScope(Dispatchers.Main).launch {
            lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    lifecycleEventState.update { event }
                    when (event) {
                        Lifecycle.Event.ON_DESTROY -> {
                            lifecycleOwner.lifecycle.removeObserver(this)
                        }

                        else -> Unit
                    }
                }
            })
        }
    }

    open fun isShowAd(): Boolean {
        return config.isShow && !isCancelRequestAndShowAllAds
    }

    open fun canRequestAds(): Boolean {
        return isShowAd()
                && isOnline()
                && UmpManager.getInstance(context as Activity).getConsentResult(context)
                && !isCancelRequestAndShowAllAds
    }

    abstract fun cancel()

    fun setTagForDebug(tag: String) {
        this.tag = tag
    }

    fun isActiveState(): Boolean {
        return flagActive.get()
    }

    fun canReloadAd(): Boolean {
        return flagUserEnableReload
    }

    internal fun logZ(message: String) {
        Log.d(this::class.java.simpleName, "${tag}: $message")
    }

    internal fun logInterruptExecute(message: String) {
        logZ("$message not execute because has called cancel()")
    }

    internal fun isOnline(): Boolean {
        val netInfo = runCatching {
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        }.getOrNull()
        return netInfo != null && netInfo.isConnected
    }
}

interface IAdsConfig {
    val id: String
    val idHigh: String?
    val isShow: Boolean
}

/**
 * Enum representing the visibility options for advertising elements.
 */
enum class AdOptionVisibility {
    /**
     * The advertising element is not visible and does not occupy any space in the layout.
     */
    GONE,

    /**
     * The advertising element is invisible but still occupies space in the layout.
     */
    INVISIBLE
}

const val MAX_SMALL_INLINE_BANNER_HEIGHT = 50
fun getAdRequest(): AdRequest {
    val builder = AdRequest.Builder()
    return builder.build()
}

fun getCollapsibleAdRequest(type: String): AdRequest {
    try {
        val builder = AdRequest.Builder()
        builder.addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
            putString("collapsible", type)
        })
        return builder.build()
    } catch (ex: Exception) {
        return getAdRequest()
    }
}

fun getAdSize(
    mActivity: Activity,
    useInlineAdaptive: Boolean,
    inlineStyle: Int
): AdSize {

    // Step 2 - Determine the screen width (less decorations) to use for the ad width.
    val display = mActivity.windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val widthPixels = outMetrics.widthPixels.toFloat()
    val density = outMetrics.density
    val adWidth = (widthPixels / density).toInt()

    // Step 3 - Get adaptive ad size and return for setting on the ad view.
    return if (useInlineAdaptive) {
        if (inlineStyle == BannerInlineStyle.LARGE_STYLE) {
            Log.e("TAG", "getAdSize: 2121")
            AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(
                mActivity,
                adWidth
            )
        } else {
            Log.e("TAG", "getAdSize: ")
            AdSize.getInlineAdaptiveBannerAdSize(
                adWidth,
                MAX_SMALL_INLINE_BANNER_HEIGHT
            )
        }
    } else AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        mActivity,
        adWidth
    )
}

@IntDef(BannerInlineStyle.SMALL_STYLE, BannerInlineStyle.LARGE_STYLE)
annotation class BannerInlineStyle {
    companion object {
        const val SMALL_STYLE = 0
        const val LARGE_STYLE = 1
    }
}

@StringDef(BannerCollapsibleGravity.BOTTOM, BannerCollapsibleGravity.TOP)
annotation class BannerCollapsibleGravity {
    companion object {
        const val BOTTOM = "bottom"
        const val TOP = "top"
    }
}
