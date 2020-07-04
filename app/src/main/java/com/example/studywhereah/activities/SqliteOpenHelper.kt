package com.example.studywhereah.activities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import com.example.studywhereah.models.SavedLocationModel

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
                COLUMN_LATITUDE + " REAL," + COLUMN_LONGITUDE + " REAL" + ")")// Create Saved locations Table Query.
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

        val db = this.writableDatabase // Create and/or open a database that will be used for reading and writing.
        val success = db.insert(TABLE_SAVED_LOCATIONS, null, values) // Insert query is return
        db.close() // Database is closed after insertion.
        return success
    }

//    fun deleteLocation(name: String): Int {
//        val db = this.writableDatabase
//        val contentValues = ContentValues()
//        contentValues.put(COLUMN_NAME, name)
//        // delete row
//        val success = db.delete(TABLE_SAVED_LOCATIONS, COLUMN_NAME + "=" + name, null)
//        db.close()
//        return success
//    }

    fun deleteLocation(slm: SavedLocationModel): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ID, slm.id)
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

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME))
                address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS))
                latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))
                longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))

                val slm = SavedLocationModel(id, name, address, latitude, longitude)
                slmList.add(slm)
            } while (cursor.moveToNext())
        }
        return slmList
    }

//        /**
//     * Function returns the list of history table data.
//     */
//    fun getAllSavedLocationsNameList(): ArrayList<String> {
//        val list = ArrayList<String>() // ArrayList is initialized
//        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.
//
//        // Runs the provided SQL and returns a Cursor over the result set.
//        // Query for selecting all the data from saved locations table.
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)
//
//        // Move the cursor to the next row.
//        while (cursor.moveToNext()) {
//            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
//            list.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME))) // value is added in the list
//        }
//        cursor.close() // Cursor is closed after its used.
//        return list // List is returned.
//    }
//
//    fun getAllSavedLocationsAddressList(): ArrayList<String> {
//        val list = ArrayList<String>() // ArrayList is initialized
//        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.
//
//        // Runs the provided SQL and returns a Cursor over the result set.
//        // Query for selecting all the data from saved locations table.
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)
//
//        // Move the cursor to the next row.
//        while (cursor.moveToNext()) {
//            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
//            list.add(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS))) // value is added in the list
//        }
//        cursor.close() // Cursor is closed after its used.
//        return list // List is returned.
//    }
//
//    fun getAllSavedLocationsLatitudeList(): ArrayList<Double> {
//        val list = ArrayList<Double>() // ArrayList is initialized
//        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.
//
//        // Runs the provided SQL and returns a Cursor over the result set.
//        // Query for selecting all the data from saved locations table.
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)
//
//        // Move the cursor to the next row.
//        while (cursor.moveToNext()) {
//            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
//            list.add(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))) // value is added in the list
//        }
//        cursor.close() // Cursor is closed after its used.
//        return list // List is returned.
//    }
//
//    fun getAllSavedLocationsLongitudeList(): ArrayList<Double> {
//        val list = ArrayList<Double>() // ArrayList is initialized
//        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.
//
//        // Runs the provided SQL and returns a Cursor over the result set.
//        // Query for selecting all the data from saved locations table.
//        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)
//
//        // Move the cursor to the next row.
//        while (cursor.moveToNext()) {
//            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
//            list.add(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))) // value is added in the list
//        }
//        cursor.close() // Cursor is closed after its used.
//        return list // List is returned.
//    }

    companion object {
        private const val DATABASE_VERSION = 2 // This DATABASE Version
        private const val DATABASE_NAME = "StudyWhereAh.db" // Name of the DATABASE
        private const val TABLE_SAVED_LOCATIONS = "SaveLocations" // Table Name
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"        // columns
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }
}