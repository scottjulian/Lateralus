package xyz.lateralus;


import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import xyz.lateralus.app.R;
import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.Utils;
import xyz.lateralus.components.gcm.RegistrationIntentService;
import xyz.lateralus.components.network.LateralusAuth;
import xyz.lateralus.fragments.RegisterFragment;
import xyz.lateralus.fragments.SettingsFragment;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener, CheckBox.OnCheckedChangeListener{
    private static final String TAG = "SignInActivity";

    private EditText  _emailField;
    private EditText  _passwordField;
    private TextView  _messageTextView;
    private ActionBar _actionBar;
    private Menu _menu;

    private LateralusAuth _auth;
    private LateralusPreferences _prefs;

    private BroadcastReceiver _registrationBroadcastReceiver;

    private static final long ONE_DAY_MILLIS = 1000 * 60 * 60 * 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        _auth = new LateralusAuth(this, new AuthDelegate());
        _prefs = new LateralusPreferences(this);
        _actionBar = getSupportActionBar();
        //_actionBar.setTitle(R.string.title_activity_sign_in);

        if(_prefs.isUserSignedIn() && !signInTimedOut()){
            switchToMainActivity();
        }

        _emailField = (EditText) findViewById(R.id.edit_text_sign_in_email);
        _emailField.setText((_prefs.rememberEmail()) ? _prefs.getEmail() : "");
        _passwordField = (EditText) findViewById(R.id.edit_text_sign_in_password);
        _messageTextView = (TextView) findViewById(R.id.text_view_sign_in_message);

        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(R.id.text_view_register_account).setOnClickListener(this);
        ((CheckBox) findViewById(R.id.check_box_remember_email)).setChecked(_prefs.rememberEmail());
        ((CheckBox) findViewById(R.id.check_box_remember_email)).setOnCheckedChangeListener(this);

        if(!Utils.checkPlayServices(this)) {
            setText(_messageTextView, "Google Play Services is required for this app.", R.color.lateralus_red);
            findViewById(R.id.text_view_register_account).setVisibility(View.INVISIBLE);
            findViewById(R.id.button_sign_in).setVisibility(View.INVISIBLE);
            _emailField.setVisibility(View.INVISIBLE);
            _passwordField.setVisibility(View.INVISIBLE);
            findViewById(R.id.check_box_remember_email).setVisibility(View.INVISIBLE);
        }
        else {
            setBroadcastReceiver();
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(findViewById(R.id.button_sign_in).getVisibility() == View.VISIBLE) {
            getMenuInflater().inflate(R.menu.sign_in_menu, menu);
            _menu = menu;
            return true;
        }
        return false;
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.home:
            case android.R.id.home:
                getFragmentManager().popBackStack();
                showRegisterActionBar(false);
                return true;
            case R.id.action_settings:
                loadSettingsFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
            showRegisterActionBar(false);
            showSettingsActionBar(false);
        }
        else {
            super.onBackPressed();
        }
    }

    /* -------- AUTHENTICATION -------- */

    private void authenticate(){
        String email = _emailField.getText().toString();
        String pass = _passwordField.getText().toString();
        if(inputIsValid(email, pass)){
            if(_prefs.getGcmToken().isEmpty() || !_prefs.wasGcmTokenSentToServer()) {
                setText(_messageTextView, "Error with GCM. Try again in 1 min", R.color.lateralus_red);
                // TODO: send error to server
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
                return;
            }
            else if(Utils.internetActive(this)){
                setText(_messageTextView, "Signing in...", R.color.lateralus_gray_med);
                _auth.signIn(email, pass);
            }
            else {
                setText(_messageTextView, "You need to be connected to the internet to authenticate your account.", R.color.lateralus_red);
            }
        }
    }

    private class AuthDelegate implements LateralusAuth.LateralusAuthDelegate{
        @Override
        public void authFinished(final Boolean success, final String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(success){
                        _prefs.setLastSignInMillis(Utils.getTimestampMillis());
                        switchToMainActivity();
                    }
                    else {
                        setText(_messageTextView, msg, R.color.lateralus_red);
                    }
                }
            });
        }
    }

    private void switchToMainActivity(){
        Intent app = new Intent(this, MainActivity.class);
        startActivity(app);
        finish();
    }

    /* -------- GCM -------- */

    private void setBroadcastReceiver(){
        _registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean sentToken = _prefs.wasGcmTokenSentToServer();
                String token = _prefs.getGcmToken();
                if(sentToken && !token.isEmpty()){
                    // YAY! GCM SUCCESS, now we can login into lateralus
                }
            }
        };
    }

    /* -------- Helpers -------- */

    private Boolean inputIsValid(String email, String pass){
        if(!Utils.isValidEmail(email)){
            setText(_messageTextView, "Please enter a valid email", R.color.lateralus_red);
            return false;
        }
        else if(!Utils.passwordHasCorrectLength(pass)){
            setText(_messageTextView, "Password must be at least 6 characters.", R.color.lateralus_red);
            return false;
        }
        return true;
    }

    private void setText(TextView tv, String text, int colorId){
        tv.setText(text);
        tv.setTextColor(getResources().getColor(colorId));
    }

    private void loadRegisterFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.activity_sign_in_container, new RegisterFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        showRegisterActionBar(true);
    }

    private void showRegisterActionBar(Boolean show){
        _menu.findItem(R.id.action_settings).setVisible(!show);
        if(show){
            _actionBar.setTitle(R.string.title_fragment_register);
            _actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else{
            _actionBar.setTitle(R.string.title_activity_sign_in);
            _actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private void loadSettingsFragment(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right,
                R.anim.slide_in_right,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.activity_sign_in_container, new SettingsFragment());
        transaction.addToBackStack(null);
        transaction.commit();
        showSettingsActionBar(true);
    }

    private void showSettingsActionBar(Boolean show){
        _menu.findItem(R.id.action_settings).setVisible(!show);
        if(show){
            _actionBar.setTitle(R.string.title_fragment_settings);
            _actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else{
            _actionBar.setTitle(R.string.title_activity_main);
            _actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    private Boolean signInTimedOut() {
        long last = _prefs.getLastSignInMillis();
        return last <= 0 || (last + ONE_DAY_MILLIS) < Utils.getTimestampMillis();
    }

    /* -------- View Listeners -------- */

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        int id = button.getId();
        switch(id){
            case R.id.check_box_remember_email:
                _prefs.shouldRememberEmail(checked);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        setText(_messageTextView, "", R.color.lateralus_gray_med);
        int id = view.getId();
        switch(id){
            case R.id.button_sign_in:
                authenticate();
                break;
            case R.id.text_view_register_account:
                loadRegisterFragment();
                break;
        }
    }
}
