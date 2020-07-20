package com.example.studywhereah.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.example.studywhereah.R
import com.example.studywhereah.adapters.LocationRecommendAdapter
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.LocationModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_locations_recommended.*
import kotlinx.android.synthetic.main.activity_locations_recommended.toolbar_locations_recommended_activity
import kotlinx.android.synthetic.main.activity_maps.*
import java.net.URI
import kotlin.math.*

class LocationsRecommendedActivity : AppCompatActivity() {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0
    var selectedLatitude : Double = 0.0
    var selectedLongitude : Double = 0.0
    var locationsList = ArrayList<LocationModel>()
    // creating a variable collectionRef helps to cache the entire collection onCreate.
    // meaning searches work offline.
    private val fsInstance = Firebase.firestore
    var collectionRef = fsInstance.collection("Study Spots")
    // references for firebase cloud storage
    var storage = com.google.firebase.storage.FirebaseStorage.getInstance()
    var storageRef = storage.reference

    var maxTravelTime : Int = 0
    var crowdLevel : Int = 0
    var foodAvailable : Boolean = true
    var chargingPorts : Boolean = true
    private lateinit var sortedLocationsList : ArrayList<LocationModel>
    // try to replicate the filtering done here, using cloudstore commands

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations_recommended)

        setSupportActionBar(toolbar_locations_recommended_activity)

        val actionbar = supportActionBar //actionbar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true) //set back button
            actionbar.title = "LOCATIONS RECOMMENDED!" // Setting an title in the action bar.
        }

        toolbar_locations_recommended_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        currentLatitude = intent.getDoubleExtra(Constants.CURRENTLATITUDE, 0.0)
        currentLongitude = intent.getDoubleExtra(Constants.CURRENTLONGITUDE, 0.0)
        selectedLatitude = intent.getDoubleExtra(Constants.SELECTEDLATITUDE, 0.0)
        selectedLongitude = intent.getDoubleExtra(Constants.SELECTEDLONGITUDE, 0.0)

        maxTravelTime = intent.getIntExtra(Constants.MAXTRAVELTIME, 0)
        crowdLevel = intent.getIntExtra(Constants.CROWDLEVEL, 0)
        foodAvailable = intent.getBooleanExtra(Constants.FOODAVAILABLE, true)
        chargingPorts = intent.getBooleanExtra(Constants.CHARGINGPORTS, true)

        // Run the recommendation algo
        recoLocation(foodAvailable, chargingPorts, maxTravelTime, crowdLevel)

//        setUpLocationOneView()
//        setUpLocationTwoView()

