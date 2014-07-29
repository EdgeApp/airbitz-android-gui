package com.airbitz.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
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
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.airbitz.adapters.MapInfoWindowAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.objects.BusinessVenue;
import com.airbitz.utils.CacheUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class MapBusinessDirectoryFragment extends Fragment implements CustomMapFragment.OnMapReadyListener,
        CurrentLocationManager.OnLocationChange {//implements GestureDetector.OnGestureListener {
    private static final String TAG = MapBusinessDirectoryFragment.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private ImageButton mLocateMeButton;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Marker mUserLocationMarker;

    private LinearLayout mDummyFocus;

    private EditText mSearchEdittext;
    private EditText mLocationEdittext;

    private RelativeLayout llListContainer;

    private FrameLayout flMapContainer;

    private boolean locationEnabled;

    int mapHeight;

    private LinearLayout mDragLayout;
    private FrameLayout mFrameLayout;
    private TextView mTitleTextView;
    private ListView mSearchListView;
    //private ViewAnimator mViewAnimator;

    private LinearLayout mMapLayout;

    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private ArrayList<BusinessVenue> mBusinessVenueList;

    private float aPosY;
    private float dY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ArrayList<LocationSearchResult> mLocation;
    private ArrayList<Business> mBusinessList;

    private Location mCurrentLocation;

    private LocationAdapter mLocationAdapter;

    private Intent mIntent = null;
    private int[] locSticky = {0,0};
    private int[] locFrame = {0,0};

    private LinearLayout mapView;
    private CustomMapFragment mapFragment;

    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType;
    private Bundle mVenueBundle;

    //private GestureDetector mGestureDetector;

    private HashMap<Marker, Integer> mMarkerId = new HashMap<Marker, Integer>();
    private HashMap<Marker, String> mMarkerDistances = new HashMap<Marker, String>();
    private HashMap<Marker, String> mMarkerImageLink = new HashMap<Marker, String>();

    private int mWindowHeight = 0;

    private List<BusinessSearchResult> mVenues;
    private static String mLocationWords = "";

    private boolean mCameraChangeListenerEnabled = false;

    private List<LatLng> mMarkersLatLngList;
    private float mDragBarThreshold;

    private CurrentLocationManager mLocationManager;

    private LinearLayout mVenueFragmentLayout;

    private AsyncTask<String, Void, String> mGetVenuesAsyncTask;
    private GetVenuesByBoundTask mGetVenuesByBoundAsyncTask;
    private LocationAutoCompleteAsynctask mLocationAutoCompleteAsyncTask;
    private BusinessAutoCompleteAsynctask mBusinessAutoCompleteAsyncTask;


    VenueFragment mFragmentVenue;

    private double mDensity;

    int dragBarHeight = 0;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mVenueBundle = this.getArguments();
        mBusinessName = mVenueBundle.getString(BusinessDirectoryFragment.BUSINESS);
        mLocationName = mVenueBundle.getString(BusinessDirectoryFragment.LOCATION);
        mBusinessType = mVenueBundle.getString(BusinessDirectoryFragment.BUSINESSTYPE);

        if(mLocationManager==null) {
            mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        }
    }

    @Override
    public void onMapReady() {
        mGoogleMap = mapFragment.getMap();
        initializeMap();
        mLocationManager.addLocationChangeListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_business_directory_2, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        checkLocationManager();
        if(locationEnabled && mLocationManager.getLocation() != null){
            System.out.println("!YaY! Location isn't null!");
            mCurrentLocation = mLocationManager.getLocation();
        }else{
            System.out.println("!BOO! Location is null!");
        }

        mVenueFragmentLayout = (LinearLayout) view.findViewById(R.id.venue_container);
        if(mVenueFragmentLayout.getChildCount()<=0) {
            mFragmentVenue = new VenueFragment();
            mVenueBundle.putBoolean("from_business",false);
            mVenueBundle.putBoolean("locationEnabled", locationEnabled);
            mFragmentVenue.setArguments(mVenueBundle);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.venue_container, mFragmentVenue, "venue").commit();
        }

        mDummyFocus = (LinearLayout) view.findViewById(R.id.fragment_mapbusinessdirectory_dummy_focus);

        mapView = (LinearLayout) view.findViewById(R.id.map_view);
        if(mapView.getChildCount()<=0) {
            mapFragment = CustomMapFragment.newInstance();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.map_view, mapFragment, "map").commit();
        }

        mMapLayout = (LinearLayout) view.findViewById(R.id.map_view_layout);

        mDragLayout = (LinearLayout) view.findViewById(R.id.dragLayout);
        llListContainer = (RelativeLayout) view.findViewById(R.id.list_view_container);
        flMapContainer = (FrameLayout) view.findViewById(R.id.map_container);

        mBusinessList = new ArrayList<Business>();
        mLocation = new ArrayList<LocationSearchResult>();

        mLocateMeButton = (ImageButton) view.findViewById(R.id.locateMeButton);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);

        mBackButton.setVisibility(View.VISIBLE);

        mSearchEdittext = (EditText) view.findViewById(R.id.edittext_search);
        mLocationEdittext = (EditText) view.findViewById(R.id.edittext_location);

        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mSearchEdittext.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        mLocationEdittext.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);
        mTitleTextView.setTypeface(BusinessDirectoryFragment.montserratRegularTypeFace);

        mSearchEdittext.setText(mBusinessName);
        mLocationEdittext.setText(mLocationName);

        mBusinessVenueList = new ArrayList<BusinessVenue>();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if(mLocationEdittext.getVisibility()==View.VISIBLE){
                    showViewAnimatorChild(0);
                }else{
                    getActivity().onBackPressed();
                }
            };
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
//                Common.showHelpInfoDialog(this,
//                        "Info",
//                        "Business directory info");
            }
        });

        mBusinessSearchAdapter = new BusinessSearchAdapter(getActivity(), mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
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
                        if(locationEnabled) {
                            latLong = String.valueOf(getLatFromSharedPreference());
                            latLong += "," + String.valueOf(getLonFromSharedPreference());
                        }
                        if(mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                            mBusinessAutoCompleteAsyncTask.cancel(true);
                        }
                        mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusiness);
                        mBusinessAutoCompleteAsyncTask.execute(text,
                                mLocationWords,
                                latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    if(!mLocationEdittext.hasFocus()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, 0);
                    }
                    /*if (!mSearchEdittext.getText().toString().isEmpty() && mSearchEdittext.getText().toString().charAt(0) == ' ') {
                        mSearchEdittext.setText(mSearchEdittext.getText().toString().substring(1));
                    }*/
                }
            }
        });

        mSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override public void afterTextChanged(Editable editable) {

                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationEdittext.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.VISIBLE);

                String latLong = "";
                if(locationEnabled) {
                    latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                }

                try {
                    // Only include cached searches if text is empty.
                    final String query;
                    query = editable.toString();
                    final List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(getActivity())
                            : null);
                    if(mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
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
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mSearchListView.setAdapter(mLocationAdapter);

                    // Search
                    String latLong = "";
                    if(locationEnabled) {
                        latLong = String.valueOf(getLatFromSharedPreference());
                        latLong += "," + String.valueOf(getLonFromSharedPreference());
                    }
                    mLocationWords = "";

                    try {
                        if(mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                            mLocationAutoCompleteAsyncTask.cancel(true);
                        }
                        mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(getActivity()));
                        mLocationAutoCompleteAsyncTask.execute(mLocationWords,
                                latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    if(!mSearchEdittext.hasFocus()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }
            }
        });

        mLocationEdittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
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
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
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
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override public void afterTextChanged(Editable editable) {

                mSearchListView.setAdapter(mLocationAdapter);
                mSearchListView.setVisibility(View.VISIBLE);
                mLocationWords = editable.toString();

                String latLong = "";
                if(locationEnabled) {
                    latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                }

                try {
                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(getActivity())
                            : null);
                    if(mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                        mLocationAutoCompleteAsyncTask.cancel(true);
                    }
                    mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(cachedLocationSearch);
                    mLocationAutoCompleteAsyncTask.execute(mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //initializeMap();//(MapView) view.findViewById(R.id.map_view));

        search();

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

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
            @Override public boolean onTouch(View view, MotionEvent event) {
                String DEBUG_TAG = "TEST MOVE";
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        //Log.d(TAG, "action down");
                        // Save the ID of this pointer
                        mActivePointerId = event.getPointerId(0);
                        dY = event.getY(0);

                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        mWindowHeight = size.y;

                        aPosY = llListContainer.getHeight();
                        return true;

                    case (MotionEvent.ACTION_MOVE):
                        //Log.d(TAG, "action move");
                        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) flMapContainer.getLayoutParams();
                        int currentHeight = param.height;

                        if (dragBarHeight == 0) {
                            dragBarHeight = mDragLayout.getMeasuredHeight() + 10;
                        }

                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);

                        float yMove = event.getY(pointerIndexMove);

                        param.height += yMove;

                        int barY = (int)mDragLayout.getY();

                        if (param.height <= 0 || (barY + dragBarHeight >= aPosY && yMove > 0)) {
                            Log.d(TAG, "height is out of bounds.");
                            Log.d(TAG, "Height: "+(barY+dragBarHeight)+" aPosY: "+aPosY+" param height: "+param.height+" yMove: "+yMove);
                            param.height = currentHeight;
                        }

                        flMapContainer.setLayoutParams(param);

                        int padding = (mapHeight - param.height) / 2;

                        if(mGoogleMap == null){
                            initializeMap();
                        }
                        if(mGoogleMap != null) {
                            mGoogleMap.setPadding(0, padding, 0, padding);
                        }

                        return true;
                    default:
                        return true;
                }
            }
        });

        // Hide map if "On the Web" search
        if (getString(R.string.on_the_web).equalsIgnoreCase(mLocationName)) {
            mDragLayout.setVisibility(View.GONE);
            flMapContainer.setVisibility(View.GONE);
        }
        return view;
    }

    private void showViewAnimatorChild(int num) {
        if(num==0) {
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
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }


    private void search() {
        int timeout = 15000;
        if (mLocationName.equalsIgnoreCase("Current Location")) {
            if(mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                mGetVenuesAsyncTask.cancel(true);
            }
            mGetVenuesAsyncTask = new GetVenuesByLatLongTask(getActivity());
            String latlong = "";
            if(locationEnabled){
                latlong += getLatFromSharedPreference() + "," + getLonFromSharedPreference();
            }
            if (mBusinessType.equalsIgnoreCase("business")) {
                mGetVenuesAsyncTask.execute(latlong, mBusinessName, "");
            } else {
                mGetVenuesAsyncTask.execute(latlong, "", mBusinessName);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        Toast.makeText(getActivity().getApplicationContext(), "Timeout retrieving data",
                                Toast.LENGTH_LONG).show();
                        mGetVenuesAsyncTask.cancel(true);
                    }
                }
            }, timeout);
        } else {
            if(mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
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
                        Toast.makeText(getActivity().getApplicationContext(), "Timeout retrieving data",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }, timeout);
        }
        mDummyFocus.requestFocus();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        if(mBusinessCategoryAsynctask!=null)
