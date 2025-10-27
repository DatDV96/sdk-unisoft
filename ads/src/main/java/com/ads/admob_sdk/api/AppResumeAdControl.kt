package com.ads.admob_sdk.api

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ads.admob_sdk.core.AdmobManager
import com.ads.admob_sdk.utils.LoadingAdsDialog
import com.ads.admob_sdk.callback.AOACallBack
import com.ads.admob_sdk.api.config.AOAConfig
import com.ads.admob_sdk.api.model.ContentAd
import com.ads.admob_sdk.core.AppOpenAdManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class AppResumeAdControl(
    private val application: Application,
    private val lifecycleOwner: LifecycleOwner,
    private val config: AOAConfig
) : LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private var appOpenAdManager: AppOpenAdManager? = null
    private var isActivityInValid = false
    private var isDisableAppResumeOnScreen = false
    private var isDisableAppResumeByClickAction = false
    private var dialogLoading: LoadingAdsDialog? = null

    private val listAdCallback: CopyOnWriteArrayList<AOACallBack> = CopyOnWriteArrayList()
    private val TAG= AppResumeAdControl::class.simpleName

    private var isRequestAppResumeValid = true

    fun setRequestAppResumeValid(isValid : Boolean){
        isRequestAppResumeValid = isValid
    }
    fun setDisableAppResumeByClickAction() {
        isDisableAppResumeByClickAction = true
    }

    fun setDisableAppResumeOnScreen() {
        isDisableAppResumeOnScreen = true
    }

    fun setEnableAppResumeOnScreen() {
        isDisableAppResumeOnScreen = false
    }

    private var isCancelRequestAndShowAllAds = false
    fun cancelRequestAndShowAllAds(isPurchased: Boolean){
        isCancelRequestAndShowAllAds = isPurchased
        appOpenAdManager?.cancelRequestAndShowAllAds(isPurchased)
    }
    private var requestAppOpenResumeValid = false
    fun requestAppOpenResume() {
        requestAppOpenResumeValid = true
    }
    init {
        Log.e(TAG, "rửqe: ", )
        lifecycleOwner.lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
        appOpenAdManager?.setAdUnitId(config.id)
        appOpenAdManager?.setAppResumeConfig(config)
        appOpenAdManager?.registerLister(object : AOACallBack {
            override fun onAppOpenAdShow() {
                invokeAdListener {
                    it.onAppOpenAdShow()
                }
            }

            override fun onAppOpenAdClose() {
                invokeAdListener {
                    it.onAppOpenAdClose()
                }
            }

            override fun onAdLoaded(data: ContentAd) {
                invokeAdListener {
                    it.onAdLoaded(data)
                }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                invokeAdListener {
                    it.onAdFailedToLoad(loadAdError)
                }
            }

            override fun onAdClicked() {
                invokeAdListener {
                    it.onAdClicked()
                }
            }

            override fun onAdImpression() {
                invokeAdListener {
                    it.onAdImpression()
                }
            }

            override fun onAdFailedToShow(adError: AdError) {
                invokeAdListener {
                    it.onAdFailedToShow(adError)
                }
            }

        })
        application.registerActivityLifecycleCallbacks(this)
    }


    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        if (!isRequestAppResumeValid) return
        config.listClassInValid.forEach { classItem ->
            if (classItem.simpleName == activity::class.simpleName) {
                isActivityInValid = true
                return@forEach
            } else {
                isActivityInValid = false
            }
        }
    }


    override fun onActivityStarted(activity: Activity) {
        if (!isRequestAppResumeValid || isCancelRequestAndShowAllAds) return
        Log.e(TAG, "onActivityStarted: ${activity::class.simpleName}", )
        if (isDisableAppResumeByClickAction) {
            isDisableAppResumeByClickAction = false
            return
        }
        config.listClassInValid.forEach { classItem ->
            if (classItem.simpleName == activity::class.simpleName) {
                isActivityInValid = true
                return@forEach
            } else {
                isActivityInValid = false
            }
        }
        if (AdmobManager.isAdsClicked && !isActivityInValid) {
            AdmobManager.adsClickedInValid()
            return
        }
        if (isActivityInValid || isDisableAppResumeOnScreen || AdmobManager.isShowAdsFullScreen || AdmobManager.isAdsClicked) return
        handleShowAppOpenResume(activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    private fun showDialogLoading(activity: Activity) {
        if (dialogLoading == null) {
            dialogLoading = LoadingAdsDialog(activity)
        }
        dialogLoading?.show()
    }

    private fun dismissLoading() {
        try {
            dialogLoading?.dismiss()
            dialogLoading = null
        } catch (ex: Exception) {
            dialogLoading = null
            ex.printStackTrace()
        }
    }

    private fun handleShowAppOpenResume(activity: Activity) {
        if (!isRequestAppResumeValid) return
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.CREATED && appOpenAdManager?.isAdAvailable() == true) {
            lifecycleOwner.lifecycleScope.launch {
                showDialogLoading(activity)
                delay(500)
                appOpenAdManager?.showAdIfAvailable(activity, invokeListenerAdCallback())
            }
        }
    }

    override fun onActivityPaused(p0: Activity) {
        if (requestAppOpenResumeValid){
            appOpenAdManager?.loadAd(application)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (!isRequestAppResumeValid) return
        config.listClassInValid.forEach { classItem ->
            if (classItem.simpleName == activity::class.simpleName) {
                isActivityInValid = true
                return@forEach
            } else {
                isActivityInValid = false
            }
        }
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }


    fun registerAdListener(adCallback: AOACallBack) {
        this.listAdCallback.add(adCallback)
    }

    fun unregisterAdListener(adCallback: AOACallBack) {
        this.listAdCallback.remove(adCallback)
    }

    fun unregisterAllAdListener() {
        this.listAdCallback.clear()
    }

    private fun invokeAdListener(action: (adCallback: AOACallBack) -> Unit) {
        listAdCallback.forEach(action)
    }

    private fun invokeListenerAdCallback(): AOACallBack {
        return object : AOACallBack {
            override fun onAppOpenAdShow() {
                invokeAdListener { it.onAppOpenAdShow() }
            }

            override fun onAppOpenAdClose() {
                dismissLoading()
                invokeAdListener { it.onAppOpenAdClose() }
            }

            override fun onAdLoaded(data: ContentAd) {
                invokeAdListener { it.onAdLoaded(data) }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                dismissLoading()
                invokeAdListener { it.onAdFailedToLoad(loadAdError) }
            }

            override fun onAdClicked() {
                invokeAdListener { it.onAdClicked() }
            }

            override fun onAdImpression() {
                invokeAdListener { it.onAdImpression() }
            }

            override fun onAdFailedToShow(adError: AdError) {
                dismissLoading()
                invokeAdListener { it.onAdFailedToShow(adError) }
            }

        }
    }
    fun onClearData(){
        appOpenAdManager?.clearData()
    }
}