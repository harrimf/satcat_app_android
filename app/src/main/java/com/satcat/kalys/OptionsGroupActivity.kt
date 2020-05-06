package com.satcat.kalys

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.ImageStorageManager
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


    lateinit var titleContainer : View
    lateinit var imageContainer : View

    lateinit var visibilitySwitch : Switch
    lateinit var visibilityDetails : TextView
    lateinit var visibilityStatus : TextView

    lateinit var background : View

    lateinit var membersInfoBg : CardView
    lateinit var membersRecyclerBg : CardView
    lateinit var membersInfo : TextView
    lateinit var membersRecycler : RecyclerView
    lateinit var membersExitBtn : Button

    lateinit var titleInfoBg: CardView
    lateinit var titleFieldBg : CardView
    lateinit var titleInfo : TextView
    lateinit var titleField : EditText
    lateinit var titleCancel : Button
    lateinit var titleUpdate : Button

    lateinit var imageInfo : TextView
    lateinit var imageGroup : ImageView
    lateinit var imageGroupCard : CardView
    lateinit var imageCancel : Button
    lateinit var imageUpdate : Button
    lateinit var imageTapToEdit : TextView


    lateinit var titleView : TextView
    lateinit var imageView : TextView
    lateinit var previewImage : ImageView
    lateinit var previewImageCard : CardView

    lateinit var addMembersBtn : Button
    lateinit var viewMembersBtn : Button

    var fromImageSelect = false

    companion object {
        private val REQUEST_TAKE_PHOTO = 0
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options_group)

        supportActionBar!!.title = "Group Options"


        titleContainer = options_group_title_container
        imageContainer = options_group_image_container

        titleView = options_group_title
        imageView = options_group_image_name
        previewImage = options_group_image
        previewImageCard = options_group_edit_image_card

        visibilitySwitch = options_group_visibility_switch
        visibilityDetails = options_group_visibility_info
        visibilityStatus = options_group_visibility_status

        background = options_group_background

        membersInfoBg = options_group_view_members_info_bg
        membersRecyclerBg = options_group_view_members_recycler_bg
        membersInfo =  options_group_view_members_info
        membersRecycler = recycler_options_group_view_members
        membersExitBtn = options_group_view_members_exit_btn

        titleInfoBg = options_group_edit_title_info_bg
        titleFieldBg = options_group_edit_title_field_bg
        titleInfo = options_group_edit_title_info
        titleField = options_group_edit_title_field
        titleCancel = options_group_edit_title_cancel_btn
        titleUpdate = options_group_edit_title_update_btn

        imageInfo = options_group_edit_image_info
        imageGroup = options_group_edit_image
        imageGroupCard = options_group_edit_image_card
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

        imageGroup.setOnClickListener { buttonView ->
            val popupMenu = PopupMenu(this,imageGroup)
            popupMenu.menuInflater.inflate(R.menu.image_select_menu,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when(item.itemId) {
                    R.id.gallery_item ->
                        selectImageInAlbum()
                    R.id.camera_item ->
                        takePhoto()
                }
                true
            })
            popupMenu.show()
        }

        imageUpdate.setOnClickListener {
            updateGroupImage()
            imageEditVisible(false)
        }
        
        imageCancel.setOnClickListener {
            imageEditVisible(false)
        }


        initRecyclerView(membersRecycler)
    }

    override fun onStart() {
        super.onStart()

        if(!fromImageSelect) {

            val realm: Realm = Realm.getDefaultInstance()
            activeGroup = realm.where<ChatGroup>().equalTo(
                "ID",
                intent.getStringExtra("groupID")
            ).findFirst()!!
            activeChannel = realm.where<ChatChannel>().equalTo(
                "ID",
                intent.getStringExtra("channelID")
            ).findFirst()!!

            titleView.text = activeGroup.Title
            imageView.text = activeGroup.Title + " Image"

            visibilitySwitch.isChecked = !activeGroup.IsPrivate
            visibilityChangeText(!activeGroup.IsPrivate)

            visibilitySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                updateGroupVisibility()
                visibilityChangeText(isChecked)
            }

            titleInfo.text = "Update the title of " + activeGroup.Title
            titleField.hint = activeGroup.Title
            membersInfo.text = activeGroup.Title + " Members"

            previewImage.setImageBitmap(activeGroup.getImage())
            imageGroup.setImageBitmap(activeGroup.getImage())


            val list = activeGroup.Channels/*filter { channel -> channel.IsMain }*/.first()!!.Members

            membersAdapter.submitList(list)
            membersAdapter.notifyDataSetChanged()
        } else {
            fromImageSelect = false
        }

    }

    override fun onStop() {
        visibilitySwitch.setOnCheckedChangeListener(null)
        super.onStop()
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
            activeGroup.Title = titleField.text.toString()
            activeGroup.Channels.first()!!.Title = activeGroup.Title + " Channel"
        }

        titleView.text = activeGroup.Title
        imageView.text = activeGroup.Title + " Image"
        titleInfo.text = "Update the title of " + activeGroup.Title
        titleField.hint = activeGroup.Title
        membersInfo.text = activeGroup.Title + " Members"

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
        val realm : Realm = Realm.getDefaultInstance()
        
        val imageName = activeGroup.ID + "_main.png"

        ImageStorageManager.shared.saveToInternalStorage(imageGroup.drawable.toBitmap(), imageName)


        realm.executeTransaction {
            activeGroup.ImagePath = imageName
        }

        previewImage.setImageBitmap(imageGroup.drawable.toBitmap())

        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateGroup(activeGroup.giveJson())
            val toast = Toast.makeText(this, "Updated the group's image", Toast.LENGTH_SHORT)
            toast.show()
        } else {
            realm.executeTransaction {
                activeGroup.Updated = false
            }
        }    
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
        titleInfoBg.isVisible = visible
        titleFieldBg.isVisible = visible
        titleInfo.isVisible = visible
        titleField.isVisible = visible
        titleCancel.isVisible = visible
        titleUpdate.isVisible = visible
    }

    fun imageEditVisible(visible: Boolean) {
        background.isVisible = visible
        imageInfo.isVisible = visible
        imageGroup.isVisible = visible
        imageGroupCard.isVisible = visible
        imageCancel.isVisible = visible
        imageUpdate.isVisible = visible
        imageTapToEdit.isVisible = visible
    }
    
    fun viewMembersVisible(visible: Boolean) {
        background.isVisible = visible
        membersInfoBg.isVisible = visible
        membersRecyclerBg.isVisible = visible
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && null != data) {

            val imageUri: Uri? = data?.data

            if(Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val scaleBitmap = ImageStorageManager.shared.resizeBitmap(bitmap, imageGroup.width, imageGroup.height)
                imageGroup.setImageBitmap(scaleBitmap)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                val bitmap = ImageDecoder.decodeBitmap(source)
                val scaleBitmap = ImageStorageManager.shared.resizeBitmap(bitmap, imageGroup.width, imageGroup.height)
                imageGroup.setImageBitmap(scaleBitmap)
            }

            fromImageSelect = true
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TAKE_PHOTO && null != data) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val scaleBitmap = ImageStorageManager.shared.resizeBitmap(imageBitmap, imageGroup.width, imageGroup.height)
            imageGroup.setImageBitmap(scaleBitmap)
            fromImageSelect = true
        }

    }


    private fun selectImageInAlbum() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}