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
    
    var resumeFragmentActive = true



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

                supportActionBar!!.title = sectionsPagerAdapter.getPageTitle(position)


                if(position == 0) {
                    showGroupIsActive = true// Check if this is the page you want.
                    supportActionBar!!.title = activeGroup.Title
                    sectionsPagerAdapter.activeGroupFragment.fragmentIsActive()
                    sectionsPagerAdapter.activeChannelFragment.closeNotice()

                } else if (position == 1) {
                    showGroupIsActive = false
                    supportActionBar!!.title = activeChannel.Title
                    if(resumeFragmentActive) {
                        sectionsPagerAdapter.activeChannelFragment.fragmentIsActive()
                    } else {
                        resumeFragmentActive = true
                    }
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


        if(fromChannelList && showGroupIsActive.not()) {
            resumeFragmentActive = false
            viewPager.setCurrentItem(1, false)

        }

        if(viewPager.currentItem == 0) {
            supportActionBar!!.title = activeGroup.Title
        } else if (viewPager.currentItem == 1) {
            supportActionBar!!.title = activeChannel.Title
        }

    }
;





}