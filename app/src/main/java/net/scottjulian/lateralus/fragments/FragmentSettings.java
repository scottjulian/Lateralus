package net.scottjulian.lateralus.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import net.scottjulian.lateralus.R;
import net.scottjulian.lateralus.gcm.RegistrationIntentService;


public class FragmentSettings extends Fragment {
    private static final String TAG = "LateralusSettings";

    private RelativeLayout _layout;
    private SharedPreferences _prefs;
    private EditText _emailText;
    private Button _registerButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _layout = (RelativeLayout)inflater.inflate(R.layout.fragment_settings, container, false);
        _prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _emailText = (EditText) _layout.findViewById(R.id.edit_email);
        _emailText.setText(_prefs.getString(getString(R.string.key_for_email), ""));
        _registerButton = (Button) _layout.findViewById(R.id.button_register);
        _registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        return _layout;
    }



    private void register() {
        String email = _emailText.getText().toString();
        if(email == "" || email == null || email.isEmpty()){
            return;
        }
        SharedPreferences.Editor edit = _prefs.edit();
        edit.putString(getString(R.string.key_for_email), email);
        edit.commit();

        if(checkPlayServices()){
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 9000).show();
            }
            else {
                Log.i(TAG, "This device is not supported.");
                getActivity().finish();
            }
            return false;
        }
        return true;
    }
}
