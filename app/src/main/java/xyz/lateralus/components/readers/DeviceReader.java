package xyz.lateralus.components.readers;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.Utils;

import org.json.JSONObject;


public class DeviceReader extends DataReader{
    private final String TAG = "DeviceReader";
    private final String ROOT_KEY = "device";
    private final String SECRET = "lspoiv@ujsnuweyy*5mnasjd%mnasdij$$";

    private LateralusPreferences _prefs;

    public static final String BATT_CHARGING    = "charging";
    public static final String BATT_DISCHARGING = "discharging";

    public static final String KEY_BATT_LVL    = "batter_level";
    public static final String KEY_BATT_STATUS = "battery_status";
    public static final String KEY_WIFI        = "wifi";
    public static final String KEY_GPS         = "gps";
    public static final String KEY_BLUETOOTH   = "bluetooth";
    public static final String KEY_UUID        = "uuid";
    public static final String KEY_NUMBER      = "number";
    public static final String KEY_MODEL       = "model";
    public static final String KEY_EMAIL       = "email";
    public static final String KEY_SECRET      = "secret";
    public static final String KEY_TS          = "timestamp_millis";
    public static final String KEY_TOKEN       = "token";


    public DeviceReader(Context ctx) {
        super(ctx);
        _prefs = new LateralusPreferences(_ctx);
    }

    @Override
    public JSONObject getData() {
        try{
            JSONObject data = new JSONObject();
            data.put(KEY_UUID, _prefs.getUniqueDeviceId());
            data.put(KEY_NUMBER, getPhoneNumber());
            data.put(KEY_MODEL, getDeviceModel());
            data.put(KEY_BATT_LVL, getBatteryLevel());
            data.put(KEY_BATT_STATUS, getBatteryStatus());
            data.put(KEY_WIFI, Utils.isWiFiConnected(_ctx));
            data.put(KEY_GPS, Utils.isGPSOn(_ctx));
            data.put(KEY_BLUETOOTH, Utils.isBluetoothOn());
            data.put(KEY_SECRET, SECRET);
            data.put(KEY_TOKEN, _prefs.getGcmToken());
            data.put(KEY_EMAIL, _prefs.getEmail());
            data.put(KEY_TS, System.currentTimeMillis());
            return data;
        }
        catch(Exception e){
            Log.e(TAG, "Error gathering device data!");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getRootKey() {
        return ROOT_KEY;
    }

    public float getBatteryLevel() {
        try {
            Intent batteryIntent = _ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return (level == -1 || scale == -1) ? -1 : (((float) level / (float) scale) * 100.0f);
        }
        catch(Exception e){
            return 0;
        }
    }

    public String getBatteryStatus(){
        try {
            Intent batteryIntent = _ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            return (isCharging) ? BATT_CHARGING : BATT_DISCHARGING;
        }
        catch(Exception e){
            return "";
        }
    }

    public String getPhoneNumber(){
        try {
            TelephonyManager telMgr = (TelephonyManager) _ctx.getSystemService(Context.TELEPHONY_SERVICE);
            return Utils.parsePhoneNumber(telMgr.getLine1Number());
        }
        catch(Exception e){
            return "";
        }
    }

    public static String getDeviceModel() {
        return Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL.toUpperCase();
    }
}
