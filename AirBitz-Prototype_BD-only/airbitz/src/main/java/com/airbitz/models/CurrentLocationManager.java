package com.airbitz.models;

import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by matt on 6/30/14.
 */
public class CurrentLocationManager {

    private static CurrentLocationManager mInstance = null;
    private LocationClient locationClient;

    public CurrentLocationManager(Context context, GooglePlayServicesClient.ConnectionCallbacks callbacks, GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener) {
        locationClient = new LocationClient(context, callbacks, onConnectionFailedListener);
    }

    public static CurrentLocationManager getLocationManager(Context context, GooglePlayServicesClient.ConnectionCallbacks callbacks, GooglePlayServicesClient.OnConnectionFailedListener onConnectionFailedListener) {
        if (null == mInstance) {
            mInstance = new CurrentLocationManager(context, callbacks, onConnectionFailedListener);
        }
        return mInstance;
    }

    public android.location.Location getLocation() {
        return locationClient.getLastLocation();
    }

    public void disconnect() {
        locationClient.disconnect();
    }

    public void connect() {
        locationClient.connect();
    }

    public void requestUpdates(LocationRequest locationRequest, LocationListener listener){
        locationClient.requestLocationUpdates(locationRequest,listener);
    }

    public boolean getConnectionStatus(){
        return locationClient.isConnected();
    }

    public void removeUpdates(LocationListener listener){
        locationClient.removeLocationUpdates(listener);
    }

}
