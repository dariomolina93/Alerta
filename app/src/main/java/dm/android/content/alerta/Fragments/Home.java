package dm.android.content.alerta.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import dm.android.content.alerta.GPSTracker;
import dm.android.content.alerta.Permissions;
import dm.android.content.alerta.AlertaDatabaseHelper;
import dm.android.content.alerta.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Home extends Fragment {

    private Button notify, call;
    //private SMS message;
    private AdView bannerAd;
    private Permissions permissions;
    private GPSTracker gpsTracker;
    private SQLiteDatabase dbR;
    private SQLiteOpenHelper alertadbR;
    private Cursor selectedContactsCursor;
    private Location location;

    public final String tabName = "Inicio";
    static final int SMS_REQUEST = 1;  // The request code

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.home, container, false);

        super.onCreateView(inflater, container, savedInstanceState);
        permissions = new Permissions();
        permissions.setActivity(getActivity());
        //message = new SMS(getActivity());
        //message.registerReceivers();
        notify = view.findViewById(R.id.notify);
        call = view.findViewById(R.id.call);
        gpsTracker = new GPSTracker(getContext());
        location = gpsTracker.getLocation();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-4491011983892764~9524664327");
        }

        bannerAd = view.findViewById(R.id.adView);
        bannerAd.loadAd(new AdRequest.Builder().build());
      
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
                if(permissions.areAllPermissionsGranted()){ sendTextMessage();}
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
          sms += "\n\n" + "Direccion donde se encuentra su contacto: http://maps.google.com/?q=" + gpsTracker.getLatitude()+","+ gpsTracker.getLongitude() + "\n";

        Log.d("notifyEvent", "Sending Text Message");

        // Index represents the column returned from the specified query call above. Ex name = 0, phone = 1
        int j = 0;
          String numbers = "";
        while(selectedContactsCursor.moveToNext()){
            String name = selectedContactsCursor.getString(0);
            String phoneNumber = selectedContactsCursor.getString(1);

            numbers += phoneNumber + ';';

            j++;
        }

        if(j == 0) {
            Toast.makeText(getActivity(),"Porfavor seleccione contactos para mandar mensaje de texto.", Toast.LENGTH_LONG).show();
        } else{
            numbers = numbers.substring(0,numbers.length() - 1);

            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.setData(Uri.parse("sms:" + numbers));
            sendIntent.putExtra("sms_body", sms);
            startActivityForResult(sendIntent, SMS_REQUEST);
        }

      }catch (SQLiteException e) {
          Log.i("ReadData", "Can't read database");
      }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SMS_REQUEST) {
            //be default the resultcode is returning as result_cancelled
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
        //message.unRegisterReceivers();
        super.onDestroy();
        gpsTracker.stopUsingGPS();

        if(dbR != null && selectedContactsCursor!= null) {
            dbR.close();
            selectedContactsCursor.close();
        }
    }

    public String getTabName() {
        return tabName;
    }
}
