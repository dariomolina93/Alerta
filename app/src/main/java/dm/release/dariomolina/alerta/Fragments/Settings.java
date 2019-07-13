package dm.release.dariomolina.alerta.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import dm.release.dariomolina.alerta.AlertaDatabaseHelper;
import dm.release.dariomolina.alerta.Contact;
import dm.release.dariomolina.alerta.ContactNamesAdapter;
import dm.release.dariomolina.alerta.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static dm.release.dariomolina.alerta.AboutPermission.REQUEST_CODE_PICK_CONTACT;

public class Settings extends Fragment implements View.OnClickListener {

    private AdView mAdView;
    private TextInputEditText inputMessage;
    private Button editButton;
    private Button removeButton;
    private Button addButton;
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
        removeButton = view.findViewById(R.id.delete_button);
        removeButton.setOnClickListener(this);
        addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);

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
        }
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
        super.onDestroy();
        this.dbW.close();
        this.dbR.close();
    }

    public String getTabName() {
        return tabName;
    }
}
