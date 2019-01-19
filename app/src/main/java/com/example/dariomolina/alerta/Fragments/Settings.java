package com.example.dariomolina.alerta.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dariomolina.alerta.AlertaDatabaseHelper;
import com.example.dariomolina.alerta.ContactNamesAdapter;
import com.example.dariomolina.alerta.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class Settings extends Fragment implements View.OnClickListener {

    private AdView mAdView;
    private TextInputEditText inputMessage;
    private Button editButton;
    private SQLiteDatabase dbW;
    private SQLiteDatabase dbR;
    private View view;
    private View inputLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.settings, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
        }

        SQLiteOpenHelper alertaDB = new AlertaDatabaseHelper(getContext());
        try{
            this.dbR = alertaDB.getReadableDatabase();
            this.dbW = alertaDB.getWritableDatabase();

            if(AlertaDatabaseHelper.getMessage(this.dbR) != null) {
                this.setMessageToLayout(view);
            }
            setContactsAdapter();
        }catch (SQLiteException e){
            Log.i("Database", "Failed to get writable database");
        }



        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Attaching the listener to the buttons
        editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(this);

        inputLayout = view.findViewById(R.id.input_message_layout);
        inputMessage = view.findViewById(R.id.input_message);
        inputMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Log.i("DoneK", "Button id: " + id);
                if (id == EditorInfo.IME_ACTION_DONE) {
                    Log.i("DoneK","Done button pressed");
                    Log.i("DoneK", "Input entered by user: " + inputMessage.getText().toString());
                    AlertaDatabaseHelper.setMessage(dbW, inputMessage.getText().toString());
                    setMessageToLayout(view);
                    onClickDone();
                    hideKeyboard(view);
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edit_button:
                this.onClickEdit();
        }
    }

    private void onClickEdit() {
        Log.i("EditButton", "Edit button PRESSED!");
        editButton.setVisibility(View.GONE);
        inputMessage.setVisibility(View.VISIBLE);
        inputLayout.setVisibility(View.VISIBLE);
    }

    private void onClickDone() {
        editButton.setVisibility(View.VISIBLE);
        inputMessage.setText("");
        inputMessage.setVisibility(View.GONE);
        inputLayout.setVisibility(View.GONE);
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setMessageToLayout(View view) {
        TextView messageView = view.findViewById(R.id.message);
        String msg = AlertaDatabaseHelper.getMessage(this.dbR);
        messageView.setText(msg);
    }

    private void setContactsAdapter() {
        RecyclerView recycleView = view.findViewById(R.id.contacts_recycler);
        Cursor contactsCursor = AlertaDatabaseHelper.getAllContacts(dbR);
        String[] names = new String[contactsCursor.getCount()];
        int i = 0;
        while(contactsCursor.moveToNext()) {
            names[i++] = contactsCursor.getString(0);
        }

        ContactNamesAdapter contactsAdapter = new ContactNamesAdapter(names);
        recycleView.setAdapter(contactsAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        recycleView.setLayoutManager(layoutManager);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dbW.close();
        this.dbR.close();
    }
}
