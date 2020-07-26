package com.example.studywhereah.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.SavedLocationModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_location_activity.*
import kotlinx.android.synthetic.main.activity_sign_in_first.*


class AddLocationActivity: AppCompatActivity() {

    // fields inputted by user
    private var placeName: String? = null
    private var hasPort: Boolean? = null
    private var hasFood: String? = null
    private var specialInfo: String? = null

    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    private var fireStoreInstance = Firebase.firestore
    private var collectionNameToStoreIn = "Study Spots"

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var RC_SIGN_IN = 10001

    private val SELECT_LOCATION_CODE = 112
    private val PICK_IMAGE = 1
    // as of now only a single image can be uploaded
    private var imageToUpload: Uri? = null
    private var firebaseStorage = FirebaseStorage.getInstance()
    private var fbsReference = firebaseStorage.reference

    // tasks
    private var imageUploadTask: StorageTask<UploadTask.TaskSnapshot>? = null
    private var firestoreTask: Task<Void>? = null
    private var locationSaved: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (currentUser == null) {
            //not logged in
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
            setContentView(R.layout.activity_add_location_activity)
            setSupportActionBar(toolbar_add_location_activity)
            var actionbar = supportActionBar
            if (actionbar != null) {
                actionbar.setDisplayHomeAsUpEnabled(true) //set back button
                actionbar.title = "Add Location"
            }

            toolbar_add_location_activity.setNavigationOnClickListener {
                onBackPressed()
            }

            btn_choose_from_gallery.setOnClickListener {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
            }

            tv_select_on_map.setOnClickListener {
                var intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("CALLINGACTIVITY", "AddLocationActivity")
                // retain user inputs
                startActivityForResult(intent, SELECT_LOCATION_CODE)
            }

            btn_proceed_add_location.setOnClickListener {

                placeName = input_place_name.text.toString()
                hasPort = checkbox_charge_ports.isChecked
                hasFood = input_food_avail.text.toString()
                specialInfo = input_special_info.text.toString()

                if (selectedLat == null || selectedLng == null) {
                    Toast.makeText(this, "Please select a location before proceeding", Toast.LENGTH_LONG).show()
                } else if (placeName == null) {
                    Toast.makeText(this, "Please give your spot a name", Toast.LENGTH_LONG).show()
                } else if (checkbox_save_to_device.isChecked && checkbox_upload_to_database.isChecked) {
                    if (imageToUpload == null) {
                        Toast.makeText(this,
                            "Please select an image to upload",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        saveLocation(
                            placeName!!,"", selectedLat!!, selectedLng!!, -1,
                            arrayListOf(-1, -1), hasFood!!, hasPort!!, ArrayList<Int>())
                        uploadImage()
                        uploadToFirestore(placeName!!,
                            hasPort!!, hasFood!!, selectedLat!!, selectedLng!!, specialInfo!!
                        )
                        Log.e("WHY", "Both complete")
                    }

                } else if (checkbox_save_to_device.isChecked) {

                    saveLocation(
                        placeName!!,"", selectedLat!!, selectedLng!!, -1,
                        arrayListOf(-1, -1), hasFood!!, hasPort!!, ArrayList<Int>())

                } else if (checkbox_upload_to_database.isChecked) {
                    if (imageToUpload == null) {
                        Toast.makeText(this,
                            "Please select an image to upload",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        placeName = input_place_name.text.toString()
                        val hasPort = checkbox_charge_ports.isChecked
                        val hasFood = input_food_avail.text.toString()
                        val specialInfo = input_special_info.text.toString()
                        uploadImage()
                        uploadToFirestore(placeName!!, hasPort, hasFood, selectedLat!!, selectedLng!!, specialInfo)
                    }

                } else {

                    Toast.makeText(
                        this,
                        "You need to Save to device or Share Location to proceed",
                        Toast.LENGTH_LONG
                    ).show()

                }

                Tasks.whenAll(imageUploadTask, firestoreTask).addOnSuccessListener {
                    if (locationSaved!!) {
                        this.finish()
                    }
                }

            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                //cache the image Uri
                imageToUpload = data?.data
                iv_chosen_photo.setImageURI(imageToUpload)
                Toast.makeText(this, "Image Selected", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == SELECT_LOCATION_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                selectedLat = data!!.getDoubleExtra("latToSave", 0.0)
                selectedLng = data!!.getDoubleExtra("lngToSave", 0.0)
                tv_select_on_map.text = "Selected: " + "(" + selectedLat.toString() + ", " + selectedLng.toString() + ")"
            }
        }
    }

    private fun saveLocation(name: String, address: String, latitude: Double, longitude: Double,
                             phoneNum: Int, operatingHours: ArrayList<Int>, hasFood: String, hasPort: Boolean, imagesOfLocation: ArrayList<Int>) {
        val dbHandler = SqliteOpenHelper(this, null)
        val slm = SavedLocationModel(0, name, address, latitude, longitude, phoneNum, operatingHours, hasFood, hasPort, imagesOfLocation)
        val status = dbHandler.addLocation(slm)
        locationSaved = status > 0
        if (status > 0) {
            Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToFirestore(placeName: String, hasPort: Boolean, hasFood: String
                                  , lat: Double, lng: Double, specialInfo: String) {
        val document = hashMapOf(
            "address" to "Not Available",
            "coords" to GeoPoint(lat, lng),
            "cPort" to hasPort,
            "fAvail" to hasFood,
            "oHours" to listOf(-1, -1),
            "pNum" to -1,
            "specialInfo" to specialInfo
        )
        firestoreTask =
        fireStoreInstance.collection(collectionNameToStoreIn)
            .document(placeName).set(document).addOnSuccessListener {
                Toast.makeText(this, "Upload Success", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Upload FAILED", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImage() {
        // loop thru array and use the index as the index for filename
        if (imageToUpload != null) {
            var filePath = fbsReference.child("$placeName/$placeName 1")
            var uploadTask = filePath.putFile(imageToUpload!!)
            imageUploadTask =
            uploadTask.addOnSuccessListener {
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
            }
        }

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
}