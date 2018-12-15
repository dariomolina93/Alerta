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

    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    HashMap<String, Integer> perms;
    public static final String SHAREDPREFERENCENAME = "ContactNameAndNumbers";
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;
    private final int REQUEST_CODE_PICK_CONTACT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_permissions);
        sharedPreference = getSharedPreferences(SHAREDPREFERENCENAME, MODE_PRIVATE);
        editor = sharedPreference.edit();

        perms = new HashMap<>();
        perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);

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
        int smsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        int contactsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        ArrayList<String> permissionsNeeded = new ArrayList<>();
        if (contactsPermission != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS);

        if (smsPermission != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.SEND_SMS);

        if (!permissionsNeeded.isEmpty()) {
            Log.d("check&RequestPermission", "requesting permission");
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
        else{
            editor.clear().apply();
            selectContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    Log.d("RequestPermissionResult", "grantResults array contains elements.");
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        Log.d("RequestPermissionResult", "One or both about_permissions are not granted.");

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
                        else {
                            Log.d("RequestPermissionResult", "Opening app setttings to manually enable about_permissions.");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_ID_MULTIPLE_PERMISSIONS);
                        }
                    }
                    else{
                        editor.clear().apply();
                        selectContacts();
                    }
                }
            }
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    private void selectContacts() {
        Intent phonebookIntent = new Intent("intent.action.INTERACTION_TOPMENU");
        phonebookIntent.putExtra("additional", "phone-multi");
        phonebookIntent.putExtra("FromMMS", true);
        startActivityForResult(phonebookIntent, REQUEST_CODE_PICK_CONTACT);
    }

    private HashMap<String,String> getAllContacts()
    {
        HashMap<String,String> nameAndPhone = new HashMap<>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

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
            if(requestCode == REQUEST_CODE_PICK_CONTACT)
            {
                Bundle bundle =  data.getExtras();
                ArrayList<String> selectedContacts = bundle.getStringArrayList("result");

                HashMap<String,String> allContacts = getAllContacts();
                for(int i =0; i < selectedContacts.size(); i++)
                {
                    String[] results = selectedContacts.get(i).split(";");
                    String phoneNumber = results[1].replaceAll("[^0-9]", "");
                    if(allContacts.get(phoneNumber) != null)
                        editor.putString(phoneNumber,allContacts.get(phoneNumber));
                }

                editor.apply();
                super.onActivityResult(requestCode,resultCode,data);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return;
            }
        }
    }

}
