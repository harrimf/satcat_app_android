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
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_options_group.*

class OptionsGroupActivity : AppCompatActivity() {

    lateinit var  activeGroup : ChatGroup
    lateinit var activeChannel : ChatChannel

    lateinit var membersAdapter : RecyclerAdapter


    lateinit var titleContainer : RelativeLayout
    lateinit var imageContainer : RelativeLayout

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

    lateinit var imageInfo : TextView
    lateinit var imageBtn : ImageButton
    lateinit var imageCancel : Button
    lateinit var imageUpdate : Button
    lateinit var imageTapToEdit : TextView


    lateinit var titleView : TextView
    lateinit var imageView : TextView

    lateinit var editTitleInfo : TextView
    lateinit var editTitleTxt : EditText
    
    lateinit var viewMembersInfo : TextView

    lateinit var addMembersBtn : Button
    lateinit var viewMembersBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_group)

        titleContainer = options_group_title_container
        imageContainer = options_group_image_container


        visibilitySwitch = options_group_visibility_switch
        visibilityDetails = options_group_visibility_info
        visibilityStatus = options_group_visibility_status

        background = options_group_background

        membersInfo =  options_group_view_members_info
        membersRecycler = recycler_options_group_view_members
        membersExitBtn = options_group_view_members_exit_btn

        titleInfo = options_group_edit_title_info
        titleField = options_group_edit_title_field
        titleCancel = options_group_edit_title_cancel_btn
        titleUpdate = options_group_edit_title_update_btn

        imageInfo = options_group_edit_image_info
        imageBtn = options_group_edit_image
        imageCancel = options_group_edit_image_cancel_btn
        imageUpdate = options_group_edit_image_update_btn
        imageTapToEdit = options_group_edit_image_tap_to_edit

        addMembersBtn = options_group_add_members_btn

        viewMembersBtn = options_group_view_members_btn



        addMembersBtn.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            AddMembersActivity.editedChannel = activeChannel
            AddMembersActivity.fromOptions = true
            AddMembersActivity.searchGroupOnly = false
            startActivity(intent)
        }

        viewMembersBtn.setOnClickListener {
            viewMembersVisible(true)
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

        imageContainer.setOnClickListener {
            imageEditVisible(true)
        }

        imageUpdate.setOnClickListener {
            updateGroupImage()
            imageEditVisible(false)
        }
        
        imageCancel.setOnClickListener {
            imageEditVisible(false)
        }

        visibilitySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            updateGroupVisibility()
            visibilityChangeText(buttonView.isChecked)
        }

        initRecyclerView(membersRecycler)
    }

    override fun onStart() {
        super.onStart()

        titleView = options_group_title
        imageView = options_group_image_name

        editTitleInfo = options_group_edit_title_info
        editTitleTxt = options_group_edit_title_field
        
        viewMembersInfo = options_group_view_members_info
        

        val realm : Realm = Realm.getDefaultInstance()
        activeGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("groupID")).findFirst()!!
        activeChannel = realm.where<ChatChannel>().equalTo("ID", intent.getStringExtra("channelID")).findFirst()!!

        titleView.text = activeGroup.Title
        imageView.text = activeGroup.Title + " Image"

        visibilityChangeText(!activeGroup.IsPrivate)

        editTitleInfo.text = "Update the title of " + activeGroup.Title
        editTitleTxt.hint = activeGroup.Title
        viewMembersInfo.text = activeGroup.Title + " Members"


        val list  = activeGroup.Channels/*filter { channel -> channel.IsMain }*/.first()!!.Members

        membersAdapter.submitList(list) //TODO: Handle 2 lists for recycler
        membersAdapter.notifyDataSetChanged()

    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            membersAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.VIEWMEMBERS) //give string for group
            //can use decorator
            adapter = membersAdapter

        }

    }

    fun updateTitle() {
        val realm : Realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            activeGroup.Title = editTitleTxt.text.toString()
            activeGroup.Channels.first()!!.Title = activeGroup.Title + " Channel"
        }

        titleView.text = activeGroup.Title
        imageView.text = activeGroup.Title + " Image"
        editTitleInfo.text = "Update the title of " + activeGroup.Title
        editTitleTxt.hint = activeGroup.Title
        viewMembersInfo.text = activeGroup.Title + " Members"

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateGroup(activeGroup.giveJson())
        } else {
            realm.executeTransaction {
                activeGroup.Updated = true
                activeGroup.Channels.first()!!.Updated = false
            }
        }
    }

    fun updateGroupImage() {
        //TODO: implement editing of image
        //TODO: add toast for confirmation
        
    }

    fun updateGroupVisibility() {
        val realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            activeGroup.IsPrivate = !visibilitySwitch.isChecked
        }

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateGroup(activeGroup.giveJson())
            val toast = Toast.makeText(this, "Updated the group's visibility", Toast.LENGTH_SHORT)
            toast.show()
        } else {
            realm.executeTransaction {
                activeGroup.Updated = false
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

    fun imageEditVisible(visible: Boolean) {
        background.isVisible = visible
        imageInfo.isVisible = visible
        imageBtn.isVisible = visible
        imageCancel.isVisible = visible
        imageUpdate.isVisible = visible
        imageTapToEdit.isVisible = visible
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
            visibilityDetails.text = "Group Visibility • Everyone"
        } else {
            visibilityStatus.text = "Private"
            visibilityDetails.text = "Group Visibility • Group Members"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}