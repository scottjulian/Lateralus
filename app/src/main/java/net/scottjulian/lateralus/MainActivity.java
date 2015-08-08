package net.scottjulian.lateralus;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.scottjulian.lateralus.components.DeviceController;
import net.scottjulian.lateralus.components.readers.DeviceReader;
import net.scottjulian.lateralus.fragments.SettingsFragment;
import net.scottjulian.lateralus.gcm.RegistrationIntentService;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BroadcastReceiver _registrationBroadcastReceiver;
    private SharedPreferences _prefs;
    private TextView _textSecureStatus;
    private Button _btnMakeSecure;
    private DeviceReader _deviceReader;

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
        setContentView(R.layout.activity_main);

        _prefs = PreferenceManager.getDefaultSharedPreferences(this);
        _textSecureStatus = (TextView) findViewById(R.id.text_view_secure_status);
        _btnMakeSecure = (Button) findViewById(R.id.button_make_secure);
        _btnMakeSecure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSettingsFragment();
            }
        });

        _deviceReader = new DeviceReader(this);

        setBroadcastReceiver();
        checkRegistrationStatus();

    }

    private void setBroadcastReceiver(){
        _registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean sentToken = _prefs.getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);
                if (sentToken){
                    Log.d(TAG, "SEND");
                }
                else {
                    Log.d(TAG, "ERRR");
                }
            }
        };
    }

    public void checkRegistrationStatus(){
        String email = _deviceReader.getEmail();
        String token = _deviceReader.getGcmToken();
        if(email.isEmpty() || token.isEmpty()){
            // not secure
            _textSecureStatus.setText("NOT SECURE");
            _textSecureStatus.setTextColor(getResources().getColor(R.color.lateralus_red));
            _btnMakeSecure.setVisibility(View.VISIBLE);
        }
        else{
            // secure
            _textSecureStatus.setText("SECURE");
            _textSecureStatus.setTextColor(getResources().getColor(R.color.lateralus_green));
            _btnMakeSecure.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings) {
            //loadSettingsFragment();
            DeviceController.hideAppInDrawer(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            checkRegistrationStatus();
            getFragmentManager().popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void loadSettingsFragment(){
        _btnMakeSecure.setVisibility(View.INVISIBLE);
        SettingsFragment fragment = new SettingsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_main_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
