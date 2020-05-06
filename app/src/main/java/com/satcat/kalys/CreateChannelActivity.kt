package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.satcat.kalys.Models.User
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_create_channel.*
import kotlinx.android.synthetic.main.activity_create_group.*

class CreateChannelActivity : AppCompatActivity() {

    lateinit var membersAdapter : RecyclerAdapter

    var members: List<User> = emptyList()

    lateinit var activeGroup : ChatGroup
    val newChannel : ChatChannel = ChatChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_channel)

        supportActionBar!!.title = "Create Channel"


        val realm = Realm.getDefaultInstance()

        activeGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("groupID")).findFirst()!!

        val addMemberBtn: Button = findViewById(R.id.create_channel_add_members_btn)
        addMemberBtn.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            AddMembersActivity.editedChannel = newChannel
            AddMembersActivity.fromOptions = false
            AddMembersActivity.searchGroupOnly = true
            AddMembersActivity.associatedGroup = activeGroup
            startActivity(intent)

            //TODO: When navigating to AddMembersActvity send info for fromOptions and searchChannelOnly & associatedgroup in intent

        }

        val createGroupBtn : Button = findViewById(R.id.create_channel_create_btn)
        createGroupBtn.setOnClickListener {
            makeChannel()
            finish()
        }

        val visibilityStatus = create_channel_visibility_status
        val visibilityDetails = create_channel_visibility_info
        val visibilitySwitch : Switch = findViewById(R.id.create_channel_visibility_switch)

        visibilitySwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.isChecked) {
                visibilityStatus.text = "Public"
                visibilityDetails.text = "Channel Visibility • Everyone"
            } else {
                visibilityStatus.text = "Private"
                visibilityDetails.text = "Channel Visibility • Channel Members"
            }

        })

        initRecyclerView(findViewById(R.id.recycler_create_channel_users))

    }

    override fun onStart() {
        super.onStart()

        members = newChannel.Members.toList()
        membersAdapter.submitList(members)
        membersAdapter.notifyDataSetChanged()

        val memberCount = members.count()

        val membersLbl : TextView = findViewById(R.id.create_channel_members_count)
        membersLbl.text = "Members • " + memberCount.toString()
        if(memberCount > 0) {
            val removeLbl : TextView = findViewById(R.id.create_channel_members_remove_text)
            removeLbl.isVisible = true
        }

        val headerLbl : TextView = findViewById(R.id.create_channel_header_info)
        headerLbl.text = "The new Channel will be added to " + activeGroup.Title
    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            membersAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.CREATEUSERS) //give string for channel
            //can use decorator
            adapter = membersAdapter

        }

    }

    private fun makeChannel() {

        val realm = Realm.getDefaultInstance()


        val titleTxt = create_channel_name_field
        val noticeTxt = create_channel_notice_field
        val privacySwitch = create_channel_visibility_switch

        newChannel.Title = titleTxt.text.toString()
        newChannel.Notice = if(noticeTxt.text.isBlank()) "" else noticeTxt.text.toString()
        newChannel.IsPrivate = !privacySwitch.isChecked
        newChannel.NoticeSender = UserManager.shared.getUser()
        newChannel.GroupID = activeGroup.ID

        realm.executeTransaction {
            activeGroup.Channels.add(newChannel) //newChannel is also added to Realm
        }

        //TODO: Check for correct inputs


        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.createChannel(newChannel.giveJson())
        } else {
            realm.executeTransaction {
                newChannel.Networked = false
            }
        }

        //Add self after sending
        realm.executeTransaction {
            newChannel.Members.add(UserManager.shared.getUser())
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

}