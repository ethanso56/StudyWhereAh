package com.example.studywhereah.activities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.studywhereah.models.SavedLocationModel
import org.json.JSONArray
import org.json.JSONObject

class SqliteOpenHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) :
    SQLiteOpenHelper(
        context, DATABASE_NAME,
        factory, DATABASE_VERSION
    ) {



    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_SAVED_LOCATIONS_TABLE = ("CREATE TABLE " + TABLE_SAVED_LOCATIONS +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_NAME + " TEXT," + COLUMN_ADDRESS + " TEXT," +
                COLUMN_LATITUDE + " REAL," + COLUMN_LONGITUDE + " REAL," + COLUMN_PHONENUM + " INTEGER," +
                COLUMN_HASFOOD + " INTEGER," + COLUMN_HASPORT + " INTEGER," + COLUMN_OPERATINGHOURS + " TEXT," +
                COLUMN_IMAGESOFLOCATION + " TEXT" + ")")// Create Saved locations Table Query.
        db.execSQL(CREATE_SAVED_LOCATIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_LOCATIONS) // It drops the existing history table
        onCreate(db) // Calls the onCreate function so all the updated table will be created.
    }

    fun addLocation(slm: SavedLocationModel):Long {
        val values =
            ContentValues() // Creates an empty set of values using the default initial size
        values.put(COLUMN_NAME, slm.name) // Putting the value to the column along with the value.
        values.put(COLUMN_ADDRESS, slm.address)
        values.put(COLUMN_LATITUDE, slm.latitude)
        values.put(COLUMN_LONGITUDE, slm.longitude)
        values.put(COLUMN_PHONENUM, slm.phoneNum)
        values.put(COLUMN_HASFOOD, if (slm.hasFood) 1 else 0)
        values.put(COLUMN_HASPORT, if (slm.hasPort) 1 else 0)

        val jsonOperatingHours = JSONObject()
        jsonOperatingHours.put("operatingHours", JSONArray(slm.operatingHours))
        val operatingHoursString : String = jsonOperatingHours.toString()
        values.put(COLUMN_OPERATINGHOURS, operatingHoursString)

        val jsonImagesOfLocation = JSONObject()
        jsonImagesOfLocation.put("imagesOfLocation", JSONArray(slm.imagesOfLocation))
        val imagesOfLocationString : String = jsonImagesOfLocation.toString()
        values.put(COLUMN_IMAGESOFLOCATION, imagesOfLocationString)

        val db = this.writableDatabase // Create and/or open a database that will be used for reading and writing.
        val success = db.insert(TABLE_SAVED_LOCATIONS, null, values) // Insert query is return
        db.close() // Database is closed after insertion.
        return success
    }

    fun deleteLocation(slm: SavedLocationModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, slm.id)
        Log.e("id", slm.id.toString())
        // delete row
        val success = db.delete(TABLE_SAVED_LOCATIONS, COLUMN_ID + "=" + slm.id, null)
        db.close()
        return success
    }

    fun viewLocation(): ArrayList<SavedLocationModel> {
        val slmList = ArrayList<SavedLocationModel>()

        val selectQuery = "SELECT * FROM $TABLE_SAVED_LOCATIONS"

        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)

        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var name: String
        var address: String
        var latitude: Double
        var longitude: Double
        var phoneNum: Int
        var hasFood: Int
        var hasPort: Int
        var operatingHours: String
        var imagesOfLocation: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS))
                latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))
                longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))
                phoneNum = cursor.getInt(cursor.getColumnIndex(COLUMN_PHONENUM))
                hasFood = cursor.getInt(cursor.getColumnIndex(COLUMN_HASFOOD))
                hasPort = cursor.getInt(cursor.getColumnIndex(COLUMN_HASPORT))
                operatingHours = cursor.getString(cursor.getColumnIndex(COLUMN_OPERATINGHOURS))
                imagesOfLocation = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGESOFLOCATION))

                val jsonOperatingHours = JSONObject(operatingHours)
                val operatingHoursListJson : JSONArray? = jsonOperatingHours.optJSONArray("operatingHours")
                val operatingHoursList = ArrayList<Int>()
                if (operatingHoursListJson != null) {
                    for (i in 0 until operatingHoursListJson.length()) {
                        operatingHoursList.add(operatingHoursListJson.getInt(i))
                    }
                }

                val jsonImagesOfLocation = JSONObject(imagesOfLocation)
                val imagesOfLocationListJson = jsonImagesOfLocation.optJSONArray("imagesOfLocation")
                val imagesOfLocationList = ArrayList<Int>()
                if (imagesOfLocationListJson != null) {
                    for (i in 0 until imagesOfLocationListJson.length()) {
                        imagesOfLocationList.add(imagesOfLocationListJson.getInt(i))
                    }
                }

                val slm = SavedLocationModel(id, name, address, latitude, longitude, phoneNum, operatingHoursList,
                    hasFood == 1, hasPort == 1, imagesOfLocationList) //add the other properties into database
                slmList.add(slm)
            } while (cursor.moveToNext())
        }
        return slmList
    }

    companion object {
        private const val DATABASE_VERSION = 3 // This DATABASE Version
        private const val DATABASE_NAME = "StudyWhereAh.db" // Name of the DATABASE
        private const val TABLE_SAVED_LOCATIONS = "SaveLocations" // Table Name
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"        // columns
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_PHONENUM = "phoneNum"
        private const val COLUMN_HASFOOD = "hasFood"
        private const val COLUMN_HASPORT = "hasPort"
        private const val COLUMN_OPERATINGHOURS = "operatingHours"
        private const val COLUMN_IMAGESOFLOCATION = "imagesOfLocation"
    }
}