package com.example.studywhereah.activities

import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import com.example.studywhereah.R
import com.example.studywhereah.adapters.LocationRecommendAdapter
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.LocationModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_locations_recommended.*
import kotlinx.android.synthetic.main.activity_locations_recommended.toolbar_locations_recommended_activity
import kotlin.math.*

class LocationsRecommendedActivity : AppCompatActivity() {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0
    var selectedLatitude : Double = 0.0
    var selectedLongitude : Double = 0.0
    var locationsList = ArrayList<LocationModel>()
    private val fsInstance = Firebase.firestore
    var collectionRef = fsInstance.collection("Study Spots")
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
        // use firebase's where() method which has query of time complexity O(num of results)
        if (foodAvailable && chargingPorts) {
            collectionRef.whereEqualTo("fAvail", true)
                .whereEqualTo("cPort", true).get().addOnSuccessListener { documents ->
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
                        var lModel = LocationModel(
                            placeName, address, coords.latitude, coords.longitude,
                            0.0,
                            ArrayList(listOf(
                                R.drawable.img_bedok_library1,
                                R.drawable.img_bedok_library1)
                            ), phoneNumber, operatingHours, fAvail, cPort, 0)
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

                }
        } else if (foodAvailable) {
            collectionRef.whereEqualTo("fAvail", true)
                .get().addOnSuccessListener { documents ->
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
                            ArrayList(listOf(
                                R.drawable.img_bedok_library1,
                                R.drawable.img_bedok_library1)
                            ), phoneNumber, operatingHours, fAvail, cPort, 0)
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

                }
        } else if (chargingPorts) {
            collectionRef.whereEqualTo("cPort", true)
                .get().addOnSuccessListener { documents ->
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
                            ArrayList(listOf(
                                R.drawable.img_bedok_library1,
                                R.drawable.img_bedok_library1)
                            ), phoneNumber, operatingHours, fAvail, cPort, 0)
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
                }
        } else {
            //return all locations
            collectionRef.get()
                .addOnSuccessListener { documents ->
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
                            ArrayList(listOf(
                                R.drawable.img_bedok_library1,
                                R.drawable.img_bedok_library1)
                            ), phoneNumber, operatingHours, fAvail, cPort, 0)
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
