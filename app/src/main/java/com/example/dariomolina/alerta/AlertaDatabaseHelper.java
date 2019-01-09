package com.example.dariomolina.alerta;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlertaDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "Alerta";
    private static final int DB_VERSION = 1;

    /** Keywords for accessing the data in Contacts table */
    public static final String TABLE_CONTACTS = "CONTACTS";
    public static final String CONTACT_NAME = "NAME";
    public static final String CONTACT_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String CONTACT_TABLE_ID = "_id";


    public AlertaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateAlertaDatabase(db, 0, DB_VERSION);
    }

    /** Gets called if the user's database version is lower than the current DB_VERSION */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateAlertaDatabase(db, oldVersion, newVersion);
    }

    private void updateAlertaDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("AlertaDB", "Running the udpateAlertaDatabase(...)");
        if (oldVersion < 1){
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (" + CONTACT_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CONTACT_NAME + " TEXT, "
                    + CONTACT_PHONE_NUMBER + " TEXT);");
            Log.i("AlertaDB", "Populating database");
        }
    }
}
