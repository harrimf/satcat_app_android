package com.satcat.kalys.SignupLogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.SocketIOManager.LoginUserEvent
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.Models.User
import com.satcat.kalys.R
import com.satcat.kalys.StartTabActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var realm: Realm

    val loginUserJSON = JSONObject()

    lateinit var token:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        EventBus.getDefault().register(this)

        realm = Realm.getDefaultInstance()

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                // Get new Instance ID token
                token = if(task.result?.token != null)  task.result?.token!! else ""
            })


        val loginBtn : Button = findViewById(R.id.login_btn)

        val userNameTxt : EditText = findViewById(R.id.login_username_field)

        val passwordTxt : EditText = findViewById(R.id.login_password_field)

        loginBtn.setOnClickListener {

            loginUserJSON.put("UserName", userNameTxt.text.toString())
            loginUserJSON.put("Password", passwordTxt.text.toString())

            if(SocketIOManager.shared.socketConnected()) {
                SocketIOManager.shared.login(loginUserJSON)

                loginBtn.isEnabled = false
                loginBtn.postDelayed(Runnable { loginBtn.setEnabled(true) }, 5000)
            } else {
                val toast = Toast.makeText(this, "Could not connect to the network and login, try again later", Toast.LENGTH_LONG)
                toast.show()
            }

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginUserEvent(event: LoginUserEvent): Unit {


        val success = event!!.json.getBoolean("status")
        val reason = event!!.json.getString("reason")
        val userJson = event!!.json.optJSONObject("user")


        if (success && userJson != null) {

            val userSearch = realm.where<User>().equalTo("ID", userJson.getString("ID"))


            if(userSearch.findAll().count() == 0) {

                realm.executeTransaction {
                    val user = realm.createObject<User>()
                    user.setFromJSON(userJson)
                    user.Password = loginUserJSON.getString("Password")
                    user.IsLocal = true

                    if(user.DeviceToken != token) {
                        user.DeviceToken = token
                        SocketIOManager.shared.adjustToken(token)
                    }
                }
            } else {
                realm.executeTransaction {
                    userSearch.findFirst()!!.Password = loginUserJSON.getString("Password")

                    if(userSearch.findFirst()!!.DeviceToken != token) {
                        userSearch.findFirst()!!.DeviceToken = token
                        SocketIOManager.shared.adjustToken(token)
                    }
                }

                UserManager.shared.setLocalUser(userSearch.findFirst()!!.ID)



                //update device token
            }

            val intent = Intent(this, StartTabActivity::class.java)
            startActivity(intent)
        } else {
            //error: show reason on statuslbl
            Log.d("problem", reason)

            val toast = Toast.makeText(this, "The username & password combination is not valid, try again", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }




}