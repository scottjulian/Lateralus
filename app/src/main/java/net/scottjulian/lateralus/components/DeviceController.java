package net.scottjulian.lateralus.components;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;


public class DeviceController {
    private static final String TAG = "DeviceController";

    public static void hideAppInDrawer(Context ctx){
        try {
            // TODO call from GCM
            if(ctx instanceof Activity) {
                Activity act = (Activity) ctx;
                PackageManager p = act.getPackageManager();
                p.setComponentEnabledSetting(act.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
        catch(Exception e){
            Log.e(TAG, "Could not hide app from drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void showAppInDrawer(Context ctx){
        try {
            // TODO call from GCM
            if(ctx instanceof Activity) {
                Activity act = (Activity) ctx;
                PackageManager p = act.getPackageManager();
                p.setComponentEnabledSetting(act.getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
        }
        catch(Exception e){
            Log.e(TAG, "Could not show app in drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
