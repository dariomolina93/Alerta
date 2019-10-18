package dm.android.content.alerta;

import android.app.FragmentManager;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

import java.util.ArrayList;
import java.util.List;

import static dm.android.content.alerta.AboutPermission.REQUEST_CODE_PICK_CONTACT;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase dbW;
    private SQLiteDatabase dbR;
    private ContactNamesAdapter contactsAdapter;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(1);

        SQLiteOpenHelper alertaDB = new AlertaDatabaseHelper(this);
        try{
            this.dbW = alertaDB.getWritableDatabase();

        }catch (SQLiteException e){
            Log.i("Database", "Failed to get writable database");
        }
    }

    @Override
    public void onBackPressed() {

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void insertContact(String name, String phoneNumber) {
        if(this.dbW == null) {
            Log.i("insertData", "FAILED: data is null");
            return;
        }
        AlertaDatabaseHelper.addContactToDB(this.dbW, name, phoneNumber);
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
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dbW.close();
    }
}