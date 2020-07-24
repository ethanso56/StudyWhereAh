package com.example.studywhereah.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studywhereah.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_sign_in_first.*
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity: AppCompatActivity() {

    private var currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        setSupportActionBar(toolbar_update_profile_activity)
        var actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true) //set back button
            actionbar.title = "Update Profile"
        }

        btn_confirm_profile_updates.setOnClickListener {
            var newUsername = input_username.text.toString()
            var newEmail = input_email.text.toString()
            var newPass = input_password.text.toString()
//            if (newUsername.length > 0) {
//                currentUser?.updateProfile({
//                    displayName: "yep"
//                })
//            }
            if (newEmail.length > 0) {
                currentUser?.updateEmail(newEmail)
            }
            if (newPass.length > 0) {
                currentUser?.updatePassword(newPass)
            }
            Toast.makeText(this, "The updates will shortly be complete", Toast.LENGTH_LONG)
            this.finish()
        }
    }
}