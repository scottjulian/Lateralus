package net.scottjulian.lateralus.components.phone;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.Collections;


public class PhoneReader {

    public static final String CONTENT_CALL_LOG = "content://call_log/calls";

    private static final String COL_DATE = "date";
    private static final String COL_NUMBER = "number";
    private static final String COL_DURATION = "duration";

    public static ArrayList<Phonecall> getPhonecalls(Context ctx, int count){
        ArrayList<Phonecall> calls = new ArrayList<>();
        Cursor cursor = ctx.getContentResolver().query(Uri.parse(CONTENT_CALL_LOG), null, null, null, null);
        if(cursor.moveToFirst()) {
            int x = 0;
            do {
                calls.add(new Phonecall(
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)),
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)),
                        cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION)),
                        cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)),
                        cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                ));
                ++x;
            }
            while(cursor.moveToNext() && x < count);
            cursor.close();
        }

        //sort
        if(calls.size() > 1){
            Collections.sort(calls);
        }

        return calls;
    }

}
