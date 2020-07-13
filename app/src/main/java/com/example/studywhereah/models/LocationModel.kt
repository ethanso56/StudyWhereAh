package com.example.studywhereah.models

class LocationModel(
    private val name: String,
    private val address: String,
    private val latitude: Double,
    private val longitude: Double,
    // distanceToUser default value is 0.0 until method
    // calculateDistanceAndSetPropertyForAllLocations is called
    private var distanceToUser: Double,
    private var imageArrList: ArrayList<Int>,
    private val phoneNum: Number,
    private val operatingHours: ArrayList<Number>,
    private val foodAvailable: String,
    private val chargingPorts: Boolean,
    private val crowdLevel: Int
) {

    fun getName() : String {
        return name
    }

    fun getAddress() : String {
        return address
    }

    fun getImages() : ArrayList<Int> {
        return imageArrList
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

    // determines the current system time and estimates the crowd level.
    fun getCrowdLevel() : Int {
        return crowdLevel
    }

}

