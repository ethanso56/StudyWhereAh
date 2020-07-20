package com.example.studywhereah.models

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult

// imagesOfLocation is a cache of the photos, we may remove if we find it to be pointless.
class SavedLocationModel(
    val id : Int,
    val name : String,
    val address : String,
    val latitude: Double,
    val longitude: Double,
    val phoneNum: Int,
    val operatingHours: ArrayList<Int>,
    val hasFood: String,
    val hasPort: Boolean,
    val imagesOfLocation: ArrayList<Int>
) {

}