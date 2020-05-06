package com.satcat.kalys

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class GroupSectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    val activeGroupFragment: ActiveGroupFragment = ActiveGroupFragment()
    val activeChannelFragment: ActiveChannelFragment = ActiveChannelFragment()



    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                activeGroupFragment
            }
            1 -> {
                activeChannelFragment
            }
            else -> {
                throw IllegalStateException("$position is illegal")
            }
        }
    }


    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}