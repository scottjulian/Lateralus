package net.scottjulian.lateralus.components.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

import net.scottjulian.lateralus.R;


public class LocationReader {

    private Context _ctx;
    private LocationManager _locManager;
    private LocListener _listener;
    private Location _currentBestLocation;
    private LocDelegate _delegate;

    private static final int   MAX_DELTA_TIME    = 1000 * 60 * 2; // 2 min
    private static final long  MIN_UPDATE_TIME   = 1000 * 30; // 30 secs
    private static final float MIN_UPDATE_METERS = 5.0f; // 5 meters

    public LocationReader(Context ctx, LocDelegate delegate){
        _ctx = ctx;
        _locManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        _listener = new LocListener();
        _delegate = delegate;
    }

    public void startTracking(Boolean gps){
        if(gps){
            if(isGPSEnabled()) {
                attachLocListener(LocationManager.GPS_PROVIDER);
            }
            else{
                //TODO send no gps message
                attachLocListener(LocationManager.NETWORK_PROVIDER);
            }
        }
        else{
            attachLocListener(LocationManager.NETWORK_PROVIDER);
        }
    }

    public void requestSingleLocationUpdate(Boolean gps){
        _locManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, _listener, null);
    }

    public Location getLastLocation(){
        if(isGPSEnabled()){
            return _locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return _locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public void stop(){
        detachLocListener();
    }

    public Boolean isGPSEnabled(){
        return _locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void sendLocation(Location loc){
        _delegate.onLocationReceived(loc);
    }

    private void sendError(){
        _delegate.onErrorRecevied();
    }

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

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

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
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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
    }

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
                stop();
                sendError();
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
