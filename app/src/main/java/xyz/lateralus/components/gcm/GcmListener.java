package xyz.lateralus.components.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import xyz.lateralus.components.LateralusMessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;


public class GcmListener extends GcmListenerService {

    private static final String TAG = "GcmListener";

    private static LateralusMessageHandler handler = null;

    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        JSONObject json = new JSONObject();
        try {
            Set<String> keys = data.keySet();
            for(String key : keys) {
                json.put(key, data.get(key));
            }
        }
        catch(JSONException e) {
            Log.e(TAG, "Could not create json object from GCM");
            e.printStackTrace();
            // TODO: send error message to server?
            return;
        }

        // TODO: send received messsage to server?

        if(handler == null){
            handler = new LateralusMessageHandler(getApplicationContext());
        }
        handler.onMessageReceived(json);
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, "Message " + msgId + " Sent");
    }

}
