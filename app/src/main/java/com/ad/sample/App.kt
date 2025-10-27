package com.ad.sample

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.admob_sdk.api.AdSdkManager
import com.ads.admob_sdk.api.AppResumeAdControl
import com.ads.admob_sdk.api.config.AOAConfig
import com.ads.admob_sdk.api.config.AdSdkConfig
import com.google.android.gms.ads.AdActivity

class App : Application() {
    companion object{
        var appResumeAdHelper: AppResumeAdControl? = null
            private set
    }
    private fun initAppOpenAd(): AppResumeAdControl {
        val listClassInValid = mutableListOf<Class<*>>()
        listClassInValid.add(AdActivity::class.java)
        val config = AOAConfig(
            id ="ca-app-pub-3940256099942544/9257395921",
            isShow = true,
            listClassInValid = listClassInValid,
        )
        return AppResumeAdControl(
            application = this,
            lifecycleOwner = ProcessLifecycleOwner.get(),
            config = config
        )
    }
    override fun onCreate() {
        super.onCreate()
            // init admob
        val vioAdConfig = AdSdkConfig(
            application = this
        )
        AdSdkManager.INSTANCE.initAdmob(this, vioAdConfig)
        appResumeAdHelper = initAppOpenAd()
    }
}