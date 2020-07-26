package com.example.studywhereah.activities

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studywhereah.R
import com.example.studywhereah.adapters.ImageViewsAdapter
import com.example.studywhereah.constants.Constants
import com.example.studywhereah.models.SavedLocationModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_maps.*
import java.nio.DoubleBuffer
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : FragmentActivity(), GoogleMap.OnMapLoadedCallback, OnMapReadyCallback{

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
    private var phoneNumber: Int? = null
    // An ArrayList where operatingHours.get(0) is the opening time
    // and operatingHours.get(1) is the closing time
    private var operatingHours = ArrayList<Int>()
    private var hasFood: String? = null
    private var hasPort: Boolean? = null
    private var specialInfo: String? = null
    private var imagesOfLocation = ArrayList<Int>()

//    private var saveBtnClicked : Boolean = true
    private val dbHandler = SqliteOpenHelper(this, null)

    //Firebase storage instance
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference

    //For the slide up panel
    private lateinit var bsb: BottomSheetBehavior<ConstraintLayout>

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var RC_SIGN_IN = 10001

    private var latToSave: Double? = null
    private var lngToSave: Double? = null
    private var markerForAddPlace: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

//        if (currentUser == null) {
//            // start login activity
//            var providers = arrayListOf<AuthUI.IdpConfig>(
//                AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
//            )
//
//            startActivityForResult(
//                AuthUI.getInstance()
//                    .createSignInIntentBuilder()
//                    .setAvailableProviders(providers)
//                    .build(),
//                RC_SIGN_IN)
//        }

//        if (currentUser == null) {
//            var intent = Intent(this, LoginRegisterActivity::class.java)
//            startActivity(intent)
//            this.finish()
//        }


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
                .setInitialQuery(nameOfLocation)
                .build(this@MapsActivity)

            startActivityForResult(intent, 100)
        }

        mapFragment.getMapAsync(this)

        //Enable the LinearLayout to work like a slide up panel
        ll_location_details.visibility = View.INVISIBLE
        ll_button_row.bringToFront()
        bsb = BottomSheetBehavior.from(ll_location_details)
