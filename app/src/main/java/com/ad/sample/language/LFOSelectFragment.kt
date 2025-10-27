package com.ad.sample.language

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ad.sample.ConfigManager
import com.ad.sample.NativeControl
import com.ads.admob_sdk.api.NativeAdControl
import com.ads.admob_sdk.api.config.NativeConfig
import com.ad.sample.R
import com.ad.sample.databinding.FragmentLanguage1Binding
import com.ad.sample.onboard.OnboardingActivity

open class LFOSelectFragment : Fragment() {
    private var binding: FragmentLanguage1Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLanguage1Binding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.textView5?.text = "Select Language"
        binding?.button3?.setOnClickListener {
            startActivity(Intent(this.context, OnboardingActivity::class.java))
        }
        setNativeAd()
    }


    private fun setNativeAd() {
        val nativeAdControl = activity?.let {
            NativeAdControl(
                activity = it,
                lifecycleOwner = this,
                config = NativeConfig(
                    id = "ca-app-pub-3940256099942544/2247696110",
                    idHigh = "ca-app-pub-3940256099942544/1044960115",
                    isShow = ConfigManager.isShowNativeLanguageDup,
                    layoutId = R.layout.layout_native_big
                )
            )
        }
        nativeAdControl?.setNativeContainer(binding?.layoutAdNative)
        nativeAdControl?.setShimmerLayoutView(binding?.includeShimmer?.shimmerContainerNative)
        NativeControl.nativeLFO2.observe(viewLifecycleOwner) { ad ->
            ad?.let {
                nativeAdControl?.showAd(ad)
            } ?: run {
                nativeAdControl?.requestAds()
            }
        }
        context?.let { NativeControl.requestNativeOnboardingFullScreen23(it) }
        context?.let { NativeControl.requestNativeOnboarding1(it) }
    }
}

