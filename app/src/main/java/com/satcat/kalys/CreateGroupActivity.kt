package com.satcat.kalys

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.ImageStorageManager
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

    lateinit var addMembersBtn : Button
    lateinit var addImageBtn : Button
    lateinit var createGroupBtn : Button
    lateinit var visibilityStatus : TextView
    lateinit var visibilityDetails : TextView
    lateinit var visibilitySwitch : Switch
    lateinit var membersLbl : TextView
    lateinit var removeLbl : TextView
    lateinit var titleTxt : EditText
    lateinit var noticeTxt : EditText
    lateinit var groupImg : ImageView

    companion object {
        private val REQUEST_TAKE_PHOTO = 0
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        supportActionBar!!.title = "Create Group"

        addMembersBtn = findViewById(R.id.create_group_add_members_btn)
        addImageBtn = findViewById(R.id.create_group_add_image)
        createGroupBtn = findViewById(R.id.create_group_create_btn)
        visibilityStatus = create_group_visibility_status
        visibilityDetails = create_group_visibility_info
        visibilitySwitch = findViewById(R.id.create_group_visibility_switch)
        membersLbl  = findViewById(R.id.create_group_members_count)
        removeLbl = findViewById(R.id.create_group_members_remove_text)
        titleTxt = create_group_name_field
        noticeTxt = create_group_notice_field
        groupImg = create_group_image


        addMembersBtn.setOnClickListener {
            val intent = Intent(this, AddMembersActivity::class.java)
            AddMembersActivity.editedChannel = mainChannel
            AddMembersActivity.fromOptions = false
            AddMembersActivity.searchGroupOnly = false
            startActivity(intent)
        }



        addImageBtn.setOnClickListener { buttonView ->
            val popupMenu = PopupMenu(this,addImageBtn)
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

        createGroupBtn.setOnClickListener {
            makeGroup()
            finish()
        }
        
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

        membersLbl.text = "Members • " + memberCount.toString()
        if(memberCount > 0) {
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
        
        group = ChatGroup()
        group.Title = titleTxt.text.toString()
        group.Notice = if(noticeTxt.text.isBlank()) "" else noticeTxt.text.toString()
        group.IsPrivate = !visibilitySwitch.isChecked
        group.Channels.add(mainChannel)

        if(groupImg.drawable.toBitmap() != null) {
            val imageName = group.ID
            ImageStorageManager.shared.saveToInternalStorage(groupImg.drawable.toBitmap(), imageName)
            group.ImagePath = imageName
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM) {
            // I'M GETTING THE URI OF THE IMAGE AS DATA AND SETTING IT TO THE IMAGEVIEW
            val imageUri: Uri? = data?.data

            if(Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val scaleBitmap = ImageStorageManager.shared.resizeBitmap(bitmap, groupImg.width, groupImg.height)
                groupImg.setImageBitmap(scaleBitmap)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                val bitmap = ImageDecoder.decodeBitmap(source)
                val scaleBitmap =  ImageStorageManager.shared.resizeBitmap(bitmap, groupImg.width, groupImg.height)
                groupImg.setImageBitmap(scaleBitmap)
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TAKE_PHOTO ) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val scaleBitmap =  ImageStorageManager.shared.resizeBitmap(imageBitmap, groupImg.width, groupImg.height)
            groupImg.setImageBitmap(scaleBitmap)
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

}