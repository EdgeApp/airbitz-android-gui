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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbitz.objects.CurrentLocationManager;

import java.util.List;

public class MapBuilder {

    public static class MapLatLng {
        double mLat;
        double mLng;

        public MapLatLng(double lat, double lng) {
            this.mLat = lat;
            this.mLng = lng;
        }

        public MapLatLng(Location l) {
            this.mLat = l.getLatitude();
            this.mLng = l.getLongitude();
        }

        public double getLatitude() {
            return mLat;
        }

        public double getLongitude() {
            return mLng;
        }
    }

    public static class MapMarker {
        MapLatLng mPos;
        String mTitle;
        String mSnippet;
        int mIcon;
        String mId;
        String mDistance;
        String mProfileImage;

        public MapMarker(MapLatLng position) {
            mPos = position;
            mTitle = "";
            mSnippet = "";
            mId = "";
            mDistance = "";
            mProfileImage = "";
        }

        public MapMarker title(String title) {
            mTitle = title;
            return this;
        }

        public MapMarker snippet(String snippet) {
            mSnippet = snippet;
            return this;
        }

        public MapMarker icon(int res) {
            mIcon = res;
            return this;
        }

        public MapMarker id(String id) {
            mId = id;
            return this;
        }

        public MapMarker distance(String distance) {
            mDistance = distance;
            return this;
        }

        public MapMarker profileImage(String image) {
            mProfileImage = image;
            return this;
        }

        public MapLatLng getPos() {
            return mPos;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getId() {
            return mId;
        }

        public String getDistance() {
            return mDistance;
        }
    }

    public interface OnCameraChangeListener {
        public void onCameraChange(MapLatLng sw, MapLatLng ne, MapLatLng ll);
    };

    public interface OnMyLocationChangeListener {
        public void onMyLocationChange(Location location);
    }

    public interface OnInfoWindowClickListener {
        public void click(MapMarker marker);
    }

    public interface MapShim {
        public void setLocationEnabled(boolean enabled);

        public void setCurrentLocation(Location location);

        public void setCameraChangeListener(OnCameraChangeListener listener);

        public void setOnLocationChangeListener(OnMyLocationChangeListener listener);

        public void setOnInfoWindowClickListener(OnInfoWindowClickListener listener);

        public void animateCamera(MapLatLng latlng);

        public void drawCurrentLocation(MapMarker shimMarker);

        public void showCurrentLocationInfo();

        public void zoomToContainAllMarkers(List<MapLatLng> markers);

        public void clearMarkers();

        public void addMarker(MapMarker marker);

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState);

        public void onResume();

        public void onPause();

        public void onDestroy();

        public void onLowMemory();

        public boolean isEmpty();
    };

    static class EmptyLayer implements MapShim {
        public void setLocationEnabled(boolean enabled) { }

        public void setCurrentLocation(Location location) { }

        public void setCameraChangeListener(OnCameraChangeListener listener) { }

        public void setOnLocationChangeListener(OnMyLocationChangeListener listener) { }

        public void setOnInfoWindowClickListener(OnInfoWindowClickListener listener) { }

        public void animateCamera(MapLatLng latlng) { }

        public void showCurrentLocationInfo() { }

        public void drawCurrentLocation(MapMarker shimMarker) { }

        public void zoomToContainAllMarkers(List<MapLatLng> markers) { }

        public void clearMarkers() { }

        public void addMarker(MapMarker marker) { }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return null;
        }

        public void onResume() {}

        public void onPause() {}

        public void onDestroy() {}

        public void onLowMemory() {}

        public boolean isEmpty() {
            return true;
        }
    };

    public static MapShim createShim(Context context) {
        if (CurrentLocationManager.supportsPlayServices(context)) {
            return new GoogleMapLayer(context);
        } else {
            return new EmptyLayer();
        }
    }
}
