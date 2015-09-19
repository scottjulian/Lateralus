package xyz.lateralus.components.gcm;


import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;


public class InstanceIdListener extends InstanceIDListenerService {

    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
