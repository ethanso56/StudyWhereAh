package com.example.studywhereah.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import kotlinx.android.synthetic.main.activity_location_details.*


class LocationDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var nameOfLocation = ""
    private var latitudeOfLocation = 0.0
    private var longitudeOfLocation = 0.0
    private var imageOfLocation1 = -1
    private var imageOfLocation2 = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_details)



        nameOfLocation = intent.getStringExtra(Constants.NAMEOFLOCATION)
        latitudeOfLocation = intent.getDoubleExtra(Constants.LATITUDEOFLOCATION, 0.0)
        longitudeOfLocation = intent.getDoubleExtra(Constants.LONGITUDEOFLOCATION, 0.0)
        imageOfLocation1 = intent.getIntExtra(Constants.IMAGEOFLOCATION1, -1)
        imageOfLocation2 = intent.getIntExtra(Constants.IMAGEOFLOCATION2, -1)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_details) as SupportMapFragment
        mapFragment.getMapAsync(this)

        iv_location_detail1.setImageResource(imageOfLocation1)
        iv_location_detail2.setImageResource(imageOfLocation2)
        tv_location_detail_name.text = nameOfLocation

        btn_navigate_here.setOnClickListener {
            val gmmIntentUri: Uri = Uri.parse("google.navigation:q=$latitudeOfLocation, $longitudeOfLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }


    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.apply {
            val location = LatLng(latitudeOfLocation, longitudeOfLocation)
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Marker at location")
            )
            moveCamera(CameraUpdateFactory.newLatLng(location))
            moveCamera(CameraUpdateFactory.zoomTo(15.0f))
        }
    }



}
