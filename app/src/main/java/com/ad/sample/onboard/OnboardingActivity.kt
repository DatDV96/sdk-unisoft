package com.ad.sample.onboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.ad.sample.ConfigManager
import com.ad.sample.MainActivity
import com.ad.sample.NativeControl
import com.ad.sample.databinding.ActivityOnboardingBinding
import java.util.concurrent.atomic.AtomicBoolean

class OnboardingActivity : AppCompatActivity(), PageListener {
    private var binding: ActivityOnboardingBinding? = null

    private val onboarding1 by lazy { OnboardingPage1Fragment.newInstance() }
    private val onboardingFull23 by lazy { OnboardingFullScreenFragment.newInstance() }
    private val onboarding2 by lazy { OnboardingPage2Fragment.newInstance() }
    private val onboardingFull12 by lazy { OnboardingFullScreenFragment.newInstance() }
    private val onboarding3 by lazy { OnboardingPage3Fragment.newInstance() }
    private var mCustomPagerAdapter: CustomPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        Log.e("mCustomPagerAdapter", "onCreate: rrr", )
        setContentView(binding?.root)
        setupViewPager()
    }

    private fun setupViewPager() {
        Log.e("mCustomPagerAdapter", "setupViewPager: f", )
        mCustomPagerAdapter = CustomPagerAdapter(supportFragmentManager)
        onboarding1.let { mCustomPagerAdapter?.addPage(it) }
        if (ConfigManager.isShowNativeFullScreen) {
            onboardingFull12.let { mCustomPagerAdapter?.addPage(it) }
        }
        onboarding2.let { mCustomPagerAdapter?.addPage(it) }
        if (ConfigManager.isShowNativeFullScreen) {
            onboardingFull23.let { mCustomPagerAdapter?.addPage(it) }
        }
        onboarding3.let { mCustomPagerAdapter?.addPage(it) }

        onboarding1.setRemovePageListener(this)
        onboarding2.setRemovePageListener(this)
        onboarding3.setRemovePageListener(this)
        onboardingFull23.setRemovePageListener(this)
        binding?.vpTutorial?.offscreenPageLimit = mCustomPagerAdapter?.count ?: 0
        binding?.vpTutorial?.setPagingEnabled(true)
        binding?.vpTutorial?.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                Log.e("OnboardingPage1Fragment", "onPageSelected: ", )
                if (position == 2) {
                    requestNativeObFull23()
                    NativeControl.requestNativeOnboarding1(this@OnboardingActivity)
                }
            }
        })
        Log.e("mCustomPagerAdapter", "setupViewPager: ", )
        binding?.vpTutorial?.adapter = mCustomPagerAdapter
    }


    private var isRequestFull23 = AtomicBoolean(false)

    private fun requestNativeObFull23() {
        if (!ConfigManager.isShowNativeFullScreen) return
        if (isRequestFull23.getAndSet(true)) return
        NativeControl.requestNativeOnboardingFullScreen23(this)
    }


    override fun removePage(tag: String?) {
    }

    override fun onNextPage() {
        val maxPage = binding?.vpTutorial?.adapter?.count ?: 0
        val currentPosition = binding?.vpTutorial?.currentItem ?: 0
        if (currentPosition >= maxPage - 1) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            binding?.vpTutorial?.setCurrentItem(currentPosition + 1, true)
        }
    }

    override fun onPreviousPage() {
        val currentPosition = binding?.vpTutorial?.currentItem ?: 0
        if (currentPosition > 0) {
            binding?.vpTutorial?.setCurrentItem(currentPosition - 1, true)
        }
    }
}