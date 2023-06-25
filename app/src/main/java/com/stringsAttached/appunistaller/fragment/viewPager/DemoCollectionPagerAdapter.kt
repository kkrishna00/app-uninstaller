package com.stringsAttached.appunistaller.fragment.viewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.stringsAttached.appunistaller.fragment.fragment.FragmentScreenData
import com.stringsAttached.appunistaller.fragment.fragment.AppContainerFragment
import com.stringsAttached.appunistaller.fragment.fragment.ViewPagerAdapterScreenData

class DemoCollectionPagerAdapter(
    fm: FragmentManager,
    private var screenData: ViewPagerAdapterScreenData
) : FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.userApps,
                        userApp = true
                    )
                )
            }
            1 -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.systemApps,
                        userApp = false
                    )
                )
            }
            else -> {
                AppContainerFragment.newInstance(
                    screenData = FragmentScreenData(
                        installedApps = screenData.userApps,
                        userApp = true
                    )
                )
            }
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> {
                "${screenData.userApps.size} USER APPS"
            }
            1 -> {
                "${screenData.systemApps.size} SYSTEM APPS"
            }
            else -> {
                "INVALID"
            }
        }
    }

    fun updateData(screenData: ViewPagerAdapterScreenData) {
        this.screenData = screenData
        notifyDataSetChanged()
    }
}