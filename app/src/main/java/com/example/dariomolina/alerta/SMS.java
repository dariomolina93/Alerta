package com.example.dariomolina.alerta;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/*
    Created by: Dario Molina
    Date: 11/22/2018
 */

public class SMS
{

    private Context applicationContext;
    private BroadcastReceiver messageSendReceiver, messageDeliveredReceiver;
    private final String SEND = "SMS_SEND";
    private final String DELIVERED = "SMS_DELIVERED";
    private static final int SMS_MAX_LENGTH = 160;
    private String name;

    public SMS(Context context)
    {
        applicationContext = context;
        messageSendReceiver = null;
        messageDeliveredReceiver = null;
        name = null;
    }

    public void registerReceivers()
    {
        messageSendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.d("registerRecievers", "SMS has been sent.");
                        Toast.makeText(applicationContext, "Mensaje de texto a sido enviado para " + name + ".", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Log.d("registerRecievers", "Generic Test failure. SMS not send.");
                        Toast.makeText(applicationContext, "Mensaje de texto no fue enviado para " + name + ". Porfavor intente de nuevo.", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Log.d("registerRecievers", "No Service.  SMS not send.");
                        Toast.makeText(applicationContext, "Sin Servicio, mensaje de Texto no fue enviado para " + name + ".", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Log.d("registerRecievers", "Null PDU. SMS not send.");
                        Toast.makeText(applicationContext, "Mensaje de texto no fue enviado para " + name + ".  Porfavor intente de nuevo.", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Log.d("registerRecievers", "Error Radio off");
                        Toast.makeText(applicationContext, "Sin coneccion a wifi. Si esta conectado," +
                                " asegurese que pueda mandar mensajes por wifi.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        messageDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Log.d("registerRecievers", "SMS delivered");
                        Toast.makeText(applicationContext, "Su contacto " + name + " a recibido su mensaje de texto.", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("registerRecievers", "SMS not delivered");
                        Toast.makeText(applicationContext, "Su contacto " + name + " no recibio su mensaje de texto.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        applicationContext.registerReceiver(messageSendReceiver, new IntentFilter(SEND));
        applicationContext.registerReceiver(messageDeliveredReceiver , new IntentFilter(DELIVERED));
    }

    public void sendSMS(String phoneNumber, String message, String name)
    {
        this.name = name;
        Log.d("sendSMS", "registering pending intents for sms send and  sms delivered.");
        PendingIntent sentPI = PendingIntent.getBroadcast(applicationContext, 0, new Intent(SEND), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(applicationContext, 0, new Intent(DELIVERED), 0);
        SmsManager smsManager = SmsManager.getDefault();

        if(message.length() > SMS_MAX_LENGTH) {
            Log.d("sendSMS", "sms > 160 chars.");
            smsManager.sendMultipartTextMessage(phoneNumber, null, smsManager.divideMessage(message), new ArrayList<>(Arrays.asList(sentPI)), new ArrayList<>(Arrays.asList(deliveredPI)));
        }
        else {
            Log.d("sendSMS", "sms <= 160 chars.");
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        }
    }

    public void unRegisterReceivers()
    {
        Log.d("unRegisterReceivers", "unregistering receivers for sms.");
        applicationContext.unregisterReceiver(messageSendReceiver);
        applicationContext.unregisterReceiver(messageDeliveredReceiver);
    }

}

