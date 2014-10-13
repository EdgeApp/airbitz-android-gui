package com.airbitz.models;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CurrentLocationManager {

    private static CurrentLocationManager mInstance = null;

    private Context mContext;

    private PlayLocationManager mPlay;
    private AndroidLocationManager mAndroid;

    public CurrentLocationManager(Context context) {
        mContext = context;
        if (supportsPlayServices(context)) {
            mPlay = new PlayLocationManager(mContext);
        } else {
            mAndroid = new AndroidLocationManager(mContext);
        }
    }

    public static boolean supportsPlayServices(Context context) {
        return ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    public static CurrentLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new CurrentLocationManager(context);
        }
        return mInstance;
    }

    public void addLocationChangeListener(OnLocationChange listener) {
        if (mPlay != null) {
            mPlay.addLocationChangeListener(listener);
        } else {
            mAndroid.addLocationChangeListener(listener);
        }
    }

    public void removeLocationChangeListener(OnLocationChange listener) {
        if (mPlay != null) {
            mPlay.removeLocationChangeListener(listener);
        } else {
            mAndroid.removeLocationChangeListener(listener);
        }
    }

    public Location getLocation() {
        if (mPlay != null) {
            return mPlay.getLocation();
        } else {
            return mAndroid.getLocation();
        }
    }

    public void attemptConnection() {
        if (mPlay != null) {
            mPlay.attemptConnection();
        } else {
            mAndroid.attemptConnection();
        }
    }

    public void onConnected(Bundle bundle) {
        if (mPlay != null) {
            mPlay.onConnected(bundle);
        } else {
            mAndroid.onConnected(bundle);
        }
    }

    public void onDisconnected() {
        if (mPlay != null) {
            mPlay.onDisconnected();
        } else {
            mAndroid.onDisconnected();
        }
    }

    public void onLocationChanged(Location location) {
        if (mPlay != null) {
            mPlay.onLocationChanged(location);
        } else {
            mAndroid.onLocationChanged(location);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("TAG_LOC", "Connection to LocationClient failed");
    }

    public interface OnLocationChange {
        public void OnCurrentLocationChange(Location location);
    }
}