//        val scale: Float = resources.displayMetrics.density
//        val peekPanelInPx = ((hsv_location_images.height + tv_location_detail_name.height) * scale + 0.5f).toInt()
//        bsb.setPeekHeight(940, true)
        hsv_location_images.measure(0,0)
        tv_location_detail_name.measure(0, 0)
        tv_location_detail_openOrClose.measure(0,0)
        tv_location_detail_operating_hours.measure(0, 0)
        btn_get_place1.measure(0, 0)
        val displayDensity = resources.displayMetrics.densityDpi
        val hsvHeightInPx = 110 * (displayDensity/160)
        val placeNameHeightInPx = tv_location_detail_name.measuredHeight
        val getPlaceBtnHeightInPx = btn_get_place1.measuredHeight
        val peekHeight = hsvHeightInPx + placeNameHeightInPx + getPlaceBtnHeightInPx
        bsb.setPeekHeight(peekHeight, true)

        // set the half expanded height to always be showing
        // the horiScrollView, place name, openOrClose and operatingHours.
        val openOrCloseHeightInPx = tv_location_detail_openOrClose.measuredHeight
        val operatingHoursHeightInPx = tv_location_detail_operating_hours.measuredHeight
        val screenHeight = this.resources.displayMetrics.heightPixels
        // 50px is added to the height of the peek panel so as to keep the panel nicely cut off
        // just below operatingHours
        val ratio: Float =
            ((getPlaceBtnHeightInPx + operatingHoursHeightInPx + openOrCloseHeightInPx +
                    placeNameHeightInPx + hsvHeightInPx + 50).toFloat() / screenHeight.toFloat())
        bsb.halfExpandedRatio = (ratio)


        // coming from LocationsRecommendedActivity
        if (intent.getStringExtra("CALLINGACTIVITY") == "LocationsRecommendedActivity") {
            // when called from locationsRecActvity, we load the images from the web.
            nameOfLocation = intent.getStringExtra(Constants.NAMEOFLOCATION)
            latitudeOfLocation = intent.getDoubleExtra(Constants.LATITUDEOFLOCATION, 0.0)
            longitudeOfLocation = intent.getDoubleExtra(Constants.LONGITUDEOFLOCATION, 0.0)
            addressOfLocation = intent.getStringExtra(Constants.ADDRESSOFLOCATION)
            phoneNumber = intent.getIntExtra(Constants.PHONENUMBER, 0)
            operatingHours = intent.getIntegerArrayListExtra(Constants.OPERATINGHOURS)!!
            hasFood = intent.getStringExtra(Constants.FOODAVAILABLE)
            hasPort = intent.getBooleanExtra(Constants.CHARGINGPORTS, false)
            specialInfo = intent.getStringExtra(Constants.SPECIALINFO)
//            imagesOfLocation = intent.getIntegerArrayListExtra(Constants.IMAGESOFLOCATION)!!
            val slm = SavedLocationModel(0, nameOfLocation!!, addressOfLocation!!,
                latitudeOfLocation!!, longitudeOfLocation!!, phoneNumber!!, operatingHours!!,
            hasFood!!, hasPort!!, imagesOfLocation!!)
            //imagesOfLocation does nothing here

            var listResultTask = getImagesTask()
            listResultTask.addOnSuccessListener { listResult ->
                var list = listResult.items
                var storageRefArrList = ArrayList<StorageReference>()

                for (i in 0 until list.size step 1) {
                    storageRefArrList.add(list.get(i))
                }
                var layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false)
                var adapter = ImageViewsAdapter(storageRefArrList)

                rv_images.layoutManager = layoutManager
                rv_images.adapter = adapter
//                    var task = list[i].getBytes(350*1000)
//                    task.addOnSuccessListener { byteArr ->
//                        var bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//                        var imgView = ImageView(this)
//                        imgView.scaleType = ImageView.ScaleType.FIT_XY
//                        imgView.maxWidth = 500
//                        imgView.setImageBitmap(bitmap)
//                        ll_images.addView(imgView)

//                    storageRefArrList.add(list[i])

            }


            selectedLatitude = latitudeOfLocation as Double
            selectedLongitude = longitudeOfLocation as Double
            tv_search.text = nameOfLocation
//            tv_search.text = ((getPlaceBtnHeightInPx + operatingHoursHeightInPx + openOrCloseHeightInPx +
//                    placeNameHeightInPx + hsvHeightInPx).toFloat()/screenHeight.toFloat()).toString()

            // make the saved locations button disappear
            btn_saved_locations.visibility = View.INVISIBLE

            // make the location details appear
            ll_location_details.visibility = View.VISIBLE

            // set the TextViews to contain the results obtained from Google Places.
