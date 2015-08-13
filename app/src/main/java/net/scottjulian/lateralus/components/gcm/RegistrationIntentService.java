package net.scottjulian.lateralus.components.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import net.scottjulian.lateralus.R;
import net.scottjulian.lateralus.components.network.Network;
import net.scottjulian.lateralus.components.readers.DeviceReader;

import org.json.JSONObject;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                Log.i(TAG, "GCM Registration Token: " + token);

                saveTokenToPrefs(sharedPreferences, token);
                sendRegistrationToServer();
                subscribeTopics(token);
                sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
            }
        }
        catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void saveTokenToPrefs(SharedPreferences prefs, String token){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.key_for_token), token);
        editor.commit();
    }

    private void sendRegistrationToServer() {
        DeviceReader dr = new DeviceReader(this);
        try {
            Network.fireJsonData(Network.API_REGISTER, new JSONObject().put("json", dr.getData()));
        }
        catch(Exception e){
            Log.d(TAG, "could not create registration json");
            e.printStackTrace();
        }
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
