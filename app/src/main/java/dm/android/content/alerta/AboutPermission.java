package dm.android.content.alerta;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import java.util.List;

public class AboutPermission extends AppCompatActivity {
    private Permissions permissions;
    public static final int REQUEST_CODE_PICK_CONTACT = 2;
    private SQLiteDatabase dbW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_permissions);
        permissions = new Permissions();
        permissions.setActivity(this);
        SQLiteOpenHelper alertaDB = new AlertaDatabaseHelper(this);
        try{
            this.dbW = alertaDB.getReadableDatabase();
        }catch (SQLiteException e){
            Log.i("insertData", "Failed to get readable database");
            Log.i("insertData", "ERROR: " + e.toString());
        }
        (findViewById(R.id.forwardButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permissions.checkAndRequestPermissions();
                if(permissions.areAllPermissionsGranted()) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    }
                }
        });

        (findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deleting preferences to prevent bug for going to main menu when user clicks back after first screen
                getApplicationContext().getSharedPreferences("Alerta_preferences",MODE_PRIVATE).edit().clear().apply();
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
                return;
            }
        });
    }
    @Override
    public void onRequestPermissionsResult ( int requestCode, String permissions[], int[] grantResults){
        this.permissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(this.permissions.areAllPermissionsGranted()){
            selectContacts();
        }
    }
    //this launches the android ui to select the users contacts
    private void selectContacts() {
        new MultiContactPicker.Builder(this) //Activity/fragment context
                .theme(R.style.AppTheme) //Optional - default: MultiContactPicker.Azure
                .hideScrollbar(false) //Optional - default: false
                .showTrack(true) //Optional - default: true
                .searchIconColor(Color.BLACK) //Option - default: White
                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                .handleColor(ContextCompat.getColor(this, R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleColor(ContextCompat.getColor(this, R.color.colorPrimary)) //Optional - default: Azure Blue
                .bubbleTextColor(Color.WHITE) //Optional - default: White
                .setTitleText("Selecione Contactos Emergencia") //Optional - default: Select Contacts
                //.setSelectedContacts("10", "5" / myList) //Optional - will pre-select contacts of your choice. String... or List<ContactResult>
                .setLoadingType(MultiContactPicker.LOAD_SYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                // .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out) //Optional - default: No animation overrides
                .showPickerForResult(REQUEST_CODE_PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            //this is being called when user selects to add/update their emergency contacts
            if(requestCode == REQUEST_CODE_PICK_CONTACT) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
                results.size();

                for(int i = 0; i < results.size(); i++){
                    String phoneNumber = results.get(i).getPhoneNumbers().get(0).getNumber().replaceAll("[^0-9]", "");
                    insertContact(results.get(i).getDisplayName(),phoneNumber);
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                return;
            }
        }
    }

    public void insertContact(String name, String phoneNumber) {
        if(this.dbW == null) {
            Log.i("insertData", "FAILED: data is null");
            return;
        }
        AlertaDatabaseHelper.addContactToDB(this.dbW, name, phoneNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dbW.close();
    }

}
