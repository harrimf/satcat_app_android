package com.satcat.kalys

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.satcat.kalys.Models.ChatChannel
import com.shuhart.stickyheader.StickyAdapter
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.layout_channels_cell.view.*
import kotlinx.android.synthetic.main.layout_channels_header.view.*
import java.util.*


class HeaderAdapter(private val recyclerType : HeaderRecyclerType, private var items: ArrayList<RecyclerChannel>)
    : StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder?>() {

    companion object {
        private const val LAYOUT_HEADER = 0
        private const val LAYOUT_CHILD = 1
    }

    enum class HeaderRecyclerType {
        ALLCHANNELS, GROUPCHANNELS
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == LAYOUT_HEADER) {
            HeaderViewholder(inflater.inflate(R.layout.layout_channels_header, parent, false))
        } else {
            when(recyclerType) {
                HeaderRecyclerType.ALLCHANNELS -> AllChannelsViewHolder(inflater.inflate(R.layout.layout_channels_cell, parent, false))
                HeaderRecyclerType.GROUPCHANNELS -> GroupChannelsViewHolder(inflater.inflate(R.layout.layout_channels_cell, parent, false))
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        if (items[position].header) {
            (holder as HeaderViewholder).bind((items[position] as RecyclerChannelHeader).headerText)
        } else {
            when(holder) {
                is AllChannelsViewHolder -> {
                    val channel = (items[position] as RecyclerChannelChild).channel
                    holder.bind(channel)
                    holder.itemView.setOnClickListener {
                        //navigate to new activity
                        val intent = Intent(holder.itemView.context, GroupTabActivity::class.java)
                        intent.putExtra("activeGroup", channel.GroupID)
                        intent.putExtra("activeChannel", channel.ID)
                        intent.putExtra("fromChannelList", true)
                        intent.putExtra("showGroupIsActive", false)
                        holder.itemView.context.startActivity(intent)
                    }
                }
                is GroupChannelsViewHolder -> {
                    val channel = (items[position] as RecyclerChannelChild).channel
                    holder.bind(channel)
                    holder.itemView.setOnClickListener {
                        //navigate to new activity
                        if (holder.itemView.context.javaClass.equals(GroupTabActivity::class.java)) {

                            val tabActivity = holder.itemView.context as GroupTabActivity

                            if(channel.IsMember) {
                                val realm : Realm = Realm.getDefaultInstance()
                                tabActivity.activeChannel = realm.where<ChatChannel>().equalTo("ID", channel.ID).findFirst()!!
                                tabActivity.viewPager.setCurrentItem(1, true)
                                tabActivity.showGroupIsActive = false

                                realm.executeTransaction {
                                    channel.NotificationCount = 0
                                }

                            } else {
                                tabActivity.sectionsPagerAdapter.activeGroupFragment.joinChannel = channel

                                tabActivity.sectionsPagerAdapter.activeGroupFragment.setJoinChannelText(channel)

                                val members = channel.Members.toList()
                                tabActivity.sectionsPagerAdapter.activeGroupFragment.joinChannelMembersAdapter.submitList(members)
                                tabActivity.sectionsPagerAdapter.activeGroupFragment.joinChannelMembersAdapter.notifyDataSetChanged()

                                tabActivity.sectionsPagerAdapter.activeGroupFragment.joinChannelVisible(true)

                            }
                        }
                    }
                }
            }
        }
    }

    fun submitList(list: ArrayList<RecyclerChannel>) {
        items = list
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].header) { LAYOUT_HEADER  } else LAYOUT_CHILD
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        return items[itemPosition].sectionPosition
    }

    override fun onBindHeaderViewHolder(
        holder: RecyclerView.ViewHolder,
        headerPosition: Int
    ) {
        (holder as HeaderViewholder).bind((items[headerPosition] as RecyclerChannelHeader).headerText)
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return createViewHolder(parent, LAYOUT_HEADER)
    }

    class HeaderViewholder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val header = itemView.header_channels_lbl

        fun bind(title: String) {
            header.text = title

        }
    }


    class AllChannelsViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val channelTitle = itemView.channel_title
        val channelMembers = itemView.channel_members_count
        val channelType = itemView.channel_type
        val channelNotifications = itemView.channel_notifications
        val channelNotificationImg = itemView.notification_image
        val channelNotificationCount = itemView.channel_notification_count

        fun bind(channel: ChatChannel) {

            val memberCount = channel.Members.count()

            channelTitle.text = channel.Title
            channelMembers.text = if (memberCount == 1) "1 Member" else  "$memberCount Members"
            channelType.text =  if (channel.IsMain) " • " + "Main" else ""
            channelNotifications.text = "New Messages"

            updateNotificationViews(channel)


        }

        private fun updateNotificationViews(channel: ChatChannel) {

            val notificationCount = channel.NotificationCount

            if(channel.ReceivedNotice && notificationCount > 0) {
                channelNotificationCount.text = notificationCount.toString()
                channelNotifications.text = "New Messages & Notice"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text

            } else if(channel.ReceivedNotice) {
                channelNotificationCount.text = ""
                channelNotifications.text = "New Notice"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text


            } else if (notificationCount > 0) {
                channelNotificationCount.text = notificationCount.toString()
                channelNotifications.text = "New Messages"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text
            } else {
                channelNotificationCount.text = ""
                channelNotifications.text = ""
                channelNotificationImg.setImageResource(R.drawable.notification_off)
            }
        }
    }


    class GroupChannelsViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val channelTitle = itemView.channel_title
        val channelMembers = itemView.channel_members_count
        val channelType = itemView.channel_type
        val channelNotifications = itemView.channel_notifications
        val channelNotificationImg = itemView.notification_image
        val channelNotificationCount = itemView.channel_notification_count
        val channelJoinImg = itemView.channel_join_image


        fun bind(channel: ChatChannel) {

            val memberCount = channel.Members.count()

            channelTitle.text = channel.Title
            channelMembers.text = if (memberCount == 1) "1 Member" else  "$memberCount Members"
            channelType.text =  if (channel.IsMain) " • " + "Main" else ""
            channelNotifications.text = "New Messages"

            if(!channel.IsMember) {
                channelJoinImg.visibility = View.VISIBLE
                channelNotificationImg.visibility = View.GONE
            } else {
                channelJoinImg.visibility = View.GONE
                channelNotificationImg.visibility = View.VISIBLE


            }

            updateNotificationViews(channel)

        }

        private fun updateNotificationViews(channel: ChatChannel) {

            val notificationCount = channel.NotificationCount

            if(channel.ReceivedNotice && notificationCount > 0) {
                channelNotificationCount.text = notificationCount.toString()
                channelNotifications.text = "New Messages & Notice"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text

            } else if(channel.ReceivedNotice) {
                channelNotificationCount.text = ""
                channelNotifications.text = "New Notice"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text


            } else if (notificationCount > 0) {
                channelNotificationCount.text = notificationCount.toString()
                channelNotifications.text = "New Messages"
                channelNotificationImg.setImageResource(R.drawable.notification_on)
                channelMembers.text = " • " + channelMembers.text
            } else {
                channelNotificationCount.text = ""
                channelNotifications.text = ""
                channelNotificationImg.setImageResource(R.drawable.notification_off)
            }
        }
    }
}