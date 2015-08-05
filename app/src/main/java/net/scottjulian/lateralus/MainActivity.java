package net.scottjulian.lateralus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import net.scottjulian.lateralus.gcm.RegistrationIntentService;

import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BroadcastReceiver _registrationBroadcastReceiver;
    private SharedPreferences _prefs;

    // GCM SERVER API KEY
    // AIzaSyB3wb7sIYgvg2YB_u3f2oqRtsQuNJFKWbc

    // GCM SENDER ID
    // 1036162702266

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(_registrationBroadcastReceiver, new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(_registrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _prefs = PreferenceManager.getDefaultSharedPreferences(this);
        saveUniqueDeviceId();

        _registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean sentToken = _prefs.getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
                if (sentToken){
                    Log.d(TAG, "SEND");
                    setContentView(R.layout.activity_main);
                }
                else {
                    Log.d(TAG, "ERRR");
                }
            }
        };

        String email = _prefs.getString(getString(R.string.key_for_email), null);
        if(email == null){
            setContentView(R.layout.activity_main_register);
            findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    register();
                }
            });
        }
        else{
            setContentView(R.layout.activity_main);
        }

        /*
        LocationReader lr = new LocationReader(MainActivity.this, new LocDelegate() {
            @Override
            public void onLocationReceived(Location loc) {
                // nothing
            }

            @Override
            public void onErrorRecevied() {
                // nothing
            }
        });
        lr.startTracking(true);
        */
    }

    private void saveUniqueDeviceId() {
        String did = _prefs.getString(getString(R.string.key_for_device_id), null);
        if(did == null) {
            final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            String deviceId = deviceUuid.toString();
            SharedPreferences.Editor edit = _prefs.edit();
            edit.putString(getString(R.string.key_for_device_id), deviceId);
            edit.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void register() {
        EditText emailText = (EditText) findViewById(R.id.edit_email);
        String email = emailText.getText().toString();
        if(email == "" || email == null || email.isEmpty()){
            return;
        }
        SharedPreferences.Editor edit = _prefs.edit();
        edit.putString(getString(R.string.key_for_email), email);
        edit.commit();

        if(checkPlayServices()){
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 9000).show();
            }
            else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
