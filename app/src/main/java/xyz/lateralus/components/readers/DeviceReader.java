package xyz.lateralus.components.readers;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import xyz.lateralus.Config;
import xyz.lateralus.app.R;
import xyz.lateralus.components.Utils;

import org.json.JSONObject;

import java.util.UUID;


public class DeviceReader extends DataReader{
    private final String TAG = "DeviceReader";
    private final String ROOT_KEY = "device";
    private final String SECRET = "lspoiv@ujsnuweyy*5mnasjd%mnasdij$$";

    private SharedPreferences _prefs;

    public static final String BATT_CHARGING    = "charging";
    public static final String BATT_DISCHARGING = "discharging";

    public static final String KEY_BATT_LVL    = "batter_level";
    public static final String KEY_BATT_STATUS = "battery_status";
    public static final String KEY_UUID        = "uuid";
    public static final String KEY_NUMBER      = "number";
    public static final String KEY_MODEL       = "model";
    public static final String KEY_EMAIL       = "email";
    public static final String KEY_SECRET      = "secret";
    public static final String KEY_TS          = "timestamp_millis";
    public static final String KEY_TOKEN       = "token";


    public DeviceReader(Context ctx) {
        super(ctx);
        _prefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
    }

    @Override
    public JSONObject getData() {
        try{
            JSONObject data = new JSONObject();
            data.put(KEY_UUID, getUniqueDeviceId());
            data.put(KEY_NUMBER, getPhoneNumber());
            data.put(KEY_MODEL, getDeviceModel());
            data.put(KEY_BATT_LVL, getBatteryLevel());
            data.put(KEY_BATT_STATUS, getBatteryStatus());
            data.put(KEY_SECRET, SECRET);
            data.put(KEY_TOKEN, getGcmToken());
            data.put(KEY_EMAIL, getEmail());
            data.put(KEY_TS, System.currentTimeMillis());
            return new JSONObject().put(ROOT_KEY, data);
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

    public String getEmail(){
        return _prefs.getString(_ctx.getString(R.string.key_for_email), "");
    }

    public String getGcmToken(){
        return _prefs.getString(_ctx.getString(R.string.key_for_token), "");
    }

    public String getUniqueDeviceId(){
        String uuid = _prefs.getString(_ctx.getString(R.string.key_for_device_id), null);
        if(uuid == null) {
            final TelephonyManager tm = (TelephonyManager) _ctx.getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(_ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            uuid = deviceUuid.toString();
            SharedPreferences.Editor edit = _prefs.edit();
            edit.putString(_ctx.getString(R.string.key_for_device_id), uuid);
            edit.commit();
        }
        return uuid;
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