//            iv_location_detail1.setImageResource(imagesOfLocation[0])
//            iv_location_detail2.setImageResource(imagesOfLocation[1])
            tv_location_detail_name.text = nameOfLocation

            // initilize the btn_save_location background correctly
            var locationSaved = dbHandler.containsLocation(slm)
            if (locationSaved) {
                btn_save_location.setBackgroundResource(R.drawable.ic_bookmark_black_24dp)
//                saveBtnClicked = true
            }

            btn_save_location.setOnClickListener {
                // if the location we are looking at exists in SQLite Database
                if (!locationSaved) {
//                    saveBtnClicked = true
                    btn_save_location.setBackgroundResource(R.drawable.ic_bookmark_black_24dp)
                    saveLocation(nameOfLocation!!, addressOfLocation!!, latitudeOfLocation!!, longitudeOfLocation!!,
                        phoneNumber!!, operatingHours, hasFood!!, hasPort!!, imagesOfLocation)
                } //else {
//                    btn_save_location.setBackgroundResource(R.drawable.ic_bookmark_border_black_24dp)
//                    deleteLocation(nameOfLocation!!, addressOfLocation!!, latitudeOfLocation!!, longitudeOfLocation!!)
//                    saveBtnClicked = false
//                }

            }

            tv_location_detail_address.text = addressOfLocation
            if (phoneNumber == -1) {
                tv_location_detail_phone_number.text = "Not Available"
            } else {
                tv_location_detail_phone_number.text = phoneNumber.toString()
            }
            val openTime = operatingHours[0]
            val closeTime = operatingHours[1]
            val calObj = Calendar.getInstance()
            val currTime = (calObj.get(Calendar.HOUR_OF_DAY) * 100) + (calObj.get(Calendar.MINUTE))
            if (currTime in openTime until closeTime) {
                //Opened!
                tv_location_detail_openOrClose.text = "Open"
            } else {
                tv_location_detail_openOrClose.text = "Closed"
                tv_location_detail_openOrClose.setTextColor(Color.RED)
            }
            tv_location_detail_operating_hours.text = "" + openTime + " to " + closeTime
            tv_location_detail_food_available.text = hasFood

            if (hasPort!!) {
                tv_location_detail_charging_ports.text = "Charging ports available"
            } else {
                tv_location_detail_food_available.text = "Sadly, no charging ports"
            }
            tv_location_special_instructions.text = specialInfo
        }

        if (intent.getStringExtra("CALLINGACTIVITY") == "SavedLocationsActivity") {
            nameOfLocation = intent.getStringExtra(Constants.NAMEOFLOCATION)
            latitudeOfLocation = intent.getDoubleExtra(Constants.LATITUDEOFLOCATION, 0.0)
            longitudeOfLocation = intent.getDoubleExtra(Constants.LONGITUDEOFLOCATION, 0.0)
            addressOfLocation = intent.getStringExtra(Constants.ADDRESSOFLOCATION)
            phoneNumber = intent.getIntExtra(Constants.PHONENUMBER, 0)
            operatingHours = intent.getIntegerArrayListExtra(Constants.OPERATINGHOURS)!!
            hasFood = intent.getStringExtra(Constants.FOODAVAILABLE)
            hasPort = intent.getBooleanExtra(Constants.CHARGINGPORTS, false)
//            imagesOfLocation = intent.getIntegerArrayListExtra(Constants.IMAGESOFLOCATION)!!

            var listResultTask = getImagesTask()
            listResultTask.addOnSuccessListener { listResult ->
                var list = listResult.items
                var storageRefArrList = ArrayList<StorageReference>()

                for (i in 0 until list.size step 1) {
                    storageRefArrList.add(list.get(i))
                }
                var layoutManager = LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL, false)
                var adapter = ImageViewsAdapter(storageRefArrList)

                rv_images.layoutManager = layoutManager
                rv_images.adapter = adapter
//                    var task = list[i].getBytes(350*1000)
//                    task.addOnSuccessListener { byteArr ->
//                        var bitmap = BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
//                        var imgView = ImageView(this)
//                        imgView.scaleType = ImageView.ScaleType.FIT_XY
//                        imgView.maxWidth = 500
//                        imgView.setImageBitmap(bitmap)
//                        ll_images.addView(imgView)

//                    storageRefArrList.add(list[i])

            }

            selectedLatitude = latitudeOfLocation as Double
            selectedLongitude = longitudeOfLocation as Double

            tv_search.text = nameOfLocation

            // make the saved locations button disappear
            btn_saved_locations.visibility = View.INVISIBLE

            // make the location details appear
            ll_location_details.visibility = View.VISIBLE

            // set the TextViews to contain the results obtained from Google Places.
