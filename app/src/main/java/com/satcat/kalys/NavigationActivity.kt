package com.satcat.kalys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.satcat.kalys.Managers.UserManager
import com.satcat.kalys.SignupLogin.WelcomeActivity


class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if(UserManager.shared.isOneLocalUser()) {
            val i = Intent(this, StartTabActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        } else {
            val i = Intent(this, WelcomeActivity::class.java)
            startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }



    }
}
