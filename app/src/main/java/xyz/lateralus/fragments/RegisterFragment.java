package xyz.lateralus.fragments;


import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import xyz.lateralus.app.R;
import xyz.lateralus.components.LateralusPreferences;
import xyz.lateralus.components.Utils;
import xyz.lateralus.components.network.LateralusAuth;


public class RegisterFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "LateralusRegister";

    private LinearLayout _layout;
    private LateralusPreferences _prefs;

    private LateralusAuth _auth;

    private TextView _emailField;
    private TextView _passwordField;
    private TextView _passwordVerifyField;
    private TextView _messageTextView;
    private TextView _deviceNicknameField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _prefs = new LateralusPreferences(getActivity());
        _auth = new LateralusAuth(getActivity(), new AuthDelegate());

        _layout = (LinearLayout)inflater.inflate(R.layout.fragment_register, container, false);
        _layout.findViewById(R.id.button_register_new_account).setOnClickListener(this);
        _emailField = (TextView) _layout.findViewById(R.id.edit_text_register_email);
        _passwordField = (TextView) _layout.findViewById(R.id.edit_text_register_password);
        _passwordVerifyField = (TextView) _layout.findViewById(R.id.edit_text_register_verify_password);
        _messageTextView = (TextView) _layout.findViewById(R.id.text_view_register_message);
        _deviceNicknameField = (TextView) _layout.findViewById(R.id.edit_text_register_device_nickname);
        setHasOptionsMenu(false);
        return _layout;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        setText(_messageTextView, "", R.color.lateralus_gray_med);
        switch(id){
            case R.id.button_register_new_account:
                registerAccount();
                break;
        }
    }

    private Boolean inputIsValid(String email, String pass, String pass2, String deviceName){
        if(!Utils.isValidEmail(email)){
            setText(_messageTextView, "Please enter a valid email.", R.color.lateralus_red);
            return false;
        }
        else if(!Utils.passwordHasCorrectLength(pass)){
            setText(_messageTextView, "Password has to be at least 6 characters.", R.color.lateralus_red);
            return false;
        }
        else if(!pass.equals(pass2)){
            setText(_messageTextView, "Passwords do not match.", R.color.lateralus_red);
            return false;
        }
        else if(deviceName.isEmpty()){
            setText(_messageTextView, "Please enter a nickname for this device.", R.color.lateralus_red);
            return false;
        }
        return true;
    }

    private void registerAccount(){
        String email = _emailField.getText().toString();
        String pass = _passwordField.getText().toString();
        String pass2 = _passwordVerifyField.getText().toString();
        String deviceName = _deviceNicknameField.getText().toString();
        if(inputIsValid(email, pass, pass2, deviceName)){
            setText(_messageTextView, "Registering...", R.color.lateralus_gray_med);
            _auth.register(email, pass, deviceName);
        }
    }

    private void registerSuccess(){
        // TODO
    }

    private void setText(TextView tv, String text, int colorId){
        tv.setText(text);
        tv.setTextColor(getResources().getColor(colorId));
    }


    private class AuthDelegate implements LateralusAuth.LateralusAuthDelegate{
        @Override
        public void authFinished(final Boolean success, final String msg) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(success) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Registration Success!")
                                .setMessage(msg)
                                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().onBackPressed();
                                    }

                                })
                                .show();
                    }
                    else {
                        setText(_messageTextView, msg, R.color.lateralus_red);
                    }
                }
            });
        }
    }
}
