package net.scottjulian.lateralus.components.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;


public class LocationReader {
    private static final String TAG = "LocationReader";
    private static final String ROOT_KEY = "location";

    private Context _ctx;
    private static LocationManager _locManager;
    private static LocListener _listener;
    private Location _currentBestLocation;
    private LocDelegate _delegate;

    private static final int   MAX_DELTA_TIME    = 1000 * 60 * 2; // mins
    private static final long  MIN_UPDATE_TIME   = 1000 * 15; // secs
    private static final float MIN_UPDATE_METERS = 3.0f; // meters

    public LocationReader(Context ctx, LocDelegate delegate){
        _ctx = ctx;
        _locManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        _listener = new LocListener();
        _delegate = delegate;
    }

    /* -------- Delegate -------- */

    private void sendLocation(Location loc){
        _delegate.onLocationReceived(loc);
    }

    private void sendError(String msg){
        _delegate.onErrorReceived(msg);
    }

    private void sendMessage(String msg){
        _delegate.onMessageReceived(msg);
    }

    /* -------- Public -------- */

    public void startTracking(Boolean gps){
        if(gps){
            if(isGPSEnabled()) {
                attachLocListener(LocationManager.GPS_PROVIDER);
            }
            else{
                sendMessage("GPS is not enabled");
                attachLocListener(LocationManager.NETWORK_PROVIDER);
            }
        }
        else{
            attachLocListener(LocationManager.NETWORK_PROVIDER);
        }
        sendMessage("Start Tracking message received");
    }

    public void requestSingleLocationUpdate(Boolean gps){
        sendMessage("Get single location message received");
        if(gps && isGPSEnabled()) {
            _locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, _listener, null);
        }
        else{
            _locManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, _listener, null);
        }
    }

    public Location getLastLocation(){
        if(isGPSEnabled()){
            return _locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return _locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public void stopTracking(){
        detachLocListener();
        sendMessage("Stop Tracking message received");
    }

    public Boolean isGPSEnabled(){
        return _locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static JSONObject getJsonFromLocation(Location loc){
        try{
            JSONObject data = new JSONObject();
            data.put("latitude", loc.getLatitude());
            data.put("longitude", loc.getLongitude());
            data.put("accuracy", loc.getAccuracy());
            data.put("altitude", loc.getAltitude());
            data.put("provider", loc.getProvider());
            data.put("speed", loc.getSpeed());
            return new JSONObject().put(ROOT_KEY, data);
        }
        catch(Exception e){
            Log.e(TAG, "Could not create json from location");
            e.printStackTrace();
        }
        return null;
    }

    public static String getRootKey(){
        return ROOT_KEY;
    }

    /* -------- Private -------- */

    private void switchGpsToNetwork(){
        detachLocListener();
        attachLocListener(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > MAX_DELTA_TIME;
        boolean isSignificantlyOlder = timeDelta < -MAX_DELTA_TIME;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        }
        else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        }
        else if (isNewer && !isLessAccurate) {
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        return (provider1 == null) ? provider2 == null : provider1.equals(provider2);
    }

    private void detachLocListener(){
        _locManager.removeUpdates(_listener);
    }

    private void attachLocListener(String provider){
        if(provider.equals(LocationManager.GPS_PROVIDER)){
            _locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_METERS, _listener);
        }
        else if(provider.equals(LocationManager.NETWORK_PROVIDER)){
            _locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_TIME, MIN_UPDATE_METERS, _listener);
        }
        else{
            sendError("Could not attach location listener. No provider is available");
        }
    }

    /* -------- Listener -------- */

    private class LocListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            if(isBetterLocation(location, _currentBestLocation)){
                _currentBestLocation = location;
            }
            sendLocation(_currentBestLocation);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle bundle) {
            if(status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE){
                stopTracking();
                sendError("status changed to 'out of service' or is unavailable");
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                if(isGPSEnabled()) {
                    attachLocListener(LocationManager.GPS_PROVIDER);
                }
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if(provider.equals(LocationManager.GPS_PROVIDER)){
                switchGpsToNetwork();
            }
        }
    }
}
