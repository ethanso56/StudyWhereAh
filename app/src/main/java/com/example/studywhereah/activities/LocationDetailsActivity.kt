package com.example.studywhereah.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MotionEventCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.OpeningHours
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_location_details.*


class LocationDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    //pass the details from Google Places to this Activity via the intent
    //Store them here as variables
    //display these variables in the TextViews inside the layout.
    //need to add: checking of whether the location name matches our database, if yes add our info,
    // else say "not in database"
    private var nameOfLocation = ""
    private var latitudeOfLocation = 0.0
    private var longitudeOfLocation = 0.0
    private var rating = 0.0
    private lateinit var openingHours: OpeningHours
    private var userRatingsTotal = 0
    private lateinit var phoneNumber: String
    private var imageOfLocation1 = -1
    private var imageOfLocation2 = -1

    private lateinit var bsb: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_details)

        //Assigning variables
        nameOfLocation = intent.getStringExtra(Constants.NAMEOFLOCATION)
        latitudeOfLocation = intent.getDoubleExtra(Constants.LATITUDEOFLOCATION, 0.0)
        longitudeOfLocation = intent.getDoubleExtra(Constants.LONGITUDEOFLOCATION, 0.0)
//        rating = intent.getDoubleExtra(Constants.RATING, 0.0)
//        userRatingsTotal = intent.getIntExtra(Constants.USERRATINGSTOTAL, 0)
//        phoneNumber = intent.getStringExtra(Constants.PHONENUMBER)
        imageOfLocation1 = intent.getIntExtra(Constants.IMAGEOFLOCATION1, -1)
        imageOfLocation2 = intent.getIntExtra(Constants.IMAGEOFLOCATION2, -1)

        //Assigning passed data to textViews within the info window
        iv_location_detail1.setImageResource(imageOfLocation1)
        iv_location_detail2.setImageResource(imageOfLocation2)
        tv_location_detail_name.text = nameOfLocation
        tv_locationAddress.text = "" + latitudeOfLocation + longitudeOfLocation
//        tv_location_detail_openOrClose.setText("FAKE: Open")
//        tv_location_detail_rating.setText(rating.toString())
//        tv_location_ratings_total.setText("(" + userRatingsTotal.toString() + ")")

        bsb = BottomSheetBehavior.from(ll_location_details)
        bsb.setPeekHeight(600, true)

        ll_location_details.setOnClickListener (object: View.OnClickListener {
            override fun onClick(v: View?) {
                if (bsb.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    bsb.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    bsb.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        )

        btn_toggle_info.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                if (ll_location_details.isVisible) {
                    ll_location_details.setVisibility(View.INVISIBLE)
//                    ll_location_details.animate().translationY(1.0f).start()
                } else {
                    ll_location_details.setVisibility(View.VISIBLE)
                }
            }
        })


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_details) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
            animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
        }
    }



}
