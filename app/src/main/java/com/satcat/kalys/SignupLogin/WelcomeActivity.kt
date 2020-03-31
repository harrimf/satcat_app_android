package com.satcat.kalys.SignupLogin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.satcat.kalys.R

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val loginBtn: Button = findViewById(R.id.welcome_login_btn)
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val registerBtn: Button = findViewById(R.id.welcome_register_btn)
        registerBtn.setOnClickListener {
            val intent = Intent(this, SignupNameActivity::class.java)
            startActivity(intent)
        }

    }
}