//        cv_location1.setOnClickListener {
//            val intent = Intent(this, MapsActivity::class.java)
//            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[0].getName())
//            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[0].getLatitude())
//            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[0].getLongitude())
//            intent.putExtra(Constants.ADDRESSOFLOCATION, sortedLocationsList[0].getAddress())
//            intent.putExtra(Constants.IMAGESOFLOCATION, sortedLocationsList[0].getImages())
//            intent.putExtra(Constants.OPERATINGHOURS, sortedLocationsList[0].getOperatingHours())
//            intent.putExtra(Constants.PHONENUMBER, sortedLocationsList[0].getPhoneNum())
//            intent.putExtra(Constants.FOODAVAILABLE, sortedLocationsList[0].getFoodAvailable())
//            intent.putExtra(Constants.CHARGINGPORTS, sortedLocationsList[0].getChargingPorts())
//            //Line below is to tell MapsActivity when Mapactivity was launched from locationsRecommendedActivity
//            intent.putExtra("CALLINGACTIVITY", "LocationsRecommendedActivity")
//            startActivity(intent)
//        }
//
//        cv_location2.setOnClickListener {
//            val intent = Intent(this, MapsActivity::class.java)
//            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[1].getName())
//            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[1].getLatitude())
//            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[1].getLongitude())
//            intent.putExtra(Constants.ADDRESSOFLOCATION, sortedLocationsList[1].getAddress())
//            intent.putExtra(Constants.IMAGESOFLOCATION, sortedLocationsList[1].getImages())
//            intent.putExtra(Constants.OPERATINGHOURS, sortedLocationsList[1].getOperatingHours())
//            intent.putExtra(Constants.PHONENUMBER, sortedLocationsList[1].getPhoneNum())
//            intent.putExtra(Constants.FOODAVAILABLE, sortedLocationsList[1].getFoodAvailable())
//            intent.putExtra(Constants.CHARGINGPORTS, sortedLocationsList[1].getChargingPorts())
//            //Line below is to tell MapsActivity when Mapactivity was launched from locationsRecommendedActivity
//            intent.putExtra("CALLINGACTIVITY", "LocationsRecommendedActivity")
//            startActivity(intent)
//        }

    }

    // based on the set user preferences, queries cloud firestore with the filters and
    // creates locationModel objects to be stored in the locationsList arraylist.

    //create a "Waiting" spinner when data is being fetched.
    @RequiresApi(Build.VERSION_CODES.N)
    private fun recoLocation(foodAvail: Boolean, chargingPorts: Boolean,
                             maxTravelTime: Int, crowdLevel: Int) {
        pb_firestore_loading.visibility = View.VISIBLE
        // use firebase's where() method which has query of time complexity O(num of results)
        // problem is for fAvail i want to provide information apart from "true" but query
        // is complicated.

        fun retrieveLocationImages(placeName: String) {
            var arrOfImages = ArrayList<Uri>()
            var photoRetrievalIndex = 1
            var path = "$placeName/$placeName $photoRetrievalIndex.jpg"
//            var imageURI = storageRef
//                .child("$placeName/$placeName $photoRetrievalIndex.jpg").downloadUrl
            // add the imageURIs to the arrList
            storageRef.child(path).listAll().addOnSuccessListener {
                var listOfStorageRefs: List<StorageReference> = it.items
                for (i in 0 until listOfStorageRefs.size-1 step 1) {
                    listOfStorageRefs[i].downloadUrl.addOnSuccessListener { uriObj ->
                        arrOfImages.add(uriObj)
                    }
                }
            }

            Log.e("path used: ", path)
            Log.e("images $placeName", arrOfImages.toString())
        }

        if (foodAvail && chargingPorts) {
            collectionRef.whereEqualTo("fAvail", "Study spot located in a mall")
                .whereEqualTo("cPort", true).get().addOnSuccessListener { documents ->
                    pb_firestore_loading.visibility = View.INVISIBLE
                    tv_firebase_loading_msg.visibility = View.INVISIBLE
                    for (document in documents) {
                        val placeName = document.id
                        val address = document.get("address") as String
                        val coords = document.get("coords") as GeoPoint
                        val phoneNumber = document.get("pNum") as Number
                        val operatingHours= document.get("oHours") as ArrayList<Number>
                        val fAvail = document.get("fAvail") as String
                        val cPort = document.get("cPort") as Boolean
                        // retrieve all available images using placeName.
//                        retrieveLocationImages(placeName)

                        // crowdlevel 0 is low
                        // imageArrList is set to null here and reSet later when sortedLocationsList
                        // is fully initialised
                        var lModel = LocationModel(
                            placeName, address, coords.latitude, coords.longitude,
                            0.0,
                            phoneNumber, operatingHours, fAvail, cPort, 0)
                        locationsList.add(lModel)
                    }
                    calculateDistanceAndSetPropertyForAllLocations(locationsList)

                    //******** Bottom two if chunks CAN BE IMPROVED maybe filter when querying and only add if satisfied.
                    if (maxTravelTime == 0) {
                        // remove all locations > 4km
                        locationsList.removeIf { it.getDistanceToUser() > 4 }
                    } else if (maxTravelTime == 1) {
                        // remove all locations > 8km
                        locationsList.removeIf { it.getDistanceToUser() > 8 }
                    } else if (maxTravelTime == 2) {
                        // remove all locations > 12km
                        locationsList.removeIf { it.getDistanceToUser() > 12 }
                    }

                    // we need a update crowd level based on current time method

                    if (crowdLevel == 0) {
                        // remove all locations that have "mid" and "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 0 }
                    } else if (crowdLevel == 1) {
                        // remove all locations that have "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 1 }
                    }

                    sortedLocationsList =
                        ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))

                    //only retrieve images after all processing is complete
//                    for (i in 0 until sortedLocationsList.size step 1) {
//                        var lModel = sortedLocationsList[i]
//                        var imageUri = ""
//                        var imagesArrList = ArrayList<Task<ByteArray>>()
//                        var placeName = lModel.getName()
//                        // we have designated placeName 2.png as the preview photo
////                        var path = "$placeName/$placeName 2.png"
//                        var allStorageRefs = storageRef.child(placeName.toString()).listAll()
//
//                        // maxDownloadSizeBytes = 300kb
////                        var sr = storageRef.child(path).getBytes(300*1000)
////                        imagesArrList.add(sr)
//                        lModel.setImages(allStorageRefs)
////                        Log.e("arr", lModel.getImages().toString())
//                    }


                    val adapter = LocationRecommendAdapter(this,
                        R.layout.row_item, sortedLocationsList)
                    content_main_list_view.adapter = adapter
                    if (sortedLocationsList.size == 0) {
                        tv_fail_to_load.visibility = View.VISIBLE
                        tv_firebase_loading_msg.visibility = View.VISIBLE
                        tv_firebase_loading_msg.text = "We apologize as there are \nno suitable spots for you!"
                    }
                }
        } else if (foodAvail) {
            // as "fAvail" is a string more values may exist.
            collectionRef.whereEqualTo("fAvail", "Study spot located in a mall")
                .get().addOnSuccessListener { documents ->
                    pb_firestore_loading.visibility = View.INVISIBLE
                    tv_firebase_loading_msg.visibility = View.INVISIBLE
                    for (document in documents) {
                        val placeName = document.id
                        val address = document.get("address") as String
                        val coords = document.get("coords") as GeoPoint
                        val phoneNumber = document.get("pNum") as Int
                        val operatingHours= document.get("oHours") as ArrayList<Number>
                        val fAvail = document.get("fAvail") as String
                        val cPort = document.get("cPort") as Boolean
                        //note that the images are set to a default as cloud storage is not set up yet
                        // crowdlevel 0 is low
                        var lModel = LocationModel(placeName, address, coords.latitude,
                            coords.longitude, 0.0,
                            phoneNumber, operatingHours, fAvail, cPort, 0)
                        locationsList.add(lModel)
                    }
                    calculateDistanceAndSetPropertyForAllLocations(locationsList)

                    //CAN BE IMPROVED HAHA
                    if (maxTravelTime == 0) {
                        // remove all locations > 4km
                        locationsList.removeIf { it.getDistanceToUser() > 4 }
                    } else if (maxTravelTime == 1) {
                        // remove all locations > 8km
                        locationsList.removeIf { it.getDistanceToUser() > 8 }
                    } else if (maxTravelTime == 2) {
                        // remove all locations > 12km
                        locationsList.removeIf { it.getDistanceToUser() > 12 }
                    }

                    // we need a update crowd level based on current time method

                    if (crowdLevel == 0) {
                        // remove all locations that have "mid" and "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 0 }
                    } else if (crowdLevel == 1) {
                        // remove all locations that have "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 1 }
                    }

                    sortedLocationsList =
                        ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))
                    val adapter = LocationRecommendAdapter(this,
                        R.layout.row_item, sortedLocationsList)
                    content_main_list_view.adapter = adapter
                    if (sortedLocationsList.size == 0) {
                        tv_fail_to_load.visibility = View.VISIBLE
                        tv_firebase_loading_msg.visibility = View.VISIBLE
                        tv_firebase_loading_msg.text = "We apologize as there are no suitable spots for you!"
                    }

                }
        } else if (chargingPorts) {
            collectionRef.whereEqualTo("cPort", true)
                .get().addOnSuccessListener { documents ->
                    pb_firestore_loading.visibility = View.INVISIBLE
                    tv_firebase_loading_msg.visibility = View.INVISIBLE
                    for (document in documents) {
                        val placeName = document.id
                        val address = document.get("address") as String
                        val coords = document.get("coords") as GeoPoint
                        val phoneNumber = document.get("pNum") as Number
                        val operatingHours= document.get("oHours") as ArrayList<Number>
                        val fAvail = document.get("fAvail") as String
                        val cPort = document.get("cPort") as Boolean
                        //note that the images are set to a default as cloud storage is not set up yet
                        // crowdlevel 0 is low
                        var lModel = LocationModel(placeName, address, coords.latitude,
                            coords.longitude, 0.0,
                            phoneNumber, operatingHours, fAvail, cPort, 0)
                        locationsList.add(lModel)
                    }
                    calculateDistanceAndSetPropertyForAllLocations(locationsList)

                    //CAN BE IMPROVED HAHA
                    if (maxTravelTime == 0) {
                        // remove all locations > 4km
                        locationsList.removeIf { it.getDistanceToUser() > 4 }
                    } else if (maxTravelTime == 1) {
                        // remove all locations > 8km
                        locationsList.removeIf { it.getDistanceToUser() > 8 }
                    } else if (maxTravelTime == 2) {
                        // remove all locations > 12km
                        locationsList.removeIf { it.getDistanceToUser() > 12 }
                    }

                    // we need a update crowd level based on current time method

                    if (crowdLevel == 0) {
                        // remove all locations that have "mid" and "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 0 }
                    } else if (crowdLevel == 1) {
                        // remove all locations that have "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 1 }
                    }

                    sortedLocationsList =
                        ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))
                    val adapter = LocationRecommendAdapter(this,
                        R.layout.row_item, sortedLocationsList)
                    content_main_list_view.adapter = adapter
                    if (sortedLocationsList.size == 0) {
                        tv_fail_to_load.visibility = View.VISIBLE
                        tv_firebase_loading_msg.visibility = View.VISIBLE
                        tv_firebase_loading_msg.text = "We apologize as there are no suitable spots for you!"
                    }
                }
        } else {
            //return all locations
            collectionRef.get()
                .addOnSuccessListener { documents ->
                    pb_firestore_loading.visibility = View.INVISIBLE
                    tv_firebase_loading_msg.visibility = View.INVISIBLE
                    for (document in documents) {
                        val placeName = document.id
                        val address = document.get("address") as String
                        val coords = document.get("coords") as GeoPoint
                        val phoneNumber = document.get("pNum") as Number
                        val operatingHours= document.get("oHours") as ArrayList<Number>
                        val fAvail = document.get("fAvail") as String
                        val cPort = document.get("cPort") as Boolean
                        //note that the images are set to a default as cloud storage is not set up yet
                        // crowdlevel 0 is low
                        var lModel = LocationModel(placeName, address, coords.latitude,
                            coords.longitude, 0.0,
                            phoneNumber, operatingHours, fAvail, cPort, 0)
                        locationsList.add(lModel)
                    }
                    calculateDistanceAndSetPropertyForAllLocations(locationsList)

                    //CAN BE IMPROVED HAHA
                    if (maxTravelTime == 0) {
                        // remove all locations > 4km
                        locationsList.removeIf { it.getDistanceToUser() > 4 }
                    } else if (maxTravelTime == 1) {
                        // remove all locations > 8km
                        locationsList.removeIf { it.getDistanceToUser() > 8 }
                    } else if (maxTravelTime == 2) {
                        // remove all locations > 12km
                        locationsList.removeIf { it.getDistanceToUser() > 12 }
                    }

                    // we need a update crowd level based on current time method

                    if (crowdLevel == 0) {
                        // remove all locations that have "mid" and "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 0 }
                    } else if (crowdLevel == 1) {
                        // remove all locations that have "high" levels
                        locationsList.removeIf { it.getCrowdLevel() > 1 }
                    }

                    sortedLocationsList =
                        ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))
                    val adapter = LocationRecommendAdapter(applicationContext,
                        R.layout.row_item, sortedLocationsList)
                    content_main_list_view.adapter = adapter

                    if (sortedLocationsList.size == 0) {
                        tv_fail_to_load.visibility = View.VISIBLE
                        tv_firebase_loading_msg.visibility = View.VISIBLE
                        tv_firebase_loading_msg.text = "We apologize as there are no suitable spots for you!"
                    }
                }
        }
    }

    private fun isCurrentLocation() : Boolean {
        return !(selectedLatitude > 0 || selectedLongitude > 0)
    }

    private fun calculateDistanceToLocation(locationLatitude: Double, locationLongitude: Double) : Double{
        val radiusOfEarth = 6371
        val dLat = degToRad((if (isCurrentLocation()) currentLatitude else selectedLatitude) - locationLatitude)
        val dLon = degToRad((if (isCurrentLocation()) currentLongitude else selectedLongitude) - locationLongitude)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(degToRad(locationLatitude)) * cos(degToRad(currentLatitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = radiusOfEarth * c
        return d
    }

    private fun degToRad(deg: Double) : Double {
        return deg * (Math.PI/180)
    }

    private fun calculateDistanceAndSetPropertyForAllLocations(locations : ArrayList<LocationModel>) {
        for (location in locations) {
            var distance = calculateDistanceToLocation(location.getLatitude(), location.getLongitude())
            location.setDistanceToUser(distance)
        }
    }

//    private fun setUpLocationOneView() {
//        tv_location1Title.text = sortedLocationsList[0].getName()
//        tv_location1Address.text = "• Address: " + sortedLocationsList[0].getAddress()
//        val phoneNum = sortedLocationsList[0].getPhoneNum()
//        if (phoneNum == -1) {
//            tv_phoneNum1.text = "• Phone number: Not Available"
//        } else {
//            tv_phoneNum1.text = "• Phone number: " + phoneNum.toString()
//        }
//        val openTime = sortedLocationsList[0].getOperatingHours()[0]
//        val closeTime = sortedLocationsList[0].getOperatingHours()[1]
//        tv_operatingHours1.text = "• Operating hours: " + openTime +" to " + closeTime
//        iv_location1.setImageResource(sortedLocationsList[0].getImages()[0])
//        Log.e("sLocationList size: ", sortedLocationsList.size.toString())
//    }
//
//    private fun setUpLocationTwoView() {
//        tv_location2Title.text = sortedLocationsList[1].getName()
//        tv_location2Address.text = "• Address: " + sortedLocationsList[1].getAddress()
//        val phoneNum = sortedLocationsList[1].getPhoneNum()
//        if (phoneNum == -1) {
//            tv_phoneNum2.text = "• Phone number: Not Available"
//        } else {
//            tv_phoneNum2.text = "• Phone number: " + phoneNum.toString()
//        }
//        tv_operatingHours2.text = "• Operating hours: " + sortedLocationsList[1].getOperatingHours()
//        val openTime = sortedLocationsList[1].getOperatingHours()[0]
//        val closeTime = sortedLocationsList[1].getOperatingHours()[1]
//        tv_operatingHours2.text = "• Operating hours: " + openTime +" to " + closeTime
//        iv_location2.setImageResource(sortedLocationsList[1].getImages()[0])
//    }

}
