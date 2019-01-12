package com.example.dariomolina.alerta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.HashMap;

public class AboutPermission extends AppCompatActivity {

    private Permissions permissions;
    private int j = 0;
    private final int REQUEST_CODE_PICK_CONTACT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_permissions);
        permissions = new Permissions(this);
        (findViewById(R.id.forwardButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissions.checkAndRequestPermissions();
                if(permissions.areAllPermissionsGranted()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    }
                }
        });

        (findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
                return;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, String permissions[], int[] grantResults){
        this.permissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(this.permissions.areAllPermissionsGranted()){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }
    //this launches the android ui to select the users contacts
    private void selectContacts() {
        Intent phonebookIntent = new Intent("intent.action.INTERACTION_TOPMENU");
        phonebookIntent.putExtra("additional", "phone-multi");
        phonebookIntent.putExtra("FromMMS", true);
        startActivityForResult(phonebookIntent, REQUEST_CODE_PICK_CONTACT);
    }

    private HashMap<String,String> getAllContacts()
    {
        HashMap<String,String> nameAndPhone = new HashMap<>();
        //this will make an inner query call from android to get all contacts from user's phone
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            //while iterating through each contact, store name and phone
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            //string phone numbers to solely leave the digits.
            phoneNumber =  phoneNumber.replaceAll("[^0-9]", "");
            nameAndPhone.put(phoneNumber,name);
        }
        phones.close();
        return nameAndPhone;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
        {
            //if request code is the one I provided(indicates this is the result of user selecting their contacts)
            if(requestCode == REQUEST_CODE_PICK_CONTACT)
            {
                Bundle bundle =  data.getExtras();
                //this will get the contacts the user selected from android ui
                //the selected contacts are returned in a string that consists of ('uriID;phoneNumber')
                ArrayList<String> selectedContacts = bundle.getStringArrayList("result");

                //this will get all the contacts in the user's phone
                HashMap<String,String> allContacts = getAllContacts();
                for(int i =0; i < selectedContacts.size(); i++)
                {
                    //results will contain elements[uri, phoneNumber]
                    String[] results = selectedContacts.get(i).split(";");

                    //leaving just the digits in phoneNumber  Ex. (831)444-2322 -> 8314442322
                    String phoneNumber = results[1].replaceAll("[^0-9]", "");

                    //simply get the key value from allContacts map, and store the contacts selected by the user in the sharedpreference object
                    //and store their number and name
                    if(allContacts.get(phoneNumber) != null)
                        permissions.getEditor().putString(phoneNumber,allContacts.get(phoneNumber));

                    //MARIO once you create the local Database, we can get rid of this portion and simply pass the values passed the numbers
                    //from the user without having to compare the and store the user's number and name.
                }

                permissions.getEditor().apply();
                //after contacts have been stored, simply send user to home screen
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return;
            }
        }
    }

}
