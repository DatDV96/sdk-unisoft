package com.ad.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ads.admob_sdk.api.AdSplashControl
import com.ads.admob_sdk.api.BannerAdControl
import com.ads.admob_sdk.api.UmpManager
import com.ads.admob_sdk.api.config.AdSplashConfig
import com.ads.admob_sdk.api.config.BannerAdConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.callback.BannerCallBack
import com.ads.admob_sdk.callback.InterstitialCallback
import com.ads.admob_sdk.callback.UmpCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.ad.sample.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.atomic.AtomicBoolean

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val TAG = "AdSplashControl"
    private lateinit var binding: ActivitySplashBinding

    private var isFirstRequestAds = AtomicBoolean(false)
    private val umpManager by lazy { UmpManager.getInstance(this) }

    private val callInterSuccess = MutableStateFlow(false)
    private val callBannerSuccess = MutableStateFlow(false)
    private val bannerAdHelper by lazy {
        initBannerAdSplash()
    }

    private fun initBannerAdSplash(): BannerAdControl {
        val adConfig = BannerAdConfig(
            id = "ca-app-pub-3940256099942544/6300978111",
            isShow = ConfigManager.isShowBannerSplash,
        )
        return BannerAdControl(this, this, adConfig).apply {
            registerAdListener(object : BannerCallBack {
                override fun onAdClicked() {

                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    callBannerSuccess.value = true
                }

                override fun onAdFailedToShow(adError: AdError) {
                    callBannerSuccess.value = true
                }


                override fun onAdImpression() {
                }

                override fun onAdLoaded(data: ContentAd) {
                    callBannerSuccess.value = true
                }

            })
        }

    }

    private val interstitialAdSplashHelper by lazy {
        initInterstitialAdSplash()
    }

    private fun initInterstitialAdSplash(): AdSplashControl {
        val adConfig = AdSplashConfig(
            id = "ca-app-pub-3940256099942544/1033173712",
            idHigh = "ca-app-pub-3940256099942544/1033173712",
            idAdOpen = "ca-app-pub-3940256099942544/9257395921",
            idAdOpenHigh = "ca-app-pub-3940256099942544/9257395921",
            isShow = ConfigManager.isShowInterSplash, //  điều kiện show ad
            timeOut = 30_0000,
            isShowInter = false
        )
        return AdSplashControl(this, this, adConfig).apply {
            registerAdListener(object : InterstitialCallback {
                override fun onAdClicked() {

                }

                override fun onAdClose() {
                    Log.e(TAG, "onAdClose: ", )
                    handleNextAction()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    callInterSuccess.value = true
                }

                override fun onAdFailedToShow(adError: AdError) {
                    callInterSuccess.value = true
                }

                override fun onAdImpression() {
                }

                override fun onAdLoaded(data: ContentAd) {
                    callInterSuccess.value = true
                }

                override fun onInterstitialShow() {
                }

            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        App.appResumeAdHelper?.setDisableAppResumeOnScreen()
        requestUMP()
    }

    private fun requestUMP() {
        Log.e(TAG, "requestUMP: ", )
        umpManager.initReleaseConsent(object : UmpCallback{
            override fun onSuccess(errorMessage: String?) {
                requestAds()
            }

        })
    }



    private fun requestAds() {
        Log.e(TAG, "requestAds: ", )
        App.appResumeAdHelper?.setRequestAppResumeValid(
            // chỗ  này đạt điều kiện show khi lấy remote xong
            true
        )
        // điều kiện show inter splash nếu true thì gọi request false thì gọi cho next action
        if (ConfigManager.isShowInterSplash) {
            interstitialAdSplashHelper.requestAds()
        } else {
            callInterSuccess.value = true
        }
        // điều kiện show banner splash nếu true thì gọi request false thì gọi cho next action
        if (ConfigManager.isShowBannerSplash) {
            bannerAdHelper.setContainer(binding.frAds)
            bannerAdHelper.requestAds()
        } else {
            callBannerSuccess.value = true
        }
        callBannerSuccess.combine(callInterSuccess) { requestBannerComplete, requestInterComplete ->
            requestInterComplete && requestBannerComplete
        }.filter { it }.onEach {
            Log.e(TAG, "requestAds: e", )
            preloadAds()
            delay(1000)
            interstitialAdSplashHelper.interstitialAdValue?.let {
                interstitialAdSplashHelper.showAd(it)
            } ?: run {
                handleNextAction()
            }
        }.launchIn(lifecycleScope)
    }

    private fun handleNextAction() {
        App.appResumeAdHelper?.setEnableAppResumeOnScreen()
        App.appResumeAdHelper?.requestAppOpenResume()
        val intentAction = Intent(this@SplashActivity, LanguageActivity::class.java)
        startActivity(intentAction)
        finish()
    }

    private fun preloadAds() {
        NativeControl.requestNativeLFO1(this)
    }

    override fun onDestroy() {
        App.appResumeAdHelper?.setEnableAppResumeOnScreen()
        super.onDestroy()
    }

}