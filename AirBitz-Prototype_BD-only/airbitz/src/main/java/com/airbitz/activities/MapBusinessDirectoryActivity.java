
package com.airbitz.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.airbitz.App;
import com.airbitz.R;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.MapInfoWindowAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.VenueFragment;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.objects.BusinessVenue;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.utils.CacheUtil;
import com.airbitz.utils.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.greenhalolabs.halohalo.ResHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class MapBusinessDirectoryActivity extends Activity implements GestureDetector.OnGestureListener {

    private static final String TAG = MapBusinessDirectoryActivity.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private LinearLayout mTopLayout;
    private ImageButton mLocateMeButton;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Marker mUserLocationMarker;

    private ClearableEditText mSearchEdittext;
    private ClearableEditText mLocationEdittext;

    private LinearLayout llListContainer;

    private FrameLayout flMapContainer;

    int mapHeight;

    private LinearLayout mDragLayout;
    private FrameLayout mFrameLayout;
    private TextView mTitleTextView;
    private ListView mSearchListView;
    private ViewAnimator mViewAnimator;

    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private ArrayList<BusinessVenue> mBusinessVenueList;

    private float aPosY;
    private float aLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ArrayList<LocationSearchResult> mLocation;
    private ArrayList<Business> mBusinessList;

    private Location mCurrentLocation;

    private LocationAdapter mLocationAdapter;

    private Intent mIntent = null;

    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType;

    private GestureDetector mGestureDetector;

    private HashMap<Marker, Integer> mMarkerId = new HashMap<Marker, Integer>();
    private HashMap<Marker, String> mMarkerDistances = new HashMap<Marker, String>();
    private HashMap<Marker, String> mMarkerImageLink = new HashMap<Marker, String>();

    private List<BusinessSearchResult> mVenues;
    private static String mLocationWords = "";

    private boolean mCameraChangeListenerEnabled = false;

    private List<LatLng> mMarkersLatLngList;
    private float mDragBarThreshold;

    private GetVenuesByLocationTask mGetVenuesByLocationTask;
    private GetVenuesByCategoryTask mGetVenuesByCategoryTask;
    private GetVenuesByBusinessTask mGetVenuesByBusinessTask;
    private GetVenuesByLatLongTask mGetVenuesByLatLongTask;
    private GetVenuesByBusinessAndLocation mGetVenuesByBusinessAndLocation;
    
    VenueFragment fragmentVenue;

    private double mDensity;

    int dragBarHeight = 0;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationName = getIntent().getStringExtra(BusinessDirectoryActivity.LOCATION);
        mBusinessName = getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESS);
        mBusinessType = getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESSTYPE);
        setContentView(R.layout.activity_map_business_directory_2);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        fragmentVenue = (VenueFragment)getFragmentManager().findFragmentById(R.id.venue);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mDragLayout = (LinearLayout) findViewById(R.id.dragLayout);
        llListContainer = (LinearLayout) findViewById(R.id.list_view_container);
        flMapContainer = (FrameLayout) findViewById(R.id.map_container);
        mViewAnimator = (ViewAnimator) findViewById(R.id.ViewAnimator);

        // mFrameLayout = (FrameLayout) findViewById(R.id.frame_layout);

        mBusinessList = new ArrayList<Business>();
        mLocation = new ArrayList<LocationSearchResult>();

        mGestureDetector = new GestureDetector(this);

        // mTopLayout = (LinearLayout) findViewById(R.id.topLayout);
        mLocateMeButton = (ImageButton) findViewById(R.id.locateMeButton);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mBackButton.setVisibility(View.VISIBLE);

        mSearchEdittext = (ClearableEditText) findViewById(R.id.edittext_search);
        mLocationEdittext = (ClearableEditText) findViewById(R.id.edittext_location);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mSearchListView = (ListView) findViewById(R.id.listview_search);
        mSearchEdittext.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mLocationEdittext.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mTitleTextView.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);

        mSearchEdittext.setText(mBusinessName);
        mLocationEdittext.setText(mLocationName);

        mBusinessVenueList = new ArrayList<BusinessVenue>();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                finish();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Common.showHelpInfoDialog(MapBusinessDirectoryActivity.this,
                                          "Info",
                                          "Business directory info");
            }
        });

        mBusinessSearchAdapter = new BusinessSearchAdapter(MapBusinessDirectoryActivity.this, mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {

                    mSearchListView.setAdapter(mBusinessSearchAdapter);
                    mLocationEdittext.setVisibility(View.VISIBLE);
                    mViewAnimator.setDisplayedChild(1);

                    // Start search
                    try {
                        final String text = mSearchEdittext.getText().toString();
                        final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                                ? null
                                : CacheUtil.getCachedBusinessSearchData(MapBusinessDirectoryActivity.this));
                        String latLong = String.valueOf(getLatFromSharedPreference());
                        latLong += "," + String.valueOf(getLonFromSharedPreference());
                        new BusinessAutoCompleteAsynctask(cachedBusiness).execute(text,
                                                                                  mLocationWords,
                                                                                  latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mSearchEdittext.onTextChanged();
            }

            @Override public void afterTextChanged(Editable editable) {

                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationEdittext.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.VISIBLE);
                String latLong = String.valueOf(getLatFromSharedPreference());
                latLong += "," + String.valueOf(getLonFromSharedPreference());

                try {
                    // Only include cached searches if text is empty.
                    final String query = editable.toString();
                    final List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(MapBusinessDirectoryActivity.this)
                            : null);
                    new BusinessAutoCompleteAsynctask(cachedBusinesses).execute(editable.toString(),
                                                                                mLocationWords, latLong);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLocationAdapter = new LocationAdapter(MapBusinessDirectoryActivity.this, mLocation);
        mSearchListView.setAdapter(mLocationAdapter);

        mLocationEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mSearchListView.setAdapter(mLocationAdapter);

                    // Search
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                    mLocationWords = "";

                    try {
                        new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(MapBusinessDirectoryActivity.this)).execute(mLocationWords,
                                                                                                                                            latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        final View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                int keyAction = keyEvent.getAction();
                String test = "";
                mIntent = new Intent(MapBusinessDirectoryActivity.this, MapBusinessDirectoryActivity.class);
                if (keyAction == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.FLAG_EDITOR_ACTION:
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESS, mSearchEdittext.getText()
                                                                                                .toString());
                            mIntent.putExtra(BusinessDirectoryActivity.LOCATION, mLocationEdittext.getText()
                                                                                                  .toString());
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESSTYPE, mBusinessType);
                            startActivity(mIntent);
                            finish();
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESS, mSearchEdittext.getText()
                                                                                                .toString());
                            mIntent.putExtra(BusinessDirectoryActivity.LOCATION, mLocationEdittext.getText()
                                                                                                  .toString());
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESSTYPE, mBusinessType);
                            startActivity(mIntent);
                            finish();
                            return true;
                        default:
                            return false;
                    }
                }

                return false;
            }
        };
        mLocationEdittext.setOnKeyListener(keyListener);
        mSearchEdittext.setOnKeyListener(keyListener);

        mLocationEdittext.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mLocationEdittext.onTextChanged();
            }

            @Override public void afterTextChanged(Editable editable) {

                    mSearchListView.setAdapter(mLocationAdapter);
                    mSearchListView.setVisibility(View.VISIBLE);
                    mLocationWords = editable.toString();
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());

                    try {
                        List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                                ? CacheUtil.getCachedLocationSearchData(MapBusinessDirectoryActivity.this)
                                : null);

                        new LocationAutoCompleteAsynctask(cachedLocationSearch).execute(mLocationWords, latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        });

        initializeMap();
        // initializeMarker();

        int timeout = 15000;
        if (mLocationName.equalsIgnoreCase("Current Location")) {
            mGetVenuesByLatLongTask = new GetVenuesByLatLongTask(this);
            String latlong = "" + getLatFromSharedPreference() + "," + getLonFromSharedPreference();
            if (mBusinessType.equalsIgnoreCase("business")) {

                mGetVenuesByLatLongTask.execute(latlong, mBusinessName, "");
            } else {

                mGetVenuesByLatLongTask.execute(latlong, "", mBusinessName);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mGetVenuesByLatLongTask.getStatus() == AsyncTask.Status.RUNNING)
                        mGetVenuesByLatLongTask.cancel(true);
                }
            }, timeout);
        } else {
            mGetVenuesByBusinessAndLocation = new GetVenuesByBusinessAndLocation(this);
            mGetVenuesByBusinessAndLocation.execute(mBusinessName, mLocationName, mBusinessType);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mGetVenuesByBusinessAndLocation.getStatus() == AsyncTask.Status.RUNNING)
                        mGetVenuesByBusinessAndLocation.cancel(true);
                }
            }, timeout);
        }

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                boolean locationFieldShouldFocus = false;

                if (mSearchEdittext.isFocused()) {

                    final BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter) mSearchListView.getAdapter();

                    final Business business = businessSearchAdapter.getItem(position);

                    mSearchEdittext.setText(business.getName());
                    mBusinessType = business.getType();

                    if ("business".equalsIgnoreCase(mBusinessType)) {
                        Intent intent = new Intent(MapBusinessDirectoryActivity.this,
                                                   DirectoryDetailActivity.class);
                        intent.putExtra("bizId", business.getId());
                        intent.putExtra("bizName", business.getName());
                        startActivity(intent);
                    } else {
                        CacheUtil.writeCachedBusinessSearchData(MapBusinessDirectoryActivity.this,
                                                                businessSearchAdapter.getItem(position));
                        locationFieldShouldFocus = true;
                    }

                } else if (mLocationEdittext.isFocused()) {
                    final LocationAdapter locationAdapter = (LocationAdapter) mSearchListView.getAdapter();
                    final LocationSearchResult location = locationAdapter.getItem(position);
                    mLocationEdittext.setText(location.getLocationName());
                    CacheUtil.writeCachedLocationSearchData(MapBusinessDirectoryActivity.this,
                                                            location.getLocationName());
                }

                if (locationFieldShouldFocus) {
                    mLocationEdittext.requestFocus();
                    mLocationEdittext.setSelection(mLocationEdittext.length());
                    mLocationEdittext.setSelected(false);
                } else {
                    mSearchEdittext.requestFocus();
                    mSearchEdittext.setSelection(mSearchEdittext.length());
                    mSearchEdittext.setSelected(false);
                }

            }
        });

        // final ViewTreeObserver observer = mFrameLayout.getViewTreeObserver();
        // observer.addOnGlobalLayoutListener(
        // new ViewTreeObserver.OnGlobalLayoutListener() {
        // @Override public void onGlobalLayout() {
        // mDragBarThreshold = (mFrameLayout.getHeight() * 2) - 70;
        // }
        // });

        mDragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent event) {
                String DEBUG_TAG = "TEST MOVE";
                int action = MotionEventCompat.getActionMasked(event);
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        Log.d(TAG, "action down");
                        // Save the ID of this pointer
                        mActivePointerId = event.getPointerId(0);
                        final float y = event.getY(mActivePointerId);

                        aLastTouchY = y;
                        if (aPosY == 0) {
                            aPosY = llListContainer.getLayoutParams().height;
                        }
                        return true;

                    case (MotionEvent.ACTION_MOVE):
                        Log.d(TAG, "action move");
                        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) flMapContainer.getLayoutParams();
                        int currentHeight = param.height;

                        if (dragBarHeight == 0) {
                            dragBarHeight = mDragLayout.getMeasuredHeight() + 10;
                        }

                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);

                        float yMove = event.getY(pointerIndexMove);

                        // Log.d(TAG, String.format("yMove: %f", yMove));
                        //
                        // if (yMove < 0 && mapOriginalHeight == param.height) {
                        // Log.d(TAG, "max height reached");
                        // //return true;
                        // }
                        //
                        // if (param.height == 0 && yMove > 0) {
                        // Log.d(TAG, "min height reached");
                        // //return true;
                        // }

                        param.height += yMove;

                        // final float dy = yMove - aLastTouchY;
                        //
                        // aPosY += dy;
                        //
                        // if (aPosY > mDragBarThreshold) {
                        // aPosY = mDragBarThreshold;
                        // }
                        //
                        // param.height = (int) (aPosY);

                        // int bottomPadding = param.height - mapOriginalHeight;

                        // Log.d(TAG, String.format("bottomPadding: %d", bottomPadding));

                        // param.setMargins(0, 0, 0, bottomPadding);

                        int[] dragBarLocation = new int[2];
                        mDragLayout.getLocationOnScreen(dragBarLocation);

                        Log.d(TAG, "dragLayout location: " + dragBarLocation[1]);
                        Log.d(TAG, "dragLayout height: " + dragBarHeight);
                        Log.d(TAG, "display height: " + App.getDisplayHeight());

                        Log.d(TAG, String.format("flMapContainer height: %d", param.height));
                        if (param.height <= 0 || (dragBarLocation[1] + dragBarHeight >= App.getDisplayHeight() && yMove > 0)) {
                            Log.d(TAG, "height is out of bounds.");
                            param.height = currentHeight;
                        }

                        flMapContainer.setLayoutParams(param);

                        int padding = (mapHeight - param.height) / 2;
                        Log.d(TAG, "map padding: " + String.valueOf(padding));
                        mGoogleMap.setPadding(0, padding, 0, padding);

                        return true;
                    default:
                        return true;
                }
            }
        });

        // Hide map if "On the Web" search
        if (ResHelper.getStringByResId(R.string.on_the_web).equalsIgnoreCase(mLocationName)) {
            mDragLayout.setVisibility(View.GONE);
            flMapContainer.setVisibility(View.GONE);
        }
    }

    @Override public void onBackPressed() {
        if (mViewAnimator.getDisplayedChild() == 1) {
            mViewAnimator.setDisplayedChild(0);
            mLocationEdittext.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onResume() {

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    private void initializeMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mGoogleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                     .show();
            }

            mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override public void onMyLocationChange(Location location) {
                    mCurrentLocation = location;
                    drawCurrentLocationMarker(location);
                }
            });
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

            mapHeight = (int) ResHelper.convertDpToPx(480); //hardcoded in layout

            int padding = (int) ResHelper.convertDpToPx(120);

            mGoogleMap.setPadding(0, padding, 0, padding);

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Criteria cri = new Criteria();
            String provider = locationManager.getBestProvider(cri, true);
            mCurrentLocation = locationManager.getLastKnownLocation(provider);

            LatLng currentLatLng;

            if (mCurrentLocation != null) {
                currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            } else {
                mCurrentLocation = new Location("dummyProvider");
                mCurrentLocation.setLatitude(getLatFromSharedPreference());
                mCurrentLocation.setLongitude(getLonFromSharedPreference());
                currentLatLng = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
            }
            try {
                mCurrentLocation = mGoogleMap.getMyLocation();
                Log.d("TAG LOC",
                      "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());

                currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            } catch (Exception e) {

            }

            mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            MapInfoWindowAdapter customInfoWindowAdapter = new MapInfoWindowAdapter(MapBusinessDirectoryActivity.this);
            mGoogleMap.setInfoWindowAdapter(customInfoWindowAdapter);

            mLocateMeButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    Log.d("TAG_LOC",
                          "CUR LOC: " + mCurrentLocation.getLatitude()
                                  + "; "
                                  + mCurrentLocation.getLongitude());

                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                                                      mCurrentLocation.getLongitude());

                    drawCurrentLocationMarker(currentLatLng);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                    mUserLocationMarker.showInfoWindow();
                }
            });

            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override public void onInfoWindowClick(Marker marker) {
                    if (marker.getTitle().equalsIgnoreCase(ResHelper.getStringByResId(R.string.your_location))) {

                    } else {
                        Intent intent = new Intent(MapBusinessDirectoryActivity.this,
                                                   DirectoryDetailActivity.class);
                        int id = mMarkerId.get(marker);
                        String distance = mMarkerDistances.get(marker);
                        intent.putExtra("bizId", "" + id);
                        intent.putExtra("bizName", marker.getTitle());
                        intent.putExtra("bizDistance", distance);
                        startActivity(intent);
                    }
                }
            });

            mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override public void onCameraChange(CameraPosition cameraPosition) {

                    if (mCameraChangeListenerEnabled) {
                        LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                        LatLng southWestLatLng = latLngBounds.southwest;
                        LatLng northEastLatLng = latLngBounds.northeast;
                        String southWest = "" + southWestLatLng.latitude + "%2C" + southWestLatLng.longitude;
                        String northEast = "" + northEastLatLng.latitude + "%2C" + northEastLatLng.longitude;
                        String bound = southWest + "%7C" + northEast;
                        String userLatLong = "" + mCurrentLocation.getLatitude()
                                             + ","
                                             + mCurrentLocation.getLongitude();

                        GetVenuesByBoundTask getVenuesByBoundTask = new GetVenuesByBoundTask(MapBusinessDirectoryActivity.this);
                        getVenuesByBoundTask.execute(bound, mBusinessName, userLatLong);
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
        }
    }

    private void drawCurrentLocationMarker(Location location) {
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }
        LatLng currentPosition;
        // added default location to prevent breaking
        if (location != null) {
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            currentPosition = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
        }
        mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                                      .position(currentPosition)
                                                                      .title(ResHelper.getStringByResId(R.string.your_location))
                                                                      .snippet("")
                                                                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
                                        );
    }

    private void drawCurrentLocationMarker(LatLng location) {
        if (mUserLocationMarker != null) {
            mUserLocationMarker.remove();
        }
        if (location == null) {
            location = new LatLng(getLatFromSharedPreference(), getLonFromSharedPreference());
        }
        mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                                                                      .position(location)
                                                                      .title(ResHelper.getStringByResId(R.string.your_location))
                                                                      .snippet("")
                                                                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
                                        );
    }

    protected void initializeMarker() {

        mMarkersLatLngList = new ArrayList<LatLng>();
        if (mBusinessVenueList.size() > 0) {

            LatLng currentLatLng = new LatLng(0, 0);
            for (BusinessVenue businessVenue : mBusinessVenueList) {
                mMarkersLatLngList.add(businessVenue.getLocation());
                currentLatLng = businessVenue.getLocation();
                mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(businessVenue.getLocation())
                                                        .title(businessVenue.getName())
                                                        .snippet(businessVenue.getAddress())
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                          )
                          .showInfoWindow();
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

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
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

            zoomToContainAllMarkers();

//            if (firstMarker != null) {
//                firstMarker.showInfoWindow();
//            } else {
//                drawCurrentLocationMarker(mCurrentLocation);
//                mUserLocationMarker.showInfoWindow();
//            }
        } else {
            drawCurrentLocationMarker(mCurrentLocation);
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

            if (firstMarker == null) {
                drawCurrentLocationMarker(mCurrentLocation);
                mUserLocationMarker.showInfoWindow();
            }
        } else {
            drawCurrentLocationMarker(mCurrentLocation);
            mUserLocationMarker.showInfoWindow();
        }

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

            mLocation.clear();

            // Add current location and on the web
            mLocation.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocation.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocation.add(new LocationSearchResult("Result not found", false));
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
                mBusinessList.add(new Business("Result not found", "", ""));
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
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
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
                if (isNewVenuesAdded(results.getBusinessSearchObjectArray())) {
                    mVenues = results.getBusinessSearchObjectArray();
                    mGoogleMap.clear();
                    initializeMarkerWithBoundSearchResult();
                    fragmentVenue.setListView(searchResult);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private class GetVenuesByLocationTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByLocationTask(Context context) {
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

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected String doInBackground(String... params) {
            return mApi.getSearchByLocation(params[0], "", "", "");
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                mVenues = results.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mProgressDialog.dismiss();
        }
    }

    private class GetVenuesByBusinessTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBusinessTask(Context context) {
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
            return mApi.getSearchByTerm(params[0], "", "", "");
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
        }
    }

    private class GetVenuesByCategoryTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByCategoryTask(Context context) {
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
            return mApi.getSearchByCategory(params[0], "", "", "");
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mProgressDialog.dismiss();
        }
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
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
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
            String latLong = String.valueOf(getLatFromSharedPreference())
                             + "," + String.valueOf(getLonFromSharedPreference());
            return mApi.getSearchByCategoryOrBusinessAndLocation(params[0], params[1], "", "", "1",
                                                                 params[2], latLong);
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mProgressDialog.dismiss();
        }
    }

    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.PREF_NAME, MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference() {
        double lat = (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LAT_KEY);
        return lat;
    }

    private double getLonFromSharedPreference() {
        double lon = (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LON_KEY);
        return lon;
    }

    @Override public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override public void onShowPress(MotionEvent motionEvent) {

    }

    @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override public void onLongPress(MotionEvent motionEvent) {

    }

    @Override public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if (start != null & finish != null) {

            float yDistance = Math.abs(finish.getY() - start.getY());

            if ((finish.getRawX() > start.getRawX()) && (yDistance < 10)) {
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if (xDistance > 100) {
                    finish();
                    return true;
                }
            }

        }
        return false;
    }
}
