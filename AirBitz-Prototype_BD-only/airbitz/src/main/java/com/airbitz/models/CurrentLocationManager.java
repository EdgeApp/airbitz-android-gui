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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    private List<OnLocationChange> mObservers = new CopyOnWriteArrayList<OnLocationChange>();

    public interface OnLocationChange {
        public void OnCurrentLocationChange(Location location);
    }

    public void addLocationChangeListener(OnLocationChange listener) {
        if(mObservers.isEmpty()) {
                attemptConnection();
        }
        if(!mObservers.contains(listener)) {
            mObservers.add(listener);
            Log.d("CurrentLocationManager", "Listener added: "+listener);
        }
        if(null != listener && null != mCurrentLocation){
                listener.OnCurrentLocationChange(mCurrentLocation);
        }
    }

    public void removeLocationChangeListener(OnLocationChange listener) {
//        mRemovers.add(listener);
        mObservers.remove(listener);
        Log.d("CurrentLocationManager", "Listener removed: " + listener);
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
        if(null == mCurrentLocation && locationClient!=null && locationClient.isConnected()){
            mCurrentLocation = locationClient.getLastLocation();
        }
        return mCurrentLocation;
    }

    public void attemptConnection() {
        if(locationClient==null || !locationClient.isConnected()) {
            Log.d("CurrentLocationManager", "Attempting connection");
            locationClient = new LocationClient(mContext, this, this);
            locationClient.connect();
        }
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
        if (location.hasAccuracy() && !mObservers.isEmpty()) {
            mCurrentLocation = location;
            Log.d("TAG_LOC",
                    "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());

                Iterator<OnLocationChange> i = mObservers.iterator(); // Must be in synchronized block
                while (i.hasNext()) {
                    i.next().OnCurrentLocationChange(mCurrentLocation);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG_LOC", "Connection to LocationClient failed");
    }
}
