package dm.android.content.alerta.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dm.android.content.alerta.AlertaDatabaseHelper;
import dm.android.content.alerta.Contact;
import dm.android.content.alerta.ContactNamesAdapter;
import dm.android.content.alerta.R;
//import com.google.android.gms.ads.AdView;
import com.google.android.material.textfield.TextInputEditText;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static dm.android.content.alerta.AboutPermission.REQUEST_CODE_PICK_CONTACT;

//public class Settings extends Fragment implements View.OnClickListener, RewardedVideoAdListener {
public class Settings extends Fragment implements View.OnClickListener, MoPubRewardedVideoListener {
    //private AdView mAdView;
    private MoPubRewardedVideoListener rewardedVideoListener;
    private TextInputEditText inputMessage;
    private Button editButton;
    private Button removeButton;
    private Button addButton;
    private Button addVideoButton;
    private SQLiteDatabase dbW;
    private SQLiteDatabase dbR;
    private View view;
    private View inputLayout;
    private ContactNamesAdapter contactsAdapter;
    private String tabName = "Ajustes";

    private ArrayList<Contact> removeContacts;
    private ArrayList<Contact> contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.settings, container, false);
        MoPub.onCreate(getActivity());

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

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("920b6145fb1546cf8b5cf2ac34638bb7")
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(getContext(), sdkConfiguration, initSdkListener());

        //Attaching the listener to the buttons
        editButton = view.findViewById(R.id.edit_button);
        editButton.setOnClickListener(this);
        removeButton = view.findViewById(R.id.delete_button);
        removeButton.setOnClickListener(this);
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        addVideoButton = view.findViewById(R.id.adVideoButton);
        addVideoButton.setOnClickListener(this);

        inputLayout = view.findViewById(R.id.input_message_layout);
        inputMessage = view.findViewById(R.id.input_message);
        inputMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Log.i("DoneK", "Button id: " + id);
                if (id == EditorInfo.IME_ACTION_DONE) {
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
                break;
            case R.id.delete_button:
                this.onClickRemove();
                break;
            case R.id.add_button:
                this.onClickAdd();
                break;
            case R.id.adVideoButton:
                if (MoPubRewardedVideos.hasRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7")) {
                    MoPubRewardedVideos.showRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");
                } else {
                    Log.d("TAG", "The video wasn't loaded yet.");
                    MoPubRewardedVideos.loadRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");

                    Toast.makeText(getActivity(), "Video se esta cargando, porfavor intente de nuevo", Toast.LENGTH_LONG).show();
                }
//                if (mRewardedVideoAd.isLoaded()) {
//                    mRewardedVideoAd.show();
//                } else {
//                    Log.d("TAG", "The video wasn't loaded yet.");
//                    mRewardedVideoAd.loadAd("ca-app-pub-4491011983892764/5462131000",
//                            new AdRequest.Builder().build());
//
//                    Toast.makeText(getActivity(), "Video se esta cargando, porfavor intente de nuevo", Toast.LENGTH_LONG).show();
//                }
                break;
        }
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */

                MoPubRewardedVideos.loadRewardedVideo("920b6145fb1546cf8b5cf2ac34638bb7");
            }
        };
    }

    private void onClickAdd() {
        this.selectContacts();
        // Reload current fragment
    }

    private void onClickRemove() {
        int position;

        for (int i = 0; i < removeContacts.size(); i++) {
            position = contacts.indexOf(removeContacts.get(i));
            this.contacts.remove(removeContacts.get(i));
            this.contactsAdapter.notifyItemRemoved(position);
            AlertaDatabaseHelper.removeContact(dbR, removeContacts.get(i));
        }
        removeContacts.clear();
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
        this.contacts = new ArrayList<>();
        this.removeContacts = new ArrayList<>();

        while(contactsCursor.moveToNext()) {
            this.contacts.add(new Contact(contactsCursor.getString(0),
                    contactsCursor.getString(1),
                    contactsCursor.getString(2)));
        }

        this.contactsAdapter = new ContactNamesAdapter(contacts);
        recycleView.setAdapter(contactsAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        recycleView.setLayoutManager(layoutManager);

        this.contactsAdapter.setListener(new ContactNamesAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Log.i("ContactsLis", "Contact selected " + position);
            }

            @Override
            public void onClickCheckBox(Contact contact, boolean isChecked) {
                if (isChecked) {
                    removeContacts.add(contact);
                } else {
                    removeContacts.remove(contact);
                }
            }
        });
    }

    private void selectContacts() {
        Log.i("SelectContacts", "About to select contacts");
        Intent phonebookIntent = new Intent("intent.action.INTERACTION_TOPMENU");
        phonebookIntent.putExtra("additional", "phone-multi");
        phonebookIntent.putExtra("FromMMS", true);
        startActivityForResult(phonebookIntent, REQUEST_CODE_PICK_CONTACT);
    }

    private HashMap<String,String> getAllContacts()
    {
        HashMap<String,String> nameAndPhone = new HashMap<>();
        //this will make an inner query call from android to get all contacts from user's phone
        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        while (phones.moveToNext())
        {
            //while iterating through each contact, store name and phone
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            //string phone numbers to solely leave the digits.
            phoneNumber =  phoneNumber.replaceAll("[^0-9]", "");
            nameAndPhone.put(phoneNumber,name);
        }
        phones.close();
        return nameAndPhone;
    }

    public void insertContact(String name, String phoneNumber) {
        if(this.dbW == null) {
            Log.i("insertData", "FAILED: data is null");
            return;
        }
        int size = contactsAdapter.getItemCount();
        Contact newContact = new Contact(name, phoneNumber, "0");
        Log.i("insertData", "Name: " + name + " phoneNumber: " + phoneNumber);
        if (!contacts.contains(newContact)) {
            AlertaDatabaseHelper.addContactToDB(this.dbW, name, phoneNumber);
            contacts.add(new Contact(name, phoneNumber, "0"));
            contactsAdapter.notifyItemInserted(size);
        }else {
            Log.i("insertData","Duplicate contact: " + "Name: " + name + " phoneNumber: " + phoneNumber);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            //if request code is the one I provided(indicates this is the result of user selecting their contacts)
            if(requestCode == REQUEST_CODE_PICK_CONTACT)
            {
                Bundle bundle =  data.getExtras();
                //this will get the contacts the user selected from android ui
                //the selected contacts are returned in a string that consists of ('uriID;phoneNumber')
                ArrayList<String> selectedContacts = bundle.getStringArrayList("result");

                //this will get all the contacts in the user's phone
                HashMap<String,String> allContacts = getAllContacts();
                for(int i =0; i < selectedContacts.size(); i++)
                {
                    //results will contain elements[uri, phoneNumber]
                    String[] results = selectedContacts.get(i).split(";");

                    //leaving just the digits in phoneNumber  Ex. (831)444-2322 -> 8314442322
                    String phoneNumber = results[1].replaceAll("[^0-9]", "");

                    //simply get the key value from allContacts map, and store the contacts selected by the user in the sharedpreference object
                    //and store their number and name
                    if(allContacts.get(phoneNumber) != null) {
                        insertContact(allContacts.get(phoneNumber), phoneNumber);
                    }
                }
                //after contacts have been stored, simply send user to home screen
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        MoPub.onDestroy(getActivity());
        super.onDestroy();
        this.dbW.close();
        this.dbR.close();
    }

    @Override
    public void onResume() {
        MoPub.onResume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        MoPub.onPause(getActivity());
        //mRewardedVideoAd.pause(getActivity());
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        MoPub.onStop(getActivity());
    }

    public String getTabName() {
        return tabName;
    }

    @Override
    public void onRewardedVideoLoadSuccess(String adUnitId) {
                // Called when the video for the given adUnitId has loaded. At this point you should be able to call MoPubRewardedVideos.showRewardedVideo(String) to show the video.
        MoPubRewardedVideos.showRewardedVideo(adUnitId);
    }
    @Override
    public void onRewardedVideoLoadFailure(String adUnitId, MoPubErrorCode errorCode) {
        // Called when a video fails to load for the given adUnitId. The provided error code will provide more insight into the reason for the failure to load.
    }

    @Override
    public void onRewardedVideoStarted(String adUnitId) {
        // Called when a rewarded video starts playing.
    }

    @Override
    public void onRewardedVideoPlaybackError(String adUnitId, MoPubErrorCode errorCode) {
        //  Called when there is an error during video playback.
    }

    @Override
    public void onRewardedVideoClicked(String adUnitId) {
        //  Called when a rewarded video is clicked.
    }

    @Override
    public void onRewardedVideoClosed(String adUnitId) {
        // Called when a rewarded video is closed. At this point your application should resume.
        MoPubRewardedVideos.loadRewardedVideo(adUnitId);
    }

    @Override
    public void onRewardedVideoCompleted(Set<String> adUnitIds, MoPubReward reward) {
        // Called when a rewarded video is completed and the user should be rewarded.
        // You can query the reward object with boolean isSuccessful(), String getLabel(), and int getAmount().
    }
}
