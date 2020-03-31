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
import androidx.core.view.isVisible
import com.hbb20.CountryCodePicker
import com.satcat.kalys.R

class SignupVerifyActivity : AppCompatActivity() {

    lateinit var continueBtn : Button
    lateinit var emailTxt : EditText
    lateinit var phonenumberTxt : EditText
    lateinit var phonenumberCountryCode : CountryCodePicker
    lateinit var verifyTxt : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_verify)

        continueBtn = findViewById(R.id.signup_verify_continue_btn)
        emailTxt = findViewById(R.id.signup_verify_email_field)
        phonenumberTxt = findViewById(R.id.signup_verify_phone_field)
        phonenumberCountryCode = findViewById(R.id.signup_verify_phone_country_field)
        verifyTxt = findViewById(R.id.signup_verify_code_field)


        continueBtn.setOnClickListener {
            if(emailTxt.text.isNotBlank()) {

                val intent = Intent(this, SignupImageActivity::class.java)
                startActivity(intent)
            } else {
                val toast = Toast.makeText(this, "Ensure that you have listed a valid email", Toast.LENGTH_LONG)
                toast.show()
            }

        }

        emailTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                SignupImageActivity.email = emailTxt.text.toString()

            }
        })


        phonenumberTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                SignupImageActivity.phonenumber = phonenumberCountryCode.selectedCountryCodeWithPlus + phonenumberTxt.text.toString()

            }
        })

        verifyTxt.isVisible = false
        verifyTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

            }
        })

    }

    override fun onStart() {
        super.onStart()

        if(SignupImageActivity.email != "") {
            emailTxt.setText(SignupImageActivity.email)
        }

//        if(SignupImageActivity.phonenumber != "") {
//            phonenumberTxt.setText(SignupImageActivity.phonenumber)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}