package com.example.dariomolina.alerta;

import android.content.SharedPreferences;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message = new SMS(this);
        message.registerReceivers();
        notify = findViewById(R.id.notify);

        notify.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String sms = "Testing activities.";
                Log.d("notifyEvent", "Sending Text Message");

                SharedPreferences sharedPreference = getApplicationContext().getSharedPreferences("ContactNameAndNumbers", MODE_PRIVATE);
                Map<String,?> keys = sharedPreference.getAll();
                int i = 0;
                for(Map.Entry<String,?> entry : keys.entrySet()){

                    String name = entry.getValue().toString();
                    String phone = entry.getKey();
                    message.sendSMS(phone, sms, name, i);
                    i++;
                }
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        message.unRegisterReceivers();
        super.onDestroy();
    }
}
