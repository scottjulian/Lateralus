package net.scottjulian.lateralus.gcm;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import net.scottjulian.lateralus.LateralusMessageHandler;
import net.scottjulian.lateralus.MainActivity;
import net.scottjulian.lateralus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;


public class GcmListener extends GcmListenerService {

    private static final String TAG = GcmListener.class.getSimpleName();

    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);

        JSONObject json = new JSONObject();
        Set<String> keys = data.keySet();
        for (String key : keys) {
            try {
                json.put(key, data.get(key));
            }
            catch(JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Could not create json object");
                return;
            }
        }

        MsgProcessTask mpt = new MsgProcessTask();
        mpt.execute(json);

        //sendNotification(from);
    }

    private class MsgProcessTask extends AsyncTask<JSONObject, Integer, String> {

        @Override
        protected String doInBackground(JSONObject... jsonObjects) {
            if(jsonObjects.length > 0){
                LateralusMessageHandler.processMessage(getApplicationContext(), jsonObjects[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onMessageSent(String msgId) {
        super.onMessageSent(msgId);
    }

    /**
     * For Testing
     * @param message
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_action_android)
                .setContentTitle("GCM Message")
                .setContentText("received message from: " + message)
                .setAutoCancel(true)
                //.setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(2 , notificationBuilder.build());
    }
}
