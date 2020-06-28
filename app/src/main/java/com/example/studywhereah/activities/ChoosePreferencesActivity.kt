package com.example.studywhereah.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.DropBoxManager
import android.util.Log
import android.view.View
import android.widget.*
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import kotlinx.android.synthetic.main.activity_choose_preferences.*
import java.nio.DoubleBuffer

class ChoosePreferencesActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0
    var selectedLatitude : Double = 0.0
    var selectedLongitude : Double = 0.0
    var maxTravelTime : Int = 0
    var crowdLevel : Int = 0
    var foodAvailable : Boolean = true
    var chargingPorts : Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_preferences)

        currentLatitude = intent.getDoubleExtra(Constants.CURRENTLATITUDE, 0.0)
        currentLongitude = intent.getDoubleExtra(Constants.CURRENTLONGITUDE, 0.0)
        selectedLatitude = intent.getDoubleExtra(Constants.SELECTEDLATITUDE, 0.0)
        selectedLongitude = intent.getDoubleExtra(Constants.SELECTEDLONGITUDE, 0.0)

        // set up travel timing spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.timing_arrays,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            travel_timing.adapter = adapter
        }
        travel_timing.onItemSelectedListener = this

        // set up crowd timing spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.crowd_arrays,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            crowd_timing.adapter = adapter
        }
        crowd_timing.onItemSelectedListener = this

        btn_search.setOnClickListener {
            val intent = Intent(this, LocationsRecommendedActivity::class.java)
            intent.putExtra(Constants.CURRENTLATITUDE, currentLatitude)
            intent.putExtra(Constants.CURRENTLONGITUDE, currentLongitude)
            intent.putExtra(Constants.SELECTEDLATITUDE, selectedLatitude)
            intent.putExtra(Constants.SELECTEDLONGITUDE, selectedLongitude)
            intent.putExtra(Constants.MAXTRAVELTIME, maxTravelTime)
            intent.putExtra(Constants.CROWDLEVEL, crowdLevel)
            intent.putExtra(Constants.FOODAVAILABLE, foodAvailable)
            intent.putExtra(Constants.CHARGINGPORTS, chargingPorts)
            startActivity(intent)
        }
    }

//    open class TravelTimingSpinnerClass : AdapterView.OnItemSelectedListener {
//        override fun onNothingSelected(parent: AdapterView<*>?) {
////            Toast.makeText(TravelTimingSpinnerClass, "nothing selected", Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//            maxTravelTime = position
//            Log.d("selected", position.toString())
//        }
//
//    }
//
//    open class CrowdednessSpinnerClass : AdapterView.OnItemSelectedListener {
//        override fun onNothingSelected(parent: AdapterView<*>?) {
////            Toast.makeText(this, "nothing selected", Toast.LENGTH_SHORT).show()        }
//        }
//
//        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//            Log.d("selected2", position.toString())
//        }
//
//    }

        fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.checkbox_food_available -> {
                    if (checked) {

                    } else {
                        foodAvailable = false
                    }
                }
                R.id.checkbox_charging_ports -> {
                    if (checked) {

                    } else {
                        chargingPorts = false
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "nothing selected", Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner : Spinner = parent as Spinner
        if (spinner.id == R.id.travel_timing) {
            maxTravelTime = 100
//            Toast.makeText(this, "$position selected1", Toast.LENGTH_SHORT).show()
        } else {
            crowdLevel = position
//            Toast.makeText(this, "$position selected2", Toast.LENGTH_SHORT).show()
        }
    }
}
