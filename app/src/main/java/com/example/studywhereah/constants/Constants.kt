package com.example.studywhereah.constants

import com.example.studywhereah.R
import com.example.studywhereah.models.LocationModel

class Constants {


    companion object {
        const val CURRENTLATITUDE: String = "current_latitude"
        const val CURRENTLONGITUDE: String = "current_longitude"
        const val NAMEOFLOCATION: String = "name_of_location"
        const val LATITUDEOFLOCATION: String = "latitude_of_location"
        const val LONGITUDEOFLOCATION: String = "longitude_of_location"
        const val IMAGEOFLOCATION1: String = "image_of_location1"
        const val IMAGEOFLOCATION2: String = "image_of_location2"

        fun getLocationList() : ArrayList<LocationModel> {
            val locationList = ArrayList<LocationModel>()

            val clementiLibrary = LocationModel(
                "Clementi Library",
                "3155 Commonwealth Avenue West #05-13/14/15 The Clementi Mall, 129588",
                1.314942,
                103.764027,
                0.0,
                R.drawable.img_clementi_library1,
                R.drawable.img_clementi_library2,
                -1,
                "10am to 9pm",
                true,
                true
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
                true
            )
            locationList.add(bedokLibrary)

            return locationList
        }
    }
}