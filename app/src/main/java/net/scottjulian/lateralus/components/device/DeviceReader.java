package net.scottjulian.lateralus.components.device;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Browser;
import android.telephony.TelephonyManager;

import java.util.HashMap;


public class DeviceReader {

    public static final String BATT_CHARGING    = "charging";
    public static final String BATT_DISCHARGING = "discharging";

    public static float getBatteryLevel(Context ctx) {
        Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1) {
            return 666f;
        }
        return ((float)level / (float)scale) * 100.0f;
    }

    public static String getBatteryStatus(Context ctx){
        Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        return (isCharging) ? BATT_CHARGING : BATT_DISCHARGING;
    }

    public static HashMap<String, String> getInternetHistory(Context ctx, int count){
        HashMap<String, String> history = new HashMap<>();
        String[] proj = new String[] { Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL };
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        Cursor cursor = ctx.getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, null);
        if(cursor.moveToFirst()) {
            do{
                String title = cursor.getString(cursor.getColumnIndex(Browser.BookmarkColumns.TITLE));
                String url = cursor.getString(cursor.getColumnIndex(Browser.BookmarkColumns.URL));
                history.put(url, title);
            }
            while(cursor.moveToNext());
        }
        return history;
    }

    public static String getPhoneNumber(Context ctx){
        TelephonyManager telMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return telMgr.getLine1Number();
    }

    public static String getDeviceModel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }


}
