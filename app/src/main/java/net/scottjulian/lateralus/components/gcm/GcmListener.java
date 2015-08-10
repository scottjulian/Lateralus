package net.scottjulian.lateralus.components.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import net.scottjulian.lateralus.components.LateralusMessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;


public class GcmListener extends GcmListenerService {

    private static final String TAG = GcmListener.class.getSimpleName();

    private LateralusMessageHandler handler = null;

    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        JSONObject json = new JSONObject();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            try {
                json.put(key, data.get(key));
            }
            catch(JSONException e) {
                Log.e(TAG, "Could not create json object");
                e.printStackTrace();
                // TODO: send error message to server
                return;
            }
        }
        // TODO: send received messsage to server


        if(handler == null){
            handler = new LateralusMessageHandler(getApplicationContext());
        }

        handler.processMessage(json);
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, "Message " + msgId + " Sent");
    }

}
