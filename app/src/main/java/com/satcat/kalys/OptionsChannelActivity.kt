package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_options_channel.*

class OptionsChannelActivity : AppCompatActivity() {

    lateinit var  activeGroup : ChatGroup
    lateinit var activeChannel : ChatChannel


    lateinit var membersAdapter : RecyclerAdapter


    lateinit var titleContainer : RelativeLayout

    lateinit var visibilitySwitch : Switch
    lateinit var visibilityDetails : TextView
    lateinit var visibilityStatus : TextView

    lateinit var background : View

    lateinit var membersInfo : TextView
    lateinit var membersRecycler : RecyclerView
    lateinit var membersExitBtn : Button

    lateinit var titleInfo : TextView
    lateinit var titleField : EditText
    lateinit var titleCancel : Button
    lateinit var titleUpdate : Button

    lateinit var titleView : TextView

    lateinit var editTitleInfo : TextView
    lateinit var editTitleTxt : EditText

    lateinit var viewMembersInfo : TextView

    lateinit var addMembersBtn : Button
    lateinit var viewMembersBtn : Button
    lateinit var leaveChannelBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_channel)

        titleContainer = options_channel_title_container

        visibilitySwitch = options_channel_visibility_switch
        visibilityDetails = options_channel_visibility_info
        visibilityStatus = options_channel_visibility_status

        background = options_channel_background

        membersInfo =  options_channel_view_members_info
        membersRecycler = recycler_options_channel_view_members
        membersExitBtn = options_channel_view_members_exit_btn

        titleInfo = options_channel_edit_title_info
        titleField = options_channel_edit_title_field
        titleCancel = options_channel_edit_title_cancel_btn
        titleUpdate = options_channel_edit_title_update_btn

        addMembersBtn = options_channel_add_members_btn
        viewMembersBtn = options_channel_view_members_btn
        leaveChannelBtn = options_channel_leave_channel

        addMembersBtn.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            AddMembersActivity.editedChannel = activeChannel
            if(!activeChannel.IsMain) {
                AddMembersActivity.searchGroupOnly = true
                AddMembersActivity.associatedGroup = activeGroup
            } else {
                AddMembersActivity.searchGroupOnly = false
            }
            AddMembersActivity.fromOptions = true
            startActivity(intent)
        }


        viewMembersBtn.setOnClickListener {
            viewMembersVisible(true)
        }

        leaveChannelBtn.setOnClickListener {
            exitChannel()
            finish()
        }

        membersExitBtn.setOnClickListener {
            viewMembersVisible(false)
        }

        titleContainer.setOnClickListener {
            titleEditVisible(true)
        }

        titleCancel.setOnClickListener {
            titleEditVisible(false)
        }

        titleUpdate.setOnClickListener {
            updateTitle()
            titleEditVisible(false)
        }

        visibilitySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            updateChannelVisibility()
            visibilityChangeText(buttonView.isChecked)
        }


        initRecyclerView(membersRecycler)

    }

    override fun onStart() {
        super.onStart()

        titleView = options_channel_title

        editTitleInfo = options_channel_edit_title_info
        editTitleTxt = options_channel_edit_title_field

        viewMembersInfo = options_channel_view_members_info

        val realm : Realm = Realm.getDefaultInstance()
        activeGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("groupID")).findFirst()!!
        activeChannel = realm.where<ChatChannel>().equalTo("ID", intent.getStringExtra("channelID")).findFirst()!!


        titleView.text = activeChannel.Title

        visibilityChangeText(!activeChannel.IsPrivate)

        editTitleInfo.text = "Update the title of " + activeChannel.Title
        editTitleTxt.hint = activeChannel.Title
        viewMembersInfo.text = activeChannel.Title + " Members"

        if(activeChannel.IsMain) {
            visibilitySwitch.isEnabled = false
        }


        val list  = activeChannel.Members

        membersAdapter.submitList(list) //TODO: Handle 2 lists for recycler
        membersAdapter.notifyDataSetChanged()

    }

    fun updateTitle() {
        val realm : Realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            activeChannel.Title = editTitleTxt.text.toString()
        }

        titleView.text = activeChannel.Title
        editTitleInfo.text = "Update the title of " + activeChannel.Title
        editTitleTxt.hint = activeChannel.Title
        viewMembersInfo.text = activeChannel.Title + " Members"

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateChannel(activeChannel.giveJson())
        } else {
            realm.executeTransaction {
                activeChannel.Updated = true
            }
        }
    }

    fun updateChannelVisibility() {
        val realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            activeChannel.IsPrivate = !visibilitySwitch.isChecked
        }

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateChannel(activeChannel.giveJson())

            val toast = Toast.makeText(this, "Update the channel's visibility", Toast.LENGTH_SHORT)
            toast.show()

        } else {
            realm.executeTransaction {
                activeChannel.Updated = false
            }
        }
    }

    fun exitChannel() {
        if(!activeChannel.IsMain) {
            val realm: Realm = Realm.getDefaultInstance()

            if(SocketIOManager.shared.socketConnected()) {
                SocketIOManager.shared.deleteChannel(activeChannel.giveJson())
            } else {
                realm.executeTransaction {
                    activeChannel.Removable = false
                }
            }

            var i = 0
            var index = -1

            for (member in activeChannel.Members) {
                if(member.ID == UserManager.shared.getUser().ID) {
                    index = i
                }
                i += 1
            }

            if (index != -1) {
                realm.executeTransaction {
                    activeChannel.IsMember = false
                    activeChannel.Members.removeAt(index)
                }
            }
        }
    }

    fun titleEditVisible(visible: Boolean) {
        background.isVisible = visible
        titleInfo.isVisible = visible
        titleField.isVisible = visible
        titleCancel.isVisible = visible
        titleUpdate.isVisible = visible
    }


    fun viewMembersVisible(visible: Boolean) {
        background.isVisible = visible
        membersInfo.isVisible = visible
        membersRecycler.isVisible = visible
        membersExitBtn.isVisible = visible
    }

    fun visibilityChangeText(public: Boolean) {
        if(public) {
            visibilityStatus.text = "Public"
            visibilityDetails.text = "Channel Visibility • Everyone"
        } else {
            visibilityStatus.text = "Private"
            visibilityDetails.text = "Channel Visibility • Channel Members"
        }
    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            membersAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.VIEWMEMBERS) //give string for channel options
            //can use decorator
            adapter = membersAdapter

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}