//            iv_location_detail1.setImageResource(imagesOfLocation[0])
//            iv_location_detail2.setImageResource(imagesOfLocation[1])
            tv_location_detail_name.text = nameOfLocation

            btn_save_location.setBackgroundResource(R.drawable.ic_bookmark_black_24dp)

            tv_location_detail_address.text = addressOfLocation

            if (phoneNumber == -1) {
                tv_location_detail_phone_number.text = "Not Available"
            } else {
                tv_location_detail_phone_number.text = phoneNumber.toString()
            }
            val openTime = operatingHours[0]
            val closeTime = operatingHours[1]
            val calObj = Calendar.getInstance()
            val currTime = (calObj.get(Calendar.HOUR_OF_DAY) * 100) + (calObj.get(Calendar.MINUTE))
            if (currTime in openTime until closeTime) {
                //Opened!
                tv_location_detail_openOrClose.text = "Open"
            } else {
                tv_location_detail_openOrClose.text = "Closed"
                tv_location_detail_openOrClose.setTextColor(Color.RED)
            }
            tv_location_detail_operating_hours.text = "" + openTime + " to " + closeTime

                tv_location_detail_food_available.text = hasFood


            if (hasPort!!) {
                tv_location_detail_charging_ports.text = "Charging ports available"
            } else {
                tv_location_detail_food_available.text = "Sadly, no charging ports"
            }

        }

        if (intent.getStringExtra("CALLINGACTIVITY") == "AddLocationActivity") {
            ll_button_row.visibility = View.INVISIBLE
            btn_select_location.visibility = View.VISIBLE

        }

        btn_select_location.setOnClickListener {
            if (markerForAddPlace == null) {
                Toast.makeText(this,
                    "Long press on the map to select a location", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent()
                intent.putExtra("latToSave", latToSave)
                intent.putExtra("lngToSave", lngToSave)
                setResult(Activity.RESULT_OK, intent)
                onBackPressed()
            }

        }

        bsb.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        ll_location_details.setOnClickListener {
            if (bsb.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bsb.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            } else if (bsb.state == BottomSheetBehavior.STATE_HALF_EXPANDED){
                bsb.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bsb.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }

        btn_saved_locations.setOnClickListener {
            val intent = Intent(this, SavedLocationsActivity::class.java)
            startActivity(intent)
        }

        btn_add_location.setOnClickListener{
            var intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

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

        btn_profile_page.setOnClickListener{
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        btn_navigate_here.setOnClickListener {
            val gmmIntentUri: Uri = Uri.parse("google.navigation:q=$latitudeOfLocation, $longitudeOfLocation")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
    }

    fun startLoginIntent() {
        if (currentUser == null) {
            // start login activity
            var providers = arrayListOf<AuthUI.IdpConfig>(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN)
        }
    }

    // method to query images for current location from firebase storage
    fun getImagesTask() : Task<ListResult> {
        var queryPath = nameOfLocation.toString()
        return storageRef.child(queryPath).listAll()
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
            //always hide the infopanel when using the searchbar.
            ll_location_details.setVisibility(View.INVISIBLE)
            //make the layout padding_bottom correct
            tv_search.measure(0, 0)
            ll_button_row.measure(0, 0)

                mMap.setPadding(0, tv_search.measuredHeight + 60, 0, ll_button_row.measuredHeight)

            var place = Autocomplete.getPlaceFromIntent(data!!)
            var placeLatLng = place.latLng
            latitudeOfLocation = placeLatLng?.latitude
            longitudeOfLocation = placeLatLng?.longitude
//            addressOfLocation = place.address
            nameOfLocation = place.name

            //previously this area contained code to set up the swipe up bottomSheet.

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(placeLatLng!!, 15.0f))
            mMap.addMarker(MarkerOptions().position(placeLatLng!!))
            //Set address on searchbar_edit_text
            tv_search.text = nameOfLocation
            selectedLatitude = placeLatLng.latitude
            selectedLongitude = placeLatLng.longitude
            //We can get the locality name, lat and long from place.
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            //When error initialize status
            var status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(applicationContext, status.statusMessage
            , Toast.LENGTH_SHORT).show()
        } else if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                var user = FirebaseAuth.getInstance().currentUser
                Log.e("User email", user?.email)
                if (user?.metadata?.creationTimestamp == user?.metadata?.lastSignInTimestamp) {
                    //new user
                    Toast.makeText(this, "Congrats you have just signed up!", Toast.LENGTH_SHORT)
                } else {
                    Toast.makeText(this, "Welcome back Mugger!", Toast.LENGTH_SHORT)
                }
                var intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                this.finish()
            } else {
                // sign in failed
                var idpResponse = IdpResponse.fromResultIntent(data)
                if (idpResponse == null) {
                    Log.e("Cancelled", "User has cancelled sign in request")
                } else {
                    // we dk so we log the error
                    Log.e("Error", idpResponse.error?.message)
                }
            }
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        //code below is to set the padding of the "interactable" portion of the map :)
        tv_search.measure(0, 0)
        ll_button_row.measure(0, 0)
        val searchBarHeightInPx = tv_search.measuredHeight
        val buttonRowHeightInPx = ll_button_row.measuredHeight
        tv_search.viewTreeObserver.addOnGlobalLayoutListener {
            if (intent.getStringExtra("CALLINGACTIVITY") == "LocationsRecommendedActivity" ||
                intent.getStringExtra("CALLINGACTIVITY") == "SavedLocationsActivity") {
                hsv_location_images.measure(0,0)
                tv_location_detail_name.measure(0, 0)
                tv_location_detail_openOrClose.measure(0,0)
                tv_location_detail_operating_hours.measure(0, 0)
                val hsvHeightInPx = hsv_location_images.measuredHeight
                val placeNameHeightInPx = tv_location_detail_name.measuredHeight
                val openOrCloseHeightInPx = tv_location_detail_openOrClose.measuredHeight
                val operatingHoursHeightInPx = tv_location_detail_operating_hours.measuredHeight
                val bottomPadding = hsvHeightInPx + placeNameHeightInPx +
                        openOrCloseHeightInPx + operatingHoursHeightInPx + buttonRowHeightInPx
                mMap.setPadding(0, searchBarHeightInPx + 60, 0, bottomPadding)

            } else {
                mMap.setPadding(0, searchBarHeightInPx + 60, 0, buttonRowHeightInPx)
            }
        }

        mMap.setOnMapLoadedCallback(this)
        mMap?.apply {
            settleLocation()
            //the reason the code below is in onMapReady and not onCreate is because
            //the map has to be initialized in order for the animate camera to work.
            //Only if there is data passed from a previous activity
        }
    }

    override fun onMapLoaded() {
        if (latitudeOfLocation != null && longitudeOfLocation != null) {
            val location = LatLng(latitudeOfLocation!!, longitudeOfLocation!!)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
            mMap.addMarker(MarkerOptions().position(location))
        }
        if (intent.getStringExtra("CALLINGACTIVITY") == "AddLocationActivity") {
            mMap.setOnMapLongClickListener {
                if (markerForAddPlace == null) {
                    markerForAddPlace = mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(it.latitude, it.longitude))
                    )
                    latToSave = it.latitude
                    lngToSave = it.longitude
                } else {
                    markerForAddPlace!!.position = LatLng(it.latitude, it.longitude)
                    latToSave = it.latitude
                    lngToSave = it.longitude
                }
            }
            latToSave = currentLatitude
            lngToSave = currentLongitude
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
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
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

    private fun saveLocation(name: String, address: String, latitude: Double, longitude: Double,
                             phoneNum: Int, operatingHours: ArrayList<Int>, hasFood: String, hasPort: Boolean, imagesOfLocation: ArrayList<Int>) {
        val dbHandler = SqliteOpenHelper(this, null)
        val slm = SavedLocationModel(0, name, address, latitude, longitude, phoneNum, operatingHours, hasFood, hasPort, imagesOfLocation)
        val status = dbHandler.addLocation(slm)
        if (status > 0) {
            Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun deleteLocation(name: String, address: String, latitude: Double, longitude: Double) {
//        val slm = SavedLocationModel(1, name, address, latitude, longitude)
//        val status = dbHandler.deleteLocation(slm)
////        Toast.makeText(this, "$status", Toast.LENGTH_SHORT).show()
//        if (status > -1) {
//            Toast.makeText(this, "Location Deleted", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "$status", Toast.LENGTH_SHORT).show()
//        }
//    }

}
