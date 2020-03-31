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
import com.satcat.kalys.R
import org.json.JSONObject

class SignupNameActivity : AppCompatActivity() {

    private var userObj:JSONObject = JSONObject()

    lateinit var firstNameTxt : EditText
    lateinit var lastNameTxt : EditText
    lateinit var continueBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_name)

        firstNameTxt = findViewById(R.id.signup_name_firstname_field)
        lastNameTxt = findViewById(R.id.signup_name_lastname_field)
        continueBtn = findViewById(R.id.signup_name_continue_btn)

        continueBtn.setOnClickListener {
            if(firstNameTxt.text.toString().isNotBlank()) {
                val intent = Intent(this, SignupAccountActivity::class.java)
                startActivity(intent)
            } else {
                val toast = Toast.makeText(this, "You don't have a first name?", Toast.LENGTH_LONG)
                toast.show()
            }

        }

        firstNameTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                SignupImageActivity.firstname = firstNameTxt.text.toString()

            }
        })


        lastNameTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                SignupImageActivity.lastname = lastNameTxt.text.toString()

            }
        })
        

    }

    override fun onStart() {
        super.onStart()

        if(SignupImageActivity.firstname != "") {
            firstNameTxt.setText(SignupImageActivity.firstname)
        }
        
        if(SignupImageActivity.lastname != "") {
            lastNameTxt.setText(SignupImageActivity.lastname)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}