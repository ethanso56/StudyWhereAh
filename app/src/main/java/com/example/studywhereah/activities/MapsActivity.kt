package com.example.studywhereah.activities

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.fragment.app.FragmentActivity
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : FragmentActivity(), GoogleMap.OnMapLoadedCallback, OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mBounds: LatLngBounds.Builder = LatLngBounds.Builder()
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    private var currentLatitude : Double = 0.0
    private var currentLongitude : Double = 0.0

    private var selectedLatitude : Double = -1.0
    private var selectedLongitude : Double = -1.0

    private var nameOfLocation: String? = null
    private var latitudeOfLocation: Double? = null
    private var longitudeOfLocation: Double? = null
    private var addressOfLocation: String? = null
    private var rating: Double? = null
//    private lateinit var openingHours: OpeningHours
    private var userRatingsTotal: Int? = null
    private var phoneNumber: String? = null
    private var imagesOfLocation = ArrayList<Int>()

    //An ArrayList of locations that exist in our database.
    // maybe change it to a hashtable?
    private var curatedLocationList = Constants.getLocationList()

    //For the slide up panel
    private lateinit var bsb: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        /**
         * Initialize the places sdk if it is not initialized earlier using the api key.
         */
        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        // Initialize places, we may need the places api key (diff from google maps android sdk api key
        // for the string parameter

        // Set EditText non focusable, the code below is to initiate an autocomplete activity
        // when the searchbar is clicked on.
        tv_search.setOnClickListener {
            var fieldList: List<Place.Field> = listOf(Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME)

            var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                .setCountry("SG")
                .build(this@MapsActivity)

            startActivityForResult(intent, 100)
        }

        mapFragment.getMapAsync(this)

        //Enable the LinearLayout to work like a slide up panel
        ll_location_details.setVisibility(View.INVISIBLE)
        ll_button_row.bringToFront()
        bsb = BottomSheetBehavior.from(ll_location_details)
        bsb.setPeekHeight(700, true)

        if (intent.getStringExtra("CALLINGACTIVITY") == "LocationsRecommendedActivity") {

            nameOfLocation = intent.getStringExtra(Constants.NAMEOFLOCATION)
            latitudeOfLocation = intent.getDoubleExtra(Constants.LATITUDEOFLOCATION, 0.0)
            longitudeOfLocation = intent.getDoubleExtra(Constants.LONGITUDEOFLOCATION, 0.0)
            imagesOfLocation = intent.getIntegerArrayListExtra(Constants.IMAGESOFLOCATION)!!


//            mMap.animateCamera(
//                CameraUpdateFactory.newLatLngZoom(
//                    LatLng(
//                        latitudeOfLocation!!,
//                        longitudeOfLocation!!
//                    ), 15.0f
//                )
//            )
//            mMap.addMarker(
//                MarkerOptions().position(
//                    LatLng(
//                        latitudeOfLocation!!,
//                        longitudeOfLocation!!
//                    )
//                )
//                    .title(nameOfLocation)
//            )

            //Set address on searchbar_edit_text
            selectedLatitude = latitudeOfLocation as Double
            selectedLongitude = longitudeOfLocation as Double
            tv_search.text = nameOfLocation

            ll_location_details.setVisibility(View.VISIBLE)
            // set the TextViews to contain the results obtained from Google Places.

            tv_location_detail_name.text = nameOfLocation
            tv_location_detail_rating.text = rating.toString()
            tv_location_ratings_total.text = userRatingsTotal.toString()
            tv_location_type.text = "LIBRARY (hardcoded)"
            iv_location_detail1.setImageResource(imagesOfLocation.get(0))
            iv_location_detail2.setImageResource(imagesOfLocation.get(1))
            tv_locationAddress.text =
                "Located at: " + latitudeOfLocation + ", " + longitudeOfLocation

        }

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

        //button to toggle the overlay panel.
        btn_toggle_info.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                if (tv_search.text != "Search") {
                    if (ll_location_details.isVisible) {
                        //should tv_search be ll_button_row instead?
                        ll_location_details.setVisibility(View.INVISIBLE)
                    } else {
                        //need to add the feature of re centering the map when the info window is up.
                        ll_location_details.setVisibility(View.VISIBLE)
                    }
                }

            }
        })

        btn_get_place1.setOnClickListener {
            val intent = Intent(this, ChoosePreferencesActivity::class.java)
            intent.putExtra(Constants.CURRENTLATITUDE, currentLatitude)
            intent.putExtra(Constants.CURRENTLONGITUDE, currentLongitude)
            if (selectedLatitude >= 0 || selectedLongitude >= 0) {
                intent.putExtra(Constants.SELECTEDLATITUDE, selectedLatitude)
                intent.putExtra(Constants.SELECTEDLONGITUDE, selectedLongitude)
            }
            startActivity(intent)
        }

        btn_navigate_here.setOnClickListener {
            val gmmIntentUri: Uri = Uri.parse("google.navigation:q=$latitudeOfLocation, $longitudeOfLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

//    private fun addPointToViewPort(newPoint: LatLng) {
//        mBounds.include(newPoint)
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), 13))
//    }

    // overidding this function is apparently crucial for Places Autocomplete
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //When success
            //Initialize place
            var place = Autocomplete.getPlaceFromIntent(data!!)
            var placeLatLng = place.latLng
            latitudeOfLocation = placeLatLng?.latitude
            longitudeOfLocation = placeLatLng?.longitude
            addressOfLocation = place.address
            nameOfLocation = place.name
            rating = place.rating
            userRatingsTotal = place.userRatingsTotal
            phoneNumber = place.phoneNumber

            ll_location_details.setVisibility(View.VISIBLE)
            // set the TextViews to contain the results obtained from Google Places.
            iv_location_detail1.setImageResource(curatedLocationList[1].getImages().get(0))
            iv_location_detail2.setImageResource(curatedLocationList[1].getImages().get(1))
            tv_location_detail_name.text = nameOfLocation
            tv_location_detail_rating.text = rating.toString()
            tv_location_ratings_total.text = userRatingsTotal.toString()
            tv_location_type.text = "LIBRARY (hardcoded)"
            tv_locationAddress.text = "Located at: " + latitudeOfLocation + ", " + longitudeOfLocation

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng!!, 15.0f))
            mMap.addMarker(MarkerOptions().position(placeLatLng!!)
                .title(nameOfLocation + ", CHECK IT OUT!" ))
            //Set address on searchbar_edit_text
