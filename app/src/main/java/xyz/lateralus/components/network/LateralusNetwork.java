package xyz.lateralus.components.network;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class LateralusNetwork {
    private static final String TAG = "LateralusNetwork";

    public static final String POST = "POST";
    public static final String API_URL  = "https://lateralus.xyz/api/v1/";

    public static final String API_PUT         = "put/";
    public static final String API_NEW_ACCOUNT = "register/";
    public static final String API_NEW_DEVICE  = "device/";
    public static final String API_AUTH        = "authenticate/";
    //public static final String API_GET         = "get/";

    public static final int ERROR_RESPONSE_CODE = 666;

    public static void sendJsonData(String apiEndpoint, JSONObject json, NetworkFireTaskDelegate delegate){
        String url = API_URL + parseApiEndpoint(apiEndpoint);
        delegate = (delegate != null) ? delegate : new FireTaskDelegate();
        NetworkFireTask task = new NetworkFireTask(url, delegate);
        task.execute(json);
    }

    private static String parseApiEndpoint(String endpoint){
        switch(endpoint){
            // case API_GET: return API_GET;
            case API_NEW_ACCOUNT: return API_NEW_ACCOUNT;
            case API_AUTH: return API_AUTH;
            case API_NEW_DEVICE : return API_NEW_DEVICE;
            default: return API_PUT;
        }
    }

    private static class NetworkFireTask extends AsyncTask<JSONObject, Integer, Integer> {
        private static final String TAG = "FireTask";
        private NetworkFireTaskDelegate _delegate;
        private String _url;

        public NetworkFireTask(String url, NetworkFireTaskDelegate delegate){
            super();
            _url = url;
            _delegate = delegate;
        }

        @Override
        protected Integer doInBackground(JSONObject... jsons) {
            int responseCode = 0;
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
                    responseCode = connection.getResponseCode();

                    InputStreamReader resReader;
                    if(responseCode >= 400){
                        resReader = new InputStreamReader(connection.getErrorStream());
                    }
                    else{
                        resReader = new InputStreamReader(connection.getInputStream());
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader reader = new BufferedReader(resReader);
                    String line;
                    while((line = reader.readLine()) != null){
                        stringBuilder.append(line).append("\n");
                    }

                    Log.d(TAG, "Response Code: " + responseCode);
                    Log.d(TAG, "Response Message: " + connection.getResponseMessage());
                    Log.d(TAG, "Response: " + stringBuilder.toString());

                    dos.close();
                    connection.disconnect();
                    _delegate.onFinish(responseCode, stringBuilder.toString());
                    return responseCode;
                }
                catch(Exception e){
                    Log.e(TAG, "ERR: Network Exception > " + e.getMessage());
                    e.printStackTrace();
                    _delegate.onFinish(ERROR_RESPONSE_CODE, "Network Exception");
                    return ERROR_RESPONSE_CODE;
                }
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer resCode) {
            Log.d(TAG, "Network fire task finished with code: " + resCode);
        }
    }

    public interface NetworkFireTaskDelegate {
        void onFinish(int resCode, String responseString);
    }

    static class FireTaskDelegate implements NetworkFireTaskDelegate {
        @Override
        public void onFinish(int resCode, String responseString) {
            Log.d(TAG, "FireTaskDelegate finished with code: " + resCode);
        }
    }

}
