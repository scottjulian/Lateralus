package xyz.lateralus.components.network;


import android.os.AsyncTask;
import android.util.Log;

import xyz.lateralus.Config;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class Network {
    private static final String TAG = "LateralusNetwork";
    private static final String POST = "POST";

    public static final String API_PUT = "put/";
    public static final String API_GET = "get/";
    public static final String API_REGISTER = "register/";

    public static void fireJsonData(String apiEndpoint, JSONObject json){
        String url = Config.API_URL + parseApiEndpoint(apiEndpoint);
        FireTaskHandler delegate = new FireTaskHandler();
        FireTask task = new FireTask(url, delegate);
        task.execute(json);
    }

    private static String parseApiEndpoint(String endpoint){
        switch(endpoint){
            case API_GET:
                return API_GET;
            case API_REGISTER:
                return API_REGISTER;
            default:
                return API_PUT;
        }
    }

    private static class FireTask extends AsyncTask<JSONObject, Integer, String> {
        private static final String TAG = "FireTask";
        private FireTaskDelegate _delegate;
        private String _url;

        public static final String SUCCESS = "Success";
        public static final String FAILURE = "Failure";

        public FireTask(String url, FireTaskDelegate delegate){
            super();
            _url = url;
            _delegate = delegate;
        }

        @Override
        protected String doInBackground(JSONObject... jsons) {
            if(jsons.length > 0){
                JSONObject json = jsons[0];
                try {
                    URL url = new URL(_url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod(POST);
                    connection.setRequestProperty("Content-Type", "application/json");

                    DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                    String jsonStr = json.toString();
                    byte[] data = jsonStr.getBytes("UTF-8");
                    dos.write(data);
                    dos.flush();
                    connection.connect();

                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while((line = reader.readLine()) != null){
                        stringBuilder.append(line).append("\n");
                    }

                    Log.d(TAG, "Response Code: " + connection.getResponseCode());
                    Log.d(TAG, "Response Message: " + connection.getResponseMessage());
                    Log.d(TAG, "Response: " + stringBuilder.toString());

                    dos.close();
                    connection.disconnect();
                }
                catch(Exception e){
                    Log.e(TAG, "ERR: Network Exception > " + e.getMessage());
                    e.printStackTrace();
                    return FAILURE;
                }
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(String msg) {
            if(_delegate != null) {
                if(!msg.equals(SUCCESS)) {
                    _delegate.onFailure(msg);
                }
                _delegate.onSuccess(msg);
            }
        }
    }

    public interface FireTaskDelegate {
        void onSuccess(String message);
        void onFailure(String message);
    }

    static class FireTaskHandler implements FireTaskDelegate{

        @Override
        public void onSuccess(String message) {
            Log.d(TAG, "FireTask finished Successfully!");
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "FireTask Failed: " + message);
        }
    }

}
