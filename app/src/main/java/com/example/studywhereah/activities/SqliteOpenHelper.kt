package com.example.studywhereah.activities

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

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
                "(" + COLUMN_NAME + " TEXT," + COLUMN_ADDRESS + " TEXT," +
                COLUMN_LATITUDE + " REAL," + COLUMN_LONGITUDE + " REAL" + ")")// Create Saved locations Table Query.
        db.execSQL(CREATE_SAVED_LOCATIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_LOCATIONS) // It drops the existing history table
        onCreate(db) // Calls the onCreate function so all the updated table will be created.
    }

    fun addLocation(name: String, address: String, latitude: Double, longitude: Double) {
        val values =
            ContentValues() // Creates an empty set of values using the default initial size
        values.put(COLUMN_NAME, name) // Putting the value to the column along with the value.
        values.put(COLUMN_ADDRESS, address)
        values.put(COLUMN_LATITUDE, latitude)
        values.put(COLUMN_LONGITUDE, longitude)

        val db = this.writableDatabase // Create and/or open a database that will be used for reading and writing.
        db.insert(TABLE_SAVED_LOCATIONS, null, values) // Insert query is return
        db.close() // Database is closed after insertion.
    }

    fun deleteLocation(name: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_NAME, name)
        // delete row
        val success = db.delete(TABLE_SAVED_LOCATIONS, COLUMN_NAME + "=" + name, null)
        db.close()
        return success
    }

        /**
     * Function returns the list of history table data.
     */
    fun getAllSavedLocationsNameList(): ArrayList<String> {
        val list = ArrayList<String>() // ArrayList is initialized
        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.

        // Runs the provided SQL and returns a Cursor over the result set.
        // Query for selecting all the data from saved locations table.
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)

        // Move the cursor to the next row.
        while (cursor.moveToNext()) {
            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
            list.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME))) // value is added in the list
        }
        cursor.close() // Cursor is closed after its used.
        return list // List is returned.
    }

    fun getAllSavedLocationsAddressList(): ArrayList<String> {
        val list = ArrayList<String>() // ArrayList is initialized
        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.

        // Runs the provided SQL and returns a Cursor over the result set.
        // Query for selecting all the data from saved locations table.
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)

        // Move the cursor to the next row.
        while (cursor.moveToNext()) {
            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
            list.add(cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS))) // value is added in the list
        }
        cursor.close() // Cursor is closed after its used.
        return list // List is returned.
    }

    fun getAllSavedLocationsLatitudeList(): ArrayList<Double> {
        val list = ArrayList<Double>() // ArrayList is initialized
        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.

        // Runs the provided SQL and returns a Cursor over the result set.
        // Query for selecting all the data from saved locations table.
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)

        // Move the cursor to the next row.
        while (cursor.moveToNext()) {
            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
            list.add(cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE))) // value is added in the list
        }
        cursor.close() // Cursor is closed after its used.
        return list // List is returned.
    }

    fun getAllSavedLocationsLongitudeList(): ArrayList<Double> {
        val list = ArrayList<Double>() // ArrayList is initialized
        val db = this.readableDatabase // Create and/or open a database that will be used for reading and writing.

        // Runs the provided SQL and returns a Cursor over the result set.
        // Query for selecting all the data from saved locations table.
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SAVED_LOCATIONS", null)

        // Move the cursor to the next row.
        while (cursor.moveToNext()) {
            // Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
            list.add(cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE))) // value is added in the list
        }
        cursor.close() // Cursor is closed after its used.
        return list // List is returned.
    }

    companion object {
        private const val DATABASE_VERSION = 1 // This DATABASE Version
        private const val DATABASE_NAME = "StudyWhereAh.db" // Name of the DATABASE
        private const val TABLE_SAVED_LOCATIONS = "save_locations" // Table Name
//        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "_name"        // columns
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
    }
}