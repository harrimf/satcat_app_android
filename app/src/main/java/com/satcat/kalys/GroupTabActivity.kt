package com.satcat.kalys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.google.android.material.tabs.TabLayout
import io.realm.Realm
import io.realm.kotlin.where


class GroupTabActivity : AppCompatActivity() {

    lateinit var activeGroup: ChatGroup
    lateinit var activeChannel: ChatChannel
    var fromChannelList: Boolean = false
    var showGroupIsActive: Boolean = true

    lateinit var viewPager : ViewPager
    lateinit var sectionsPagerAdapter: GroupSectionsPagerAdapter

    private lateinit var realm: Realm



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_group_tab)

        realm = Realm.getDefaultInstance()

        sectionsPagerAdapter =
            GroupSectionsPagerAdapter(this, supportFragmentManager)

        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter

        viewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if(position == 0) {
                    showGroupIsActive = true// Check if this is the page you want.
                    sectionsPagerAdapter.activeGroupFragment.fragmentIsActive()

                    sectionsPagerAdapter.activeChannelFragment.closeNotice()

                } else if (position == 1) {
                    showGroupIsActive = false
                    sectionsPagerAdapter.activeChannelFragment.fragmentIsActive()
                }
            }
        })

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)


    }

    override fun onStart() {
        super.onStart()

        val channelQuery = realm.where<ChatChannel>().equalTo("ID", intent.getStringExtra("activeChannel")).findFirst()

        activeGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("activeGroup")).findFirst()!!
        activeChannel = channelQuery ?: activeGroup.Channels.first()!! //handles leaving channel
        fromChannelList = intent.getBooleanExtra("fromChannelList",  false)
        showGroupIsActive = intent.getBooleanExtra("showGroupIsActive", true)
    }
;





}