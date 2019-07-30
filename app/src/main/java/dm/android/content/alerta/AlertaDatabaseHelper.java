package dm.android.content.alerta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlertaDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "Alerta";
    private static final int DB_VERSION = 1;

    /** Keywords for accessing the data in Contacts table */
    public static final String TABLE_CONTACTS = "CONTACTS";
    public static final String CONTACT_NAME = "NAME";
    public static final String CONTACT_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String CONTACT_TABLE_ID = "_id";

    /** Keywords for accessing the data in Message table */
    public static final String TABLE_MESSAGE = "MESSAGE";
    public static final String TEXT_MESSAGE = "SMS";
    public static final String MESSAGE_TABLE_ID = "_id";

    public AlertaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateAlertaDatabase(db, 0, DB_VERSION);
    }

    /** Gets called if the user's database version is lower than the current DB_VERSION */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateAlertaDatabase(db, oldVersion, newVersion);
    }

    private void updateAlertaDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("AlertaDB", "Running the udpateAlertaDatabase(...)");
        if (oldVersion < 1){
            db.execSQL("CREATE TABLE " + TABLE_CONTACTS + " (" + CONTACT_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CONTACT_NAME + " TEXT, "
                    + CONTACT_PHONE_NUMBER + " TEXT);"
            );
            Log.i("AlertaDB", "Creating the CONTACTS table");

            db.execSQL("CREATE TABLE " + TABLE_MESSAGE + " (" + MESSAGE_TABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TEXT_MESSAGE + " TEXT);"
            );
            Log.i("AlertaDB", "Creating the MESSAGE table");
        }
    }

    public static boolean removeContact(SQLiteDatabase db, Contact contact) {
        if(db == null || contact == null)
            return false;
        return db.delete(TABLE_CONTACTS, CONTACT_PHONE_NUMBER + "=?", new String[]{contact.getPhoneNumber()}) > 0;
    }

    public static boolean addContactToDB(SQLiteDatabase db, String name, String phoneNumber) {
        if (!phoneNumber.matches("[0-9]+")) {
            Log.i("insertData", "Phone number incorrect format " + phoneNumber);
            return false;
        }

        try{
            ContentValues contactValues = new ContentValues();
            contactValues.put(AlertaDatabaseHelper.CONTACT_NAME, name);
            contactValues.put(AlertaDatabaseHelper.CONTACT_PHONE_NUMBER, phoneNumber);

            db.insert(AlertaDatabaseHelper.TABLE_CONTACTS, null, contactValues);

            Log.i("insertData", "Data added successfully");
            return true;
        } catch (SQLiteException e) {
            Log.i("insertData", "Adding data FAILED");
            Log.i("insertData", "ERROR " + e.toString());
            return false;
        }
    }

    public static Cursor getAllContacts(SQLiteDatabase db) {
        Cursor selectedContactsCursor;
        try{
            selectedContactsCursor = db.query(AlertaDatabaseHelper.TABLE_CONTACTS,
                    new String[]{AlertaDatabaseHelper.CONTACT_NAME, //Index 0
                                 AlertaDatabaseHelper.CONTACT_PHONE_NUMBER, //Index 1
                                 AlertaDatabaseHelper.CONTACT_TABLE_ID}, //Index 2
                    null, null, null, null, null
            );

            Log.i("ReadData", "FAILED to get all contacts");
            return selectedContactsCursor;
        }catch (SQLiteException e) {
            Log.i("ReadData", "FAILED to get all contacts");
            return null;
        }
    }

    public static void setMessage(SQLiteDatabase db, String message) {
        if(message.equals(""))
            return;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(AlertaDatabaseHelper.TEXT_MESSAGE, message);
            db.insert(AlertaDatabaseHelper.TABLE_MESSAGE, null, contentValues);
        } catch(SQLiteException e) {
            Log.i("InsertData", "Failed to insert message");
        }
    }

    public static String getMessage(SQLiteDatabase db) {
        Cursor messageCursor;
        try{
            messageCursor = db.query(AlertaDatabaseHelper.TABLE_MESSAGE,
                    new String[]{AlertaDatabaseHelper.TEXT_MESSAGE}, //Index 0
                    null, null, null, null, null
            );
            if(messageCursor.moveToLast())
                return messageCursor.getString(0);
            return null;
        }catch (SQLiteException e) {
            Log.i("GetMessage","Failed to retrieve message");
        }
        return null;
    }
}
