package com.airbitz.fragments;

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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.maps.MapBuilder;
import com.airbitz.fragments.maps.MapBuilder.MapLatLng;
import com.airbitz.fragments.maps.MapBuilder.MapMarker;
import com.airbitz.fragments.maps.GoogleMapLayer;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.utils.CacheUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class MapBusinessDirectoryFragment extends Fragment implements
        CurrentLocationManager.OnLocationChange {

    private static final int INVALID_POINTER_ID = -1;
    private static String mLocationWords = "";
    private final String TAG = getClass().getSimpleName();

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mapHeight;
    private int aPosBottom = -10000;
    private int dragBarHeight = 0;
    private View view;
    private ImageButton mLocateMeButton;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private MapMarker mUserLocationMarker;
    private EditText mSearchEdittext;
    private EditText mLocationEdittext;
    private RelativeLayout llListContainer;
    private FrameLayout flMapContainer;
    private boolean locationEnabled;
    private LinearLayout mDragLayout;
    private TextView mTitleTextView;
    private ListView mSearchListView;
    private LinearLayout mMapLayout;
    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private ListView mVenueListView;
    private VenueAdapter mVenueAdapter;
    private float aPosY;
    private ArrayList<LocationSearchResult> mLocation;
    private ArrayList<Business> mBusinessList;
    private Location mCurrentLocation;
    private LocationAdapter mLocationAdapter;
    private LinearLayout mapView;
    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType;
    private Bundle mVenueBundle;
    private List<BusinessSearchResult> mVenues = new ArrayList<BusinessSearchResult>();
    private List<MapLatLng> mMarkersLatLngList;
    private CurrentLocationManager mLocationManager;
    private AsyncTask<String, Void, String> mGetVenuesAsyncTask;
    private GetVenuesByBoundTask mGetVenuesByBoundAsyncTask;
    private LocationAutoCompleteAsynctask mLocationAutoCompleteAsyncTask;
    private BusinessAutoCompleteAsynctask mBusinessAutoCompleteAsyncTask;
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
        }
        if (mBusinessType == null) {
            mBusinessType = mVenueBundle.getString(BusinessDirectoryFragment.BUSINESSTYPE);
        }

        if (mLocationManager == null) {
            mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        }

        if (mMapShim == null) {
            mMapShim = MapBuilder.createShim(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map_business_directory, container, false);

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
        if (mCurrentLocation != null) {
            mMapShim.setCurrentLocation(mCurrentLocation);
        }

        // Setup the map
        mMapShim.setOnInfoWindowClickListener(new MapBuilder.OnInfoWindowClickListener() {
            public void click(MapMarker marker) {
                final String cl = getString(R.string.your_location);
                if (!marker.getTitle().equalsIgnoreCase(cl)) {
                    showDirectoryDetailFragment(marker.getId(),
                                                marker.getTitle(),
                                                marker.getDistance());
                }
            }
        });
        mMapShim.setCameraChangeListener(new MapBuilder.OnCameraChangeListener() {
            public void onCameraChange(MapLatLng sw, MapLatLng ne, MapLatLng ll) {
                String southWest = "" + sw.getLatitude() + "%2C" + sw.getLongitude();
                String northEast = "" + ne.getLatitude() + "%2C" + ne.getLongitude();
                String bound = southWest + "%7C" + northEast;
                String userLatLong = "";
                if (ll != null) {
                    userLatLong = "" + ll.getLatitude() + "," + ll.getLongitude();
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

        mBusinessList = new ArrayList<Business>();
        mLocation = new ArrayList<LocationSearchResult>();

        mLocateMeButton = (ImageButton) view.findViewById(R.id.locateMeButton);
        mHelpButton = (ImageButton) view.findViewById(R.id.fragment_category_button_help);

        mBackButton = (ImageButton) view.findViewById(R.id.fragment_category_button_back);
        mBackButton.setVisibility(View.VISIBLE);

        mSearchEdittext = (EditText) view.findViewById(R.id.edittext_search);
        mSearchEdittext.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        mSearchEdittext.setText(mBusinessName);

        mLocationEdittext = (EditText) view.findViewById(R.id.edittext_location);
        mLocationEdittext.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        mLocationEdittext.setText(mLocationName);

        mTitleTextView = (TextView) view.findViewById(R.id.fragment_category_textview_title);
        mTitleTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        mSearchListView = (ListView) view.findViewById(R.id.listview_search);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationEdittext.getVisibility() == View.VISIBLE) {
                    showViewAnimatorChild(0);
                } else {
                    getActivity().onBackPressed();
                }
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mBusinessSearchAdapter = new BusinessSearchAdapter(getActivity(), mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    return;
                }
                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationEdittext.setVisibility(View.VISIBLE);
                showViewAnimatorChild(1);

                // Start search
                try {
                    final String text = mSearchEdittext.getText().toString();
                    final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                            ? null
                            : CacheUtil.getCachedBusinessSearchData(getActivity()));
                    String latLong = "";
                    if (locationEnabled && null != mCurrentLocation) {
                        latLong = String.valueOf(mCurrentLocation.getLatitude());
                        latLong += "," + String.valueOf(mCurrentLocation.getLongitude());
                    }
                    if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mBusinessAutoCompleteAsyncTask.cancel(true);
                    }
                    mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusiness);
                    mBusinessAutoCompleteAsyncTask.execute(text,
                            mLocationWords,
                            latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mBusinessName.equals(mSearchEdittext.getText().toString())) {
                    return;
                }
                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationEdittext.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.VISIBLE);

                String latLong = "";
                if (locationEnabled && null != mCurrentLocation) {
                    latLong = String.valueOf(mCurrentLocation.getLatitude());
                    latLong += "," + String.valueOf(mCurrentLocation.getLongitude());
                }

                try {
                    // Only include cached searches if text is empty.
                    final String query;
                    query = editable.toString();
                    final List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(getActivity())
                            : null);
                    if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mBusinessAutoCompleteAsyncTask.cancel(true);
                    }
                    mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusinesses);
                    mBusinessAutoCompleteAsyncTask.execute(query,
                            mLocationWords, latLong);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLocationAdapter = new LocationAdapter(getActivity(), mLocation);
        mSearchListView.setAdapter(mLocationAdapter);

        mLocationEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mSearchListView.setAdapter(mLocationAdapter);

                    // Search
                    String latLong = "";
                    if (locationEnabled && null != mCurrentLocation) {
                        latLong = String.valueOf(mCurrentLocation.getLatitude());
                        latLong += "," + String.valueOf(mCurrentLocation.getLongitude());
                    }
                    mLocationWords = "";

                    try {
                        if (mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                            mLocationAutoCompleteAsyncTask.cancel(true);
                        }
                        mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(getActivity()));
                        mLocationAutoCompleteAsyncTask.execute(mLocationWords,
                                latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mLocationEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    showViewAnimatorChild(0);
                    mBusinessType = "business";
                    mBusinessName = mSearchEdittext.getText().toString();
                    mLocationName = mLocationEdittext.getText().toString();
                    search();
                    return true;
                }
                return false;
            }
        });

        mSearchEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    showViewAnimatorChild(0);
                    mBusinessType = "business";
                    mBusinessName = mSearchEdittext.getText().toString();
                    mLocationName = mLocationEdittext.getText().toString();
                    search();
                    return true;
                }
                return false;
            }
        });

        mLocationEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchListView.setAdapter(mLocationAdapter);
                mSearchListView.setVisibility(View.VISIBLE);
                mLocationWords = editable.toString();

                String latLong = "";
                if (locationEnabled && null != mCurrentLocation) {
                    latLong = String.valueOf(mCurrentLocation.getLatitude());
                    latLong += "," + String.valueOf(mCurrentLocation.getLongitude());
                }

                try {
                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(getActivity())
                            : null);
                    if (mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mLocationAutoCompleteAsyncTask.cancel(true);
                    }
                    mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(cachedLocationSearch);
                    mLocationAutoCompleteAsyncTask.execute(mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                boolean locationFieldShouldFocus = false;

                if (mSearchEdittext.isFocused()) {

                    final BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter) mSearchListView.getAdapter();

                    final Business business = businessSearchAdapter.getItem(position);
                    mSearchEdittext.setText(business.getName());
                    mBusinessType = business.getType();

                    if ("business".equalsIgnoreCase(mBusinessType)) {
                        showDirectoryDetailFragment(business.getId(), business.getName(), "");
                    } else {
                        CacheUtil.writeCachedBusinessSearchData(getActivity(),
                                businessSearchAdapter.getItem(position));
                        locationFieldShouldFocus = true;
                    }

                } else if (mLocationEdittext.isFocused()) {
                    final LocationAdapter locationAdapter = (LocationAdapter) mSearchListView.getAdapter();
                    final LocationSearchResult location = locationAdapter.getItem(position);
                    mLocationEdittext.setText(location.getLocationName());
                    CacheUtil.writeCachedLocationSearchData(getActivity(),
                            location.getLocationName());
                }

                if (locationFieldShouldFocus) {
                    mLocationEdittext.requestFocus();
                } else {
                    mSearchEdittext.requestFocus();
                }
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

        return view;
    }

    private void showViewAnimatorChild(int num) {
        if (num == 0) {
            mSearchListView.setVisibility(View.GONE);
            mMapLayout.setVisibility(View.VISIBLE);
            mLocationEdittext.setVisibility(View.GONE);
        } else {
            mSearchListView.setVisibility(View.VISIBLE);
            mMapLayout.setVisibility(View.GONE);
        }
    }

    private void showDirectoryDetailFragment(String id, String name, String distance) {
        Bundle bundle = new Bundle();
        bundle.putString(DirectoryDetailFragment.BIZID, id);
        bundle.putString(DirectoryDetailFragment.BIZNAME, name);
        bundle.putString(DirectoryDetailFragment.BIZDISTANCE, distance);
        Fragment fragment = new DirectoryDetailFragment();
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
    }

    private void search() {
        // Clear existing venues
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
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.fragment_directory_detail_timeout_retrieving_data),
                                Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.fragment_directory_detail_timeout_retrieving_data),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, BusinessDirectoryFragment.CATEGORY_TIMEOUT);
        }

        mLocateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationEnabled && null != mCurrentLocation) {
                    MapLatLng currentLatLng =
                        new MapLatLng(mCurrentLocation.getLatitude(),
                                       mCurrentLocation.getLongitude());
                    drawCurrentLocationMarker(mCurrentLocation);
                    mMapShim.animateCamera(currentLatLng);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),
                          getString(R.string.no_location_found), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mBusinessAutoCompleteAsyncTask.cancel(true);
        }
        if (mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLocationAutoCompleteAsyncTask.cancel(true);
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
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), getString(R.string.fragment_business_enable_location_services), Toast.LENGTH_SHORT).show();
        } else {
            locationEnabled = true;
        }
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

    class LocationAutoCompleteAsynctask extends AsyncTask<String, Integer, List<LocationSearchResult>> {

        private List<LocationSearchResult> mCacheData = null;

        public LocationAutoCompleteAsynctask(List<LocationSearchResult> cacheData) {
            mCacheData = cacheData;
        }

        @Override
        protected List<LocationSearchResult> doInBackground(String... strings) {
            return AirbitzAPI.getApi().getHttpAutoCompleteLocation(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(List<LocationSearchResult> result) {
            if (getActivity() == null) {
                return;
            }

            mLocation.clear();

            // Add current location and on the web
            mLocation.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocation.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocation.add(new LocationSearchResult(getString(R.string.fragment_business_no_results), false));
            } else {

                // Add cached location searches
                if (mCacheData != null) {
                    for (LocationSearchResult location : mCacheData) {
                        if (!mLocation.contains(location)) {
                            mLocation.add(0, location);
                        }
                    }
                }

                // Add all location results
                for (LocationSearchResult l : result) {
                    if (!mLocation.contains(l)) {
                        mLocation.add(l);
                    }
                }
            }
            mLocationAdapter.notifyDataSetChanged();
            mLocationAutoCompleteAsyncTask = null;
        }

        @Override
        protected void onCancelled(List<LocationSearchResult> JSONResult) {
            mLocationAutoCompleteAsyncTask = null;
            super.onCancelled();
        }
    }

    class BusinessAutoCompleteAsynctask extends AsyncTask<String, Integer, List<Business>> {

        private List<Business> mCacheData = null;

        public BusinessAutoCompleteAsynctask(List<Business> cacheData) {
            mCacheData = cacheData;
        }

        @Override
        protected List<Business> doInBackground(String... strings) {
            List<Business> jsonParsingResult = AirbitzAPI.getApi().getHttpAutoCompleteBusiness(strings[0],
                    strings[1],
                    strings[2]);
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(List<Business> businesses) {
            if (getActivity() == null) {
                return;
            }

            mBusinessList.clear();
            if (businesses == null) {
                mBusinessList.add(new Business(getString(R.string.fragment_business_no_results), "", ""));
            } else {

                // Add all businesses first
                mBusinessList.addAll(businesses);

                // Add cached businesses
                if (mCacheData != null) {
                    for (Business business : mCacheData) {
                        if (!mBusinessList.contains(business)) {
                            mBusinessList.add(0, business);
                        }
                    }
                }
            }
            mBusinessSearchAdapter.notifyDataSetChanged();
            mBusinessAutoCompleteAsyncTask = null;
        }

        @Override
        protected void onCancelled(List<Business> JSONResult) {
            mBusinessAutoCompleteAsyncTask = null;
            super.onCancelled();
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
                if (isNewVenuesAdded(results.getBusinessSearchObjectArray())) {
                    updateVenueResults(searchResult, false);
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
            if (getActivity() == null) {
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
            if (getActivity() == null)
                return;

            updateVenueResults(searchResult, true);

            showMessageProgress("", false);
            mGetVenuesAsyncTask = null;
        }
    }
}
