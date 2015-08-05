package net.scottjulian.lateralus.components.network;


import android.content.Context;
import android.util.Log;

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

    private static final String MOTHERSHIP_BASE_URL = "https://lateralus.scottjulian.net:443/";
    private static final String API_URL_PIECE       = "api/v1/";
    private static final String POST = "POST";

    public static final String API_PUT = "put/";
    public static final String API_GET = "get/";
    public static final String API_REGISTER = "register/";

    public static void sendRegistrationToken(Context ctx) {
        try{
            JsonDataCreator dc = new JsonDataCreator(ctx);
            JSONObject data = dc.createRegistrationData();
            fireJsonData(ctx, API_REGISTER, data);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Should be called asynchronously!
     * @param json
     */
    public static void fireJsonData(Context ctx, String apiEndpoint, JSONObject json){
        try {
            Log.d(TAG, "Firing Data: \n\n" + json.toString(4) + "\n\n");

            URL url = new URL(MOTHERSHIP_BASE_URL + API_URL_PIECE + apiEndpoint);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            Log.d(TAG, "port: " + connection.getURL().getPort());
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(POST);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setSSLSocketFactory(getSocketFactory(ctx));

            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            String jsonStr = json.toString();
            byte[] data = jsonStr.getBytes("UTF-8");
            dos.write(data);
            dos.flush();

            Log.d(TAG, "Method: " + connection.getRequestMethod());
            Log.d(TAG, "do output: " + connection.getDoOutput());
            connection.connect();

            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while((line = reader.readLine()) != null){
                stringBuilder.append(line + "\n");
            }

            Log.d(TAG, "Response Code: " + connection.getResponseCode());
            Log.d(TAG, "Response Message: " + connection.getResponseMessage());
            Log.d(TAG, "Response: " + stringBuilder.toString());

            dos.close();
            connection.disconnect();
        }
        catch(Exception e){
            Log.e(TAG, "ERR: Network Exception");
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory getSocketFactory(Context ctx) {
        try{
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = ctx.getAssets().open("lateralus.crt");
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
            return null;
        }
    }

}
