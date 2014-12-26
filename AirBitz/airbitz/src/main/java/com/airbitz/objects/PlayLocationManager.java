/**
 * Copyright (c) 2014, Airbitz Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms are permitted provided that 
 * the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Redistribution or use of modified source code requires the express written
 *    permission of Airbitz Inc.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Airbitz Project.
 */

package com.airbitz.objects;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by matt on 6/30/14.
 */
public class PlayLocationManager implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    static String TAG = PlayLocationManager.class.getSimpleName();

    private static PlayLocationManager mInstance = null;
    private LocationRequest mLocationRequest;
    private LocationClient locationClient;
    private Location mCurrentLocation;
    private Context mContext;
    // Callback interface for adding and removing location change listeners
    private List<CurrentLocationManager.OnCurrentLocationChange> mObservers = new CopyOnWriteArrayList<CurrentLocationManager.OnCurrentLocationChange>();

    public PlayLocationManager(Context context) {
        mContext = context;
    }

    public static PlayLocationManager getLocationManager(Context context) {
        if (null == mInstance) {
            mInstance = new PlayLocationManager(context);
        }
        return mInstance;
    }

    public void addLocationChangeListener(CurrentLocationManager.OnCurrentLocationChange listener) {
        if (mObservers.isEmpty()) {
            attemptConnection();
        }
        if (!mObservers.contains(listener)) {
            mObservers.add(listener);
            Log.d(TAG, "Listener added: " + listener);
        }
        if (null != listener && null != mCurrentLocation) {
            listener.OnCurrentLocationChange(mCurrentLocation);
        }
    }

    public void removeLocationChangeListener(CurrentLocationManager.OnCurrentLocationChange listener) {
        mObservers.remove(listener);
        Log.d(TAG, "Listener removed: " + listener);
        if (mObservers.size() <= 0) {
            locationClient.disconnect();
        }
    }

    public Location getLocation() {
        if (null == mCurrentLocation && locationClient != null && locationClient.isConnected()) {
            mCurrentLocation = locationClient.getLastLocation();
        }
        return mCurrentLocation;
    }

    public void attemptConnection() {
        if (locationClient == null || !locationClient.isConnected()) {
            Log.d(TAG, "Attempting connection");
            locationClient = new LocationClient(mContext, this, this);
            locationClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected.");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(AndroidLocationManager.MIN_TIME_MILLIS * 2);
        mLocationRequest.setFastestInterval(AndroidLocationManager.MIN_TIME_MILLIS);
        mLocationRequest.setSmallestDisplacement(AndroidLocationManager.MIN_DIST_METERS);
        locationClient.requestLocationUpdates(mLocationRequest, this);
        mCurrentLocation = locationClient.getLastLocation();
        if (mCurrentLocation != null) {
            onLocationChanged(mCurrentLocation);
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected. Please re-connect.");
        if (mObservers.size() != 0) {
            attemptConnection();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        if (location.hasAccuracy() && !mObservers.isEmpty()) {
            mCurrentLocation = location;
            Log.d(TAG, "CUR LOC: " + mCurrentLocation.getLatitude() + "; "
                                   + mCurrentLocation.getLongitude());

            Iterator<CurrentLocationManager.OnCurrentLocationChange> i = mObservers.iterator();
            while (i.hasNext()) {
                i.next().OnCurrentLocationChange(mCurrentLocation);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection to LocationClient failed");
    }
}
