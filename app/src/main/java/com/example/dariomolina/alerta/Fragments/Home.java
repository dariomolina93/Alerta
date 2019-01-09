package com.example.dariomolina.alerta.Fragments;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.dariomolina.alerta.R;
import com.example.dariomolina.alerta.SMS;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Home extends Fragment implements RewardedVideoAdListener {

    Button notify;
    SMS message;
    private AdView mAdView;
    //private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.home, container, false);

        super.onCreateView(inflater, container, savedInstanceState);
        message = new SMS(getActivity());
        message.registerReceivers();
        notify = view.findViewById(R.id.notify);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
        }

        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//        mInterstitialAd = new InterstitialAd(this);
//        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
//        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());



        notify.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String sms = "Testing activities.";
                Log.d("notifyEvent", "Sending Text Message");

                SharedPreferences sharedPreference = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    sharedPreference = getContext().getSharedPreferences("ContactNameAndNumbers", MODE_PRIVATE);
                }
                Map<String,?> keys = sharedPreference.getAll();
                int i = 0;
                for(Map.Entry<String,?> entry : keys.entrySet()){

                    String name = entry.getValue().toString();
                    String phone = entry.getKey();
                    message.sendSMS(phone, sms, name, i);
                    i++;
                }
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
            }
        });

        return view;
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
