package com.satcat.kalys

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.satcat.kalys.Models.ChatMessage
import com.satcat.kalys.Models.User
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.layout_channels_cell.view.*
import kotlinx.android.synthetic.main.layout_channels_cell.view.notification_image
import kotlinx.android.synthetic.main.layout_groups_cell.view.*
import kotlinx.android.synthetic.main.layout_messages_cell.view.*
import kotlinx.android.synthetic.main.layout_notices_cell.view.*
import kotlinx.android.synthetic.main.layout_search_group_cell.view.*
import kotlinx.android.synthetic.main.layout_user_cell.view.*
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

class RecyclerAdapter(type: RecyclerType) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<Any> = ArrayList()
    private var recyclerType: RecyclerType = type



    enum class RecyclerType {
        GROUPS, /*ALLCHANNELS, GROUPCHANNELS,*/ GROUPNOTICES, JOINCHANNELMEMBERS, MESSAGES, CHANNELSWITCH, CREATEUSERS, ADDMEMBERS, JOINGROUPS, VIEWMEMBERS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(recyclerType) {
            RecyclerType.GROUPS -> GroupsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_groups_cell, parent, false))
            RecyclerType.GROUPNOTICES -> NoticesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_notices_cell, parent, false))
            RecyclerType.JOINCHANNELMEMBERS -> JoinChannelUsersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_user_cell, parent, false))
            RecyclerType.MESSAGES -> MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_messages_cell, parent, false))
            RecyclerType.CHANNELSWITCH -> SwitchChannelsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_channels_cell, parent, false))
            RecyclerType.CREATEUSERS -> CreateUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_user_cell, parent, false))
            RecyclerType.ADDMEMBERS -> AddMembersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_user_cell, parent, false))
            RecyclerType.JOINGROUPS -> JoinGroupsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_search_group_cell, parent, false))
            RecyclerType.VIEWMEMBERS -> ViewMemberViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_user_cell, parent, false))

        }
    }

    fun submitList(list: List<Any>) {
        items = list
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //can bind multiple view holders with when

        when(holder) {
            is GroupsViewHolder -> {
                val group = items.get(position) as ChatGroup
                holder.bind(group)
                holder.itemView.setOnClickListener {
                    //navigate to new activity
                    val intent = Intent(holder.itemView.context, GroupTabActivity::class.java)
                    intent.putExtra("activeGroup", group.ID)
                    intent.putExtra("activeChannel", group.Channels.first()!!.ID)
                    intent.putExtra("fromChannelList", false)
                    intent.putExtra("showGroupIsActive", true)
                    holder.itemView.context.startActivity(intent)

                }
            }

            is MessagesViewHolder -> {
                val message = items.get(position) as ChatMessage
                holder.bind(message)
                holder.itemView.setOnClickListener {

                }

            }

            is NoticesViewHolder -> {
                val channel = items.get(position) as ChatChannel
                holder.bind(channel)
                holder.itemView.setOnClickListener {

                }
            }

            is JoinChannelUsersViewHolder -> {
                val user = items.get(position) as User
                holder.bind(user)
            }

            is SwitchChannelsViewHolder -> {
                val channel = items.get(position) as ChatChannel
                holder.bind(channel)
                holder.itemView.setOnClickListener {
                    //navigate to new activity
                    if (holder.itemView.context.javaClass.equals(GroupTabActivity::class.java)) {
                        val realm : Realm = Realm.getDefaultInstance()

                        val groupTab = holder.itemView.context as GroupTabActivity

                        realm.executeTransaction {
                            channel.NotificationCount = 0
                        }
                        //TODO: Update count

                        val messages = channel.Messages.toList()

                        groupTab.activeChannel = channel
                        groupTab.sectionsPagerAdapter.activeChannelFragment.messages = messages
                        groupTab.sectionsPagerAdapter.activeChannelFragment.messageAdapter.submitList(messages)
                        groupTab.sectionsPagerAdapter.activeChannelFragment.messageAdapter.notifyDataSetChanged()

                        groupTab.sectionsPagerAdapter.activeChannelFragment.setupNoticeText()
                        groupTab.sectionsPagerAdapter.activeChannelFragment.switchChannelVisible(false)

                    }
                }
            }

            is CreateUserViewHolder -> {
                val user = items.get(position) as User
                holder.bind(user)
                holder.itemView.setOnClickListener {
                    if (holder.itemView.context.javaClass.equals(CreateGroupActivity::class.java)) {
                        val createGroup = holder.itemView.context as CreateGroupActivity

                        for (member in createGroup.members) {
                            if(member.UserName == holder.member.UserName) {
                                createGroup.mainChannel.Members.remove(member)
                            }
                        }

                        createGroup.members = createGroup.mainChannel.Members.toList()
                        createGroup.membersAdapter.submitList(createGroup.members)
                        createGroup.membersAdapter.notifyDataSetChanged()

                        val memberTxt : TextView = createGroup.findViewById(R.id.create_group_members_count)
                        val memberCount = createGroup.members.count()
                        memberTxt.text = "Members • " + memberCount.toString()
                        if(memberCount == 0) {
                            val removeLbl : TextView = createGroup.findViewById(R.id.create_group_members_remove_text)
                            removeLbl.isVisible = false
                        }

                    } else if (holder.itemView.context.javaClass.equals(CreateChannelActivity::class.java)){
                        val createChannel = holder.itemView.context as CreateChannelActivity

                        for (member in createChannel.members) {
                            if(member.UserName == holder.member.UserName) {
                                createChannel.newChannel.Members.remove(member)
                            }
                        }

                        createChannel.members = createChannel.newChannel.Members.toList()
                        createChannel.membersAdapter.submitList(createChannel.members)
                        createChannel.membersAdapter.notifyDataSetChanged()

                        val memberTxt : TextView = createChannel.findViewById(R.id.create_channel_members_count)
                        val memberCount = createChannel.members.count()
                        memberTxt.text = "Members • " + memberCount.toString()
                        if(memberCount == 0) {
                            val removeLbl : TextView = createChannel.findViewById(R.id.create_channel_members_remove_text)
                            removeLbl.isVisible = false
                        }

                    }
                }
            }

            is AddMembersViewHolder -> {
                val user = items.get(position) as User
                holder.bind(user)
                holder.itemView.setOnClickListener {
                    if (holder.itemView.context.javaClass.equals(AddMembersActivity::class.java)) {
                        val membersActivity = holder.itemView.context as AddMembersActivity
                        if(!membersActivity.userIDs.contains(user.ID)) {
                            membersActivity.usersAdded.add(user)
                            membersActivity.userIDs.add(user.ID)
                            //TODO: add handle for image
                            membersActivity.userAdapter.notifyDataSetChanged()

                        }

                    }
                }
            }

            is JoinGroupsViewHolder -> {
                val pair = items.get(position) as Pair<ChatGroup, JSONObject>

                holder.bind(pair.first, pair.second)

                holder.itemView.setOnClickListener {

                    //navigate to new activity
                    if(!holder.inGroup) {
                        if(SocketIOManager.shared.socketConnected()) {
                            SocketIOManager.shared.joinGroup(pair.first.giveJson())
                        } else {
                            val toast = Toast.makeText(holder.itemView.context, "Could not connect to the network, failed to join group", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    }

                }
            }

            is ViewMemberViewHolder -> {
                val user = items.get(position) as User
                holder.bind(user)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class GroupsViewHolder constructor(
        itemView:View
    ) : RecyclerView.ViewHolder(itemView) {
        val groupTitle = itemView.group_title
        val groupMembers = itemView.group_members_count
        val groupChannels = itemView.group_channels_count
        val groupImg = itemView.group_image
        val groupNotifications = itemView.group_notifications_lbl
        val groupNotificationImage = itemView.group_notification_image

        fun bind(group: ChatGroup) {

            val channelCount = group.Channels!!.count()
            val memberCount = if(channelCount > 0) group.Channels.first()!!.Members.count() else 0 //to avoid annoying begin situation

            groupTitle.text = group.Title
            groupMembers.text =  if (memberCount == 1) "1 Member" else  "$memberCount Members"
            groupChannels.text =  if (channelCount == 1) "1 Channel" else  "$channelCount Channels"
            groupImg.setImageBitmap(group.getImage())
            updateNotificationViews(group)

        }

        private fun updateNotificationViews(group: ChatGroup) {
            var msgOn = false
            var noticeOn = false

            for(channel in group.Channels) {
                if(channel.NotificationCount > 0 && channel.IsMember) {
                    msgOn = true
                }

                if(channel.ReceivedNotice && channel.IsMember) {
                    noticeOn = true
                }

                if(msgOn && noticeOn) {
                    break
                }
            }

            if(msgOn && noticeOn) {
                groupNotifications.text = "New Messages & Notice"
                groupNotificationImage.setImageResource(R.drawable.notification_on)
            } else if (noticeOn) {
                groupNotifications.text = "New Notice"
                groupNotificationImage.setImageResource(R.drawable.notification_on)
            } else if (msgOn) {
                groupNotifications.text = "New Messages"
                groupNotificationImage.setImageResource(R.drawable.notification_on)
            } else {
                groupNotifications.text = ""
                groupNotificationImage.setImageResource(R.drawable.notification_off)
            }

        }

    }

    class MessagesViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val senderTxt = itemView.sender_txt
        val messageTxt = itemView.message_txt
        val messageBox = itemView.message_txt_card
        val timeTxt = itemView.time_txt
        val senderImg = itemView.sender_image
        lateinit var Message:ChatMessage
        //val attachmentImg = itemView.attachment_image


        fun bind(message: ChatMessage) {

            Message = message


            if(message.ImagePath != "") { //IMAGE
                //TODO: Handle image

            } else {
                messageTxt.text = message.Text


            }

            if(message.Sender!!.ID == UserManager.shared.getUser().ID) {
                messageBox.setCardBackgroundColor(Color.parseColor("#ff6363"))
            } else {
                messageBox.setCardBackgroundColor(Color.parseColor("#eeeeee"))
            }

            senderImg.setImageBitmap(Message.Sender!!.getImage())
            senderTxt.text = message.Sender!!.FirstName
            timeTxt.text = " • " + DateFormat.getDateTimeInstance().format(message.CreatedDate)
        }


    }

    class NoticesViewHolder constructor(
        itemView: View
    ):  RecyclerView.ViewHolder(itemView) {

        val noticeTxt = itemView.notice_cell_notice_txt
        val noticeSenderTxt = itemView.notice_cell_sender
        val channelTitleTxt = itemView.notice_cell_channel_title

        fun bind(channel: ChatChannel) {
            noticeTxt.text = channel.Notice
            noticeSenderTxt.text = channel.NoticeSender!!.FirstName + channel.NoticeSender!!.LastName
            channelTitleTxt.text = channel.Title
        }

    }

    class JoinChannelUsersViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val userName = itemView.user_username
        val firstName = itemView.user_firstname
        val lastName = itemView.user_lastname
        val alreadyAdded = itemView.user_already_added
        val container = itemView.user_container
        val memberImg = itemView.user_image




        fun bind(user: User) {
            userName.text = user.UserName
            firstName.text = user.FirstName
            lastName.text = " " + user.LastName
            alreadyAdded.isVisible = false
            memberImg.setImageBitmap(user.getImage())

            container.setBackgroundColor(Color.TRANSPARENT)

            userName.setTextColor(Color.parseColor("#FFFFFF"))
            firstName.setTextColor(Color.parseColor("#FFFFFF"))
            lastName.setTextColor(Color.parseColor("#FFFFFF"))


            //Need logic of hiding and showing notification count Label
            //channelImage.setImageURI()

        }
    }


    class SwitchChannelsViewHolder constructor(
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

    class CreateUserViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val userName = itemView.user_username
        val firstName = itemView.user_firstname
        val lastName = itemView.user_lastname
        val alreadyAdded = itemView.user_already_added
        val memberImg = itemView.user_image
        lateinit var member: User



        fun bind(user: User) {
            userName.text = user.UserName
            firstName.text = user.FirstName
            lastName.text = " " + user.LastName
            alreadyAdded.visibility = View.GONE
            memberImg.setImageBitmap(user.getImage())

            member = user
            //Need logic of hiding and showing notification count Label
            //channelImage.setImageURI()

        }
    }

    class AddMembersViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val userName = itemView.user_username
        val firstName = itemView.user_firstname
        val lastName = itemView.user_lastname
        val alreadyAdded = itemView.user_already_added
        val userImg = itemView.user_image


        fun bind(user: User) {
            userName.text = user.UserName
            firstName.text = user.FirstName
            lastName.text = " " + user.LastName
            alreadyAdded.text = " • Added"
            userImg.setImageBitmap(user.getImage())

            //Need logic of hiding and showing notification count Label
            //channelImage.setImageURI()
            val membersActivity = itemView.context as AddMembersActivity

            if(membersActivity.userIDs.contains(user.ID)) {
                  alreadyAdded.isVisible = true
            }

        }
    }



    class JoinGroupsViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val groupTitle = itemView.search_group_title
        val groupChannels = itemView.search_group_channels
        val groupMembers = itemView.search_group_members
        val alreadyMember = itemView.search_group_already_member
        val groupImg = itemView.search_group_image
        var inGroup : Boolean = false


        fun bind(group: ChatGroup, extras: JSONObject) {


            val channelCount = extras.getInt("ChannelCount")
            val memberCount = extras.getInt("MemberCount")

            groupTitle.text = group.Title
            groupChannels.text = if (channelCount == 1) " • 1 Channel" else  " • $channelCount Channels"
            groupMembers.text = if (memberCount == 1) "1 Member" else  "$memberCount Members"
            alreadyMember.text = " • Already Member"
            groupImg.setImageBitmap(group.getImage())

            val realm: Realm = Realm.getDefaultInstance()

            val searchGroup = realm.where<ChatGroup>().equalTo("ID", group.ID).findFirst()

            if(searchGroup != null) {
                inGroup = true
                alreadyMember.visibility = View.VISIBLE
            } else {
                inGroup = false
                alreadyMember.visibility = View.INVISIBLE
            }

        }
    }

    class ViewMemberViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val userName = itemView.user_username
        val firstName = itemView.user_firstname
        val lastName = itemView.user_lastname
        val alreadyAdded = itemView.user_already_added
        val memberImg = itemView.user_image



        fun bind(user: User) {
            userName.text = user.UserName
            firstName.text = user.FirstName
            lastName.text = " " + user.LastName
            alreadyAdded.visibility = View.GONE
            memberImg.setImageBitmap(user.getImage())

        }
    }


}