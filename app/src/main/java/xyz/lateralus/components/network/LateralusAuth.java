package xyz.lateralus.components.network;


import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.readers.DeviceReader;


public class LateralusAuth {
    private static final String TAG = "Auth";

    private Context _ctx;
    private LateralusPreferences _prefs;
    private LateralusAuthDelegate _delegate;
    private NetDelegate _netDelegate;

    private static final String MSG_SERVER_ERROR = "Problem contacting lateralus server.";
    private static final String MSG_JSON_ERROR = "Problem with signing in.";

    public LateralusAuth(Context ctx, LateralusAuthDelegate delegate){
        _ctx = ctx;
        _prefs = new LateralusPreferences(_ctx);
        _delegate = (delegate == null) ? new AuthDelegateDefault() : delegate;
        _netDelegate = new NetDelegate();
    }

    /* -------- Delegate -------- */

    public interface LateralusAuthDelegate{
        void authFinished(Boolean Success, String msg);
    }

    private static class AuthDelegateDefault implements LateralusAuthDelegate{
        @Override
        public void authFinished(Boolean success, String msg) {
            Log.d(TAG, "Finished with message: " + msg);
        }
    }

    /* -------- Public -------- */

    public void signIn(String email, String password){
        try {
            JSONObject root = new JSONObject();
            JSONObject authData = new JSONObject();
            authData.put("email", email);
            authData.put("password", password);
            root.put("auth", authData);
            DeviceReader dr = new DeviceReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());
            LateralusNetwork.sendJsonData(LateralusNetwork.API_AUTH, new JSONObject().put("json", root), _netDelegate);
        }
        catch(Exception e){
            e.printStackTrace();
            _delegate.authFinished(false, MSG_JSON_ERROR);
        }
    }

    public void register(String email, String password, String deviceNickname){
        try {
            JSONObject root = new JSONObject();
            JSONObject regData = new JSONObject();
            regData.put("email", email);
            regData.put("password", password);
            regData.put("device_name", deviceNickname);
            root.put("register_account", regData);
            DeviceReader dr = new DeviceReader(_ctx);
            root.put(dr.getRootKey(), dr.getData());
            LateralusNetwork.sendJsonData(LateralusNetwork.API_NEW_ACCOUNT, new JSONObject().put("json", root), _netDelegate);
        }
        catch(Exception e){
            e.printStackTrace();
            _delegate.authFinished(false, MSG_JSON_ERROR);
        }
    }

    /* -------- Private ------- */

    private Boolean parseAndSaveSignInResponseData(String jsonResponseString){
        if(jsonResponseString != null && !jsonResponseString.isEmpty()) {
            try {
                JSONObject json = new JSONObject(jsonResponseString);
                _prefs.setEmail(json.getString("email"));
                _prefs.setUserType(json.getString("user_type"));
                _prefs.setUserId(json.getInt("id"));
                _prefs.setDeviceRowId(json.getInt("device_id"));
                _prefs.setUserCreatedTimestamp(json.getLong("created"));
                // TODO: save/send more info?
            }
            catch(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Response data was not valid json");
                return false;
            }
        }
        return true;
    }

    private Boolean parseAndSaveRegisterResponseData(String jsonResponseString){
        // TODO;
        return true;
    }

    private String parseServerMessage(String jsonResponseString){
        if(jsonResponseString != null && !jsonResponseString.isEmpty()) {
            try {
                JSONObject json = new JSONObject(jsonResponseString);
                return json.getString("message");
            }
            catch(Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Response data was not valid json");
            }
        }
        return MSG_SERVER_ERROR;
    }

    /* -------- Network Delegate -------- */

    private class NetDelegate implements LateralusNetwork.NetworkFireTaskDelegate{

        @Override
        public void onFinish(int resCode, String responseString) {
            Boolean success = false;
            String message = parseServerMessage(responseString);
            switch(resCode){
                case 201:
                    // created (account registered)
                    success = parseAndSaveRegisterResponseData(responseString);
                    break;
                case 202:
                    // Accepted (sign in successful)
                    success = parseAndSaveSignInResponseData(responseString);
                    break;
                case 401:
                    // Unauthorized
                    break;
                case 200:
                    // OK (not really ok... but read the server message)
                    Log.d(TAG, "200 OK - Server message: " + message);
                    break;
                case 400:
                    // Bad Request (incorrect json in post)
                case 403:
                    // Forbidden (wrong request method, not POST)
                case 404:
                    // Not Found
                case 500:
                    // Server Error
                case LateralusNetwork.ERROR_RESPONSE_CODE:
                    // App Net Error
                default:
                    // wtf...
                    message = MSG_SERVER_ERROR;
                    Log.e(TAG, "Error code: " + resCode);
                    break;
            }
            _delegate.authFinished(success, message);
        }
    }

}
