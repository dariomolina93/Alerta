package com.example.dariomolina.alerta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
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

    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    HashMap<String, Integer> perms;
    private final int REQUEST_CODE_PICK_CONTACT = 2;

    private SQLiteDatabase dbW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_permissions);

        //map to retain permissions needed
        perms = new HashMap<>();

        //prepopulating map with the required permission as key, and permission granted value as value
        perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);

        SQLiteOpenHelper alertaDB = new AlertaDatabaseHelper(this);
        try{
            this.dbW = alertaDB.getReadableDatabase();
        }catch (SQLiteException e){
            Log.i("insertData", "Failed to get readable database");
            Log.i("insertData", "ERROR: " + e.toString());
        }

        (findViewById(R.id.forwardButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestPermissions();
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

    private  void checkAndRequestPermissions() {
        Log.d("check&RequestPermission", "checking the state of each permission.");

        //store the int values of each value
        int smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int contactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        //array to keep track of which permissions need to be requested
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        //check if permissions have been granted
        if (contactsPermission != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS);

        if (smsPermission != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.SEND_SMS);

        //if the permissions are needed, make android request the permission with request ID I created
        if (!permissionsNeeded.isEmpty()) {
            Log.d("check&RequestPermission", "requesting permission");
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
        //if all permissions are granted, let user select their contacts
        else{
            //for testing purposes I'm clearing the contacts each time
            selectContacts();
        }
    }

    //this is called automatically after the permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            //request is the one I called for
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                //check the permission results from the user
                if (grantResults.length > 0) {
                    Log.d("RequestPermissionResult", "grantResults array contains elements.");
                    //setting the value of user permissions to the key value pair store previously
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    //all permissions have not been granted
                    if (perms.get(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("RequestPermissionResult", "One or both about_permissions are not granted.");

                        //permissions will be asked again and this will display the "never ask again" option
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                            Log.d("RequestPermissionResult", "Displaying alert dialog for requesting about_permissions.");
                            showDialogOK("Permiso para leer contactos y mandar mensages son necesarios para utilizar la aplicacion.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //if the "never ask again" option has been selected, it will send the user to settings page so they can select
                        //the permissions themselves.  They can't move forward until all permissions have been granted.
                        else {
                            Log.d("RequestPermissionResult", "Opening app setttings to manually enable about_permissions.");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_ID_MULTIPLE_PERMISSIONS);
                        }
                    }
                    //if all permissions have been granted, then select contacts
                    else{
                        //clearing contacts selected for testing purposes
                        selectContacts();
                    }
                }
            }
        }
    }

    //dialog box for showing message to urge user to allow permissions
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
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

    // Once the user finished selecting contacts, this method is called after
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
                    if(allContacts.get(phoneNumber) != null) {
                        insertContact(allContacts.get(phoneNumber), phoneNumber);
                    }
                }

                //after contacts have been stored, simply send user to home screen
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return;
            }
        }
    }

    public void insertContact(String name, String phoneNumber) {
        if(this.dbW == null) {
            Log.i("insertData", "FAILED: data is null");
            return;
        }
        AlertaDatabaseHelper.addContactToDB(this.dbW, name, phoneNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dbW.close();
    }

}
