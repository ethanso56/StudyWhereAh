package com.example.studywhereah.activities

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
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

    fun containsLocation(slm: SavedLocationModel): Boolean {
        // address is used as the key for the data as it has the highest probability of being unique.
        val slmAddress = slm.address
        val query = "SELECT $COLUMN_ADDRESS FROM $TABLE_SAVED_LOCATIONS WHERE $COLUMN_ADDRESS = '$slmAddress'"
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(query, null)
            Log.e("query message: ", query)
            Log.e("Cursor Row Count: ", cursor.count.toString())
        } catch (e: SQLiteException) {
            db.execSQL(query)
            return false
        }
        db.close()
        return (cursor.count > 0)
    }

    // the method addLocation does not check if the locationModel to be added has been added before
    // as the save location button checks for that before allowing the save button to be clicked.
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