package com.example.studywhereah.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.LocationModel
import kotlinx.android.synthetic.main.activity_locations_recommended.*
import kotlin.math.*

class LocationsRecommendedActivity : AppCompatActivity() {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0
    var locationsList = Constants.getLocationList()
    private lateinit var sortedLocationsList : ArrayList<LocationModel>


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

        calculateDistanceAndSetPropertyForAllLocations(locationsList)
        sortedLocationsList = ArrayList(locationsList.sortedWith(compareBy { it.getDistanceToUser() }))
        setUpLocationOneView()
        setUpLocationTwoView()

        cv_location1.setOnClickListener {
            val intent = Intent(this, LocationDetailsActivity::class.java)
            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[0].getName())
            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[0].getLatitude())
            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[0].getLongitude())
            intent.putExtra(Constants.IMAGEOFLOCATION1, sortedLocationsList[0].getImage1())
            intent.putExtra(Constants.IMAGEOFLOCATION2, sortedLocationsList[0].getImage2())
            startActivity(intent)
        }

        cv_location2.setOnClickListener {
            val intent = Intent(this, LocationDetailsActivity::class.java)
            intent.putExtra(Constants.NAMEOFLOCATION, sortedLocationsList[1].getName())
            intent.putExtra(Constants.LATITUDEOFLOCATION, sortedLocationsList[1].getLatitude())
            intent.putExtra(Constants.LONGITUDEOFLOCATION, sortedLocationsList[1].getLongitude())
            intent.putExtra(Constants.IMAGEOFLOCATION1, sortedLocationsList[1].getImage1())
            intent.putExtra(Constants.IMAGEOFLOCATION2, sortedLocationsList[1].getImage2())
            startActivity(intent)
        }
    }

    private fun calculateDistanceToLocation(locationLatitude: Double, locationLongitude: Double) : Double{
        var radiusOfEarth = 6371
        var dLat = degToRad(currentLatitude - locationLatitude)
        var dLon = degToRad(currentLongitude - locationLongitude)
        var a = sin(dLat / 2) * sin(dLat / 2) + cos(degToRad(locationLatitude)) * cos(degToRad(currentLatitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        var c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var d = radiusOfEarth * c
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
        iv_location1.setImageResource(sortedLocationsList[0].getImage1())
        tv_location1Address.text = "• Address: " + sortedLocationsList[0].getAddress()
        tv_phoneNum1.text = "• Phone number: " + sortedLocationsList[0].getPhoneNum().toString()
        tv_operatingHours1.text = "• Operating hours: " + sortedLocationsList[0].getOperatingHours()

    }

    private fun setUpLocationTwoView() {
        tv_location2Title.text = sortedLocationsList[1].getName()
        iv_location2.setImageResource(sortedLocationsList[1].getImage1())
        tv_location2Address.text = "• Address: " + sortedLocationsList[1].getAddress()
        tv_phoneNum2.text = "• Phone number: " + sortedLocationsList[1].getPhoneNum().toString()
        tv_operatingHours2.text = "• Operating hours: " + sortedLocationsList[1].getOperatingHours()

    }

}
