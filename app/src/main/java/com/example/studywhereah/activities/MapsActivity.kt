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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.studywhereah.R
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var mBounds: LatLngBounds.Builder = LatLngBounds.Builder()
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    private var currentLatitude : Double = 0.0
    private var currentLongitude : Double = 0.0

    // editText is a var referring to the searchbar element searchbar_edit_text
//    private lateinit var editText: EditText

//    companion object {
//        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        var COUNTRIES = arrayOf("Singapore", "I DONT KNOW", "YKNOW")

        //AutoCompleteTextView irrelevant for now
//        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
//        COUNTRIES)
//        actv_searchBar.setAdapter(adapter)
//        actv_searchBar.setOnItemClickListener(object: AdapterView.OnItemClickListener {
//
//        })

        /**
         * Initialize the places sdk if it is not initialized earlier using the api key.
         */
        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        // Initialize places, we may need the places api key(diff from google maps android sdk api key
        // for the string parameter

        // Set EditText non focusable, the code below is to initiate an autocomplete activity
        // when the searchbar is clicked on.
        tv_search.setOnClickListener(object: View.OnClickListener {
            public override fun onClick(v: View?) {
                var fieldList: List<Place.Field> = listOf(Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.NAME)

                var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
                    .setCountry("SG")
                    .build(this@MapsActivity)

                startActivityForResult(intent, 100)

            }
        })

        /**
         * got problem - press one key then will go down
         * can just copy from happy places app
         */

/*
//        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
//        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
//
//        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
//            override fun onPlaceSelected(place: Place) {
//                Log.i("place", "Place: " + place.name + ", " + place.id);
//            }
//
//            override fun onError(status: Status) {
//                Log.i("place", "An error occurred: $status");
//            }
//        })

 */
        // Code pertaining to Searchview

//        sv_location.suggestionsAdapter
//        //The code below enables an integrated searchbar that tallies with the geocoder database.
//        sv_location.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
//
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                var location = sv_location.query.toString()
//                lateinit var addressList: List<Address>
//                if (location != null || !location.equals("")) {
//                    var geocoder = Geocoder(this@MapsActivity)
//                    try {
//                        addressList = geocoder.getFromLocationName(location, 1)
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                    var address = addressList.get(0)
//                    var coordinates = LatLng(address.latitude, address.longitude)
//                    mMap.addMarker(MarkerOptions().position(coordinates).title(location))
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 10.0F))
//                }
//
//                return false
//
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                //this is where i add suggestions
//
//                var fieldList: List<Place.Field> = listOf(Place.Field.ADDRESS,
//                    Place.Field.LAT_LNG,
//                    Place.Field.NAME)
//
//                var intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList)
//                    .build(this@MapsActivity)
//
//                startActivityForResult(intent, 100)
//                return false
//            }
//        })
        mapFragment.getMapAsync(this)


//        settleLocation()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = supportFragmentManager
//                .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)


        btn_get_place1.setOnClickListener {
            val intent = Intent(this, ChoosePreferencesActivity::class.java)
            intent.putExtra("latitude", currentLatitude)
            intent.putExtra("longitude", currentLongitude)
            startActivity(intent)
        }
    }

    private fun addPointToViewPort(newPoint: LatLng) {
        mBounds.include(newPoint)
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), 13))
    }

    // overidding this function is apparently crucial for Places Autocomplete
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //When success
            //Initialize place
            var place = Autocomplete.getPlaceFromIntent(data!!)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 15.0f))
            mMap.addMarker(MarkerOptions().position(place.latLng!!)
                .title(place.name + ", CHECK IT OUT!" ))
            //Set address on searchbar_edit_text
            tv_search.setText(place.name)
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
        tv_search.viewTreeObserver.addOnGlobalLayoutListener(object:
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mMap.setPadding(0, tv_search.height + 40, 0, 0)
            }
        })
        mMap?.apply {settleLocation()}

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
