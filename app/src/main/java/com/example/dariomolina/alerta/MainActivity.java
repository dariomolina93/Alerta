package com.example.dariomolina.alerta;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.Serializable;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    Button notify;
    SMS message;
    private SQLiteDatabase db;
    private Cursor cursorSelectedContacts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message = new SMS(this);
        message.registerReceivers();
        notify = findViewById(R.id.notify);

        SQLiteOpenHelper alertaDB = new AlertaDatabaseHelper(this);
        try{
            this.db = alertaDB.getReadableDatabase();
            cursorSelectedContacts = db.query(AlertaDatabaseHelper.TABLE_CONTACTS,
                                     new String[]{AlertaDatabaseHelper.CONTACT_NAME,
                                                  AlertaDatabaseHelper.CONTACT_PHONE_NUMBER},
                                     null, null, null, null, null);

            notify.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    String sms = "Testing activities.";
                    Log.d("notifyEvent", "Sending Text Message");

                    // Index represents the column return from the specified query call above. Ex name = 0, phone = 1
                    int i = 0;
                    while(cursorSelectedContacts.moveToNext()){
                        String name = cursorSelectedContacts.getString(0);
                        String phoneNumber = cursorSelectedContacts.getString(1);
                        message.sendSMS(phoneNumber, sms, name, i);
                        i++;
                    }
                }
            });
        }catch (SQLiteException e) {
            Log.i("ReadData", "Can't read database");
        }
    }

    @Override
    protected void onDestroy()
    {
        message.unRegisterReceivers();
        super.onDestroy();
        db.close();
        cursorSelectedContacts.close();
    }
}
