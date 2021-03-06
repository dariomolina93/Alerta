package dm.android.content.alerta.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.ContactsContract;

import androidx.core.content.ContextCompat;
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
import com.google.android.material.textfield.TextInputEditText;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.util.ArrayList;
import java.util.Set;

import static dm.android.content.alerta.AboutPermission.REQUEST_CODE_PICK_CONTACT;

//public class Settings extends Fragment implements View.OnClickListener, RewardedVideoAdListener {
public class Settings extends Fragment implements View.OnClickListener {
    //private AdView mAdView;

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
    private final String adUnitId = "449cc3262685492dbd7ae424e7afa3e9";
    private MoPubRewardedVideoListener rewardedVideoListener;
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

        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(adUnitId)
                .withLogLevel(MoPubLog.LogLevel.DEBUG)
                .withLegitimateInterestAllowed(false)
                .build();

        MoPub.initializeSdk(getContext(), sdkConfiguration, initSdkListener());

        rewardedVideoListener = new MoPubRewardedVideoListener() {
            @Override
            public void onRewardedVideoLoadSuccess(String adUnitId) {
                // Called when the video for the given adUnitId has loaded. At this point you should be able to call MoPubRewardedVideos.showRewardedVideo(String) to show the video.
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
                Toast.makeText(getActivity(),"Muchas Gracias!",Toast.LENGTH_LONG).show();
                MoPubRewardedVideos.loadRewardedVideo(adUnitId);
            }

            @Override
            public void onRewardedVideoCompleted(Set<String> adUnitIds, MoPubReward reward) {
                // Called when a rewarded video is completed and the user should be rewarded.
                // You can query the reward object with boolean isSuccessful(), String getLabel(), and int getAmount().
            }
        };

        MoPubRewardedVideos.setRewardedVideoListener(rewardedVideoListener);

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
                if (MoPubRewardedVideos.hasRewardedVideo(adUnitId)) {
                    MoPubRewardedVideos.showRewardedVideo(adUnitId);
                } else {
                    Log.d("TAG", "The video wasn't loaded yet.");
                    MoPubRewardedVideos.loadRewardedVideo(adUnitId);

                    Toast.makeText(getActivity(), "Video se esta cargando, porfavor intente de nuevo", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private SdkInitializationListener initSdkListener() {
        return new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
           /* MoPub SDK initialized.
           Check if you should show the consent dialog here, and make your ad requests. */
                MoPubRewardedVideos.loadRewardedVideo(adUnitId);
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

        new MultiContactPicker.Builder(getActivity()) //Activity/fragment context
                .theme(R.style.AppTheme) //Optional - default: MultiContactPicker.Azure
                .hideScrollbar(false) //Optional - default: false
                .showTrack(true) //Optional - default: true
                .searchIconColor(Color.BLACK) //Option - default: White
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .setTitleText("Seleccione Contactos Emergencia") //Optional - default: Select Contacts
                //.setSelectedContacts("10", "5" / myList) //Optional - will pre-select contacts of your choice. String... or List<ContactResult>
                .setLoadingType(MultiContactPicker.LOAD_SYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
               // .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out) //Optional - default: No animation overrides
                .showPickerForResult(REQUEST_CODE_PICK_CONTACT);
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
        setContactsAdapter();
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
}
