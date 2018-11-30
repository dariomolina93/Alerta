package com.example.dariomolina.alerta;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button notify;
    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final int REQUEST_CODE_PICK_CONTACT = 2;
    HashMap<String, Integer> perms;
    SMS message;
    private final String HASCONTACTS = "hasContacts";
    public static final String SHAREDPREFERENCENAME = "ContactNameAndNumbers";
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        perms = new HashMap<>();
        perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
        message = new SMS(getBaseContext());
        message.registerReceivers();
        notify = findViewById(R.id.notify);
        sharedPreference = getSharedPreferences(SHAREDPREFERENCENAME, MODE_PRIVATE);
        editor = sharedPreference.edit();

        checkAndRequestPermissions();

        notify.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String sms = "We can now send messages that are > 160 chars! This gives us greater flexibility in the" +
                        " content of the message and not be restricted anymore by the previous max limit of 160 chars" +
                        " per message. The max number of chars that is allowed to send in an sms is 918." +
                        "  If a message is > 160 chars it will break down the message into batches of 158 chars, and the last" +
                        " batch being how ever many chars are left. If a message is <= 160, it will only send that one single batch.";
                Log.d("notifyRelatiesEvent", "Sending Text Message");


                Map<String,?> keys = sharedPreference.getAll();

                for(Map.Entry<String,?> entry : keys.entrySet()){

                    String value = entry.getValue().toString();
                    if(entry.getKey().toString().equals(HASCONTACTS))
                        continue;

                    message.sendSMS(entry.getKey().toString(), sms, entry.getValue().toString());
                }
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
                        Log.d("RequestPermissionResult", "One or both permissions are not granted.");

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                            Log.d("RequestPermissionResult", "Displaying alert dialog for requesting permissions.");
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
                            Log.d("RequestPermissionResult", "Opening app setttings to manually enable permissions.");
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_ID_MULTIPLE_PERMISSIONS);
                        }
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

    @Override
    protected void onResume()
    {
        super.onResume();
        if(sharedPreference.getAll().size() == 0)
            selectContacts();
    }

    @Override
    protected void onDestroy()
    {
        message.unRegisterReceivers();
        super.onDestroy();
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
            }
        }
    }
}
