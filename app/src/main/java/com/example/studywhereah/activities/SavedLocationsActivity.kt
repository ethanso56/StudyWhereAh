package com.example.studywhereah.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studywhereah.R
import com.example.studywhereah.adapters.SavedLocationsAdaptor
import com.example.studywhereah.models.SavedLocationModel
import kotlinx.android.synthetic.main.activity_saved_locations.*

class SavedLocationsActivity : AppCompatActivity() {

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

    fun setupListOfDataIntoRecyclerView() {
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


//    private fun getAllSavedLocations() {
//        val dbHandler = SqliteOpenHelper(this, null)
//        val allSavedLocationsNamesList = dbHandler.getAllSavedLocationsNameList()
//        val allSavedLocationsAddressList = dbHandler.getAllSavedLocationsAddressList()
//
//        if (allSavedLocationsNamesList.size > 0) {
////            tvSavedLocations.visibility = View.VISIBLE
//            rvSavedLocations.visibility = View.VISIBLE
//            tvNoDataAvailable.visibility = View.GONE
//
//            rvSavedLocations.layoutManager = LinearLayoutManager(this)
//            val savedLocationsAdaptor =
//                SavedLocationsAdaptor(
//                    this,
//                    allSavedLocationsNamesList,
//                    allSavedLocationsAddressList
//                )
//            rvSavedLocations.adapter = savedLocationsAdaptor
//        } else {
////            tvSavedLocations.visibility = View.GONE
//            rvSavedLocations.visibility = View.GONE
//            tvNoDataAvailable.visibility = View.VISIBLE
//        }
//    }

    fun deleteSavedLocationAlertDialog(slm: SavedLocationModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Location")
        builder.setMessage("Are you sure you want to delete?")
        builder.setIcon(R.drawable.ic_warning_black_24dp)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            val dbHandler = SqliteOpenHelper(this, null)
            val status = dbHandler.deleteLocation(slm)
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
//        val dbHandler = SqliteOpenHelper(this, null)
//        dbHandler.deleteLocation(name)

    }

}
