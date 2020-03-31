package com.satcat.kalys

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class GroupSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    lateinit var activeGroupFragment: ActiveGroupFragment
    lateinit var activeChannelFragment: ActiveChannelFragment


    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                activeGroupFragment = ActiveGroupFragment()
                return activeGroupFragment
            }
            1 -> {
                activeChannelFragment = ActiveChannelFragment()
                return activeChannelFragment
            }
            else -> {
                throw IllegalStateException("$position is illegal")
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return "Active Group"
            }
            1 -> {
                return "Active Channel"
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