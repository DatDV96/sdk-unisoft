package com.ad.sample.onboard

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter
import com.ad.sample.ConfigManager
import kotlin.collections.get

class CustomPagerAdapter(private val fragmentManager: FragmentManager) : PagerAdapter() {
    private val pages: MutableList<Fragment> = ArrayList()
    private val fragmentsPosition: MutableMap<Fragment, Int> = HashMap()
    private var currentPrimaryItem: Fragment? = null
    private var currentTransaction: FragmentTransaction? = null
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction()
        }
        val pageFragment = pages[position]
        val tag = pageFragment.arguments?.getString(ConfigManager.FRAGMENT_OB_TAG)

        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            if (fragment.id == container.id) {
                currentTransaction?.attach(fragment)
            } else {
                fragmentManager.beginTransaction().remove(fragment).commit()
                fragmentManager.executePendingTransactions()
                currentTransaction?.add(container.id, fragment, tag)
            }
        } else {
            fragment = pageFragment
            currentTransaction?.add(container.id, fragment, tag)
        }
        if (fragment !== currentPrimaryItem) {
            fragment.setMenuVisibility(false)
            fragment.userVisibleHint = false
        }
        return fragment
    }

    fun getPositionPage(fragment: Fragment): Int {
        return pages.indexOf(fragment)
    }

    override fun getCount(): Int {
        return pages.size
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (currentTransaction == null) {
            currentTransaction = fragmentManager.beginTransaction()
        }
        currentTransaction?.detach(`object` as Fragment)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        if (fragment !== currentPrimaryItem) {
            if (currentPrimaryItem != null) {
                currentPrimaryItem!!.setMenuVisibility(false)
                currentPrimaryItem!!.userVisibleHint = false
            }
            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true
            currentPrimaryItem = fragment
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        if (currentTransaction != null) {
            currentTransaction?.commitAllowingStateLoss()
            currentTransaction = null
            fragmentManager.executePendingTransactions()
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view === view
    }

    override fun getItemPosition(o: Any): Int {
        return fragmentsPosition[o] ?: return POSITION_UNCHANGED
    }

    // ---------------------------------- Page actions ----------------------------------
    fun addPage(fragment: Fragment) {
        fragmentsPosition.clear()
        pages.add(fragment)
        notifyDataSetChanged()
    }

    fun removePage(position: Int) {
        fragmentsPosition.clear()
        var pageFragment = pages[position]
        var tag = pageFragment.arguments?.getString(ConfigManager.FRAGMENT_OB_TAG)
        var fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentsPosition[fragment] = POSITION_NONE
        }
        for (i in position + 1 until pages.size) {
            pageFragment = pages[i]
            tag = pageFragment.arguments?.getString(ConfigManager.FRAGMENT_OB_TAG)
            fragment = fragmentManager.findFragmentByTag(tag)
            if (fragment != null) {
                fragmentsPosition[fragment] = i - 1
            }
        }
        pages.removeAt(position)
        notifyDataSetChanged()
    }
}
