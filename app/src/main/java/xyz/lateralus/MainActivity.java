package xyz.lateralus;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import xyz.lateralus.app.R;
import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.Utils;
import xyz.lateralus.fragments.SettingsFragment;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private LateralusPreferences _prefs;
    private Menu _menu;
    private ActionBar _actionBar;

    private TextView _textStatusAccount;
    private TextView _textStatusDevice;
    private TextView _textStatusGcm;
    private TextView _textStatusInternet;

    //xyz.lateralus
    // API: AIzaSyC806K4lp9IcyGy0NNYzERP9EQkf_u3OUc
    // Sender ID: 1036162702266

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _prefs = new LateralusPreferences(this);
        _actionBar = getSupportActionBar();

        findViewById(R.id.text_link_privacy_policy).setOnClickListener(this);
        ((TextView)findViewById(R.id.text_link_privacy_policy)).setText("privacy policy");
        ((TextView)findViewById(R.id.text_lateralus_version)).setText(getString(R.string.app_name) + " v" + Utils.getPkgVersion());
        ((TextView)findViewById(R.id.text_lateralus_url)).setText(getString(R.string.url_lateralus));
        ((TextView)findViewById(R.id.text_lateralus_email)).setText(getString(R.string.email_lateralus));

        _textStatusAccount = (TextView) findViewById(R.id.text_status_account_type);
        _textStatusDevice = (TextView) findViewById(R.id.text_status_device_id);
        _textStatusGcm = (TextView) findViewById(R.id.text_status_gcm_token);
        _textStatusInternet = (TextView) findViewById(R.id.text_status_internet);
        ((TextView) findViewById(R.id.text_title_email)).setText(_prefs.getEmail());

        setStatusTexts();
    }

    private void setStatusTexts(){
        // account
        if(!_prefs.getUserType().isEmpty()) {
            setText(_textStatusAccount, Utils.capitalizeFirstChar(_prefs.getUserType()), R.color.lateralus_green);
        }
        else{
            setText(_textStatusAccount, "Unknown", R.color.lateralus_gray_med);
        }

        // device
        if(_prefs.getDeviceRowId() > 0) {
            setText(_textStatusDevice, "Registered", R.color.lateralus_green);
        }
        else{
            setText(_textStatusDevice, "Not Registered", R.color.lateralus_gray_med);
        }

        // gcm
        if(!_prefs.getGcmToken().isEmpty()){
            setText(_textStatusGcm, "OK", R.color.lateralus_green);
        }
        else{
            setText(_textStatusGcm, "No Token", R.color.lateralus_gray_med);
        }

        // internet
        if(Utils.internetActive(this)){
            setText(_textStatusInternet, "Connected", R.color.lateralus_green);
        }
        else{
            setText(_textStatusInternet, "Not Connected", R.color.lateralus_gray_med);
        }

    }

    private void setText(TextView tv, String text, int colorId){
        tv.setText(text);
        tv.setTextColor(getResources().getColor(colorId));
    }

    private void updateStatus(){
        Toast.makeText(this, "Updating status...", Toast.LENGTH_LONG).show();
        // TODO:
        // re sign in...
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        _menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.home:
            case android.R.id.home:
                getFragmentManager().popBackStack();
                showSettingsActionBar(false);
                return true;
            case R.id.action_settings:
                loadSettingsFragment();
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
            showSettingsActionBar(false);
        }
        else {
            super.onBackPressed();
        }
    }

    private void signOut(){
        _prefs.signOut();
        Intent signInActivity = new Intent(this, SignInActivity.class);
        startActivity(signInActivity);
        finish();
    }

    private void loadSettingsFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.activity_main_container, new SettingsFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        showSettingsActionBar(true);
    }

    private void showSettingsActionBar(Boolean show){
        _menu.findItem(R.id.action_settings).setVisible(!show);
        _menu.findItem(R.id.action_sign_out).setVisible(!show);
        if(show){
            _actionBar.setTitle(R.string.title_fragment_settings);
            _actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else{
            _actionBar.setTitle(R.string.title_activity_main);
            _actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.text_link_privacy_policy:
                Utils.openLinkInBrowser(this, getString(R.string.url_lateralus_privacy_policy));
                break;
        }
    }
}
