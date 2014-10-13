package com.airbitz.models;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.airbitz.models.CurrentLocationManager.OnLocationChange;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AndroidLocationManager {

    public static final String TAG = AndroidLocationManager.class.getSimpleName();

    static AndroidLocationManager mInstance = null;
    static final long MIN_TIME = 30000;
    static final long MIN_DIST = 100;
    static final int TWO_MINUTES = 1000 * 60 * 2;

    private Context mContext;
    private LocationManager mLocationManager;
    private List<OnLocationChange> mObservers;
    private Location mCurrentLocation;

    public AndroidLocationManager(Context context) {
        mContext = context;
        this.mLocationManager =
            (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.mObservers = new CopyOnWriteArrayList<OnLocationChange>();
    }

    public void addLocationChangeListener(OnLocationChange listener) {
        if (mObservers.isEmpty()) {
            attemptConnection();
        }
        if(!mObservers.contains(listener)) {
            mObservers.add(listener);
            Log.d(TAG, "Listener added: "+listener);
        }
        if (null != listener && null != mCurrentLocation){
            listener.OnCurrentLocationChange(mCurrentLocation);
        }
    }

    public void removeLocationChangeListener(OnLocationChange listener) {
        mLocationManager.removeUpdates(mManagerListener);
    }

    public static AndroidLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new AndroidLocationManager(context);
        }
        return mInstance;
    }

    public Location getLocation() {
        return mCurrentLocation;
    }

    public void attemptConnection() {
        mCurrentLocation = getLastLocation();
        try {
            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME, MIN_DIST,
                mManagerListener);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "", e);
        }
        try {
            mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                MIN_TIME, MIN_DIST,
                mManagerListener);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "", e);
        }
    }

    public void onConnected(Bundle bundle) {
        mCurrentLocation = getLastLocation();
        if (mCurrentLocation != null) {
            onLocationChanged(mCurrentLocation);
        }
    }

    public void onDisconnected() {
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            return;
        }
        mCurrentLocation = location;
        if (mObservers != null) {
            for (OnLocationChange l : mObservers) {
                l.OnCurrentLocationChange(location);
            }
        }
    }

    private LocationListener mManagerListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            AndroidLocationManager.this.onLocationChanged(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) { }

        public void onProviderEnabled(String provider) { }

        public void onProviderDisabled(String provider) { }
    };

    public Location getLastLocation() {
        Location bestResult = null;
        for (String provider : mLocationManager.getAllProviders()) {
            Location loc = mLocationManager.getLastKnownLocation(provider);
            if (loc != null) {
                long time = loc.getTime();
                if (isBetterLocation(loc, bestResult)) {
                    bestResult = loc;
                }
            }
        }
        return bestResult;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
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
}