//            tv_search.text = nameOfLocation
            selectedLatitude = placeLatLng.latitude
            selectedLongitude = placeLatLng.longitude
            //We can get the locality name, lat and long from place.
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            //When error initialize status
            var status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(applicationContext, status.statusMessage
            , Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        //code below is to set the padding of the "interactable" portion of the map :)
        tv_search.viewTreeObserver.addOnGlobalLayoutListener { mMap.setPadding(0, tv_search.height + 40, 0, ll_button_row.height +20) }

        mMap.setOnMapLoadedCallback(this)
        mMap?.apply {
            settleLocation()
            //the reason the code below is in onMapReady and not onCreate is because
            //the map ahs to be initialized in order for the animate camera to work.
            //Only if there is data passed from a previous activity
        }
    }

    override fun onMapLoaded() {
        if (latitudeOfLocation != null && longitudeOfLocation != null) {
            val location = LatLng(latitudeOfLocation!!, longitudeOfLocation!!)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
            mMap.addMarker(MarkerOptions().position(location))
        }

    }

    // settleLocation requests for and checks for all permissions to be granted
    // then sets the currLong and currLat to be that location.
    private fun settleLocation() {
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()

            // This will redirect you to settings from where you need to turn on the location provider.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            // For Getting current location of user please have a look at below link for better understanding
            // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {

                            requestNewLocationData()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun requestNewLocationData() {
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                updateMapLocation(location)
            }
    }

    private fun updateMapLocation(location: Location?) {
        currentLatitude = location?.latitude!!
        currentLongitude = location?.longitude!!

        var location = LatLng(
            location?.latitude ?: 0.0,
            location?.longitude ?: 0.0)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
//        mMap.addMarker(MarkerOptions().position(location).title("Your current location"))
    }



}
