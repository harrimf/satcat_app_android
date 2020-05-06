package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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

    lateinit var activeGroup : ChatGroup
    lateinit var activeChannel : ChatChannel


    lateinit var membersAdapter : RecyclerAdapter

    lateinit var titleContainer : View

    lateinit var visibilitySwitch : Switch
    lateinit var visibilityDetails : TextView
    lateinit var visibilityStatus : TextView

    lateinit var background : View

    lateinit var membersInfoBg : CardView
    lateinit var membersInfo : TextView
    lateinit var membersRecyclerBg : CardView
    lateinit var membersRecycler : RecyclerView
    lateinit var membersExitBtn : Button


    lateinit var titleInfoBg : CardView
    lateinit var titleInfo : TextView
    lateinit var titleFieldbg : CardView
    lateinit var titleField : EditText
    lateinit var titleCancel : Button
    lateinit var titleUpdate : Button

    lateinit var titleView : TextView


    lateinit var addMembersBtn : Button
    lateinit var viewMembersBtn : Button
    lateinit var leaveChannelBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_channel)

        supportActionBar!!.title = "Channel Options"


        titleContainer = options_channel_title_container

        titleView = options_channel_title


        visibilitySwitch = options_channel_visibility_switch
        visibilityDetails = options_channel_visibility_info
        visibilityStatus = options_channel_visibility_status

        background = options_channel_background

        membersInfoBg = options_channel_view_members_info_bg
        membersRecyclerBg = options_channel_view_members_recycler_bg
        membersInfo =  options_channel_view_members_info
        membersRecycler = recycler_options_channel_view_members
        membersExitBtn = options_channel_view_members_exit_btn

        titleInfoBg = options_channel_edit_title_info_bg
        titleInfo = options_channel_edit_title_info
        titleFieldbg = options_channel_edit_title_field_bg
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


        initRecyclerView(membersRecycler)

    }

    override fun onStart() {
        super.onStart()


        val realm : Realm = Realm.getDefaultInstance()
        activeGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("groupID")).findFirst()!!
        activeChannel = realm.where<ChatChannel>().equalTo("ID", intent.getStringExtra("channelID")).findFirst()!!


        titleView.text = activeChannel.Title

        visibilitySwitch.isChecked = !activeChannel.IsPrivate
        visibilityChangeText(!activeChannel.IsPrivate)

        visibilitySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            updateChannelVisibility()
            visibilityChangeText(isChecked)
        }


        titleInfo.text = "Update the title of " + activeChannel.Title
        titleField.hint = activeChannel.Title
        membersInfo.text = activeChannel.Title + " Members"

        visibilitySwitch.isVisible = !activeChannel.IsMain

        val list  = activeChannel.Members

        membersAdapter.submitList(list) //TODO: Handle 2 lists for recycler
        membersAdapter.notifyDataSetChanged()

    }

    override fun onStop() {
        visibilitySwitch.setOnCheckedChangeListener(null)
        super.onStop()
    }

    fun updateTitle() {
        val realm : Realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            activeChannel.Title = titleField.text.toString()
        }

        titleView.text = activeChannel.Title
        titleInfo.text = "Update the title of " + activeChannel.Title
        titleField.hint = activeChannel.Title
        membersInfo.text = activeChannel.Title + " Members"

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
        titleInfoBg.isVisible = visible
        titleInfo.isVisible = visible
        titleFieldbg.isVisible = visible
        titleField.isVisible = visible
        titleCancel.isVisible = visible
        titleUpdate.isVisible = visible
    }


    fun viewMembersVisible(visible: Boolean) {
        background.isVisible = visible
        membersInfoBg.isVisible = visible
        membersInfo.isVisible = visible
        membersRecycler.isVisible = visible
        membersRecyclerBg.isVisible = visible
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