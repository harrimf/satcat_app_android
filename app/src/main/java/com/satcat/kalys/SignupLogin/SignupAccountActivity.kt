package com.satcat.kalys.SignupLogin

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SignupAccountActivity : AppCompatActivity() {

    lateinit var usernameTxt : EditText
    lateinit var passwordTxt : EditText
    lateinit var confirmTxt : EditText
    lateinit var continueBtn : Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_account)

        usernameTxt = findViewById(R.id.signup_account_username_field)
        passwordTxt = findViewById(R.id.signup_account_password_field)
        confirmTxt = findViewById(R.id.signup_account_confirm_field)
        continueBtn = findViewById(R.id.signup_account_continue_btn)

        continueBtn.setOnClickListener {
            if(passwordTxt.text.toString() == confirmTxt.text.toString()) {
                if(passwordTxt.text.toString().count() > 5 ) {
                    if(usernameTxt.text.toString().trim().count() > 2) {
                        if(SocketIOManager.shared.socketConnected()) {
                            SocketIOManager.shared.checkUsername(usernameTxt.text.toString().trim())
                        } else {
                            val toast = Toast.makeText(this, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    } else {
                        val toast = Toast.makeText(this, "The username must be at least three characters long", Toast.LENGTH_LONG)
                        toast.show()
                    }
                } else {
                    val toast = Toast.makeText(this, "The password must be at least six characters long", Toast.LENGTH_LONG)
                    toast.show()
                }
            } else {
                val toast = Toast.makeText(this, "The password do not match", Toast.LENGTH_LONG)
                toast.show()
            }
        }

        usernameTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                SignupImageActivity.username = usernameTxt.text.toString()

            }
        })

        passwordTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                SignupImageActivity.password = passwordTxt.text.toString()

            }
        })


        confirmTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveUsername(event: SocketIOManager.ReceiveUsernameCheckEvent): Unit {
        val success = event!!.json.getBoolean("Status")

        if(success) {
            val intent = Intent(this, SignupVerifyActivity::class.java)
            startActivity(intent)
        } else {
            val toast = Toast.makeText(this, "Username has already been taken, try another", Toast.LENGTH_LONG)
            toast.show()

        }


    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        if(SignupImageActivity.username != "") {
            usernameTxt.setText(SignupImageActivity.username)
        }

        if(SignupImageActivity.password != "") {
            passwordTxt.setText(SignupImageActivity.password)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}