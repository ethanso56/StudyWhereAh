package com.example.studywhereah.models

class LocationModel(
    private val name: String,
    private val address: String,
    private val latitude: Double,
    private val longitude: Double,
    private var distanceToUser: Double,
    private var imageArrList: ArrayList<Int>,
    private val phoneNum: Int,
    private val operatingHours: String,
    private val foodAvailable: Boolean,
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

    fun getPhoneNum() : Int {
        return phoneNum
    }

    fun getOperatingHours() : String {
        return operatingHours
    }

    fun getFoodAvailable() : Boolean {
        return foodAvailable
    }

    fun getChargingPorts() : Boolean {
        return chargingPorts
    }

    fun getCrowdLevel() : Int {
        return crowdLevel
    }

}

