package net.scottjulian.lateralus.components.location;


import android.location.Location;


public interface LocDelegate {

    void onLocationReceived(Location loc);
    void onErrorReceived(String msg);
    void onMessageReceived(String msg);
}
