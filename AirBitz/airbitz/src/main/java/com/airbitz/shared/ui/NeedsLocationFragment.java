package com.airbitz.shared.ui;

import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Created by dannyroa on 3/20/14.
 */
public class NeedsLocationFragment extends BaseFragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener  {

    protected LocationClient mLocationClient;

    protected Location mCurrentLocation;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override public void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
    }

    @Override public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {

    }



    @Override public void onDisconnected() {

    }

    @Override public void onConnectionFailed(ConnectionResult connectionResult) {

        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        getActivity(),
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the user with the error.
             */
            // TODO show error
        }

    }


}
