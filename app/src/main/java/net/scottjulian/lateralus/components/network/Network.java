package net.scottjulian.lateralus.components.network;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.scottjulian.lateralus.Config;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class Network {
    private static final String TAG = "LateralusNetwork";
    private static final String POST = "POST";

    public static final String API_PUT = "put/";
    public static final String API_GET = "get/";
    public static final String API_REGISTER = "register/";

    public static void fireJsonData(Context ctx, String apiEndpoint, JSONObject json){
        String url = Config.API_URL + parseApiEndpoint(apiEndpoint);
        FireTaskHandler delegate = new FireTaskHandler();
        FireTask task = new FireTask(ctx, url, delegate);
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

    private static SSLSocketFactory getSocketFactory(Context ctx) {
        try{
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = ctx.getAssets().open(Config.SSL_CERT);
            Certificate ca;
            ca = cf.generateCertificate(caInput);
            caInput.close();
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        }
        catch(Exception e){
            Log.e(TAG, "Error getting SSL Socket Factory!");
            e.printStackTrace();
        }
        return null;
    }

    private static class FireTask extends AsyncTask<JSONObject, Integer, String> {
        private static final String TAG = "FireTask";
        private FireTaskDelegate _delegate;
        private Context _ctx;
        private String _url;

        public static final String SUCCESS = "Success";
        public static final String FAILURE = "Failure";

        public FireTask(Context ctx, String url, FireTaskDelegate delegate){
            super();
            _url = url;
            _ctx = ctx;
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
                    connection.setSSLSocketFactory(getSocketFactory(_ctx));

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
