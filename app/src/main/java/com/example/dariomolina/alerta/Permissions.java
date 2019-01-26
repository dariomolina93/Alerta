package com.example.dariomolina.alerta;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class Permissions extends Fragment{

    private final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private HashMap<String, Integer> perms;
    public static final String SHAREDPREFERENCENAME = "ContactNameAndNumbers";
    private Activity activity;
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;
    private int smsPermission;
    private int contactsPermission;
    private int call;
    private int gpsLocation;
    private int networkLocation;
    private boolean allPermissionsGranted;

    public Permissions(){
        //map to retain permissions needed
        perms = new HashMap<>();
        allPermissionsGranted=false;
        gpsLocation=0;
        networkLocation=0;
        call=0;
        contactsPermission=0;
        smsPermission=0;

        //prepopulating map with the required permission as key, and permission granted value as value
        perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);


    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        sharedPreference = activity.getSharedPreferences(SHAREDPREFERENCENAME, MODE_PRIVATE);
        editor = sharedPreference.edit();
    }

    public SharedPreferences.Editor getEditor(){return editor;}
    //public SharedPreferences getSharedPreference(){return sharedPreference;}

    public void checkAndRequestPermissions(){
            Log.d("check&RequestPermission", "checking the state of each permission.");

            //store the int values of each value
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            smsPermission = activity.checkSelfPermission( Manifest.permission.SEND_SMS);
            contactsPermission = activity.checkSelfPermission( Manifest.permission.READ_CONTACTS);
            call = activity.checkSelfPermission(Manifest.permission.CALL_PHONE);
            gpsLocation = activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            networkLocation = activity.checkSelfPermission(Manifest.permission.INTERNET);

        }
        Log.d("check&RequestPermission", "After assigning values to permission variables");
            //array to keep track of which permissions need to be requested
            ArrayList<String> permissionsNeeded = new ArrayList<>();

        Log.d("check&RequestPermission", "checking contacts permission");
            //check if permissions have been granted
            if (contactsPermission != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.READ_CONTACTS);

        Log.d("check&RequestPermission", "checking sms permission");
            if (smsPermission != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.SEND_SMS);

        Log.d("check&RequestPermission", "checking call permission");
            if (call != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.CALL_PHONE);

        Log.d("check&RequestPermission", "checking location permission");
            if (gpsLocation != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

        Log.d("check&RequestPermission", "checking location permission");
        if (networkLocation != PackageManager.PERMISSION_GRANTED)
            permissionsNeeded.add(Manifest.permission.INTERNET);

            //if the permissions are needed, make android request the permission with request ID I created
            if (!permissionsNeeded.isEmpty()) {
                Log.d("check&RequestPermission", "requesting permission");
                allPermissionsGranted=false;
                try {
                    requestPermissions(permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                }
                catch (Exception e){
                    ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                }
            }
            //if all permissions are granted, let user select their contacts
            else {
               allPermissionsGranted=true;
            }
        }

        public boolean areAllPermissionsGranted(){return allPermissionsGranted;}

        //this is called automatically after the permissions request
        public void onRequestPermissionsResult ( int requestCode, String permissions[],
        int[] grantResults){
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
                                || perms.get(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                                || perms.get(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                                || perms.get(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                || perms.get(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
                            Log.d("RequestPermissionResult", "One or both about_permissions are not granted.");

                            //permissions will be asked again and this will display the "never ask again" option
                            //doing a try catch, if the try fails, means this was being called from an activity. Otherwise called from fragment.
                            try {
                                if (shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)
                                        || shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)
                                        || shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE)
                                        || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                                        || shouldShowRequestPermissionRationale(Manifest.permission.INTERNET)) {
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
                                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQUEST_ID_MULTIPLE_PERMISSIONS);
                                }
                            }
                            catch (Exception e){
                                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.SEND_SMS)
                                        || ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.READ_CONTACTS)
                                        || ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.CALL_PHONE)
                                        || ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.ACCESS_FINE_LOCATION)
                                        || ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.INTERNET)) {
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
                                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                    intent.setData(uri);
                                    activity.startActivityForResult(intent, REQUEST_ID_MULTIPLE_PERMISSIONS);
                                }
                            }
                        }
                        //if all permissions have been granted, then select contacts
                        else {
                            //clearing contacts selected for testing purposes
                            editor.clear().apply();
                            allPermissionsGranted=true;
                        }
                    }
                }
            }
        }

        //dialog box for showing message to urge user to allow permissions
        private void showDialogOK (String message, DialogInterface.OnClickListener okListener){
            new AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton("OK", okListener)
                    .create()
                    .show();
        }
}
