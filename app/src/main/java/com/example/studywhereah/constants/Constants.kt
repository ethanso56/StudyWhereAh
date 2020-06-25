package com.example.studywhereah.constants

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.studywhereah.R
import com.example.studywhereah.models.LocationModel
import com.google.android.libraries.places.api.model.OpeningHours
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Constants {


    companion object {
        //the following const vals are keys to access values passed on by intents via the putExtra() methods
        const val CURRENTLATITUDE: String = "current_latitude"
        const val CURRENTLONGITUDE: String = "current_longitude"
        const val SELECTEDLATITUDE : String = "selected_location_latitude"
        const val SELECTEDLONGITUDE : String = "selected_location_longitude"
        const val NAMEOFLOCATION: String = "name_of_location"
        const val LATITUDEOFLOCATION: String = "latitude_of_location"
        const val LONGITUDEOFLOCATION: String = "longitude_of_location"
        const val IMAGEOFLOCATION1: String = "image_of_location1"
        const val IMAGEOFLOCATION2: String = "image_of_location2"
        const val MAXTRAVELTIME: String = "max_travel_time"
        const val CROWDLEVEL : String = "crowd_level"
        const val FOODAVAILABLE: String = "food_available"
        const val CHARGINGPORTS: String = "charging_ports"
        const val RATING: String = "location_rating"
        const val OPENINGHOURS: String = "location_opening_hours"
        const val USERRATINGSTOTAL: String = "location_user_rating_total"
        const val PHONENUMBER: String = "location_phone_number"

        @RequiresApi(Build.VERSION_CODES.O)
        val currentTime: LocalDateTime = LocalDateTime.now()
        @RequiresApi(Build.VERSION_CODES.O)
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH")
        @RequiresApi(Build.VERSION_CODES.O)
        val formatDateCurrentTime: String = currentTime.format(formatter)

        private fun timeToCrowd(name : String, time : String) : Int {
            val timeInInt : Int = time.toInt()

            // add names to change locations
            return if (name == "library") {
                if (timeInInt < 10) {
                    0 // "low" level
                } else if (timeInInt < 12 || timeInInt > 18) {
                    1 // "mid" level
                } else {
                    2 // "high" level
                }
            } else {
                -1
            }

        }


        fun getLocationList() : ArrayList<LocationModel> {
            Log.d("time", formatDateCurrentTime)
            val locationList = ArrayList<LocationModel>()

            val clementiLibrary = LocationModel(
                "Clementi Library",
                "3155 Commonwealth Avenue West #05-13/14/15 The Clementi Mall, S129588",
                1.314942,
                103.764027,
                0.0,
                R.drawable.img_clementi_library1,
                R.drawable.img_clementi_library2,
                -1,
                "10am to 9pm",
                true,
                true,
                timeToCrowd("library", formatDateCurrentTime)
            )
            locationList.add(clementiLibrary)

            val bedokLibrary = LocationModel(
                "Bedok Library",
                "11 Bedok North Street 1, #02-03 & #03-04, Heartbeat@Bedok, S469662",
                1.327179,
                103.931762,
                0.0,
                R.drawable.img_bedok_library1,
                R.drawable.img_bedok_library1,
                63323255,
                "10am to 9pm",
                true,
                true,
                timeToCrowd("library", formatDateCurrentTime)

            )
            locationList.add(bedokLibrary)

            val yishunLibrary = LocationModel(
                "Yishun Library, ",
                "930 Yishun Ave 2, #04-01 North Wing, Northpoint City, S769098",
                1.429656,
                103.835770,
                0.0,
                R.drawable.img_yishun_library1,
                R.drawable.img_yishun_library2,
                -1,
                "10am to 9pm",
                true,
                true,
                timeToCrowd("library", formatDateCurrentTime)
            )
            locationList.add(yishunLibrary)
            return locationList
        }
    }


}