package com.airbitz.models;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 6/30/14.
 */
public class CurrentLocationManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static CurrentLocationManager mInstance = null;
    private LocationClient locationClient;
    private Location mCurrentLocation;
    LocationRequest mLocationRequest;

    public CurrentLocationManager(Context context) {
        locationClient = new LocationClient(context, this, this);

        attemptConnection();
    }

    // Callback interface for adding and removing location change listeners
    private List<OnLocationChange> mOnLocationChange = new ArrayList<OnLocationChange>();

    public interface OnLocationChange {
        public void OnCurrentLocationChange(Location location);
    }

    public void addLocationChangeListener(OnLocationChange listener) {

        if(!mOnLocationChange.contains(listener)) {
            mOnLocationChange.add(listener);
        }
    }

    public void removeLocationChangeListener(OnLocationChange listener) {
        if(mOnLocationChange.contains(listener)) {
            mOnLocationChange.remove(listener);
        }
    }

    public static CurrentLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new CurrentLocationManager(context);
        }
        return mInstance;
    }

    public Location getLocation() {
        return mCurrentLocation;
    }

    public void attemptConnection() {
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("CurrentLocationManager", "Connected.");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        locationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.d("CurrentLocationManager", "Disconnected. Please re-connect.");
        attemptConnection();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy() && !mOnLocationChange.isEmpty()) {
            mCurrentLocation = location;
            Log.d("TAG_LOC",
                    "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());
            for(OnLocationChange listener : mOnLocationChange) {
                listener.OnCurrentLocationChange(mCurrentLocation);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG_LOC", "Connection to LocationClient failed");
    }
}
