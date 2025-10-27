package com.ad.sample.onboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ad.sample.ConfigManager
import com.ad.sample.NativeControl
import com.ads.admob_sdk.api.NativeAdControl
import com.ads.admob_sdk.api.config.NativeConfig
import com.ad.sample.R
import com.ad.sample.databinding.FragmentObBinding

class OnboardingPage2Fragment : Fragment() {


    private var removePageListener: PageListener? = null
    fun setRemovePageListener(removePageListener: PageListener) {
        this.removePageListener = removePageListener
    }
    companion object {

        fun newInstance(): OnboardingPage2Fragment {
            return newInstance("OnboardingPage2Fragment")
        }

        private fun newInstance(tag: String?): OnboardingPage2Fragment {
            val fragment = OnboardingPage2Fragment()
            val args = Bundle()
            args.putString(ConfigManager.FRAGMENT_OB_TAG, tag + "_" + fragment.hashCode())
            fragment.arguments = args
            return fragment
        }
    }
    private var binding: FragmentObBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentObBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            binding?.textView5?.text = "OB 2"
            binding?.button3?.setOnClickListener {
                removePageListener?.onNextPage()
            }
        }
        initNativeAds()
    }

    private fun initNativeAds() {
        Log.e("isShowNativeOnboarding1", "initNativeAds: 2", )

        val nativeAdControl = activity?.let {
            NativeAdControl(
                activity = it,
                lifecycleOwner = this,
                config = NativeConfig(
                    id = "ca-app-pub-3940256099942544/2247696110",
                    idHigh = "ca-app-pub-3940256099942544/1044960115",
                    isShow = ConfigManager.isShowNativeOnboarding1,
                    layoutId = R.layout.layout_native_big
                )
            )
        }
        nativeAdControl?.setNativeContainer(binding?.layoutAdNative)
        nativeAdControl?.setShimmerLayoutView(binding?.includeShimmer?.shimmerContainerNative)
        NativeControl.nativeOb1.observe(viewLifecycleOwner) { ad ->
            ad?.let {
                nativeAdControl?.showAd(ad)
            } ?: run {
                nativeAdControl?.requestAds()
            }
        }
    }
}