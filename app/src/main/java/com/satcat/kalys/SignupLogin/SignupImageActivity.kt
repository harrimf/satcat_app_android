package com.satcat.kalys.SignupLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.SocketIOManager.RegisterUserEvent
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.User
import com.satcat.kalys.R
import com.satcat.kalys.StartTabActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
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

    }

    lateinit var token:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_image)

        Log.d("test232", firstname + lastname + username + password + email + phonenumber)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                // Get new Instance ID token
                token = if(task.result?.token != null)  task.result?.token!! else ""
            })

        val signinBtn: Button = findViewById(R.id.signup_image_continue_btn)
        signinBtn.setOnClickListener {

            user.FirstName = firstname
            user.LastName = lastname
            user.UserName = username
            user.Password = password
            user.Email = email
            user.PhoneNumber = phonenumber
            user.DeviceToken = token

            if(SocketIOManager.shared.socketConnected()) {
                SocketIOManager.shared.register(user.giveJson())
            } else {
                val toast = Toast.makeText(this, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                toast.show()
            }

            //TODO: need image handling

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}