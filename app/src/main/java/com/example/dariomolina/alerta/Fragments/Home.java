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

public class Home extends Fragment implements RewardedVideoAdListener {

    private Button notify, call;
    protected LocationManager locationManager, locationListener;
    private SMS message;
    private Context context;
    private AdView mAdView;
    private Permissions permissions;
    private RewardedVideoAd mRewardedVideoAd;
    private double latitude, longitude;
    private GPSTracker gpsTracker;
    private SQLiteDatabase dbR;
    private SQLiteOpenHelper alertadbR;
    private Cursor selectedContactsCursor;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
        }

        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
      
       // Reading the database and retrieving the selected contacts
       alertadbR = new AlertaDatabaseHelper(getContext());

        notify.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                permissions.checkAndRequestPermissions();
                if(permissions.areAllPermissionsGranted())
                    sendTextMessage();
            }
        });

        call.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Log.d("callEvent", "Placing call");

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:cell#"));
                startActivity(callIntent);
            }
        });

        return view;
    }

    public void sendTextMessage(){
      try{
            this.dbR = alertadbR.getReadableDatabase();
            selectedContactsCursor = AlertaDatabaseHelper.getAllContacts(this.dbR);
        String sms = "Testing activities.\n " +
                "http://maps.google.com/maps?saddr=" + gpsTracker.getLatitude()+","+ gpsTracker.getLongitude();
        Log.d("notifyEvent", "Sending Text Message");

        // Index represents the column returned from the specified query call above. Ex name = 0, phone = 1
        int i = 0;
        while(selectedContactsCursor.moveToNext()){
            String name = selectedContactsCursor.getString(0);
            String phoneNumber = selectedContactsCursor.getString(1);
            message.sendSMS(phoneNumber, sms, name, i);
            i++;
        }
        if (mRewardedVideoAd.isLoaded()) {
           mRewardedVideoAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRewardedVideoAd.resume(getContext());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRewardedVideoAd.pause(getContext());
        }
        super.onPause();
    }


    @Override
    public void onDestroy()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRewardedVideoAd.pause(getContext());
        }
        message.unRegisterReceivers();
        super.onDestroy();
        dbR.close();
        selectedContactsCursor.close();
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }
}
