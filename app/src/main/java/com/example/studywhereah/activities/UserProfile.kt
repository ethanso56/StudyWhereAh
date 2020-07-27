package com.example.studywhereah.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.example.studywhereah.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_location_activity.*
import kotlinx.android.synthetic.main.activity_sign_in_first.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.alert_dialog_input_username.*
import java.net.URL

class UserProfile: AppCompatActivity() {
    // remove login activity, instead create a profile activity, with sign out button there.
    // start AuthUI intent whenever necessary to make exp smoother.
    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var RC_SIGN_IN = 10001
    private var PICK_IMAGE = 1
    private var firebaseStorage = FirebaseStorage.getInstance()
    private var fbsReference = firebaseStorage.reference

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser == null) {
            setContentView(R.layout.activity_sign_in_first)
            setSupportActionBar(toolbar_user_profile_sign_in_first)
            var actionbar = supportActionBar
                if (actionbar != null) {
                actionbar.setDisplayHomeAsUpEnabled(true) //set back button
                actionbar.title = "Profile"
            }
            btn_sign_in.setOnClickListener{
                startLoginIntent()
                this.finish()

            }

        } else {

            setContentView(R.layout.activity_user_profile)
            setSupportActionBar(toolbar_user_profile_activity)
            var actionbar = supportActionBar
            if (actionbar != null) {
                actionbar.setDisplayHomeAsUpEnabled(true) //set back button
                actionbar.title = "Profile"
            }

            toolbar_user_profile_activity.setNavigationOnClickListener {
                onBackPressed()
            }

            btn_choose_profile_picture.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
            }

//            var profilePhotoUrl = currentUser?.photoUrl
//            var bitmap = getBitmapFromUri(profilePhotoUrl!!)
//            iv_profile_photo.setImageBitmap(bitmap)
            var storageRef =
                fbsReference.child("Profile Photos/${currentUser!!.email}")
            storageRef.getBytes(10000 * 1000).addOnSuccessListener {
                var bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                iv_profile_photo.setImageBitmap(bitmap)
            }

            var profileName = currentUser?.displayName
            Log.d("WHY", (profileName?.length).toString())
            if (profileName?.length != 0) {
                tv_profile_name.text = profileName
                Log.e("WHY", profileName)
            } else {
                tv_profile_name.text = "Click to set a username"
            }
//            val builder: AlertDialog.Builder? = this.let {
//                AlertDialog.Builder(it)
//            }

//            var inflater = this.layoutInflater
//            builder?.setView(inflater.inflate(R.layout.alert_dialog_input_username, null))
//            builder?.setPositiveButton("Confirm", DialogInterface.OnClickListener { dialog, which ->
//                Log.e("WHY", et_username_input.text.length.toString())
////                UserProfileChangeRequest.Builder().displayName = desiredUsername
////                tv_profile_name.text = desiredUsername
//            })
//           ?.setNegativeButton("Cancel", DialogInterface.OnClickListener {
//                    dialog, which ->
//
//            })
//            ?.setTitle("Set Username")?.create()
//
//            tv_profile_name.setOnClickListener {
//                builder?.show()
//            }
            tv_profile_name.setOnClickListener {
                var intent = Intent(this, UpdateProfileActivity::class.java)
                startActivity(intent)
            }

            var profileEmail = currentUser?.email
            Log.d("WHY", profileEmail)
            if (profileEmail?.length != 0) {
                tv_profile_email.text = profileEmail
            } else {
                tv_profile_email.text = "Email currently not available"
            }

            tv_saved_locations.setOnClickListener {
                val intent = Intent(this, SavedLocationsActivity::class.java)
                startActivity(intent)
            }

            tv_get_place.setOnClickListener {
                val intent = Intent(this, ChoosePreferencesActivity::class.java)
                startActivity(intent)
            }

            btn_sign_out.setOnClickListener{
                // launch log in intent
                // prompt r u sure?
                AuthUI.getInstance().signOut(this).addOnCompleteListener {
                    Toast.makeText(this, "Succesfully signed out", Toast.LENGTH_SHORT)
                    this.finish()
                    val intent = Intent(this, UserProfile::class.java)
                    startActivity(intent)

                }
            }
        }


    }

    fun getBitmapFromUri(uri: Uri): Bitmap {
        var url = URL(uri.toString())
        var connection = url.openConnection()
        connection.doInput = true
        connection.connect()
        var input = connection.getInputStream()
        var bitmap = BitmapFactory.decodeStream(input)
        return bitmap
    }

    fun startLoginIntent() {
        if (currentUser == null) {
            // start login activity
            var providers = arrayListOf<AuthUI.IdpConfig>(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setLogo(R.drawable.books)
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                var user = FirebaseAuth.getInstance().currentUser
                Log.e("User email", user?.email)
                if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                    //new user
                    Toast.makeText(this, "Congrats you have just signed up!", Toast.LENGTH_SHORT)
                } else {
                    Toast.makeText(this, "Welcome back Mugger!", Toast.LENGTH_SHORT)
                }
                var intent = Intent(this, UserProfile::class.java)
                startActivity(intent)
            } else {
                // sign in failed
                var idpResponse = IdpResponse.fromResultIntent(data)
                if (idpResponse == null) {
                    Log.e("Cancelled", "User has cancelled sign in request")
                } else {
                    // we dk so we log the error
                    Log.e("Error", idpResponse.error?.message)
                }
            }
        } else if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                var profilePhotoToUpload = data?.data
                iv_profile_photo.setImageURI(profilePhotoToUpload)
                var filePath = fbsReference.child("Profile Photos/${currentUser!!.email}")
                var uploadTask = filePath.putFile(profilePhotoToUpload!!)
                Toast.makeText(this, "Profile Photo Selected", Toast.LENGTH_SHORT).show()
                uploadTask.addOnSuccessListener {
                    Toast.makeText(this, "Profile Photo Uploaded", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}