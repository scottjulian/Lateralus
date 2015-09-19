package xyz.lateralus.components;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import xyz.lateralus.app.BuildConfig;


public class Utils {
    private static final String TAG = "Utils";

    public static final String NAME_UNKNOWN = "Unknown";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

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

    public static String parsePhoneNumber(String number){
        number = number.replace("\u0000", "").replace("\\u0000", "").replaceAll("\\s", "").replaceAll("[^\\d.]", "");
        if(number.startsWith("1")){
            number = number.substring(1);
        }
        return number;
    }

    public static void hideAppInDrawer(Context ctx){
        try {
            ctx.getPackageManager().setComponentEnabledSetting(
                    new ComponentName("xyz.lateralus", "xyz.lateralus.SignInActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        }
        catch(Exception e){
            Log.e(TAG, "Could not hide app from drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showAppInDrawer(Context ctx){
        try {
            ctx.getPackageManager().setComponentEnabledSetting(
                    new ComponentName("xyz.lateralus","xyz.lateralus.SignInActivity"),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );
        }
        catch(Exception e){
            Log.e(TAG, "Could not show app in drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean internetActive(Context ctx){
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Boolean isGPSOn(Context ctx){
        LocationManager locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);;
        return (locMan != null && locMan.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    public static Boolean isWiFiConnected(Context ctx){
        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (wifi != null && wifi.isConnected());
    }

    public static Boolean isBluetoothOn(){
        BluetoothAdapter blue = BluetoothAdapter.getDefaultAdapter();
        return (blue != null && blue.isEnabled());
    }

    public static String getMD5(String str){
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(str.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for(int x = 0; x < messageDigest.length; x++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[x]));
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // we should crash...
            //throw new RuntimeException("MD5 Algorithm Not Found!");
        }
        return "";
    }

    public static String getPkgVersion(){
        return BuildConfig.VERSION_NAME;
    }

    public static void openLinkInBrowser(Activity act, String url){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        act.startActivity(intent);
    }

    public static boolean checkPlayServices(Activity act) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(act);
        if(resultCode != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, act, 9000).show();
            }
            else {
                Log.e(TAG, "This device is not supported.");
                act.finish();
            }
            return false;
        }
        return true;
    }

    public static long getTimestampMillis(){
        return System.currentTimeMillis();
    }

    public static String capitalizeFirstChar(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static Boolean passwordHasCorrectLength(String pass){
        return pass.length() >= 6;
    }

    public static Boolean isValidEmail(String email){
        return email.length() > 6 && email.contains("@") && email.contains(".");
    }

    public static String generateDeviceUuid(Context ctx) {
        try {
            final TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return deviceUuid.toString();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
