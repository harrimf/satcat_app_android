package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_create_group.*


class CreateGroupActivity : AppCompatActivity() {

    lateinit var membersAdapter : RecyclerAdapter

    lateinit var group : ChatGroup
    var mainChannel : ChatChannel = ChatChannel()

    var members: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        val addMemberBtn: Button = findViewById(R.id.create_group_add_members_btn)
        addMemberBtn.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            AddMembersActivity.editedChannel = mainChannel
            AddMembersActivity.fromOptions = false
            AddMembersActivity.searchGroupOnly = false
            startActivity(intent)
        }

        val createGroupBtn : Button = findViewById(R.id.create_group_create_btn)
        createGroupBtn.setOnClickListener {
            makeGroup()
            finish()
        }

        val visibilityStatus = create_group_visibility_status
        val visibilityDetails = create_group_visibility_info
        val visibilitySwitch : Switch = findViewById(R.id.create_group_visibility_switch)

        visibilitySwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.isChecked) {
                visibilityStatus.text = "Public"
                visibilityDetails.text = "Group Visibility • Everyone"
            } else {
                visibilityStatus.text = "Private"
                visibilityDetails.text = "Group Visibility • Group Members"
            }

        })

        initRecyclerView(findViewById(R.id.recycler_create_users))

    }

    override fun onStart() {
        super.onStart()

        members = mainChannel.Members.toList()
        membersAdapter.submitList(members)
        membersAdapter.notifyDataSetChanged()

        val memberCount = members.count()

        val membersLbl : TextView = findViewById(R.id.create_group_members_count)
        membersLbl.text = "Members • " + memberCount.toString()
        if(memberCount > 0) {
            val removeLbl : TextView = findViewById(R.id.create_group_members_remove_text)
            removeLbl.isVisible = true
        }
    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            membersAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.CREATEUSERS) //give string for group
            //can use decorator
            adapter = membersAdapter

        }

    }

    private fun makeGroup() {

        val realm : Realm = Realm.getDefaultInstance()


        val titleTxt = create_group_name_field
        val noticeTxt = create_group_notice_field
        val privacySwitch = create_group_visibility_switch


        group = ChatGroup()
        group.Title = titleTxt.text.toString()
        group.Notice = if(noticeTxt.text.isBlank()) "" else noticeTxt.text.toString()
        group.IsPrivate = !privacySwitch.isChecked
        group.Channels.add(mainChannel)

        //TODO: handle group Image

        mainChannel.Title = titleTxt.text.toString() + " Channel"
        mainChannel.Notice = if(noticeTxt.text.isBlank()) "" else noticeTxt.text.toString()
        mainChannel.IsPrivate = false
        mainChannel.NoticeSender = UserManager.shared.getUser()
        mainChannel.IsMain = true
        mainChannel.GroupID = group.ID

        //TODO: Check for correct inputs


        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.createGroup(group.giveJson())
        } else {
            realm.executeTransaction {
                mainChannel.Networked = false
                group.Networked = false
            }
        }

        //Add self after sending
        realm.executeTransaction {
            group.Channels.first()!!.Members.add(UserManager.shared.getUser())
            realm.insertOrUpdate(group)
        }

    }

}