package com.satcat.kalys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.SignupLogin.WelcomeActivity

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(UserManager.shared.isOneLocalUser()) {
            val intent = Intent(this, StartTabActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }



    }
}
