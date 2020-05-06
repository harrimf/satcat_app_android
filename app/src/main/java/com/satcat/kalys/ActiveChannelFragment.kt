package com.satcat.kalys

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatMessage
import io.realm.Realm
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class ActiveChannelFragment : Fragment()
{

    lateinit var messageAdapter : RecyclerAdapter
    private lateinit var switchChannelAdapter : RecyclerAdapter

    var messages: List<ChatMessage> = emptyList()

    var channels : List<ChatChannel> = emptyList()

    var noticeActive = false
    var checkedHeight = false

    var defaultMessageBoxHeight = 0

    lateinit var constraintLayout : ConstraintLayout
    lateinit var background : View

    lateinit var messageRecyclerView: RecyclerView
    lateinit var switchChannelRecyclerView: RecyclerView


    lateinit var openNotice : ImageView
    lateinit var newNoticeTxt : TextView
    lateinit var messageTxt : EditText
    lateinit var sendBtn : Button

    lateinit var switchChannelBtn : Button
    lateinit var switchRecyclerBg : CardView
    lateinit var hideSwitchChannelBtn : Button


    lateinit var noticeContainer : View
    lateinit var noticeTitle: TextView
    lateinit var noticeText : TextView
    lateinit var noticeTapToEdit : TextView
    lateinit var noticeSenderImg : ImageView
    lateinit var noticeSenderImgCard : CardView
    lateinit var noticeSenderText : TextView
    lateinit var noticeClose : ImageView

    lateinit var updateNoticeInfoBg : CardView
    lateinit var updateNoticeInfoTxt : TextView
    lateinit var updateNoticeBg : CardView
    lateinit var updateNoticeTxt : TextView
    lateinit var updateNoticeBtn : Button
    lateinit var updateNoticeCancelBtn : Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_active_channel, container, false)

        setHasOptionsMenu(true)

        constraintLayout = view!!.findViewById(R.id.active_channel_layout)

        background = view!!.findViewById(R.id.active_channel_background)

        messageRecyclerView = view!!.findViewById(R.id.recycler_messages)
        switchChannelRecyclerView = view!!.findViewById(R.id.recycler_switch_channels)

        openNotice = view!!.findViewById(R.id.active_channel_open_notice)
        newNoticeTxt = view!!.findViewById(R.id.active_channel_new_notice_text)
        messageTxt = view!!.findViewById(R.id.active_channel_message_field)
        sendBtn = view!!.findViewById(R.id.active_channel_send_btn)

        switchChannelBtn = view!!.findViewById(R.id.active_channel_switch_btn)
        switchRecyclerBg = view!!.findViewById(R.id.active_channel_switch_recycler_bg)
        hideSwitchChannelBtn = view!!.findViewById(R.id.active_channel_hide_switch)

        noticeContainer = view!!.findViewById(R.id.active_channel_notice_background)
        noticeTitle = view!!.findViewById(R.id.active_channel_notice_title)
        noticeText = view!!.findViewById(R.id.active_channel_notice)
        noticeTapToEdit = view!!.findViewById(R.id.active_channel_notice_tap_to_edit)
        noticeSenderImg = view!!.findViewById(R.id.active_channel_notice_sender_img)
        noticeSenderImgCard = view!!.findViewById(R.id.active_channel_notice_sender_img_card)
        noticeSenderText = view!!.findViewById(R.id.active_channel_notice_sender)
        noticeClose = view!!.findViewById(R.id.active_channel_close_img)

        updateNoticeInfoBg = view!!.findViewById(R.id.active_channel_edit_notice_info_bg)
        updateNoticeInfoTxt = view!!.findViewById(R.id.active_channel_edit_notice_info)
        updateNoticeBg = view!!.findViewById(R.id.active_channel_edit_notice_field_bg)
        updateNoticeTxt = view!!.findViewById(R.id.active_channel_edit_notice_field)
        updateNoticeBtn = view!!.findViewById(R.id.active_channel_edit_notice_update_btn)
        updateNoticeCancelBtn = view!!.findViewById(R.id.active_channel_edit_notice_cancel_btn)

        defaultMessageBoxHeight = messageTxt.lineHeight

        messageTxt.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) { //got focus
            } else { //lost focus
                Log.d("myTag", "LOST FOCUS");
            }
        })

        messageTxt.addTextChangedListener(object : TextWatcher {


            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
                if(!checkedHeight) {
                    defaultMessageBoxHeight = messageTxt.height
                    checkedHeight = true
                }

            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {


                if(messageTxt.text.isNotBlank()) {
                    sendButtonVisible(true)

                    messageRecyclerView.setPadding(messageRecyclerView.paddingLeft, messageRecyclerView.paddingTop, messageRecyclerView.paddingRight, messageTxt.height - defaultMessageBoxHeight)

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.recycler_messages, ConstraintSet.BOTTOM, R.id.active_channel_send_btn, ConstraintSet.TOP, 0)
                    constraintSet.connect(R.id.active_channel_message_field, ConstraintSet.RIGHT, R.id.active_channel_send_btn, ConstraintSet.LEFT, 0)
                    constraintSet.applyTo(constraintLayout)
                } else {
                    sendButtonVisible(false)

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(constraintLayout)
                    constraintSet.connect(R.id.recycler_messages, ConstraintSet.BOTTOM, R.id.active_channel_switch_btn, ConstraintSet.TOP, 0)
                    constraintSet.connect(R.id.active_channel_message_field, ConstraintSet.RIGHT, R.id.active_channel_switch_btn, ConstraintSet.LEFT, 0)
                    constraintSet.applyTo(constraintLayout)
                }


            }
        })

        openNotice.setOnClickListener {
            openNotice()
        }

        sendBtn.setOnClickListener {
            createMessage()
            messageTxt.text.clear()
        }

        noticeClose.setOnClickListener {
            closeNotice()
        }

        noticeText.setOnClickListener {
            updateNoticeVisible(true)
        }

        updateNoticeBtn.setOnClickListener {
            updateNotice()
            updateNoticeVisible(false)
        }

        updateNoticeCancelBtn.setOnClickListener {
            updateNoticeVisible(false)
        }

        switchChannelBtn.setOnClickListener {
            switchChannelVisible(true)
        }

        hideSwitchChannelBtn.setOnClickListener {
            switchChannelVisible(false)
        }


        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        //update noticeTXt, noticesnderlBL, updateNoticetxt, updatenoticelbl
        //update actionbar settings btn (maybe ios specific)
        val parent = this.activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()

        setupNoticeText()


        messages = parent.activeChannel.Messages
        channels = realm.where<ChatChannel>().equalTo("IsMember", true).equalTo("GroupID", parent.activeGroup.ID ).findAll().toList()

        initRecyclerView(messageRecyclerView)
        messageAdapter.submitList(messages)
        messageAdapter.notifyDataSetChanged()

        initChannelRecyclerView(switchChannelRecyclerView)
        switchChannelAdapter.submitList(channels)
        switchChannelAdapter.notifyDataSetChanged()


    }

    fun fragmentIsActive() {
        val parent = this.activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()

        setupNoticeText()

        realm.executeTransaction {
            parent.activeChannel.NotificationCount = 0
        }

        messages = parent.activeChannel.Messages.toList()
        messageAdapter.submitList(messages)
        messageAdapter.notifyDataSetChanged()

        channels = realm.where<ChatChannel>().equalTo("IsMember", true).equalTo("GroupID",parent.activeGroup.ID ).findAll().toList()
        switchChannelAdapter.submitList(channels)
        switchChannelAdapter.notifyDataSetChanged()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.channel_settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val parent = activity as GroupTabActivity

        if(id == R.id.menu_channel_settings_btn) {
            val intent = Intent(activity, OptionsChannelActivity::class.java)
            intent.putExtra("groupID", parent.activeGroup.ID)
            intent.putExtra("channelID", parent.activeChannel.ID)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if(newConfig.keyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {

        } else if (newConfig.keyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            messageRecyclerView.setPadding(messageRecyclerView.paddingLeft, messageRecyclerView.paddingTop, messageRecyclerView.paddingRight, 0)
        }
    }


    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            messageAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.MESSAGES) //give string for group
            //can use decorator
            adapter = messageAdapter
        }
    }

    private fun initChannelRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            switchChannelAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.CHANNELSWITCH) //give string for group
            //can use decorator
            adapter = switchChannelAdapter
        }

    }

    fun setupNoticeText() {
        val parent = this.activity as GroupTabActivity

        newNoticeTxt.isVisible = parent.activeChannel.ReceivedNotice
        messageTxt.hint = "Chat in " + parent.activeChannel.Title

        noticeText.text = if(parent.activeChannel.Notice != "") parent.activeChannel.Notice else "Notice"
        noticeSenderText.text = parent.activeChannel.NoticeSender!!.FirstName

        noticeSenderImg.setImageBitmap(parent.activeChannel.NoticeSender!!.getImage())

        updateNoticeInfoTxt.text = "Update the notice for " + parent.activeChannel.Title
        updateNoticeTxt.text = parent.activeChannel.Notice


    }


    fun openNotice() {
        val parent = activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()


        noticeActive = true

        if(parent.activeChannel.ReceivedNotice) {
            realm.executeTransaction {
                parent.activeChannel.ReceivedNotice = false
            }

            channels = realm.where<ChatChannel>().equalTo("IsMember", true).equalTo("GroupID",parent.activeGroup.ID ).findAll().toList()
            switchChannelAdapter.submitList(channels)
            switchChannelAdapter.notifyDataSetChanged()

        }

        noticeVisible(true)
    }

    fun closeNotice() {
        noticeActive = false

        noticeVisible(false)
    }

    fun noticeVisible(visible: Boolean) {
        noticeContainer.isVisible = visible
        noticeTitle.isVisible = visible
        noticeText.isVisible = visible
        noticeTapToEdit.isVisible = visible
        noticeSenderImg.isVisible = visible
        noticeSenderImgCard.isVisible = visible
        noticeSenderText.isVisible = visible
        noticeClose.isVisible = visible

        openNotice.isVisible = !visible

    }

    fun updateNoticeVisible(visible : Boolean) {
        background.isVisible = visible
        updateNoticeInfoBg.isVisible = visible
        updateNoticeInfoTxt.isVisible = visible
        updateNoticeBg.isVisible = visible
        updateNoticeTxt.isVisible = visible
        updateNoticeBtn.isVisible = visible
        updateNoticeCancelBtn.isVisible = visible

    }

    fun switchChannelVisible(visible: Boolean) {
        background.isVisible = visible
        switchChannelRecyclerView.isVisible = visible
        switchRecyclerBg.isVisible = visible
        hideSwitchChannelBtn.isVisible = visible
    }

    fun sendButtonVisible(visible: Boolean) {
        sendBtn.isVisible = visible
        switchChannelBtn.isVisible = !visible

    }

    fun createMessage() {
        val parent = this.activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()


        val newMessage = ChatMessage()
        newMessage.Sender = UserManager.shared.getUser()
        newMessage.ChannelID = parent.activeChannel.ID
        newMessage.Text = messageTxt.text.trim().toString()

        realm.executeTransaction {
            realm.insertOrUpdate(newMessage)
            parent.activeChannel.Messages.add(newMessage)
        }

        messages = parent.activeChannel.Messages.toList()
        messageAdapter.submitList(messages)
        messageAdapter.notifyDataSetChanged()

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.sendMessage(newMessage.giveJson())
        } else {
            realm.executeTransaction {
                newMessage.Networked = false
            }
        }

    }

    fun updateNotice() {
        val parent = this.activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()

        val notice = updateNoticeTxt.text.trim().toString()

        realm.executeTransaction {
            parent.activeChannel.Notice = notice
            parent.activeChannel.NoticeSender = UserManager.shared.getUser()
        }

        noticeText.text = notice
        noticeSenderText.text = UserManager.shared.getUser().FirstName
        //TODO: Update sender image

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateChannel(parent.activeChannel.giveJson())
        } else {
            realm.executeTransaction {
                parent.activeChannel.Updated = false
            }
        }
    }

    fun handleReceiveChannel() {

        val parent = this.activity as GroupTabActivity

        val realm = Realm.getDefaultInstance()

        if(!noticeActive && parent.activeChannel.ReceivedNotice) {
            //hide newNoticeLbl
            newNoticeTxt.isVisible = true
        } else {
            val realm: Realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                parent.activeChannel.ReceivedNotice = false
            }
        }

        noticeText.text = parent.activeChannel.Notice
        noticeSenderText.text = parent.activeChannel.NoticeSender!!.FirstName

        updateNoticeInfoTxt.text = "Update the notice for " + parent.activeChannel.Title
        updateNoticeTxt.text = parent.activeChannel.Notice

        channels = realm.where<ChatChannel>().equalTo("IsMember", true).equalTo("GroupID",parent.activeGroup.ID ).findAll().toList()
        switchChannelAdapter.submitList(channels)
        switchChannelAdapter.notifyDataSetChanged()

    }

    fun handleReceiveMessage() {
        val realm: Realm = Realm.getDefaultInstance()

        val parent = this.activity as GroupTabActivity

        if(!parent.showGroupIsActive) {
            realm.executeTransaction {
                parent.activeChannel.NotificationCount = 0
            }
        }

        messages = parent.activeChannel.Messages.toList()
        messageAdapter.submitList(messages)
        messageAdapter.notifyDataSetChanged()

        channels = realm.where<ChatChannel>().equalTo("IsMember", true).equalTo("GroupID",parent.activeGroup.ID ).findAll().toList()
        switchChannelAdapter.submitList(channels)
        switchChannelAdapter.notifyDataSetChanged()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveChannelEvent(event: SocketIOManager.ReceiveChannelEvent): Unit {
        handleReceiveChannel()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMessageEvent(event: SocketIOManager.ReceiveMessageEvent): Unit {
        handleReceiveMessage()

    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}