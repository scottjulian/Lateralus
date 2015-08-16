package xyz.lateralus;

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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import xyz.lateralus.app.R;
import xyz.lateralus.components.readers.DeviceReader;
import xyz.lateralus.components.gcm.RegistrationIntentService;


public class SettingsFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "LateralusSettings";

    private RelativeLayout _layout;
    private SharedPreferences _prefs;
    private EditText _emailText;
    private TextView _textBoxInfo;
    private DeviceReader _deviceReader;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _layout = (RelativeLayout)inflater.inflate(R.layout.fragment_settings, container, false);
        setHasOptionsMenu(false);
        _prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        _deviceReader = new DeviceReader(getActivity());

        _emailText = (EditText) _layout.findViewById(R.id.edit_email);
        //_emailText.setText(_deviceReader.getEmail());
        _layout.findViewById(R.id.button_register).setOnClickListener(this);
        _layout.findViewById(R.id.button_unregister).setOnClickListener(this);
        _textBoxInfo = (TextView) _layout.findViewById(R.id.text_box_information);
        updateTextBoxInfo();
        return _layout;
    }

    private void register() {
        String email = _emailText.getText().toString();
        if(email == "" || email == null || email.isEmpty()){
            email = "Android";
        }

        SharedPreferences.Editor edit = _prefs.edit();
        edit.putString(getString(R.string.key_for_email), email);
        edit.commit();

        // start GCM register
        if(checkPlayServices()){
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        }
        updateTextBoxInfo();
    }

    private void unRegister(){
        SharedPreferences.Editor edit = _prefs.edit();
        edit.remove(getString(R.string.key_for_email));
        edit.remove(getString(R.string.key_for_token));
        edit.commit();
        _emailText.setText("");
        updateTextBoxInfo();
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

    private void updateTextBoxInfo(){
        _textBoxInfo.setText(String.format("uuid: %s \ntoken: %s \n",
                _deviceReader.getUniqueDeviceId(),
                _deviceReader.getGcmToken()
        ));
        _layout.findViewById(R.id.button_unregister).setVisibility(
                (_deviceReader.getEmail().isEmpty() ? View.INVISIBLE : View.VISIBLE)
        );
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button_register:
                register();
                break;
            case R.id.button_unregister:
                unRegister();
                break;
        }
    }
}
