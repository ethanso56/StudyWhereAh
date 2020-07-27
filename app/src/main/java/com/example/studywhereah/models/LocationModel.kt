package com.example.studywhereah.models

import android.graphics.drawable.Drawable
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference

class LocationModel(
    private val name: String,
    private val address: String,
    private val latitude: Double,
    private val longitude: Double,
    // distanceToUser default value is 0.0 until method
    // calculateDistanceAndSetPropertyForAllLocations is called
    private var distanceToUser: Double,
    private val phoneNum: Number,
    private val operatingHours: ArrayList<Number>,
    private val foodAvailable: String,
    private val chargingPorts: Boolean,
    private val specialInfo: String,
    private val crowdLevel: Int
) {

    //each location model has access to the firebase storage.
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    fun getName() : String {
        return name
    }

    fun getAddress() : String {
        return address
    }

    // helper function to download preview image as a byte array
//    fun getPreviewImage(): Task<ByteArray> {
//        // we use the first image as the preview image, note that the photos in firebase storage
//        // have to be .png format.
//
//        var listResultTask = getImagesTask()
//        var task = listResultTask.addOnSuccessListener { listResult ->
//            var list = listResult.items
//            // use the first photo as the preview image
//            return list.get(0).getBytes(500 * 1000).addOnSuccessListener
//
//
//        }
//    }

    fun getImagesTask() : Task<ListResult> {
        var queryPath = name.toString()
        return storageRef.child(queryPath).listAll()
    }


    fun getLatitude() : Double {
        return latitude
    }

    fun getLongitude() : Double {
        return longitude
    }

    fun getDistanceToUser() : Double {
        return distanceToUser
    }

    fun setDistanceToUser(distance: Double) {
        this.distanceToUser = distance
    }

    fun getPhoneNum() : Number {
        return phoneNum
    }

    fun getOperatingHours() : ArrayList<Number> {
        return operatingHours
    }

    fun getFoodAvailable() : String {
        return foodAvailable
    }

    fun getChargingPorts() : Boolean {
        return chargingPorts
    }

    fun getSpecialInfo() : String {
        return specialInfo
    }

    // determines the current system time and estimates the crowd level.
    fun getCrowdLevel() : Int {
        return crowdLevel
    }

}

