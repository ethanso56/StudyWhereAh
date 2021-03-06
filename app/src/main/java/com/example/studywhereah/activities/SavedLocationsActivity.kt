package com.example.studywhereah.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studywhereah.R
import com.example.studywhereah.adapters.SavedLocationsAdaptor
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.SavedLocationModel
import kotlinx.android.synthetic.main.activity_saved_locations.*
import kotlinx.android.synthetic.main.item_saved_locations_row.*

class SavedLocationsActivity : AppCompatActivity() {

    private var nameOfLocation: String? = null
    private var latitudeOfLocation: Double? = null
    private var longitudeOfLocation: Double? = null
    private var addressOfLocation: String? = null
    private var phoneNumber: Int? = null
    // An ArrayList where operatingHours.get(0) is the opening time
    // and operatingHours.get(1) is the closing time
    private var operatingHours = ArrayList<Int>()
    private var hasFood: String? = null
    private var hasPort: Boolean? = null
    private var imagesOfLocation = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_locations)

        setSupportActionBar(toolbar_saved_locations_activity)

        val actionbar = supportActionBar //actionbar
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true) //set back button
            actionbar.title = "SAVED LOCATIONS!" // Setting an title in the action bar.
        }

        toolbar_saved_locations_activity.setNavigationOnClickListener {
            onBackPressed()
        }

        setupListOfDataIntoRecyclerView()

    }

    private fun setupListOfDataIntoRecyclerView() {
        val dbHandler = SqliteOpenHelper(this, null)
        val allSavedLocationsList = dbHandler.viewLocation()

        if (allSavedLocationsList.size > 0) {
            rvSavedLocations.visibility = View.VISIBLE
            tvNoDataAvailable.visibility = View.GONE

            rvSavedLocations.layoutManager = LinearLayoutManager(this)
            val savedLocationsAdaptor = SavedLocationsAdaptor(this, allSavedLocationsList)
            rvSavedLocations.adapter = savedLocationsAdaptor
        } else {
            rvSavedLocations.visibility = View.GONE
            tvNoDataAvailable.visibility = View.VISIBLE
        }
    }

    fun deleteSavedLocationAlertDialog(slm: SavedLocationModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Location")
        builder.setMessage("Are you sure you want to delete?")
        builder.setIcon(R.drawable.ic_warning_black_24dp)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val dbHandler = SqliteOpenHelper(this, null)
            val status = dbHandler.deleteLocation(slm) //1
            if (status > -1) {
                Toast.makeText(applicationContext, "Location deleted successfully.", Toast.LENGTH_SHORT).show()
                setupListOfDataIntoRecyclerView()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // create alert dialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    fun showLocationDetails(slm: SavedLocationModel) {
        nameOfLocation = slm.name
        latitudeOfLocation = slm.latitude
        longitudeOfLocation = slm.longitude
        addressOfLocation = slm.address
        phoneNumber = slm.phoneNum
        operatingHours = slm.operatingHours
        hasFood = slm.hasFood
        hasPort = slm.hasPort
        imagesOfLocation = slm.imagesOfLocation

        val intent = Intent(this, MapsActivity::class.java)
        intent.putExtra(Constants.NAMEOFLOCATION, nameOfLocation)
        intent.putExtra(Constants.LATITUDEOFLOCATION, latitudeOfLocation!!)
        intent.putExtra(Constants.LONGITUDEOFLOCATION, longitudeOfLocation!!)
        intent.putExtra(Constants.ADDRESSOFLOCATION, addressOfLocation)
        intent.putExtra(Constants.IMAGESOFLOCATION, imagesOfLocation)
        intent.putExtra(Constants.OPERATINGHOURS, operatingHours)
        intent.putExtra(Constants.PHONENUMBER, phoneNumber!!)
        intent.putExtra(Constants.FOODAVAILABLE, hasFood!!)
        intent.putExtra(Constants.CHARGINGPORTS, hasPort!!)

        //Line below is to tell MapsActivity when Mapactivity was launched from SavedLocationsActivity
        intent.putExtra("CALLINGACTIVITY", "SavedLocationsActivity")
        startActivity(intent)

    }

}
