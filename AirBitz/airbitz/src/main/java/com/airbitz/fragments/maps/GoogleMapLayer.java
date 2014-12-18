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

package com.airbitz.fragments.maps;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.adapters.MapInfoWindowAdapter;
import com.airbitz.fragments.maps.MapBuilder.MapLatLng;
import com.airbitz.fragments.maps.MapBuilder.MapMarker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GoogleMapLayer implements MapBuilder.MapShim {

    private Context mContext;
    private Handler mHandler;
    private View view;
    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private GoogleMapLayout mWrapper;
    private Marker mUserLocationMarker;
    private Location mCurrentLocation;
    private boolean mCameraNotificationEnabled = false;
    private boolean mLocationEnabled;
    private int mMapHeight;

    private Map<Marker, Integer> mMarkerId = new HashMap<Marker, Integer>();
    private Map<Marker, String> mMarkerDistances = new HashMap<Marker, String>();
    private Map<Marker, String> mMarkerImageLink = new HashMap<Marker, String>();

    private MapBuilder.OnCameraChangeListener mOnCameraChangeListener;
    private MapBuilder.OnMyLocationChangeListener mOnLocationChangeListener;
    private MapBuilder.OnInfoWindowClickListener mInfoWindowListener;

    public GoogleMapLayer(Context context) {
        mContext = context;
    }

    @Override
    public void setLocationEnabled(boolean enabled) {
        mLocationEnabled = enabled;
    }

    @Override
    public void setCurrentLocation(Location location) {
        mCurrentLocation = location;
    }

    @Override
    public void setCameraChangeListener(MapBuilder.OnCameraChangeListener listener) {
        mOnCameraChangeListener = listener;
    }

    @Override
    public void setOnLocationChangeListener(MapBuilder.OnMyLocationChangeListener listener) {
        mOnLocationChangeListener = listener;
    }

    @Override
    public void setOnInfoWindowClickListener(MapBuilder.OnInfoWindowClickListener listener) {
        mInfoWindowListener = listener;
    }

    @Override
    public void animateCamera(MapLatLng latlng) {
        LatLng ll = new LatLng(latlng.getLatitude(), latlng.getLongitude());
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(ll));
    }

    @Override
    public void showCurrentLocationInfo() {
        if (mCurrentLocation != null) {
            CameraUpdate cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), 10);
            mGoogleMap.animateCamera(cameraUpdate);
            mCameraNotificationEnabled = true;
        }
    }

    @Override
    public void clearMarkers() {
        mMarkerId.put(null, 0);
        mMarkerDistances.put(null, "");
        getMarkerImageLink().put(null, "");
        if (!mMarkerId.isEmpty()) {
            mMarkerId.clear();
            mMarkerImageLink.clear();
            mMarkerDistances.clear();
        }
        mGoogleMap.clear();
    }

    @Override
    public void addMarker(MapMarker marker) {
        LatLng ll = new LatLng(marker.mPos.mLat, marker.mPos.mLng);
        Marker googMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(ll)
                        .title(marker.mTitle)
                        .snippet(marker.mSnippet)
                        .icon(BitmapDescriptorFactory.fromResource(marker.mIcon))
        );
        mMarkerId.put(googMarker, Integer.parseInt(marker.mId));
        mMarkerDistances.put(googMarker, marker.mDistance);
        mMarkerImageLink.put(googMarker, marker.mProfileImage);
    }

    @Override
    public void drawCurrentLocation(MapMarker shimMarker) {
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }
        LatLng ll = new LatLng(shimMarker.mPos.mLat,
                               shimMarker.mPos.mLng);
        mUserLocationMarker =
            mGoogleMap.addMarker(new MarkerOptions()
                      .position(ll)
                      .title(mContext.getString(R.string.your_location))
                      .snippet("")
                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
        );
    }

    @Override
    public void zoomToContainAllMarkers(List<MapLatLng> markers) {
        if (markers.size() > 0) {
            LatLngBounds.Builder bc = new LatLngBounds.Builder();
            for (MapLatLng ll : markers) {
                bc.include(new LatLng(ll.mLat, ll.mLng));
            }
            if (mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public Map<Marker, String> getMarkerImageLink() {
        return mMarkerImageLink;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("GoogleMapLayer", "onCreateView");
        view = inflater.inflate(R.layout.fragment_google_map, container, true);

        mWrapper = (GoogleMapLayout) view.findViewById(R.id.custom_map_wrapper);
        mWrapper.setDragListener(new GoogleMapLayout.MapDragListener() {
            public void onDragEnd() {
                mCameraNotificationEnabled = true;
            }
        });

        mMapView = (MapView) view.findViewById(R.id.custom_map_fragment);
        mMapView.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(mContext.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGoogleMap = mMapView.getMap();

        initializeMap();

        return view;
    }

    @Override
    public void onResume() {
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
    }

    public void initializeMap() {
        if (mGoogleMap == null) {
            Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.fragment_map_directory_unable_create_map), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (mLocationEnabled) {
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        mMapHeight = (int) mContext.getResources().getDimension(R.dimen.map_height);
        int padding = (int) mContext.getResources().getDimension(R.dimen.map_padding);
        mGoogleMap.setPadding(0, padding, 0, padding);

        LatLng currentLatLng = null;
        if (mCurrentLocation != null) {
            currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                                       mCurrentLocation.getLongitude());
        }
        try {
            if (mLocationEnabled && mCurrentLocation != null) {
                CameraUpdate cameraUpdate =
                    CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mCurrentLocation.getLatitude(),
                                   mCurrentLocation.getLongitude()), 10);
                mGoogleMap.animateCamera(cameraUpdate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        if (mLocationEnabled && currentLatLng != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        MapInfoWindowAdapter customInfoWindowAdapter =
            new MapInfoWindowAdapter(mContext, this);
        mGoogleMap.setInfoWindowAdapter(customInfoWindowAdapter);
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (mInfoWindowListener != null) {
                    LatLng googLL = marker.getPosition();
                    MapLatLng ll =
                        new MapLatLng(googLL.latitude, googLL.longitude);
                    MapMarker m =
                        new MapMarker(ll).title(marker.getTitle())
                                          .snippet(marker.getSnippet())
                                          .id("" + mMarkerId.get(marker))
                                          .distance(mMarkerDistances.get(marker));
                    mInfoWindowListener.click(m);
                }
            }
        });

        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (mOnLocationChangeListener != null) {
                    mOnLocationChangeListener.onMyLocationChange(location);
                }
            }
        });

        mHandler = new Handler();

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (mCameraNotificationEnabled) {
                    LatLngBounds latLngBounds =
                        mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                    final LatLng sw = latLngBounds.southwest;
                    final LatLng ne = latLngBounds.northeast;
                    if (mOnCameraChangeListener != null) {
                        mHandler.removeCallbacks(null);
                        // Throttle the onCameraChange calls
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                MapLatLng ll = null;
                                if (mLocationEnabled && null != mCurrentLocation) {
                                    ll = new MapLatLng(mCurrentLocation);
                                }
                                mOnCameraChangeListener.onCameraChange(
                                    new MapLatLng(sw.latitude, sw.longitude),
                                    new MapLatLng(ne.latitude, ne.longitude), ll);
                            }
                        }, 500);
                    }
                }
                mCameraNotificationEnabled = false;
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
    }
}
