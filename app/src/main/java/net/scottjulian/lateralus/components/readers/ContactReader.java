package net.scottjulian.lateralus.components.readers;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import org.json.JSONObject;


public class ContactReader extends DataReader {
    private static final String TAG = "ContactReader";
    private static final String ROOT_KEY = "contacts";

    public static final String NAME_UNKNOWN = "UNKNOWN";

    public ContactReader(Context ctx) {
        super(ctx);
    }

    @Override
    public JSONObject getData() {
        try{
            return new JSONObject().put(ROOT_KEY, getContacts());
        }
        catch(Exception e){
            Log.e(TAG, "Error creating contact data!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRootKey() {
        return ROOT_KEY;
    }

    private JSONObject getContacts() {
        ContentResolver cr = _ctx.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        JSONObject contacts = new JSONObject();
        try {
            if(cursor != null && cursor.moveToFirst()) {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Cursor phones = cr.query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + " = " + contactId, null, null);
                JSONObject person = new JSONObject();
                while(phones != null && phones.moveToNext()) {
                    String number = PhonecallReader.parseNumber(phones.getString(phones.getColumnIndex(Phone.NUMBER)));
                    int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
                    switch(type) {
                        case Phone.TYPE_HOME:
                            person.put("home", number);
                            break;
                        case Phone.TYPE_MOBILE:
                            person.put("mobile", number);
                            break;
                        case Phone.TYPE_WORK:
                            person.put("work", number);
                            break;
                    }
                }
                phones.close();
                contacts.put(contactName, person);
            }
            cursor.close();
            return contacts;
        }
        catch(Exception e){
            Log.e(TAG, "Error getting contact data!");
            e.printStackTrace();
        }
        return null;
    }

    public static String getContactName(Context context, String phoneNumber) {
        if(phoneNumber.isEmpty()){
            return "";
        }
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = NAME_UNKNOWN;
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }
}
