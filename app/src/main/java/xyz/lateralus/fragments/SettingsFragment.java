package xyz.lateralus.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


import xyz.lateralus.SignInActivity;
import xyz.lateralus.app.R;
import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.Utils;


public class SettingsFragment extends Fragment implements Switch.OnCheckedChangeListener, View.OnClickListener{
    private static final String TAG = "LateralusSettings";

    private LinearLayout _layout;
    private LateralusPreferences _prefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _prefs = new LateralusPreferences(getActivity());
        _layout = (LinearLayout)inflater.inflate(R.layout.fragment_settings, container, false);
        _layout.findViewById(R.id.button_hide_app).setOnClickListener(this);
        _layout.findViewById(R.id.button_delete_account).setOnClickListener(this);
        if(!_prefs.isUserSignedIn()) {
            _layout.findViewById(R.id.button_delete_account).setVisibility(View.INVISIBLE);
            _layout.findViewById(R.id.text_header_account_management).setVisibility(View.INVISIBLE);
        }

        setSwitchPositions();
        setHasOptionsMenu(false);
        return _layout;
    }

    private void setSwitchPositions(){
        // location
        Switch sw = (Switch) _layout.findViewById(R.id.switch_location);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_LOCATION));
        sw.setOnCheckedChangeListener(this);

        // messages
        sw = (Switch) _layout.findViewById(R.id.switch_textmessages);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_MESSAGES));
        sw.setOnCheckedChangeListener(this);

        // phone logs
        sw = (Switch) _layout.findViewById(R.id.switch_phonelogs);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_PHONE_LOGS));
        sw.setOnCheckedChangeListener(this);

        // chrome history
        sw = (Switch) _layout.findViewById(R.id.switch_chromehistory);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_CHROME_HIST));
        sw.setOnCheckedChangeListener(this);

        // camera
        sw = (Switch) _layout.findViewById(R.id.switch_camera);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_CAMERA));
        sw.setOnCheckedChangeListener(this);

        // microphone
        sw = (Switch) _layout.findViewById(R.id.switch_microphone);
        sw.setChecked(_prefs.hasDataPermission(LateralusPreferences.PERMISSION_MICROPHONE));
        sw.setOnCheckedChangeListener(this);

        // wifi only
        sw = (Switch) _layout.findViewById(R.id.switch_wifionly);
        sw.setChecked(_prefs.useWifiOnly());
        sw.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        int id = button.getId();
        switch(id){
            case R.id.switch_location:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_LOCATION, checked);
                break;
            case R.id.switch_textmessages:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_MESSAGES, checked);
                break;
            case R.id.switch_phonelogs:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_PHONE_LOGS, checked);
                break;
            case R.id.switch_chromehistory:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_CHROME_HIST, checked);
                break;
            case R.id.switch_camera:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_CAMERA, checked);
                break;
            case R.id.switch_microphone:
                _prefs.setDataPermission(LateralusPreferences.PERMISSION_MICROPHONE, checked);
                break;
            case R.id.switch_wifionly:
                _prefs.setWifiOnly(checked);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.button_hide_app:
                Toast.makeText(getActivity(), "Hiding " + getString(R.string.app_name) + "...", Toast.LENGTH_LONG).show();
                Utils.hideAppInDrawer(getActivity());
                break;
            case R.id.button_delete_account:
                deleteAccount();
                break;
        }
    }

    private void deleteAccount(){
        if(!_prefs.isUserSignedIn()){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Sign In")
                    .setMessage("You must sign in to delete your account.")
                    .setPositiveButton("OK", null)
                    .show();
        }
        else {
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Delete Account")
                    .setMessage("Confirm account deletion.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: delete account
                            _prefs.signOut();
                            Intent signInActivity = new Intent(getActivity(), SignInActivity.class);
                            getActivity().startActivity(signInActivity);
                            getActivity().finish();
                        }

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }


}
