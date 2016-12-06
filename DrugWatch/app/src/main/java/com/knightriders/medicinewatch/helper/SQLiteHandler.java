package com.knightriders.medicinewatch.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "history";

    // Scan table name
    private static final String TABLE_SCANS = "scans";

    // Scan Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_NDC = "ndc";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_REASON = "reason";
    private static final String KEY_DETAIL = "description";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_IMAGE_TABLE = "CREATE TABLE " + TABLE_SCANS + "("
                + KEY_ID + " TEXT," + KEY_IMAGE + " TEXT," + KEY_NDC + " TEXT," + KEY_STATUS + " TEXT," + KEY_NUMBER + " TEXT," + KEY_REASON + " TEXT," + KEY_DETAIL + " TEXT" + ")";
        db.execSQL(CREATE_IMAGE_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCANS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addScan(String id, String image, String ndc, String status, String number, String reason, String detail) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, id);
        values.put(KEY_IMAGE, image);
        values.put(KEY_NDC, ndc);
        values.put(KEY_STATUS, status);
        values.put(KEY_NUMBER, number);
        values.put(KEY_REASON, reason);
        values.put(KEY_DETAIL, detail);

        // Inserting Row
        long idd = db.insert(TABLE_SCANS, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New image inserted into sqlite: " + idd);
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getScanDetails(int position) {
        HashMap<String, String> image = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_SCANS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.move(position + 1);
        if (cursor.getCount() > 0) {
            image.put("id", cursor.getString(0));
            image.put("image", cursor.getString(1));
            image.put("ndc", cursor.getString(2));
            image.put("status", cursor.getString(3));
            image.put("number", cursor.getString(4));
            image.put("reason", cursor.getString(5));
            image.put("detail", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching history from Sqlite: " + image.toString());

        return image;
    }

    public int getTableSize() {
        String selectQuery = "SELECT  * FROM " + TABLE_SCANS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        return cursor.getCount();
    }

    public void deleteScan(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_SCANS, "id = ?", new String[]{id});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_SCANS, null, null);
        db.close();

        Log.d(TAG, "Deleted all img info from sqlite");
    }

}