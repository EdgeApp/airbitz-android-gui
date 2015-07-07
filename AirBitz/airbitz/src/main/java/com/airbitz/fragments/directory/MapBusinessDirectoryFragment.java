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

package com.airbitz.fragments.directory;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.maps.MapBuilder;
import com.airbitz.fragments.maps.MapBuilder.MapLatLng;
import com.airbitz.fragments.maps.MapBuilder.MapMarker;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.utils.CacheUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class MapBusinessDirectoryFragment extends BaseFragment implements
    NavigationActivity.OnBackPress,
    CurrentLocationManager.OnCurrentLocationChange {

    private static final int INVALID_POINTER_ID = -1;
    private static String mLocationWords = "";
    private final String TAG = getClass().getSimpleName();

    private int mActivePointerId = INVALID_POINTER_ID;
    private int aPosBottom = -10000;
    private int dragBarHeight = 0;

    private View view;
    private ImageButton mLocateMeButton;
    private MapMarker mUserLocationMarker;
    private EditText mSearchEdittext;
    private RelativeLayout llListContainer;
    private FrameLayout flMapContainer;
    private boolean locationEnabled;
    private LinearLayout mDragLayout;
    private LinearLayout mMapLayout;
    private ListView mVenueListView;
    private VenueAdapter mVenueAdapter;
    private float aPosY;
    private Location mCurrentLocation;
    private LinearLayout mapView;
    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType = "business";
    private Bundle mVenueBundle;
    private List<BusinessSearchResult> mVenues = new ArrayList<BusinessSearchResult>();
    private List<MapLatLng> mMarkersLatLngList;
    private CurrentLocationManager mLocationManager;
    private AsyncTask<String, Void, String> mGetVenuesAsyncTask;
    private GetVenuesByBoundTask mGetVenuesByBoundAsyncTask;
    private MapBuilder.MapShim mMapShim;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVenueBundle = this.getArguments();
        if (mBusinessName == null) {
            mBusinessName = mVenueBundle.getString(BusinessDirectoryFragment.BUSINESS);
        }
        if (mLocationName == null) {
            mLocationName = mVenueBundle.getString(BusinessDirectoryFragment.LOCATION);
            mLocationWords = mLocationName;
        }
        mBusinessType = mVenueBundle.getString(BusinessDirectoryFragment.BUSINESSTYPE);

        if (mLocationManager == null) {
            mLocationManager = CurrentLocationManager.getLocationManager(mActivity);
        }

        if (mMapShim == null) {
            mMapShim = MapBuilder.createShim(mActivity);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map_business_directory, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mVenueListView = (ListView) view.findViewById(R.id.map_fragment_layout);
        mVenueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDirectoryDetailFragment(mVenues.get(i).getId(), mVenues.get(i).getName(), mVenues.get(i).getDistance());
            }
        });
        mVenueAdapter = new VenueAdapter(getActivity(), mVenues);
        mVenueListView.setAdapter(mVenueAdapter);

        mCurrentLocation = mLocationManager.getLocation();
        checkLocationManager();

        // Setup the map
        mMapShim.setOnInfoWindowClickListener(new MapBuilder.OnInfoWindowClickListener() {
            public void click(MapMarker marker) {
                final String cl = getString(R.string.your_location);
                if (!marker.getTitle().equalsIgnoreCase(cl)) {
                    for(BusinessSearchResult result : mVenues) {
                        if(marker.getTitle().equalsIgnoreCase(result.getName())) {
                            showDirectoryDetailFragment(result.getId(),
                                    result.getName(),
                                    result.getDistance());
                            break;
                        }
                    }
                }
            }
        });
        mMapShim.setCameraChangeListener(new MapBuilder.OnCameraChangeListener() {
            public void onCameraChange(MapLatLng sw, MapLatLng ne, MapLatLng ll) {
                String southWest = "" + sw.getLatitude() + "%2C" + sw.getLongitude();
                String northEast = "" + ne.getLatitude() + "%2C" + ne.getLongitude();
                String bound = southWest + "%7C" + northEast;
                String userLatLong = "";
                if (mCurrentLocation != null) {
                    userLatLong = "" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
                }

                if (mGetVenuesByBoundAsyncTask != null
                        && mGetVenuesByBoundAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                    mGetVenuesByBoundAsyncTask.cancel(true);
                }
                mGetVenuesByBoundAsyncTask = new GetVenuesByBoundTask(getActivity());
                mGetVenuesByBoundAsyncTask.execute(bound, mBusinessName, userLatLong);
            }
        });

        // Show the map fragment, if the shim has one
        if (!mMapShim.isEmpty()) {
            FrameLayout c = (FrameLayout) view.findViewById(R.id.map_placeholder);
            mMapShim.onCreateView(inflater, c, savedInstanceState);
        }

        mapView = (LinearLayout) view.findViewById(R.id.map_view);
        mMapLayout = (LinearLayout) view.findViewById(R.id.map_view_layout);

        mDragLayout = (LinearLayout) view.findViewById(R.id.dragLayout);
        llListContainer = (RelativeLayout) view.findViewById(R.id.list_view_container);
        flMapContainer = (FrameLayout) view.findViewById(R.id.map_container);

        mLocateMeButton = (ImageButton) view.findViewById(R.id.locateMeButton);

        mSearchEdittext = (EditText) view.findViewById(R.id.query);
        if (!TextUtils.isEmpty(mBusinessName)) {
            mSearchEdittext.setText(mBusinessName);
        }
        mSearchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    return;
                }
                MapBusinessDirectoryFragment.popFragment(mActivity);
            }
        });

        mDragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                String DEBUG_TAG = "TEST MOVE";
                int action = event.getActionMasked();
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        mActivePointerId = event.getPointerId(0);

                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        aPosY = llListContainer.getHeight();
                        aPosBottom = llListContainer.getBottom();
                        return true;

                    case (MotionEvent.ACTION_MOVE):
                        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) flMapContainer.getLayoutParams();
                        int currentHeight = param.height;

                        if (dragBarHeight == 0) {
                            dragBarHeight = mDragLayout.getMeasuredHeight() + 10;
                        }

                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);

                        float yMove = event.getY(0);

                        param.height += yMove;

                        int barY = (int) mDragLayout.getY();

                        if (param.height <= 0 || (barY + dragBarHeight >= aPosY && yMove > 0)) {
                            param.height = currentHeight;
                        } else if (param.height > (aPosBottom - dragBarHeight - 10)) {
                            param.height = currentHeight;
                        }

                        flMapContainer.setLayoutParams(param);
                        return true;
                    default:
                        return true;
                }
            }
        });

        // Hide map if "On the Web" search or no play services support
        final String otw = getString(R.string.on_the_web);
        if (mMapShim.isEmpty() || otw.equalsIgnoreCase(mLocationName)) {
            mDragLayout.setVisibility(View.GONE);
            flMapContainer.setVisibility(View.GONE);
        }

        // to tell when the view is finished layout
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                // We use the new method when supported
                @SuppressLint("NewApi")
                // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // Send notification that map-loading has completed.
                    onLayoutFinished();
                }
            });
        }

        mLocateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEnabled && null != mCurrentLocation) {
                    Log.d(TAG, "LocateMe button good");
                    MapLatLng currentLatLng =
                            new MapLatLng(mCurrentLocation.getLatitude(),
                                    mCurrentLocation.getLongitude());
                    drawCurrentLocationMarker(mCurrentLocation);
                    mMapShim.animateCamera(currentLatLng);
                } else {
                    Log.d(TAG, getString(R.string.no_location_found));
                    if(getActivity() != null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.no_location_found), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return onBackPress();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackPress() {
        MapBusinessDirectoryFragment.popFragment(mActivity);
        return true;
    }

    private void showViewAnimatorChild(int num) {
        if (num == 0) {
            mMapLayout.setVisibility(View.VISIBLE);
        } else {
            mMapLayout.setVisibility(View.GONE);
        }
    }

    private void showDirectoryDetailFragment(String id, String name, String distance) {
        DirectoryDetailFragment.pushFragment(mActivity, id, name, distance);
    }

    private void search() {
        mVenues.clear();
        if (mLocationName.equalsIgnoreCase("Current Location")) {
            if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                mGetVenuesAsyncTask.cancel(true);
            }
            mGetVenuesAsyncTask = new GetVenuesByLatLongTask(getActivity());
            String latlong = "";
            if (locationEnabled && null != mCurrentLocation) {
                latlong += mCurrentLocation.getLatitude() + ","
                         + mCurrentLocation.getLongitude();
            }
            if (mBusinessType.equalsIgnoreCase("business")) {
                mGetVenuesAsyncTask.execute(latlong, mBusinessName, "");
            } else {
                mGetVenuesAsyncTask.execute(latlong, "", mBusinessName);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        if(getActivity() != null) {
                            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.fragment_directory_detail_timeout_retrieving_data),
                                    Toast.LENGTH_LONG).show();
                        }
                        mGetVenuesAsyncTask.cancel(true);
                    }
                }
            }, BusinessDirectoryFragment.CATEGORY_TIMEOUT);
        } else {
            if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                mGetVenuesAsyncTask.cancel(true);
            }
            mGetVenuesAsyncTask = new GetVenuesByBusinessAndLocation(getActivity());
            mGetVenuesAsyncTask.execute(mBusinessName, mLocationName, mBusinessType);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mGetVenuesAsyncTask.cancel(true);
                        if(getActivity() != null) {
                            Toast.makeText(getActivity(), getString(R.string.fragment_directory_detail_timeout_retrieving_data),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }, BusinessDirectoryFragment.CATEGORY_TIMEOUT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapShim.onPause();

        if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mGetVenuesAsyncTask.cancel(true);
        }
        if (mGetVenuesByBoundAsyncTask != null && mGetVenuesByBoundAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mGetVenuesByBoundAsyncTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapShim.onResume();

        if (!mVenues.isEmpty()) {
            List<BusinessSearchResult> venues =
                    new ArrayList<BusinessSearchResult>(mVenues);
            mVenues.clear();
            updateVenueResults(venues, true);
        } else {
            search();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapShim.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapShim.onLowMemory();
    }

    private void checkLocationManager() {
        locationEnabled = CurrentLocationManager.locationEnabled(getActivity());
        mMapShim.setLocationEnabled(locationEnabled);
    }

    private void onLayoutFinished() {
        zoomToContainAllMarkers(mMarkersLatLngList);
    }

    private void drawCurrentLocationMarker(Location location) {
        if (locationEnabled) {
            MapLatLng currentPosition = null;
            if (location != null) {
                currentPosition = new MapLatLng(location.getLatitude(),
                                                 location.getLongitude());
            } else if (null != mCurrentLocation) {
                currentPosition = new MapLatLng(mCurrentLocation.getLatitude(),
                                                 mCurrentLocation.getLongitude());
            }
            if (currentPosition != null) {
                drawCurrentLocationMarker(currentPosition);
            }
        }
    }

    private void drawCurrentLocationMarker(MapLatLng location) {
        if (location == null && null != mCurrentLocation) {
            location = new MapLatLng(mCurrentLocation.getLatitude(),
                                     mCurrentLocation.getLongitude());
        }
        if (location != null) {
            mUserLocationMarker = new MapMarker(location);
            mMapShim.drawCurrentLocation(mUserLocationMarker);
            mMapShim.showCurrentLocationInfo();
        }
    }

    private void zoomToContainAllMarkers(List<MapLatLng> markers) {
        if (markers != null && !markers.isEmpty()) {
            mMapShim.zoomToContainAllMarkers(markers);
        }
    }

    protected void initializeMarkerWithBusinessSearchResult(boolean zoom) {
        if (flMapContainer == null || flMapContainer.getVisibility() != View.VISIBLE) {
            return;
        }

        mMarkersLatLngList = new ArrayList<MapLatLng>();
        mMapShim.clearMarkers();
        if (mVenues.size() > 0) {
            for (BusinessSearchResult biz : mVenues) {
                MapLatLng ll =
                    new MapLatLng(biz.getLocationObject().getLatitude(),
                                   biz.getLocationObject().getLongitude());
                mMarkersLatLngList.add(ll);
                MapMarker marker =
                    new MapMarker(ll).id(biz.getId())
                                        .distance(biz.getDistance())
                                        .profileImage(biz.getProfileImage().getImageThumbnail())
                                        .title(biz.getName())
                                        .snippet(biz.getAddress())
                                        .icon(R.drawable.ico_bitcoin_loc);
                mMapShim.addMarker(marker);
            }
            if (zoom) {
                zoomToContainAllMarkers(mMarkersLatLngList);
            }
        } else {
            drawCurrentLocationMarker(mCurrentLocation);
        }
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mCurrentLocation = location;
        mMapShim.setCurrentLocation(location);
        mMapShim.animateCamera(new MapLatLng(mCurrentLocation));
    }

    private boolean isNewVenuesAdded(List<BusinessSearchResult> newVenues) {
        for (BusinessSearchResult item : newVenues) {
            if (!mVenues.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private void showMessageProgress(String message, boolean visible) {
        if (isAdded()) {
            ((NavigationActivity) getActivity()).showModalProgress(visible);
        }
    }

    private void updateVenueResults(String searchResult, boolean zoom) {
        try {
            SearchResult result = new SearchResult(new JSONObject(searchResult));
            updateVenueResults(result.getBusinessSearchObjectArray(), zoom);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVenueResults(List<BusinessSearchResult> venues, boolean zoom) {
        if (venues == null) {
            return;
        }
        try {
            mVenues.addAll(venues);
            mVenueAdapter.notifyDataSetChanged();
            initializeMarkerWithBusinessSearchResult(zoom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetVenuesByBoundTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetVenuesByBoundTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
            mGetVenuesByBoundAsyncTask = null;
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... params) {
            final String PAGE_SIZE = "500";
            Log.d(TAG, "params: " + params[0] + " " + params[1] + " " + params[2]);
            if (mBusinessType.equalsIgnoreCase("category")) {
                return mApi.getSearchByBoundsAndBusiness(params[0], "", params[1], params[2], PAGE_SIZE, "", "");
            } else {

                return mApi.getSearchByBoundsAndBusiness(params[0], params[1], "", params[2], PAGE_SIZE, "", "");
            }
        }

        @Override
        protected void onPostExecute(String searchResult) {
            if (getActivity() == null) {
                return;
            }
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                List<BusinessSearchResult> list = results.getBusinessSearchObjectArray();
                if(list != null && list.size() > 0) {
                    mVenues.clear();
                    updateVenueResults(list, false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mGetVenuesByBoundAsyncTask = null;
        }
    }

    private class GetVenuesByLatLongTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetVenuesByLatLongTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            showMessageProgress(getString(R.string.fragment_directory_detail_getting_venue_data), true);
        }

        @Override
        protected String doInBackground(String... params) {
            return mApi.getSearchByLatLongAndBusiness(params[0], params[1], params[2], "", "", "");
        }

        @Override
        protected void onCancelled() {
            showMessageProgress("", false);
            mGetVenuesAsyncTask = null;
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            if (mActivity == null) {
                return;
            }
            updateVenueResults(searchResult, true);
            showMessageProgress("", false);
            mGetVenuesAsyncTask = null;
        }
    }

    private class GetVenuesByBusinessAndLocation extends AsyncTask<String, Void, String> {
        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetVenuesByBusinessAndLocation(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            showMessageProgress(getString(R.string.fragment_directory_detail_getting_venue_data), true);
        }

        @Override
        protected String doInBackground(String... params) {
            String latLong = "";
            if (locationEnabled && null != mCurrentLocation) {
                latLong = String.valueOf(mCurrentLocation.getLatitude())
                        + "," + String.valueOf(mCurrentLocation.getLongitude());
            }
            return mApi.getSearchByCategoryOrBusinessAndLocation(params[0], params[1], "", "", "1",
                    params[2], latLong);
        }

        @Override
        protected void onCancelled() {
            showMessageProgress("", false);
            mGetVenuesAsyncTask = null;
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            if (mActivity == null)
                return;

            updateVenueResults(searchResult, true);
            showMessageProgress("", false);
            mGetVenuesAsyncTask = null;
        }
    }

    public static void pushFragment(NavigationActivity mActivity, String query, String loc, String type) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        Bundle bundle = new Bundle();
        bundle.putString(BusinessDirectoryFragment.BUSINESS, query);
        bundle.putString(BusinessDirectoryFragment.LOCATION, loc);
        bundle.putString(BusinessDirectoryFragment.BUSINESSTYPE, type);
        Fragment fragment = new MapBusinessDirectoryFragment();
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
        mActivity.getFragmentManager().executePendingTransactions();
    }
}
