package com.example.studywhereah.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.LocationModel
import kotlinx.android.synthetic.main.activity_locations_recommended.*
import kotlin.math.*

class LocationsRecommendedActivity : AppCompatActivity() {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0
    var selectedLatitude : Double = 0.0
    var selectedLongitude : Double = 0.0
    var locationsList = Constants.getLocationList()
    var maxTravelTime : Int = 0
    var crowdLevel : Int = 0
    var foodAvailable : Boolean = true
    var chargingPorts : Boolean = true
    private lateinit var sortedLocationsList : ArrayList<LocationModel>


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

        if (foodAvailable) {
            locationsList.removeIf { !it.getFoodAvailable() }
        }
        if (chargingPorts) {
            locationsList.removeIf { !it.getChargingPorts() }
        }

        calculateDistanceAndSetPropertyForAllLocations(locationsList)

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

        if (crowdLevel == 0) {
            // remove all locations that have "mid" and "high" levels
            locationsList.removeIf { it.getCrowdLevel() > 0 }
        } else if (crowdLevel == 1) {
            // remove all locations that have "high" levels
            locationsList.removeIf { it.getCrowdLevel() > 1 }
        }

        sortedLocationsList = ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))
        setUpLocationOneView()
        setUpLocationTwoView()

        cv_location1.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[0].getName())
            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[0].getLatitude())
            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[0].getLongitude())
            intent.putExtra(Constants.ADDRESSOFLOCATION, sortedLocationsList[0].getAddress())
            intent.putExtra(Constants.IMAGESOFLOCATION, sortedLocationsList[0].getImages())
            intent.putExtra(Constants.OPERATINGHOURS, sortedLocationsList[0].getOperatingHours())
            intent.putExtra(Constants.PHONENUMBER, sortedLocationsList[0].getPhoneNum())
            intent.putExtra(Constants.FOODAVAILABLE, sortedLocationsList[0].getFoodAvailable())
            intent.putExtra(Constants.CHARGINGPORTS, sortedLocationsList[0].getChargingPorts())
            //Line below is to tell MapsActivity when Mapactivity was launched from locationsRecommendedActivity
            intent.putExtra("CALLINGACTIVITY", "LocationsRecommendedActivity")
            startActivity(intent)
        }

        cv_location2.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[1].getName())
            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[1].getLatitude())
            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[1].getLongitude())
            intent.putExtra(Constants.ADDRESSOFLOCATION, sortedLocationsList[1].getAddress())
            intent.putExtra(Constants.IMAGESOFLOCATION, sortedLocationsList[1].getImages())
            intent.putExtra(Constants.OPERATINGHOURS, sortedLocationsList[1].getOperatingHours())
            intent.putExtra(Constants.PHONENUMBER, sortedLocationsList[1].getPhoneNum())
            intent.putExtra(Constants.FOODAVAILABLE, sortedLocationsList[1].getFoodAvailable())
            intent.putExtra(Constants.CHARGINGPORTS, sortedLocationsList[1].getChargingPorts())
            //Line below is to tell MapsActivity when Mapactivity was launched from locationsRecommendedActivity
            intent.putExtra("CALLINGACTIVITY", "LocationsRecommendedActivity")
            startActivity(intent)
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

    private fun setUpLocationOneView() {
        tv_location1Title.text = sortedLocationsList[0].getName()
//        iv_location1.setImageResource(sortedLocationsList[0].getImage1())
        tv_location1Address.text = "• Address: " + sortedLocationsList[0].getAddress()
        val phoneNum = sortedLocationsList[0].getPhoneNum()
        if (phoneNum == -1) {
            tv_phoneNum1.text = "• Phone number: Not Available"
        } else {
            tv_phoneNum1.text = "• Phone number: " + phoneNum.toString()
        }
        val openTime = sortedLocationsList[0].getOperatingHours()[0]
        val closeTime = sortedLocationsList[0].getOperatingHours()[1]
        tv_operatingHours1.text = "• Operating hours: " + openTime +" to " + closeTime
        iv_location1.setImageResource(sortedLocationsList[0].getImages()[0])

    }

    private fun setUpLocationTwoView() {
        tv_location2Title.text = sortedLocationsList[1].getName()
//        iv_location2.setImageResource(sortedLocationsList[1].getImage1())
        tv_location2Address.text = "• Address: " + sortedLocationsList[1].getAddress()
        val phoneNum = sortedLocationsList[1].getPhoneNum()
        if (phoneNum == -1) {
            tv_phoneNum2.text = "• Phone number: Not Available"
        } else {
            tv_phoneNum2.text = "• Phone number: " + phoneNum.toString()
        }
        tv_operatingHours2.text = "• Operating hours: " + sortedLocationsList[1].getOperatingHours()
        val openTime = sortedLocationsList[1].getOperatingHours()[0]
        val closeTime = sortedLocationsList[1].getOperatingHours()[1]
        tv_operatingHours2.text = "• Operating hours: " + openTime +" to " + closeTime
        iv_location2.setImageResource(sortedLocationsList[1].getImages()[0])
    }

}
