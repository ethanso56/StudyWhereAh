package com.example.studywhereah.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.studywhereah.R
import com.example.studywhereah.constants.Constants
import kotlinx.android.synthetic.main.activity_choose_preferences.*

class ChoosePreferencesActivity : AppCompatActivity() {

    var currentLatitude : Double = 0.0
    var currentLongitude : Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_preferences)

        currentLatitude = intent.getDoubleExtra(Constants.CURRENTLATITUDE, 0.0)
        currentLongitude = intent.getDoubleExtra(Constants.CURRENTLONGITUDE, 0.0)

        btn_search.setOnClickListener {
            val intent = Intent(this, LocationsRecommendedActivity::class.java)
            intent.putExtra(Constants.CURRENTLATITUDE, currentLatitude)
            intent.putExtra(Constants.CURRENTLONGITUDE, currentLongitude)
            startActivity(intent)
        }
    }
}
