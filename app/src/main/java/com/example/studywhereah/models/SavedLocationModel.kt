package com.example.studywhereah.models

class SavedLocationModel(
    val id : Int,
    val name : String,
    val address : String,
    val latitude: Double,
    val longitude: Double,
    val phoneNum: Int,
    val operatingHours: ArrayList<Int>,
    val hasFood: Boolean,
    val hasPort: Boolean,
    val imagesOfLocation: ArrayList<Int>
) {

}