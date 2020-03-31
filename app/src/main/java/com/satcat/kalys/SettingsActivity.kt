package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.SignupLogin.WelcomeActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    lateinit var background : View
    
    lateinit var usenameContainer : RelativeLayout 
    lateinit var firstnameContainer : RelativeLayout
    lateinit var lastnameContainer : RelativeLayout
    lateinit var emailContainer : RelativeLayout
    lateinit var phonenumberContainer : RelativeLayout
    lateinit var imageContainer : RelativeLayout
    lateinit var passwordContainer : RelativeLayout
    
    
    lateinit var usernameTxt : TextView
    lateinit var firstNameTxt : TextView
    lateinit var lastNameTxt : TextView
    lateinit var emailTxt : TextView
    lateinit var phonenumberTxt : TextView
    lateinit var imageTxt : TextView
    lateinit var passwordTxt : TextView
    
    lateinit var imageBtn : ImageButton
    
    lateinit var editUsernameInfo : TextView
    lateinit var editUsernameTxt : EditText
    lateinit var editUsernameCancelBtn : Button
    lateinit var editUsernameUpdateBtn : Button


    lateinit var editFirstNameInfo : TextView
    lateinit var editFirstNameTxt : EditText
    lateinit var editFirstNameCancelBtn : Button
    lateinit var editFirstNameUpdateBtn : Button

    lateinit var editLastNameInfo : TextView
    lateinit var editLastNameTxt : EditText
    lateinit var editLastNameCancelBtn : Button
    lateinit var editLastNameUpdateBtn : Button

    lateinit var editEmailInfo : TextView
    lateinit var editEmailTxt : EditText
    lateinit var editEmailCancelBtn : Button
    lateinit var editEmailUpdateBtn : Button

    lateinit var editPhonenumberInfo : TextView
    lateinit var editPhonenumberTxt : EditText
    lateinit var editPhonenumberCancelBtn : Button
    lateinit var editPhonenumberUpdateBtn : Button

    lateinit var editImageInfo: TextView
    lateinit var editImage : ImageButton
    lateinit var editImageTapToEdit : TextView
    lateinit var editImageCancelBtn : Button
    lateinit var editImageUpdateBtn : Button


    lateinit var editPasswordInfo : TextView
    lateinit var editPasswordOldTxt : EditText
    lateinit var editPasswordNewTxt : EditText
    lateinit var editPasswordConfirmTxt : EditText
    lateinit var editPasswordCancelBtn : Button
    lateinit var editPasswordUpdateBtn : Button
    
    lateinit var  logoutBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        background = settings_background

        usenameContainer = settings_username_container
        firstnameContainer = settings_firstname_container
        lastnameContainer = settings_lastname_container
        emailContainer = settings_email_container
        phonenumberContainer = settings_phonenumber_container
        imageContainer = settings_image_container
        passwordContainer = settings_password_container


        usernameTxt = settings_username
        firstNameTxt = settings_firstname
        lastNameTxt = settings_lastname
        emailTxt = settings_email
        phonenumberTxt = settings_phonenumber
        imageTxt = settings_image_txt
        passwordTxt = settings_password

        imageBtn = settings_image

        editUsernameInfo = settings_edit_username_info
        editUsernameTxt = settings_edit_username_field
        editUsernameCancelBtn = settings_edit_username_cancel_btn
        editUsernameUpdateBtn = settings_edit_username_update_btn

        editFirstNameInfo = settings_edit_firstname_info
        editFirstNameTxt = settings_edit_firstname_field
        editFirstNameCancelBtn = settings_edit_firstname_cancel_btn
        editFirstNameUpdateBtn = settings_edit_firstname_update_btn

        editLastNameInfo = settings_edit_lastname_info
        editLastNameTxt = settings_edit_lastname_field
        editLastNameCancelBtn = settings_edit_lastname_cancel_btn
        editLastNameUpdateBtn = settings_edit_lastname_update_btn

        editEmailInfo = settings_edit_email_info
        editEmailTxt = settings_edit_email_field
        editEmailCancelBtn = settings_edit_email_cancel_btn
        editEmailUpdateBtn = settings_edit_email_update_btn

        editPhonenumberInfo = settings_edit_phonenumber_info
        editPhonenumberTxt = settings_edit_phonenumber_field
        editPhonenumberCancelBtn = settings_edit_phonenumber_cancel_btn
        editPhonenumberUpdateBtn = settings_edit_phonenumber_update_btn

        editImageInfo = settings_edit_image_info
        editImage = settings_edit_image
        editImageTapToEdit = settings_edit_image_tap_to_edit
        editImageCancelBtn = settings_edit_image_cancel_btn
        editImageUpdateBtn = settings_edit_image_update_btn

        editPasswordInfo = settings_edit_password_info
        editPasswordOldTxt = settings_edit_password_old_field
        editPasswordNewTxt = settings_edit_password_field
        editPasswordConfirmTxt = settings_edit_password_confirm_field
        editPasswordCancelBtn = settings_edit_password_cancel_btn
        editPasswordUpdateBtn = settings_edit_password_update_btn
        
        logoutBtn = settings_logout_btn

        realm = Realm.getDefaultInstance()


         usenameContainer.setOnClickListener {
             usernameEditVisible(true)
         }
         firstnameContainer.setOnClickListener {
            firstNameEditVisible(true)
         }
         lastnameContainer.setOnClickListener {
            lastNameEditVisible(true)
         }
         emailContainer.setOnClickListener {
            emailEditVisible(true)
         }
         phonenumberContainer.setOnClickListener {
            phonenumberEditVisible(true)
         }
         imageContainer.setOnClickListener {
            imageEditVisible(true)
         }
         passwordContainer.setOnClickListener {
             passwordEditVisible(true)
         }
        
        

        editUsernameCancelBtn.setOnClickListener {
            usernameEditVisible(false)
        }

        editFirstNameCancelBtn.setOnClickListener {
            firstNameEditVisible(false)
        }

        editLastNameCancelBtn.setOnClickListener {
            lastNameEditVisible(false)
        }

        editEmailCancelBtn.setOnClickListener {
            emailEditVisible(false)
        }

        editPhonenumberCancelBtn.setOnClickListener {
            phonenumberEditVisible(false)
        }

        editImageCancelBtn.setOnClickListener {
            imageEditVisible(false)
        }

        editPasswordCancelBtn.setOnClickListener {
            passwordEditVisible(false)
        }




        editFirstNameUpdateBtn.setOnClickListener {
            updateFirstName()
            firstNameEditVisible(false)
        }

        editLastNameUpdateBtn.setOnClickListener {
            updateLastName()
            lastNameEditVisible(false)
        }

        editEmailUpdateBtn.setOnClickListener {
            updateEmail()
            emailEditVisible(false)
        }

        editPhonenumberUpdateBtn.setOnClickListener {
            updatePhonenumber()
            phonenumberEditVisible(false)
        }

        editImageUpdateBtn.setOnClickListener {
            updateImage()
            imageEditVisible(false)
        }

        editPasswordUpdateBtn.setOnClickListener {
            updatePassword()
            passwordEditVisible(false)
        }
        

        logoutBtn.setOnClickListener {
            SocketIOManager.shared.adjustToken("")//adjustToken

            SocketIOManager.shared.logout()

            UserManager.shared.removeLocalUsers()

            realm.executeTransaction {
                realm.deleteAll()
            }

            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)

        }


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        val user = UserManager.shared.getUser()

        usernameTxt.text = user.UserName
        firstNameTxt.text = user.FirstName
        lastNameTxt.text = user.LastName
        emailTxt.text = user.Email
        phonenumberTxt.text = if(user.PhoneNumber != "") user.PhoneNumber else "Phone Number • Tap to Add"
        imageTxt.text = user.UserName + " Image"

        editUsernameTxt.setText(user.UserName)
        editFirstNameTxt.hint = user.FirstName
        editLastNameTxt.hint = user.LastName
        editEmailTxt.hint = user.Email
        editPhonenumberTxt.hint = if(user.PhoneNumber != "") user.PhoneNumber else "Phone Number"

    }

    fun usernameEditVisible(visible: Boolean) {
        background.isVisible = visible
        editUsernameInfo.isVisible = visible
        editUsernameTxt.isVisible = visible
        editUsernameCancelBtn.isVisible = visible
        editUsernameUpdateBtn.isVisible = visible
    }

    fun firstNameEditVisible(visible: Boolean) {
        background.isVisible = visible
        editFirstNameInfo.isVisible = visible
        editFirstNameTxt.isVisible = visible
        editFirstNameCancelBtn.isVisible = visible
        editFirstNameUpdateBtn.isVisible = visible

    }

    fun lastNameEditVisible(visible: Boolean) {
        background.isVisible = visible
        editLastNameInfo.isVisible = visible
        editLastNameTxt.isVisible = visible
        editLastNameCancelBtn.isVisible = visible
        editLastNameUpdateBtn.isVisible = visible
    }

    fun emailEditVisible(visible: Boolean) {
        background.isVisible = visible
        editEmailInfo.isVisible = visible
        editEmailTxt.isVisible = visible
        editEmailCancelBtn.isVisible = visible
        editEmailUpdateBtn.isVisible = visible
    }

    fun phonenumberEditVisible(visible: Boolean) {
        background.isVisible = visible
        editPhonenumberInfo.isVisible = visible
        editPhonenumberTxt.isVisible = visible
        editPhonenumberCancelBtn.isVisible = visible
        editPhonenumberUpdateBtn.isVisible = visible
    }

    fun imageEditVisible(visible: Boolean) {
        background.isVisible = visible
        editImageInfo.isVisible = visible
        editImage.isVisible = visible
        editImageTapToEdit.isVisible = visible
        editImageCancelBtn.isVisible = visible
        editImageUpdateBtn.isVisible = visible
    }

    fun passwordEditVisible(visible: Boolean) {
        background.isVisible = visible
        editPasswordInfo.isVisible = visible
        editPasswordOldTxt.isVisible = visible
        editPasswordNewTxt.isVisible = visible
        editPasswordConfirmTxt.isVisible = visible
        editPasswordCancelBtn.isVisible = visible
        editPasswordUpdateBtn.isVisible = visible

    }
    

    fun updateFirstName() {
        val realm : Realm = Realm.getDefaultInstance()
        
        realm.executeTransaction { 
            UserManager.shared.getUser().FirstName = editFirstNameTxt.text.toString()
        }
        firstNameTxt.text = editFirstNameTxt.text
        
        socketUserUpdate()
    }

    fun updateLastName() {
        val realm : Realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            UserManager.shared.getUser().LastName = editLastNameTxt.text.toString()
        }
        lastNameTxt.text = editLastNameTxt.text
        
        socketUserUpdate()
    }

    fun updateEmail() {
        val realm : Realm = Realm.getDefaultInstance()

        realm.executeTransaction {
            UserManager.shared.getUser().Email = editEmailTxt.text.toString()
        }
        emailTxt.text = editEmailTxt.text

        socketUserUpdate()
    }

    fun updatePhonenumber() {
        val realm : Realm = Realm.getDefaultInstance()

        //TODO: check if valid number
        realm.executeTransaction {
            UserManager.shared.getUser().PhoneNumber = editPhonenumberTxt.text.toString()
        }
        phonenumberTxt.text = editPhonenumberTxt.text

        socketUserUpdate()
    }
    
    fun updateImage() {
        //TODO: implement updating image
        
    }
    
    fun updatePassword() {
        val realm : Realm = Realm.getDefaultInstance()

        //TODO: StatusLBl for different cases
        if(editPasswordNewTxt.text.toString().length < 6) {
            Log.d("", "Smaller than 6 characters")

            val toast = Toast.makeText(this, "Make sure the password is at least six characters long", Toast.LENGTH_LONG)
            toast.show()

        } else if(editPasswordNewTxt.text.toString() != editPasswordConfirmTxt.text.toString()) {
            Log.d("", "Confirm does not match new")

            val toast = Toast.makeText(this, "The new passwords do not match", Toast.LENGTH_LONG)
            toast.show()

        } else if (editPasswordNewTxt.text.toString() == UserManager.shared.getUser().Password) {
            Log.d("", "Old is new")

            val toast = Toast.makeText(this, "Make sure your password does not match any of the previous passwords", Toast.LENGTH_LONG)
            toast.show()

        } else if(editPasswordOldTxt.text.toString() != UserManager.shared.getUser().Password) {
            Log.d("", "Old text does not match old password")

            val toast = Toast.makeText(this, "The old password is not correct", Toast.LENGTH_LONG)
            toast.show()
        } else {
            if(SocketIOManager.shared.socketConnected()) {
                SocketIOManager.shared.updatePassword(editPasswordNewTxt.text.toString())
            } else {
                val toast = Toast.makeText(this, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    fun socketUserUpdate() {
        val realm : Realm = Realm.getDefaultInstance()


        if(SocketIOManager.shared.socketConnected()) {
            SocketIOManager.shared.updateUser(UserManager.shared.getUser().giveJson())
        } else {
            realm.executeTransaction {
                UserManager.shared.getUser().Updated = false
            }
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onReceiveUserupdateEvent(event: SocketIOManager.ReceiveUserUpdateEvent): Unit {
//        val attribute = event.json.getString("Attribute")
//        val value = event.json.getString("Value")
//
//
//        //weird approach... Change to local DB should be written after sending to server
//        if(attribute == "Phone Number") {
//            if(UserManager.shared.getUser().PhoneNumber != value) {
//                //update status
//            } else {
//                //update status
//            }
//
//            realm.executeTransaction {
//                UserManager.shared.getUser().PhoneNumber = value
//            }
//        } else if (attribute == "Email") {
//            if(UserManager.shared.getUser().Email != value) {
//                //update status
//            } else {
//                //update status
//            }
//
//            realm.executeTransaction {
//                UserManager.shared.getUser().Email = value
//            }
//        }
//
//
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveUserPasswordEvent(event: SocketIOManager.ReceiveUserPasswordUpdateEvent): Unit {
        val password = event.json.getString("Code")
        val status = event.json.getBoolean("Status")


        //TODO: handle status lbl
        if(status == true) {
            realm.executeTransaction {
                UserManager.shared.getUser().Password = password
            }
            val toast = Toast.makeText(this, "Successfully updated your password", Toast.LENGTH_LONG)
            toast.show()

        } else {
            val toast = Toast.makeText(this, "Could not successfully update password", Toast.LENGTH_LONG)
            toast.show()
        }
    }



}