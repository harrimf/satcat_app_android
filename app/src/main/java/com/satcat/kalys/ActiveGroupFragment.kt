package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.ChatChannel
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import io.realm.Realm
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActiveGroupFragment : Fragment()
{
    private lateinit var noticesAdapter : RecyclerAdapter
    private lateinit var channelAdapter : HeaderAdapter
    lateinit var joinChannelMembersAdapter: RecyclerAdapter


    private lateinit var realm: Realm

    lateinit var newChannelBtn : Button
    lateinit var leaveGroupBtn : Button

    lateinit var background : View

    lateinit var groupImg : ImageView

    lateinit var joinChannelTitle : TextView
    lateinit var joinChannelNoticeTitle : TextView
    lateinit var joinChannelNotice : TextView
    lateinit var joinChannelMembersTitle : TextView
    lateinit var joinChannelNoticeContainer : ConstraintLayout

    lateinit var joinChannelRecycler : RecyclerView

    lateinit var joinChannelCancelBtn : Button
    lateinit var joinChannelUpdateBtn : Button

    lateinit var joinChannel : ChatChannel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_active_group, container, false)

        val parent = activity as GroupTabActivity

        setHasOptionsMenu(true)

        newChannelBtn = view.findViewById(R.id.active_group_create_channel_btn)
        leaveGroupBtn = view.findViewById(R.id.active_group_leave_group_btn)

        background = view.findViewById(R.id.active_group_background)

        groupImg = view.findViewById(R.id.active_group_image)

        joinChannelTitle = view.findViewById(R.id.active_group_join_title)
        joinChannelNoticeTitle = view.findViewById(R.id.active_group_join_notice_title)
        joinChannelNotice = view.findViewById(R.id.active_group_join_notice)
        joinChannelMembersTitle = view.findViewById(R.id.active_group_join_members_info)
        joinChannelNoticeContainer = view.findViewById(R.id.active_group_join_notice_container)

        joinChannelRecycler = view.findViewById(R.id.active_group_join_recycler_members)

        joinChannelCancelBtn = view.findViewById(R.id.active_group_join_cancel_btn)
        joinChannelUpdateBtn = view.findViewById(R.id.active_group_join_update_btn)


        newChannelBtn.setOnClickListener {
            val intent = Intent(activity, CreateChannelActivity::class.java)
            intent.putExtra("groupID", parent.activeGroup.ID)
            startActivity(intent)
        }

        leaveGroupBtn.setOnClickListener {
            exitGroup()
            parent.finish()
        }

        joinChannelCancelBtn.setOnClickListener {
            joinChannelVisible(false)
        }

        joinChannelUpdateBtn.setOnClickListener {
            commitJoinChannel()
            joinChannelVisible(false)
        }

        realm = Realm.getDefaultInstance()

        initNoticeRecyclerView(view!!.findViewById(R.id.recycler_notices))
        initChannelRecyclerView(view!!.findViewById(R.id.recycler_channels))
        initUserRecyclerView(joinChannelRecycler)


        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val parent = activity as GroupTabActivity

        groupImg.setImageBitmap(parent.activeGroup.getImage())

        handleReceive()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val parent = activity as GroupTabActivity

        if(id == R.id.menu_group_settings_btn) {
            val intent = Intent(activity, OptionsGroupActivity::class.java)
            intent.putExtra("groupID", parent.activeGroup.ID)
            intent.putExtra("channelID", parent.activeChannel.ID)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    fun fragmentIsActive() {
        val parent = activity as GroupTabActivity
        groupImg.setImageBitmap(parent.activeGroup.getImage())

        handleReceive()
    }

    private fun initNoticeRecyclerView(recycler : RecyclerView) {

        var helper : SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            noticesAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.GROUPNOTICES) //give string for group
            //can use decorator
            adapter = noticesAdapter
        }

    }

    private fun initChannelRecyclerView(recycler : RecyclerView) {
        val parent = activity as GroupTabActivity

        val memberChannels : List<ChatChannel> = realm.where<ChatChannel>().equalTo("GroupID", parent.activeGroup.ID).equalTo("IsMember", true).findAll().toList()
        val otherChannels : List<ChatChannel> = realm.where<ChatChannel>().equalTo("GroupID", parent.activeGroup.ID).equalTo("IsMember", false).findAll().toList()

        recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            channelAdapter = HeaderAdapter(HeaderAdapter.HeaderRecyclerType.GROUPCHANNELS, recyclerList(memberChannels, otherChannels)) //give string for group
            //can use decorator
            adapter = channelAdapter
        }

        StickyHeaderItemDecorator(channelAdapter).attachToRecyclerView(recycler)

    }

    private fun initUserRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            joinChannelMembersAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.JOINCHANNELMEMBERS)
            //can use decorator
            adapter = joinChannelMembersAdapter
        }

    }

    fun exitGroup() {
        val realm : Realm = Realm.getDefaultInstance()

        val parent = activity as GroupTabActivity

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.deleteGroup(parent.activeGroup.giveJson())

            realm.executeTransaction {
               parent.activeGroup.Channels.deleteAllFromRealm()
            }

            realm.executeTransaction {
                parent.activeGroup.deleteFromRealm()
            }

        } else {
            realm.executeTransaction {
                parent.activeGroup.Removable = false
            }

        }

    }

    fun handleReceive() {
        val parent = activity as GroupTabActivity

        val memberChannels : List<ChatChannel> = realm.where<ChatChannel>().equalTo("GroupID", parent.activeGroup.ID).equalTo("IsMember", true).findAll().toList()
        val otherChannels : List<ChatChannel> = realm.where<ChatChannel>().equalTo("GroupID", parent.activeGroup.ID).equalTo("IsMember", false).findAll().toList()

        noticesAdapter.submitList(memberChannels)
        noticesAdapter.notifyDataSetChanged()

        channelAdapter.submitList(recyclerList(memberChannels, otherChannels))
        channelAdapter.notifyDataSetChanged()
    }

    private fun recyclerList(memberChannels : List<ChatChannel>, otherChannels: List<ChatChannel>) : ArrayList<RecyclerChannel> {

        val membersList = ArrayList<RecyclerChannel>()
        val othersList = ArrayList<RecyclerChannel>()


        for (channel in memberChannels) {
            membersList.add(RecyclerChannelChild(channel, 0))
        }

        for (channel in otherChannels) {
            othersList.add(RecyclerChannelChild(channel, 1))
        }


        val finalList : ArrayList<RecyclerChannel> = ArrayList()

        finalList.add(RecyclerChannelHeader("My Channels", 0))
        finalList.addAll(membersList)

        if(othersList.isNotEmpty()) {
            finalList.add(RecyclerChannelHeader("Other Channels", 1))
            finalList.addAll(othersList)

        }

        return finalList
    }

    fun setJoinChannelText(channel: ChatChannel) {
        joinChannelTitle.text = "Join " + channel.Title
        joinChannelNoticeTitle.text = channel.Title + " Notice"
        joinChannelMembersTitle.text = channel.Title + " Members • " + channel.Members.count().toString()
        joinChannelNotice.text = channel.Notice
    }

    fun commitJoinChannel() {
        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.sendUser(UserManager.shared.getUser().giveJson(), joinChannel.ID)
        } else {
            realm.executeTransaction {
                joinChannel.OfflineMembers.add(UserManager.shared.getUser())
            }
        }
    }

    fun joinChannelVisible(visible: Boolean) {
        background.isVisible = visible

        joinChannelTitle.isVisible = visible
        joinChannelNoticeTitle.isVisible = visible
        joinChannelNotice.isVisible = visible
        joinChannelMembersTitle.isVisible = visible
        joinChannelNoticeContainer.isVisible = visible

        joinChannelRecycler.isVisible = visible

        joinChannelCancelBtn.isVisible = visible
        joinChannelUpdateBtn.isVisible = visible
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveChannelEvent(event: SocketIOManager.ReceiveChannelEvent): Unit {
        handleReceive()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMessageEvent(event: SocketIOManager.ReceiveMessageEvent): Unit {
        handleReceive()

    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}