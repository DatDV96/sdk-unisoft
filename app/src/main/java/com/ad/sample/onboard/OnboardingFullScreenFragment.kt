package com.ad.sample.onboard

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
import com.ad.sample.databinding.FragmentObFullBinding

class OnboardingFullScreenFragment : Fragment() {


    private var removePageListener: PageListener? = null
    fun setRemovePageListener(removePageListener: PageListener) {
        this.removePageListener = removePageListener
    }

    companion object {

        fun newInstance(): OnboardingFullScreenFragment {
            return newInstance("OnboardingFullScreenFragment")
        }

        private fun newInstance(tag: String?): OnboardingFullScreenFragment {
            val fragment = OnboardingFullScreenFragment()
            val args = Bundle()
            args.putString(ConfigManager.FRAGMENT_OB_TAG, tag + "_" + fragment.hashCode())
            fragment.arguments = args
            return fragment
        }
    }
    private var binding: FragmentObFullBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentObFullBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initNativeAds()
    }

    private fun initNativeAds() {
        val nativeAdControl = activity?.let {
            NativeAdControl(
                activity = it,
                lifecycleOwner = this,
                config = NativeConfig(
                    id = "ca-app-pub-3940256099942544/2247696110",
                    idHigh = "ca-app-pub-3940256099942544/1044960115",
                    isShow = ConfigManager.isShowNativeFullScreen,
                    layoutId = R.layout.layout_native_full
                )
            )
        }
        nativeAdControl?.setNativeContainer(binding?.layoutAdNative)
        nativeAdControl?.setShimmerLayoutView(binding?.includeShimmer?.shimmerContainerNative)
        NativeControl.nativeObFullScreen.observe(viewLifecycleOwner) { ad ->
            ad?.let {
                nativeAdControl?.showAd(ad)
            } ?: run {
                nativeAdControl?.requestAds()
            }
        }
    }
}