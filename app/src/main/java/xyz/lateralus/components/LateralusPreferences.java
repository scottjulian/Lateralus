package xyz.lateralus.components;


import android.content.Context;
import android.content.SharedPreferences;

import xyz.lateralus.app.R;


public class LateralusPreferences {

    private Context _ctx;
    private static SharedPreferences        _prefs;
    private static SharedPreferences.Editor _editor;

    public static final String PERMISSION_LOCATION    = "lateralus.permission.location";
    public static final String PERMISSION_MESSAGES    = "lateralus.permission.messages";
    public static final String PERMISSION_PHONE_LOGS  = "lateralus.permission.phone_logs";
    public static final String PERMISSION_CHROME_HIST = "lateralus.permission.chrome_hist";
    public static final String PERMISSION_CAMERA      = "lateralus.permission.camera";
    public static final String PERMISSION_MICROPHONE  = "lateralus.permission.microphone";

    public LateralusPreferences(Context ctx){
        _ctx = ctx;
        _prefs = _ctx.getSharedPreferences(_ctx.getString(R.string.preferences_file), Context.MODE_PRIVATE);
        _editor = _prefs.edit();
    }

    public void setEmail(String email){
        _editor.putString(_ctx.getString(R.string.key_for_email), email).apply();
    }

    public String getEmail(){
        return _prefs.getString(_ctx.getString(R.string.key_for_email), "");
    }

    public void setGcmToken(String tok){
        _editor.putString(_ctx.getString(R.string.key_for_token), tok).apply();
    }

    public String getGcmToken(){
        return _prefs.getString(_ctx.getString(R.string.key_for_token), "");
    }

    public void setGcmTokenSentToServer(Boolean sent){
        _editor.putBoolean(_ctx.getString(R.string.key_for_sent_token), sent).apply();
    }

    public Boolean wasGcmTokenSentToServer(){
        return _prefs.getBoolean(_ctx.getString(R.string.key_for_sent_token), false);
    }

    public void shouldRememberEmail(Boolean remember){
        _editor.putBoolean(_ctx.getString(R.string.key_for_remember_email), remember).apply();
    }

    public Boolean rememberEmail(){
        return _prefs.getBoolean(_ctx.getString(R.string.key_for_remember_email), false);
    }

    public Boolean isUserSignedIn(){
        return !getEmail().isEmpty() && !getUserType().isEmpty() && getUserId() > 0;
    }

    public void setUserType(String type) {
        _editor.putString(_ctx.getString(R.string.key_for_user_type), type).apply();
    }

    public String getUserType(){
        return _prefs.getString(_ctx.getString(R.string.key_for_user_type), "");
    }

    public void setUserId(int id) {
        _editor.putInt(_ctx.getString(R.string.key_for_user_id), id).apply();
    }

    public int getUserId(){
        return _prefs.getInt(_ctx.getString(R.string.key_for_user_id), 0);
    }

    public void setDeviceRowId(int id) {
        _editor.putInt(_ctx.getString(R.string.key_for_device_row_id), id).apply();
    }

    public int getDeviceRowId(){
        return _prefs.getInt(_ctx.getString(R.string.key_for_device_row_id), 0);
    }

    public void setLastSignInMillis(long ts){
        _editor.putLong(_ctx.getString(R.string.key_for_last_sign_in), ts).apply();
    }

    public long getLastSignInMillis(){
        return _prefs.getLong(_ctx.getString(R.string.key_for_last_sign_in), 0);
    }

    public void setWifiOnly(Boolean b){
        _editor.putBoolean(_ctx.getString(R.string.key_for_wifi_only), b).apply();
    }

    public Boolean useWifiOnly(){
        return _prefs.getBoolean(_ctx.getString(R.string.key_for_wifi_only), false);
    }

    public void setDataPermission(String permissionKey, Boolean allow){
        _editor.putBoolean(permissionKey, allow).apply();
    }

    public void setUserCreatedTimestamp(long created) {
        _editor.putLong(_ctx.getString(R.string.key_for_user_created_timestamp), created);
    }

    public long getUserCreatedTimestamp(){
        return _prefs.getLong(_ctx.getString(R.string.key_for_user_created_timestamp), 0);
    }

    public Boolean hasDataPermission(String permissionKey){
        return getUserType().equalsIgnoreCase("god") || _prefs.getBoolean(permissionKey, true);
    }

    public void setSecret(String secret) {
        _editor.putString(_ctx.getString(R.string.key_for_secret), secret).apply();
    }

    public String getSecret(){
        return _prefs.getString(_ctx.getString(R.string.key_for_secret), "");
    }

    public void signOut(){
        setUserType("");
        setUserId(0);
    }

    public String getUniqueDeviceId(){
        String uuid = _prefs.getString(_ctx.getString(R.string.key_for_device_uuid), null);
        if(uuid == null) {
            uuid = Utils.generateDeviceUuid(_ctx);
            _editor.putString(_ctx.getString(R.string.key_for_device_uuid), uuid).apply();
        }
        return uuid;
    }


}
