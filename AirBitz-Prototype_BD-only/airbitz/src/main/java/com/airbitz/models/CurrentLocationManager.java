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
import java.util.Collections;
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
    private Context mContext;

    public CurrentLocationManager(Context context) {
        mContext = context;
    }

    // Callback interface for adding and removing location change listeners
    private List<OnLocationChange> mObservers = Collections.synchronizedList(new ArrayList<OnLocationChange>());
    private List<OnLocationChange> mRemovers = new ArrayList<OnLocationChange>();

    public interface OnLocationChange {
        public void OnCurrentLocationChange(Location location);
    }

    public void addLocationChangeListener(OnLocationChange listener) {
        if(mObservers.size() == 0) {
            locationClient = new LocationClient(mContext, this, this);
            attemptConnection();
        }
        if(!mObservers.contains(listener)) {
            mObservers.add(listener);
        }
        if(null != listener){
            if(null != mCurrentLocation) {
                listener.OnCurrentLocationChange(mCurrentLocation);
            }
        }
    }

    public void removeLocationChangeListener(OnLocationChange listener) {
        mRemovers.add(listener);
        if(mObservers.size() <= 0) {
            locationClient.disconnect();
        }
    }

    public static CurrentLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new CurrentLocationManager(context);
        }
        return mInstance;
    }

    public Location getLocation() {
        if(null == mCurrentLocation){
            mCurrentLocation = locationClient.getLastLocation();
        }
        if(mCurrentLocation == null){
        }
        return mCurrentLocation;
    }

    public void attemptConnection() {
        locationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("CurrentLocationManager", "Connected.");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(30000);
        locationClient.requestLocationUpdates(mLocationRequest, this);
        mCurrentLocation = locationClient.getLastLocation();
    }

    @Override
    public void onDisconnected() {
        Log.d("CurrentLocationManager", "Disconnected. Please re-connect.");
        if(mObservers.size()!=0) {
            attemptConnection();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(!mRemovers.isEmpty()) {
            for(OnLocationChange i : mRemovers) {
                if(mObservers.contains(i)) {
                    mObservers.remove(i);
                }
            }
            mRemovers.clear();
        }

        if (location.hasAccuracy() && !mObservers.isEmpty()) {
            mCurrentLocation = location;
            Log.d("TAG_LOC",
                    "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());
            for(OnLocationChange listener : mObservers) {
                listener.OnCurrentLocationChange(mCurrentLocation);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG_LOC", "Connection to LocationClient failed");
    }
}