//            mBusinessCategoryAsynctask.cancel(true);

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPause(){
        mLocationManager.removeLocationChangeListener(this);
        if(mGetVenuesAsyncTask != null && mGetVenuesAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mGetVenuesAsyncTask.cancel(true);
        }
        if(mGetVenuesByBoundAsyncTask != null && mGetVenuesByBoundAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mGetVenuesByBoundAsyncTask.cancel(true);
        }
        if(mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mBusinessAutoCompleteAsyncTask.cancel(true);
        }
        if(mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            mLocationAutoCompleteAsyncTask.cancel(true);
        }
        super.onPause();
    }

    @Override
    public void onResume(){
        checkLocationManager();
        super.onResume();
    }

    private void checkLocationManager(){
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for better results", Toast.LENGTH_SHORT).show();
        }else{
            locationEnabled = true;
        }
    }

//    @Override public void onBackPressed() {
//        if (mViewAnimator.getDisplayedChild() == 1) {
//            showViewAnimatorChild(0);
//        } else {
////            super.onBackPressed();
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeMap(){//MapView mapView) {//todo
        //if (mGoogleMap == null) {
            //mapView.onCreate(null);

            // Gets to GoogleMap from the MapView and does initialization stuff
            //mGoogleMap = mapView.getMap();

            //mGoogleMap = mapFragment.getMap();
        if (mGoogleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Sorry! unable to create maps, check for updates to Google Play Services", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        mGoogleMap.animateCamera(cameraUpdate);

        if (mGoogleMap == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override public void onMyLocationChange(Location location) {
                mCurrentLocation = location;
                drawCurrentLocationMarker(location);
            }
        });

        if(locationEnabled){
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        }else{
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        mapHeight = (int) getResources().getDimension(R.dimen.map_height); //hardcoded in layout

        int padding = (int) getResources().getDimension(R.dimen.map_padding);

        mGoogleMap.setPadding(0, padding, 0, padding);

//        if(locationEnabled) {
//            mCurrentLocation = mLocationManager.getLocation();
//        }

        LatLng currentLatLng = null;

        if (mCurrentLocation != null) {
            currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        } else {
                mCurrentLocation = new Location("dummyProvider");
            if(locationEnabled) {
                mCurrentLocation.setLatitude(getLatFromSharedPreference());
                mCurrentLocation.setLongitude(getLonFromSharedPreference());
                currentLatLng = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
            }
        }
        try {
            if(locationEnabled) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 10);
                mGoogleMap.animateCamera(cameraUpdate);
                Log.d("TAG LOC",
                        "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());
            }

        } catch (Exception e) {

        }

        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        if(locationEnabled) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        MapInfoWindowAdapter customInfoWindowAdapter = new MapInfoWindowAdapter(getActivity(), this);
        mGoogleMap.setInfoWindowAdapter(customInfoWindowAdapter);

        if(locationEnabled) {
            mLocateMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG_LOC",
                            "CUR LOC: " + mCurrentLocation.getLatitude()
                                    + "; "
                                    + mCurrentLocation.getLongitude()
                    );

                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude());

                    drawCurrentLocationMarker(currentLatLng);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                    mUserLocationMarker.showInfoWindow();
                }
            });
        }else{
            mLocateMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext(), "No current location found.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override public void onInfoWindowClick(Marker marker) {
                if (marker.getTitle().equalsIgnoreCase(getString(R.string.your_location))) {

                } else {
                    showDirectoryDetailFragment(""+mMarkerId.get(marker), marker.getTitle(), mMarkerDistances.get(marker));
                }
            }
        });

        mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override public void onCameraChange(CameraPosition cameraPosition) {
                System.out.println("");
                if (mCameraChangeListenerEnabled) {
                    LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                    LatLng southWestLatLng = latLngBounds.southwest;
                    LatLng northEastLatLng = latLngBounds.northeast;
                    String southWest = "" + southWestLatLng.latitude + "%2C" + southWestLatLng.longitude;
                    String northEast = "" + northEastLatLng.latitude + "%2C" + northEastLatLng.longitude;
                    String bound = southWest + "%7C" + northEast;
                    String userLatLong = "";
                    if(locationEnabled) {
                        userLatLong = "" + mCurrentLocation.getLatitude()
                                + ","
                                + mCurrentLocation.getLongitude();
                    }
                    if(mGetVenuesByBoundAsyncTask != null && mGetVenuesByBoundAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
                        mGetVenuesByBoundAsyncTask.cancel(true);
                    }
                    mGetVenuesByBoundAsyncTask = new GetVenuesByBoundTask(getActivity());
                    mGetVenuesByBoundAsyncTask.execute(bound, mBusinessName, userLatLong);
                } else {
                    mCameraChangeListenerEnabled = true;
                }

            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override public boolean onMarkerClick(Marker marker) {
                mCameraChangeListenerEnabled = false;
                marker.showInfoWindow();
                return true;
            }
        });
        mGoogleMap.setMyLocationEnabled(false);
        showViewAnimatorChild(0);
    //}
    }

    private void drawCurrentLocationMarker(Location location) {//todo
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }
        if(locationEnabled) {
            LatLng currentPosition;
            // added default location to prevent breaking
            if (location != null) {
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                currentPosition = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
            }
            if (mGoogleMap == null) {
                initializeMap();
            }
            if (mGoogleMap != null) {
                mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                .position(currentPosition)
                                .title(getString(R.string.your_location))
                                .snippet("")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
                );
            }
        }
    }

    private void drawCurrentLocationMarker(LatLng location) {//todo
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }
        if (location == null) {
            location = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
        }
        if(mGoogleMap == null){
            initializeMap();
        }
        if(mGoogleMap != null) {
            mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(getString(R.string.your_location))
                            .snippet("")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
            );
        }
    }

    protected void initializeMarker() {

        mMarkersLatLngList = new ArrayList<LatLng>();
        if (mBusinessVenueList.size() > 0) {

            LatLng currentLatLng = new LatLng(0, 0);
            for (BusinessVenue businessVenue : mBusinessVenueList) {
                mMarkersLatLngList.add(businessVenue.getLocation());
                currentLatLng = businessVenue.getLocation();
                if(mGoogleMap == null){
                    initializeMap();
                }
                if(mGoogleMap != null) {
                    mGoogleMap.addMarker(new MarkerOptions()
                                    .position(businessVenue.getLocation())
                                    .title(businessVenue.getName())
                                    .snippet(businessVenue.getAddress())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                    )
                            .showInfoWindow();
                }
            }
        }

        drawCurrentLocationMarker(mCurrentLocation);
        mUserLocationMarker.showInfoWindow();

        zoomToContainAllMarkers();
    }

    private void zoomToContainAllMarkers() {

        if (mMarkersLatLngList.size() > 0) {

            LatLngBounds.Builder bc = new LatLngBounds.Builder();

            for (LatLng item : mMarkersLatLngList) {
                bc.include(item);
            }
            if(mGoogleMap == null){
                initializeMap();
            }
            if(mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
            }
        }
    }

    protected void initializeMarkerWithBusinessSearchResult() {

        Log.d(TAG, "initializeMarkerWithBusinessSearchResult");

        mMarkersLatLngList = new ArrayList<LatLng>();
        Marker firstMarker = null;
        mMarkerId.put(null, 0);
        mMarkerDistances.put(null, "");
        getMarkerImageLink().put(null, "");
        if (!mMarkerId.isEmpty()) {
            mMarkerId.clear();
            mMarkerImageLink.clear();
            mMarkerDistances.clear();
        }
        if (mVenues.size() > 0) {

            LatLng currentLatLng = new LatLng(0, 0);
            boolean first = true;
            for (BusinessSearchResult businessSearchResult : mVenues) {

                LatLng locationLatLng = new LatLng(businessSearchResult.getLocationObject().getLatitude(),
                        businessSearchResult.getLocationObject().getLongitude());
                mMarkersLatLngList.add(locationLatLng);
                if(mGoogleMap == null){
                    initializeMap();
                }
                if(mGoogleMap != null) {
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(locationLatLng)
                                    .title(businessSearchResult.getName())
                                    .snippet(businessSearchResult.getAddress())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                    );

                    if (first) {
                        first = false;
                        currentLatLng = locationLatLng;
                        firstMarker = marker;
                    }
                    mMarkerId.put(marker, Integer.parseInt(businessSearchResult.getId()));
                    mMarkerDistances.put(marker, businessSearchResult.getDistance());
                    mMarkerImageLink.put(marker, businessSearchResult.getProfileImage().getImageThumbnail());
                }
            }

            zoomToContainAllMarkers();

//            if (firstMarker != null) {
//                firstMarker.showInfoWindow();
//            } else {
//                drawCurrentLocationMarker(mCurrentLocation);
//                mUserLocationMarker.showInfoWindow();
//            }
        } else {
            drawCurrentLocationMarker(mCurrentLocation);//TODO
            mUserLocationMarker.showInfoWindow();
        }

    }

    protected void initializeMarkerWithBoundSearchResult() {

        Log.d(TAG, "initializeMarkerWithBoundSearchResult");

        Marker firstMarker = null;
        mMarkerId.put(null, 0);
        mMarkerDistances.put(null, "");
        getMarkerImageLink().put(null, "");
        if (!mMarkerId.isEmpty()) {
            mMarkerId.clear();
            mMarkerImageLink.clear();
            mMarkerDistances.clear();
        }
        if (mVenues.size() > 0) {
            LatLng currentLatLng = new LatLng(0, 0);
            boolean first = true;
            for (BusinessSearchResult businessSearchResult : mVenues) {
                LatLng locationLatLng = new LatLng(businessSearchResult.getLocationObject().getLatitude(),
                        businessSearchResult.getLocationObject().getLongitude());
                if(mGoogleMap == null){
                    initializeMap();
                }
                if(mGoogleMap != null) {
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(locationLatLng)
                                    .title(businessSearchResult.getName())
                                    .snippet(businessSearchResult.getAddress())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                    );

                    if (first) {
                        first = false;
                        currentLatLng = locationLatLng;
                        firstMarker = marker;
                    }
                    mMarkerId.put(marker, Integer.parseInt(businessSearchResult.getId()));
                    mMarkerDistances.put(marker, businessSearchResult.getDistance());
                    mMarkerImageLink.put(marker, businessSearchResult.getProfileImage().getImageThumbnail());
                }
            }

            if (firstMarker == null) {//TODO
                drawCurrentLocationMarker(mCurrentLocation);
                mUserLocationMarker.showInfoWindow();
            }
        } else {//TODO
            drawCurrentLocationMarker(mCurrentLocation);
            mUserLocationMarker.showInfoWindow();
        }

    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mCurrentLocation = mLocationManager.getLocation();
        mLocationManager.removeLocationChangeListener(this);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 7));
    }

    public HashMap<Marker, String> getMarkerImageLink() {
        return mMarkerImageLink;
    }

    public HashMap<Marker, Integer> getMarkerId() {
        return mMarkerId;
    }

    class LocationAutoCompleteAsynctask extends AsyncTask<String, Integer, List<LocationSearchResult>> {

        private List<LocationSearchResult> mCacheData = null;

        public LocationAutoCompleteAsynctask(List<LocationSearchResult> cacheData) {
            mCacheData = cacheData;
        }

        @Override protected List<LocationSearchResult> doInBackground(String... strings) {
            return AirbitzAPI.getApi().getHttpAutoCompleteLocation(strings[0], strings[1]);
        }

        @Override protected void onPostExecute(List<LocationSearchResult> result) {
            if(getActivity()==null)
                return;

            mLocation.clear();

            // Add current location and on the web
            mLocation.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocation.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocation.add(new LocationSearchResult("No Results Found", false));
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

        @Override protected void onCancelled(List<LocationSearchResult> JSONResult){
            mLocationAutoCompleteAsyncTask = null;
            super.onCancelled();
        }
    }

    class BusinessAutoCompleteAsynctask extends AsyncTask<String, Integer, List<Business>> {

        private List<Business> mCacheData = null;

        public BusinessAutoCompleteAsynctask(List<Business> cacheData) {
            mCacheData = cacheData;
        }

        @Override protected List<Business> doInBackground(String... strings) {
            List<Business> jsonParsingResult = AirbitzAPI.getApi().getHttpAutoCompleteBusiness(strings[0],
                    strings[1],
                    strings[2]);
            ;
            return jsonParsingResult;
        }

        @Override protected void onPostExecute(List<Business> businesses) {
            mBusinessList.clear();
            if (businesses == null) {
                mBusinessList.add(new Business("No Results Found", "", ""));
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
        @Override protected void onCancelled(List<Business> JSONResult){
            mBusinessAutoCompleteAsyncTask = null;
            super.onCancelled();
        }
    }

    private class GetVenuesByBoundTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBoundTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
        }

        @Override protected void onCancelled() {
            mGetVenuesByBoundAsyncTask = null;
            super.onCancelled();
        }

        @Override protected String doInBackground(String... params) {

            Log.d(TAG, "GetVenuesByBoundTask params: " + params);
            if (mBusinessType.equalsIgnoreCase("category")) {

                return mApi.getSearchByBoundsAndBusiness(params[0], "", params[1], params[2], "", "", "");
            } else {

                return mApi.getSearchByBoundsAndBusiness(params[0], params[1], "", params[2], "", "", "");
            }
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                System.out.println("New Venues have been added: "+isNewVenuesAdded(results.getBusinessSearchObjectArray()));
                if (isNewVenuesAdded(results.getBusinessSearchObjectArray())) {
                    mVenues = results.getBusinessSearchObjectArray();
                    if(mGoogleMap == null){
                        initializeMap();
                    }
                    if(mGoogleMap != null) {
                        mGoogleMap.clear();
                        initializeMarkerWithBoundSearchResult();
                    }
                    mFragmentVenue.setListView(searchResult);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mGetVenuesByBoundAsyncTask = null;
        }
    }

    private boolean isNewVenuesAdded(List<BusinessSearchResult> newVenues) {

        for (BusinessSearchResult item : newVenues) {
            if (!mVenues.contains(item)) {
                return true;
            }
        }

        return false;

    }

    private class GetVenuesByLatLongTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByLatLongTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override protected String doInBackground(String... params) {
            return mApi.getSearchByLatLongAndBusiness(params[0], params[1], params[2], "", "", "");
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            mGetVenuesAsyncTask = null;
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                if(mGoogleMap == null){
                    initializeMap();
                }
                if(mGoogleMap != null) {
                    mGoogleMap.clear();
                    initializeMarkerWithBusinessSearchResult();
                }
                mFragmentVenue.setListView(searchResult);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
            mGetVenuesAsyncTask = null;
        }
    }

    private class GetVenuesByBusinessAndLocation extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBusinessAndLocation(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override protected String doInBackground(String... params) {
            String latLong = "";
            if(locationEnabled) {
                latLong = String.valueOf(getLatFromSharedPreference())
                        + "," + String.valueOf(getLonFromSharedPreference());
            }
            return mApi.getSearchByCategoryOrBusinessAndLocation(params[0], params[1], "", "", "1",
                    params[2], latLong);
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            mGetVenuesAsyncTask = null;
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                if(mGoogleMap == null){
                    initializeMap();
                }
                if(mGoogleMap != null) {
                    mGoogleMap.clear();
                    initializeMarkerWithBusinessSearchResult();
                }
                mFragmentVenue.setListView(searchResult);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
            mGetVenuesAsyncTask = null;
        }
    }

    private double getLatFromSharedPreference() {
        if(mCurrentLocation!=null)
            return mCurrentLocation.getLatitude();
        else
            return 0.0;
    }

    private double getLonFromSharedPreference() {
        if(mCurrentLocation!=null)
            return mCurrentLocation.getLongitude();
        else
            return 0.0;
    }

}
