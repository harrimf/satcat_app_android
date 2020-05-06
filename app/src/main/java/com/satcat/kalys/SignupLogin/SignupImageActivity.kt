package com.satcat.kalys.SignupLogin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.SocketIOManager.RegisterUserEvent
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.User
import com.satcat.kalys.R
import com.satcat.kalys.StartTabActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.satcat.kalys.CreateGroupActivity
import com.satcat.kalys.Managers.ImageStorageManager
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class SignupImageActivity : AppCompatActivity() {

    companion object {
        var firstname : String = ""
        var lastname : String = ""
        var username : String = ""
        var password : String = ""
        var email : String = ""
        var phonenumber : String = ""
        var user = User()

        private val REQUEST_TAKE_PHOTO = 0
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1


    }

    lateinit var token:String
    lateinit var profileImg : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_image)

        profileImg = findViewById(R.id.signup_image)

        Log.d("test232", firstname + lastname + username + password + email + phonenumber)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                // Get new Instance ID token
                token = if(task.result?.token != null)  task.result?.token!! else ""
            })


        profileImg.setOnClickListener { buttonView ->
            val popupMenu = PopupMenu(this,profileImg)
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

        val signinBtn: Button = findViewById(R.id.signup_image_continue_btn)
        signinBtn.setOnClickListener {

            user.FirstName = firstname
            user.LastName = lastname
            user.UserName = username
            user.Password = password
            user.Email = email
            user.PhoneNumber = phonenumber
            user.DeviceToken = token

            val imageName = user.ID + "_main.png"
            ImageStorageManager.shared.saveToInternalStorage(profileImg.drawable.toBitmap(), imageName)

            user.ImagePath = imageName

            if(SocketIOManager.shared.socketConnected()) {
                SocketIOManager.shared.register(user.giveJson())
            } else {
                val toast = Toast.makeText(this, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                toast.show()
            }

        }


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRegisterUserEvent(event: RegisterUserEvent?): Unit {

        val realm : Realm = Realm.getDefaultInstance()

        val success = event!!.json.getBoolean("status")
        val reason = event!!.json.getString("reason")

        if (success) {

            realm.executeTransaction {
                realm.insert(user)
            }

            UserManager.shared.setLocalUser(user.ID)

            val intent = Intent(this, StartTabActivity::class.java)
            startActivity(intent)

            //
            //reset all companion object values to ensure that
            firstname = ""
            lastname = ""
            username = ""
            password = ""
            email = ""
            phonenumber = ""
            user = User()

        } else {
            //error: show reason on statuslbl
            val toast = Toast.makeText(this, "Sign-up failed, try again later", Toast.LENGTH_LONG)
            toast.show()

            Log.d("problem", reason)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM) {
            val imageUri: Uri? = data?.data

            if(Build.VERSION.SDK_INT < 28) {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val scaleBitmap = ImageStorageManager.shared.resizeBitmap(bitmap, profileImg.width, profileImg.height)
                profileImg.setImageBitmap(scaleBitmap)
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, imageUri!!)
                val bitmap = ImageDecoder.decodeBitmap(source)
                val scaleBitmap = ImageStorageManager.shared.resizeBitmap(bitmap, profileImg.width, profileImg.height)
                profileImg.setImageBitmap(scaleBitmap)
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val scaleBitmap = ImageStorageManager.shared.resizeBitmap(imageBitmap, profileImg.width, profileImg.height)
            profileImg.setImageBitmap(scaleBitmap)
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

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}