package com.example.dariomolina.alerta.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.dariomolina.alerta.GPSTracker;
import com.example.dariomolina.alerta.MainActivity;
import com.example.dariomolina.alerta.Permissions;
import com.example.dariomolina.alerta.AlertaDatabaseHelper;
import com.example.dariomolina.alerta.R;
import com.example.dariomolina.alerta.SMS;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import static android.content.Context.MODE_PRIVATE;

public class Home extends Fragment{

    private Button notify, call;
    private SMS message;
    private AdView bannerAd;
    private InterstitialAd interstitialAd;
    private Permissions permissions;
    private GPSTracker gpsTracker;
    private SQLiteDatabase dbR;
    private SQLiteOpenHelper alertadbR;
    private Cursor selectedContactsCursor;
    private Location location;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.home, container, false);

        super.onCreateView(inflater, container, savedInstanceState);
        permissions = new Permissions();
        permissions.setActivity(getActivity());
        message = new SMS(getActivity());
        message.registerReceivers();
        notify = view.findViewById(R.id.notify);
        call = view.findViewById(R.id.call);
        gpsTracker = new GPSTracker(getContext());
        location = gpsTracker.getLocation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-4491011983892764~9524664327");
        }

        bannerAd = view.findViewById(R.id.adView);
        bannerAd.loadAd(new AdRequest.Builder().build());

        interstitialAd = new InterstitialAd(getActivity());
        interstitialAd.setAdUnitId("ca-app-pub-4491011983892764/2611179020");
        interstitialAd.loadAd(new AdRequest.Builder().build());
      
       // Reading the database
       alertadbR = new AlertaDatabaseHelper(getContext());
       try{
           this.dbR = alertadbR.getReadableDatabase();
       }catch (SQLiteException e){
           Log.i("DatabaseError", "Could not open a readable database");
       }

        notify.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                permissions.checkAndRequestPermissions();
                if(permissions.areAllPermissionsGranted()) sendTextMessage();
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Log.d("callEvent", "Placing call");

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                startActivity(callIntent);
            }
        });

        return view;
    }

    public void sendTextMessage(){
      try{
            this.dbR = alertadbR.getReadableDatabase();
            selectedContactsCursor = AlertaDatabaseHelper.getAllContacts(this.dbR);
            if(!gpsTracker.canGetLocation()){
                gpsTracker.showSettingsAlert();
                return;
            }

          String msg = AlertaDatabaseHelper.getMessage(dbR);
          String sms;

          if(msg == null) {
              sms = getString(R.string.default_message);
          } else {
              sms = msg;
          }
          sms += "\n\n" + "Direccion donde se encuentra su contacto: http://maps.google.com/?q=" + gpsTracker.getLatitude()+","+ gpsTracker.getLongitude();

        Log.d("notifyEvent", "Sending Text Message");

        // Index represents the column returned from the specified query call above. Ex name = 0, phone = 1
        int i = 0;
        while(selectedContactsCursor.moveToNext()){
            String name = selectedContactsCursor.getString(0);
            String phoneNumber = selectedContactsCursor.getString(1);
            message.sendSMS(phoneNumber, sms, name, i);
            i++;
        }
        if(i != 0) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                Log.d("TAG", "The interstitial wasn't loaded yet.");
            }
        }

        else {
            Toast.makeText(getActivity(),"Porfavor seleccione contactos para mandar mensaje de texto.", Toast.LENGTH_LONG).show();
        }

      }catch (SQLiteException e) {
          Log.i("ReadData", "Can't read database");
      }
    }


    @Override
    public void onRequestPermissionsResult ( int requestCode, String permissions[], int[] grantResults) {
        this.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.permissions.areAllPermissionsGranted()) {
            sendTextMessage();
        }
    }

    @Override
    public void onResume() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
        super.onResume();
        location = gpsTracker.getLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        gpsTracker.stopUsingGPS();
    }

    @Override
    public void onDestroy()
    {
        message.unRegisterReceivers();
        super.onDestroy();
        gpsTracker.stopUsingGPS();

        if(dbR != null && selectedContactsCursor!= null) {
            dbR.close();
            selectedContactsCursor.close();
        }
    }
}
