package com.satcat.kalys

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class StartSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return GroupsFragment()
            }
            1 -> {
                return AllChannelsFragment()
            }
            else -> {
                throw IllegalStateException("$position is illegal")
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return "Groups"
            }
            1 -> {
                return "All Channels"
            }
            else -> {
                return null
            }
        }
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}