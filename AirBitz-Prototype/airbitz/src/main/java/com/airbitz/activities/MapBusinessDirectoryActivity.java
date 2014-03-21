package com.airbitz.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.MapInfoWindowAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.objects.BusinessVenue;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class MapBusinessDirectoryActivity extends Activity implements GestureDetector.OnGestureListener{

    public static final String YOUR_LOCATION = "Your Location";
    private static final String TAG = MapBusinessDirectoryActivity.class.getSimpleName();

    private GoogleMap mGoogleMap;
    private LinearLayout mTopLayout;
    private ImageButton mLocateMeButton;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private Marker mUserLocationMarker;

    private ClearableEditText mSearchEdittext;
    private ClearableEditText mLocationEdittext;

    private Button mCurrentLocationButton;
    private Button mOnTheWebButton;

    private LinearLayout mCurrentLayoutSeparator;
    private LinearLayout mOnTheWebSeparator;
    private LinearLayout mListLayout;

    private LinearLayout mMainContentLayout;
    private LinearLayout mPaddingLayout;
    private LinearLayout mDummyFocusLayout;

    private LinearLayout mDragLayout;

    private RelativeLayout mHeaderLayout;
    private RelativeLayout mParentLayout;
    private RelativeLayout mNavigationLayout;
    private FrameLayout mFrameLayout;

    private LinearLayout mMapLayout;

    private TextView mTitleTextView;

    private ListView mSearchListView;

    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private ArrayList<BusinessVenue> mBusinessVenueList;

    private float aPosY;
    private float aLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ArrayList<String> mLocation;
    private ArrayList<Business> mBusinessList;

    private Location mCurrentLocation;

    private LocationAdapter mLocationAdapter;

    private Intent mIntent = null;

    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType;

    private GestureDetector mGestureDetector;

    private HashMap<Marker,Integer> mMarkerId = new HashMap<Marker, Integer>();
    private HashMap<Marker,String> mMarkerDistances = new HashMap<Marker, String>();
    private HashMap<Marker,String> mMarkerImageLink = new HashMap<Marker, String>();

    private List<BusinessSearchResult> mVenues;
    private static String mLocationWords = "";

    private boolean mCameraChangeListenerEnabled = false;

    private List<LatLng> mMarkersLatLngList;
    private float mDragBarThreshold;

    private GetVenuesByLatLongTask mGetVenuesByLatLongTask;
    private GetVenuesByBusinessAndLocation mGetVenuesByBusinessAndLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationName = getIntent().getStringExtra(BusinessDirectoryActivity.LOCATION);
        mBusinessName = getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESS);
        mBusinessType = getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESSTYPE);
        setContentView(R.layout.activity_map_business_directory);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.navigation_layout);
        mMainContentLayout = (LinearLayout) findViewById(R.id.layout_main_content);
        mDragLayout = (LinearLayout) findViewById(R.id.dragLayout);
        mPaddingLayout = (LinearLayout) findViewById(R.id.bottom_padding);
        mFrameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        mListLayout = (LinearLayout) findViewById(R.id.bottomLayout);
        mHeaderLayout = (RelativeLayout) findViewById(R.id.layout_header);
        mDragLayout = (LinearLayout) findViewById(R.id.dragLayout);

        mParentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mParentLayout.getRootView().getHeight() - mParentLayout.getHeight();
                if (heightDiff > 100) {
                    mNavigationLayout.setVisibility(View.GONE);
                }
                else
                {
                    mNavigationLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mDummyFocusLayout = (LinearLayout) findViewById(R.id.dummy_focus);

        mBusinessList = new ArrayList<Business>();
        mLocation = new ArrayList<String>();

        mGestureDetector = new GestureDetector(this);

        mTopLayout = (LinearLayout)findViewById(R.id.topLayout);
        mLocateMeButton = (ImageButton)findViewById(R.id.locateMeButton);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mSearchEdittext = (ClearableEditText) findViewById(R.id.edittext_search);
        mLocationEdittext = (ClearableEditText) findViewById(R.id.edittext_location);

        mCurrentLocationButton = (Button) findViewById(R.id.button_current_location);
        mOnTheWebButton = (Button) findViewById(R.id.button_on_the_Web);

        mCurrentLayoutSeparator = (LinearLayout) findViewById(R.id.separator_current);
        mOnTheWebSeparator = (LinearLayout) findViewById(R.id.separator_on_the_web);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mMapLayout = (LinearLayout) findViewById(R.id.map_layout);

        mSearchListView = (ListView) findViewById(R.id.listview_search);
        mSearchEdittext.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mLocationEdittext.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mTitleTextView.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mCurrentLocationButton.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mOnTheWebButton.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        mSearchEdittext.setText(mBusinessName);
        mLocationEdittext.setText(mLocationName);

        mTopLayout.getLayoutParams().height = (int)(height);

        mBusinessVenueList = new ArrayList<BusinessVenue>();

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mDragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mHeaderLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mListLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(MapBusinessDirectoryActivity.this, "Info",
                        "Business directory info");
            }
        });

        mCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationEdittext.setText("Current Location");
            }
        });

        mOnTheWebButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationEdittext.setText("On the Web");
            }
        });


        mBusinessSearchAdapter = new BusinessSearchAdapter(MapBusinessDirectoryActivity.this,
                mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mSearchEdittext.setHint("Category or Business Name");
                    mLocationEdittext.setVisibility(View.VISIBLE);
                    mCurrentLocationButton.setVisibility(View.GONE);
                    mOnTheWebButton.setVisibility(View.GONE);
                    mCurrentLayoutSeparator.setVisibility(View.GONE);
                    mOnTheWebSeparator.setVisibility(View.GONE);

                    mMainContentLayout.setVisibility(View.GONE);
                    mPaddingLayout.setVisibility(View.GONE);
                    mMapLayout.setVisibility(View.GONE);

                    mSearchListView.setAdapter(mBusinessSearchAdapter);

                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                    new BusinessAutoCompleteAsynctask(getCachedBusinessSearchData())
                            .execute(((EditText) view).getText().toString(), mLocationWords, latLong);
                }
            }
        });


        mSearchEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                int keyAction = keyEvent.getAction();
                if (keyAction == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.FLAG_EDITOR_ACTION:
                            mLocationEdittext.requestFocus();
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            mLocationEdittext.requestFocus();
                            return true;
                        default:
                            return false;
                    }
                }

                return false;
            }

        });


        mSearchEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mSearchEdittext.onTextChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() > 0) {
                    mSearchListView.setAdapter(mBusinessSearchAdapter);
                    mMainContentLayout.setVisibility(View.GONE);
                    mPaddingLayout.setVisibility(View.GONE);
                    mMapLayout.setVisibility(View.GONE);
                    mSearchListView.setVisibility(View.GONE);
                    mCurrentLocationButton.setVisibility(View.GONE);
                    mOnTheWebButton.setVisibility(View.GONE);
                    mCurrentLayoutSeparator.setVisibility(View.GONE);
                    mOnTheWebSeparator.setVisibility(View.GONE);
                    mLocationEdittext.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.VISIBLE);
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += ","+String.valueOf(getLonFromSharedPreference());

                    try {
                        new BusinessAutoCompleteAsynctask(null).execute(editable.toString(),
                                mLocationWords, latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    mDummyFocusLayout.requestFocus();
                    if(mSearchEdittext.getText().toString().length()<=0){
                        mLocationEdittext.setVisibility(View.GONE);
                    }

                    mMainContentLayout.setVisibility(View.VISIBLE);
                    mPaddingLayout.setVisibility(View.VISIBLE);
                    mMapLayout.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.GONE);
                    mCurrentLocationButton.setVisibility(View.GONE);
                    mOnTheWebButton.setVisibility(View.GONE);
                    mCurrentLayoutSeparator.setVisibility(View.GONE);
                    mOnTheWebSeparator.setVisibility(View.GONE);
                }
            }
        });

        mLocationAdapter = new LocationAdapter(MapBusinessDirectoryActivity.this, mLocation);
        mSearchListView.setAdapter(mLocationAdapter);

        mLocationEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mSearchEdittext.setHint("Search");
                    mCurrentLocationButton.setVisibility(View.VISIBLE);
                    mOnTheWebButton.setVisibility(View.VISIBLE);
                    mCurrentLayoutSeparator.setVisibility(View.VISIBLE);
                    mOnTheWebSeparator.setVisibility(View.VISIBLE);
                    mSearchListView.setAdapter(mLocationAdapter);
                    mMainContentLayout.setVisibility(View.GONE);
                    mPaddingLayout.setVisibility(View.GONE);
                    mMapLayout.setVisibility(View.GONE);
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                    new LocationAutoCompleteAsynctask(getCachedLocationSearchData())
                            .execute(((EditText) view).getText().toString(), latLong);
                }

            }
        });

        mLocationEdittext.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                int keyAction = keyEvent.getAction();
                mIntent = new Intent(MapBusinessDirectoryActivity.this,
                        MapBusinessDirectoryActivity.class);
                if (keyAction == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.FLAG_EDITOR_ACTION:
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESS,
                                    mSearchEdittext.getText().toString());
                            mIntent.putExtra(BusinessDirectoryActivity.LOCATION,
                                    mLocationEdittext.getText().toString());
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESSTYPE, mBusinessType);
                            startActivity(mIntent);
                            finish();
                            return true;
                        case KeyEvent.KEYCODE_ENTER:
                            mIntent.putExtra(BusinessDirectoryActivity.BUSINESS,
                                    mSearchEdittext.getText().toString());
                            mIntent.putExtra(BusinessDirectoryActivity.LOCATION,
                                    mLocationEdittext.getText().toString());
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

        });


        mLocationEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mLocationEdittext.onTextChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.toString().length() > 0) {
                    mSearchListView.setAdapter(mLocationAdapter);
                    mSearchListView.setVisibility(View.VISIBLE);
                        mLocationWords = editable.toString();
                        String latLong = String.valueOf(getLatFromSharedPreference());
                        latLong += ","+String.valueOf(getLonFromSharedPreference());

                        try {
                            new LocationAutoCompleteAsynctask(null).execute(mLocationWords, latLong);
                            mMainContentLayout.setVisibility(View.GONE);
                            mPaddingLayout.setVisibility(View.GONE);
                            mMapLayout.setVisibility(View.GONE);
                            mCurrentLocationButton.setVisibility(View.VISIBLE);
                            mOnTheWebButton.setVisibility(View.VISIBLE);
                            mCurrentLayoutSeparator.setVisibility(View.VISIBLE);
                            mOnTheWebSeparator.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }
                else{
                    if(mLocationEdittext.getText().toString().length()<=0){
                        mDummyFocusLayout.requestFocus();
                        mMainContentLayout.setVisibility(View.VISIBLE);
                        mPaddingLayout.setVisibility(View.VISIBLE);
                        mMapLayout.setVisibility(View.VISIBLE);
                        mSearchListView.setVisibility(View.GONE);
                        mCurrentLocationButton.setVisibility(View.GONE);
                        mOnTheWebButton.setVisibility(View.GONE);
                        mCurrentLayoutSeparator.setVisibility(View.GONE);
                        mOnTheWebSeparator.setVisibility(View.GONE);
                    }
                }
            }
        });

        initializeMap();
        initializeMarker();

        int timeout = 15000;
        if(mLocationName.equalsIgnoreCase("Current Location")){
            mGetVenuesByLatLongTask = new GetVenuesByLatLongTask(this);
            String latlong = ""+getLatFromSharedPreference()+","+getLonFromSharedPreference();
            if(mBusinessType.equalsIgnoreCase("business")){

                mGetVenuesByLatLongTask.execute(latlong,mBusinessName,"");
            } else {

                mGetVenuesByLatLongTask.execute(latlong,"",mBusinessName);
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGetVenuesByLatLongTask.getStatus() == AsyncTask.Status.RUNNING)
                        mGetVenuesByLatLongTask.cancel(true);
                }
            }, timeout);
        } else {
            mGetVenuesByBusinessAndLocation = new GetVenuesByBusinessAndLocation(this);
            mGetVenuesByBusinessAndLocation.execute(mBusinessName, mLocationName, mBusinessType);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGetVenuesByBusinessAndLocation.getStatus() == AsyncTask.Status.RUNNING)
                        mGetVenuesByBusinessAndLocation.cancel(true);
                }
            }, timeout);
        }

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if(mSearchEdittext.isFocused()){
                    BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter)mSearchListView.getAdapter();
                    mSearchEdittext.setText(businessSearchAdapter.getItemValue(position));
                    mBusinessType = businessSearchAdapter.getItem(position).getType();
                    writeCachedBusinessSearchData(businessSearchAdapter.getItem(position));
                }
                else if(mLocationEdittext.isFocused()){
                    LocationAdapter locationAdapter = (LocationAdapter)mSearchListView.getAdapter();
                    mLocationEdittext.setText(locationAdapter.getItem(position));
                    writeCachedLocationSearchData(mSearchListView.getAdapter().getItem(position).toString());
                }

                mCurrentLocationButton.setVisibility(View.GONE);
                mOnTheWebButton.setVisibility(View.GONE);
                mCurrentLayoutSeparator.setVisibility(View.GONE);
                mOnTheWebSeparator.setVisibility(View.GONE);
                mSearchListView.setVisibility(View.GONE);
            }
        });

        final ViewTreeObserver observer= mFrameLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mDragBarThreshold = (mFrameLayout.getHeight() * 2) - 70;
                    }
                });

        mDragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                String DEBUG_TAG = "TEST MOVE";
                int action = MotionEventCompat.getActionMasked(event);
                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        Log.d(TAG, "action down");
                        // Save the ID of this pointer
                        mActivePointerId = event.getPointerId(0);
                        final float y = event.getY(mActivePointerId);

                        aLastTouchY = y;
                        if (aPosY == 0){
                            aPosY = mTopLayout.getLayoutParams().height;
                        }
                        return true;

                    case (MotionEvent.ACTION_MOVE) :
                        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) mTopLayout.getLayoutParams();
                        final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                        Log.d(TAG, "action move");
                        float yMove = event.getY(pointerIndexMove);

                        final float dy = yMove - aLastTouchY;

                        aPosY += dy;

                        if(aPosY>mDragBarThreshold){
                            aPosY = mDragBarThreshold;
                        }

                        param.height = (int) (aPosY);
                        mTopLayout.setLayoutParams(param);
                        return true;
                    default :
                        return true;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(mMainContentLayout.getVisibility()==View.GONE){
            mDummyFocusLayout.requestFocus();
            mMainContentLayout.setVisibility(View.VISIBLE);
            mMapLayout.setVisibility(View.VISIBLE);
            mLocationEdittext.setVisibility(View.GONE);
            mSearchListView.setVisibility(View.GONE);

            mCurrentLocationButton.setVisibility(View.GONE);
            mOnTheWebButton.setVisibility(View.GONE);
            mCurrentLayoutSeparator.setVisibility(View.GONE);
            mOnTheWebSeparator.setVisibility(View.GONE);
        } else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    private void initializeMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mGoogleMap == null) {
                Toast.makeText(getApplicationContext(), "Sorry! unable to create maps",
                        Toast.LENGTH_SHORT).show();
            }
            mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    mCurrentLocation = location;
                    drawCurrentLocationMarker(location);
                }
            });
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            Criteria cri= new Criteria();
            String provider = locationManager.getBestProvider(cri, true);
            mCurrentLocation = locationManager.getLastKnownLocation(provider);
            LatLng currentLatLng;

            if(mCurrentLocation!=null){
                currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude());
            } else {
                mCurrentLocation = new Location("dummyProvider");
                mCurrentLocation.setLatitude(getLatFromSharedPreference());
                mCurrentLocation.setLongitude(getLonFromSharedPreference());
                currentLatLng = new LatLng(getLatFromSharedPreference(),getLonFromSharedPreference());
            }
            try{
                mCurrentLocation = mGoogleMap.getMyLocation();
                Log.d("TAG LOC","CUR LOC: "+mCurrentLocation.getLatitude()+"; "
                        +mCurrentLocation.getLongitude());

                currentLatLng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            } catch (Exception e){

            }

            mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            MapInfoWindowAdapter customInfoWindowAdapter =
                    new MapInfoWindowAdapter(MapBusinessDirectoryActivity.this);
            mGoogleMap.setInfoWindowAdapter(customInfoWindowAdapter);

            mLocateMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG LOC","CUR LOC: "+mCurrentLocation.getLatitude()+"; "
                            +mCurrentLocation.getLongitude());

                    LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude());

                    drawCurrentLocationMarker(currentLatLng);
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                    mUserLocationMarker.showInfoWindow();
                }
            });

            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(marker.getTitle().equalsIgnoreCase(YOUR_LOCATION)){

                    } else {
                        Intent intent = new Intent(MapBusinessDirectoryActivity.this,
                                DirectoryDetailActivity.class);
                        int id = mMarkerId.get(marker);
                        String distance = mMarkerDistances.get(marker);
                        intent.putExtra("bizId",""+id);
                        intent.putExtra("bizName",marker.getTitle());
                        intent.putExtra("bizDistance",distance);
                        startActivity(intent);
                    }
                }
            });

            mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {

                    if(mCameraChangeListenerEnabled){
                        LatLngBounds latLngBounds = mGoogleMap.getProjection().getVisibleRegion().latLngBounds;
                        LatLng southWestLatLng = latLngBounds.southwest;
                        LatLng northEastLatLng = latLngBounds.northeast;
                        String southWest = ""+southWestLatLng.latitude+"%2C"+southWestLatLng.longitude;
                        String northEast = ""+northEastLatLng.latitude+"%2C"+northEastLatLng.longitude;
                        String bound = southWest+"%7C"+northEast;
                        String userLatLong = ""+mCurrentLocation.getLatitude()
                                +","+mCurrentLocation.getLongitude();

                        GetVenuesByBoundTask getVenuesByBoundTask =
                                new GetVenuesByBoundTask(MapBusinessDirectoryActivity.this);
                        getVenuesByBoundTask.execute(bound,mBusinessName, userLatLong);
                    } else {
                        mCameraChangeListenerEnabled = true;
                    }

                }
            });

            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mCameraChangeListenerEnabled = false;
                    marker.showInfoWindow();
                    return true;
                }
            });
            mGoogleMap.setMyLocationEnabled(false);
        }
    }

    private void drawCurrentLocationMarker(Location location){
        if(mUserLocationMarker!=null){
            mUserLocationMarker.remove();
        }
        LatLng currentPosition;
        // added default location to prevent breaking
        if(location != null){
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }else{
            currentPosition = new LatLng(getLatFromSharedPreference(),getLonFromSharedPreference());
        }
        mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .title(YOUR_LOCATION)
                .snippet("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
        );
    }
    private void drawCurrentLocationMarker(LatLng location){
        if(mUserLocationMarker!=null){
            mUserLocationMarker.remove();
        }
        if(location==null){
            location = new LatLng(getLatFromSharedPreference(),getLonFromSharedPreference());
        }
        mUserLocationMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(YOUR_LOCATION)
                .snippet("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_your_loc))
        );
    }

    protected void initializeMarker(){

        mMarkersLatLngList = new ArrayList<LatLng>();
        if(mBusinessVenueList.size()>0){

            LatLng currentLatLng = new LatLng(0,0);
            for(BusinessVenue businessVenue:mBusinessVenueList){
                mMarkersLatLngList.add(businessVenue.getLocation());
                currentLatLng = businessVenue.getLocation();
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(businessVenue.getLocation())
                        .title(businessVenue.getName())
                        .snippet(businessVenue.getAddress())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                ).showInfoWindow();
            }
        }

        drawCurrentLocationMarker(mCurrentLocation);
        mUserLocationMarker.showInfoWindow();

        zoomToContainAllMarkers();
    }

    private void zoomToContainAllMarkers() {

        if(mMarkersLatLngList.size()>0){

            LatLngBounds.Builder bc = new LatLngBounds.Builder();

            for (LatLng item : mMarkersLatLngList) {
                bc.include(item);
            }

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50));
        }
    }


    protected void initializeMarkerWithBusinessSearchResult(){

        mMarkersLatLngList = new ArrayList<LatLng>();
        Marker firstMarker = null;
        mMarkerId.put(null,0);
        mMarkerDistances.put(null,"");
        getMarkerImageLink().put(null, "");
        if(!mMarkerId.isEmpty()){
            mMarkerId.clear();
            mMarkerImageLink.clear();
            mMarkerDistances.clear();
        }
        if(mVenues.size()>0){

            LatLng currentLatLng = new LatLng(0,0);
            boolean first = true;
            for(BusinessSearchResult businessSearchResult:mVenues){

                LatLng locationLatLng = new LatLng(businessSearchResult.getLocationObject().getLatitude(),
                        businessSearchResult.getLocationObject().getLongitude());
                mMarkersLatLngList.add(locationLatLng);

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(locationLatLng)
                        .title(businessSearchResult.getName())
                        .snippet(businessSearchResult.getAddress())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                );

                if(first){
                    first = false;
                    currentLatLng = locationLatLng;
                    firstMarker = marker;
                }
                mMarkerId.put(marker,Integer.parseInt(businessSearchResult.getId()));
                mMarkerDistances.put(marker,businessSearchResult.getDistance());
                mMarkerImageLink.put(marker, businessSearchResult.getProfileImage().getImageThumbnail());
            }

            zoomToContainAllMarkers();

            if(firstMarker!=null){
                firstMarker.showInfoWindow();
            } else {
                drawCurrentLocationMarker(mCurrentLocation);
                mUserLocationMarker.showInfoWindow();
            }
        } else {
            drawCurrentLocationMarker(mCurrentLocation);
            mUserLocationMarker.showInfoWindow();
        }

    }

    protected void initializeMarkerWithBoundSearchResult(){

        Marker firstMarker = null;
        mMarkerId.put(null, 0);
        mMarkerDistances.put(null,"");
        getMarkerImageLink().put(null, "");
        if(!mMarkerId.isEmpty()){
            mMarkerId.clear();
            mMarkerImageLink.clear();
            mMarkerDistances.clear();
        }
        if(mVenues.size()>0){
            LatLng currentLatLng = new LatLng(0,0);
            boolean first = true;
            for(BusinessSearchResult businessSearchResult:mVenues){
                LatLng locationLatLng = new LatLng(businessSearchResult.getLocationObject().getLatitude(),
                        businessSearchResult.getLocationObject().getLongitude());

                Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(locationLatLng)
                        .title(businessSearchResult.getName())
                        .snippet(businessSearchResult.getAddress())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_bitcoin_loc))
                );

                if(first){
                    first = false;
                    currentLatLng = locationLatLng;
                    firstMarker = marker;
                }
                mMarkerId.put(marker,Integer.parseInt(businessSearchResult.getId()));
                mMarkerDistances.put(marker,businessSearchResult.getDistance());
                mMarkerImageLink.put(marker, businessSearchResult.getProfileImage().getImageThumbnail());
            }

            if(firstMarker==null){
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

    class LocationAutoCompleteAsynctask extends AsyncTask<String, Integer, List<String>>{

        private List<String> mCacheData = null;
        private AirbitzAPI api = AirbitzAPI.getApi();

        public LocationAutoCompleteAsynctask(List<String> cacheData){
            mCacheData = cacheData;
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            List<String> jsonParsingResult = api.getHttpAutoCompleteLocation(strings[0], strings[1]);
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(List<String> strings) {

            mLocation.clear();
            if(strings==null){
                mLocation.add("Result not found");
            } else {
                if(mCacheData != null){
                    mLocation.addAll(mCacheData);
                }
                mLocation.addAll(RemoveDoubleLocationName(strings, mCacheData));
            }
            mLocationAdapter.notifyDataSetChanged();
            ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
            mSearchListView.setVisibility(View.VISIBLE);
        }
    }

    class BusinessAutoCompleteAsynctask extends AsyncTask<String, Integer, List<Business>>{

        private AirbitzAPI api = AirbitzAPI.getApi();
        private List<Business> mCacheData = null;

        public BusinessAutoCompleteAsynctask(List<Business> cacheData){
            mCacheData = cacheData;
        }

        @Override
        protected List<Business> doInBackground(String... strings) {
            List<Business> jsonParsingResult = api.getHttpAutoCompleteBusiness(strings[0],
                    strings[1], strings[2]);
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(List<Business> businesses) {
            mBusinessList.clear();
            if(businesses==null){
                mBusinessList.add(new Business("Result not found","",""));
            }else{
                if(mCacheData != null){
                    mBusinessList.addAll(mCacheData);
                }
                mBusinessList.addAll(RemoveDoubleBusinessName(businesses, mCacheData));
            }
            mBusinessSearchAdapter.notifyDataSetChanged();
            ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
            mSearchListView.setVisibility(View.VISIBLE);
        }
    }

    public List<String> RemoveDoubleLocationName(List<String> apiData, List<String> cacheData){

        try{
            int apiDataLength = apiData.size();
            int cacheDataLength = cacheData.size();
            List<String> resultData = new ArrayList<String>();

            boolean fullyDifferent = true;

            for(int i=0; i<apiDataLength; i++){
                for(int j=0; j<cacheDataLength; j++){
                    if(!apiData.get(i).equalsIgnoreCase(cacheData.get(j))){
                        fullyDifferent = true;
                    }
                    else{
                        fullyDifferent = false;
                        break;
                    }
                }
                if(fullyDifferent){
                    resultData.add(apiData.get(i));
                }
            }

            return resultData;
        }
        catch (Exception e){
            return apiData;
        }
    }


    public List<Business> RemoveDoubleBusinessName(List<Business> apiData, List<Business> cacheData){
        try{
            int apiDataLength = apiData.size();
            int cacheDataLength = cacheData.size();
            List<Business> resultData = null;
            boolean fullyDifferent = true;

            for(int i=0; i<apiDataLength; i++){
                for(int j=0; j<cacheDataLength; j++){
                    if(!apiData.get(i).getName().equalsIgnoreCase(cacheData.get(j).getName())){
                        fullyDifferent = true;
                    }
                    else{
                        fullyDifferent = false;
                        break;
                    }
                }
                if(fullyDifferent){
                    resultData.add(apiData.get(i));
                }
            }
            return resultData;
        }
        catch (Exception e){
            return apiData;
        }
    }

    private class GetVenuesByBoundTask extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBoundTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... params) {
            if(mBusinessType.equalsIgnoreCase("category")){

                return mApi.getSearchByBoundsAndBusiness(params[0], "",params[1],params[2],"", "", "");
            } else {

                return mApi.getSearchByBoundsAndBusiness(params[0],params[1],"",params[2], "", "", "");
            }
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                if(isNewVenuesAdded(results.getBusinessSearchObjectArray())){
                    mVenues = results.getBusinessSearchObjectArray();
                    mGoogleMap.clear();
                    initializeMarkerWithBoundSearchResult();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private boolean isNewVenuesAdded(List<BusinessSearchResult> newVenues){
        boolean result = false;
        int iterator = 0;
        while(!result&&iterator<=newVenues.size()){
            if(!mVenues.contains(newVenues.get(iterator))){
                result = true;
            }
            iterator++;
        }

        return result;
    }

    private class GetVenuesByLocationTask extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByLocationTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog  = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... params) {
            return mApi.getSearchByLocation(params[0],"","","");
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));
                mVenues = results.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            }catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }

    private class GetVenuesByBusinessTask extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBusinessTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog  = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return mApi.getSearchByTerm(params[0], "", "", "");
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try{
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            }catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }


            mProgressDialog.dismiss();
        }
    }

    private class GetVenuesByCategoryTask extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByCategoryTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog  = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return mApi.getSearchByCategory(params[0], "", "", "");
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try{
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            }catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }
    }

    private class GetVenuesByLatLongTask extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByLatLongTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog  = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return mApi.getSearchByLatLongAndBusiness(params[0],params[1],params[2], "", "", "");
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try{
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            }catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }


            mProgressDialog.dismiss();
        }
    }


    private class GetVenuesByBusinessAndLocation extends AsyncTask<String, Void, String>{

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetVenuesByBusinessAndLocation(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog  = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venues list...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String latLong = String.valueOf(getLatFromSharedPreference())
                    + ","+String.valueOf(getLonFromSharedPreference());
            return mApi.getSearchByCategoryOrBusinessAndLocation(params[0], params[1], "", "", "1",
                    params[2],latLong);
        }

        @Override
        protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                    Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            try{
                SearchResult result = new SearchResult(new JSONObject(searchResult));
                mVenues = result.getBusinessSearchObjectArray();
                mGoogleMap.clear();
                initializeMarkerWithBusinessSearchResult();
            }catch (JSONException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
            mProgressDialog.dismiss();
        }
    }


    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.PREF_NAME, MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference(){
        double lat = (double)getStateFromSharedPreferences(BusinessDirectoryActivity.LAT_KEY);
        return lat;
    }


    private double getLonFromSharedPreference(){
        double lon = (double)getStateFromSharedPreferences(BusinessDirectoryActivity.LON_KEY);
        return lon;
    }


    public void writeCachedLocationSearchData(String recentData){
        SharedPreferences cachePref = null;

        cachePref = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_LOCATIONSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();


        if(cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, null) == null){
            editor.putString(BusinessDirectoryActivity.LOC1_KEY, recentData);
        }
        else{
            if(!cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, null).equalsIgnoreCase(recentData)){

                editor.putString(BusinessDirectoryActivity.LOC2_KEY,
                        cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, ""));
                editor.putString(BusinessDirectoryActivity.LOC3_KEY,
                        cachePref.getString(BusinessDirectoryActivity.LOC2_KEY, null));
                editor.putString(BusinessDirectoryActivity.LOC4_KEY,
                        cachePref.getString(BusinessDirectoryActivity.LOC3_KEY, null));
                editor.putString(BusinessDirectoryActivity.LOC5_KEY,
                        cachePref.getString(BusinessDirectoryActivity.LOC4_KEY, null));
                editor.putString(BusinessDirectoryActivity.LOC1_KEY, recentData);
            }
        }

        editor.commit();

    }

    public List<String> getCachedLocationSearchData(){
        SharedPreferences cachePref = null;
        List<String> listRecentLocation = new ArrayList<String>();

        cachePref = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_LOCATIONSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        if((cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, null) == null) &&
                (cachePref.getString(BusinessDirectoryActivity.LOC2_KEY, null) == null) &&
                (cachePref.getString(BusinessDirectoryActivity.LOC3_KEY, null) == null) &&
                (cachePref.getString(BusinessDirectoryActivity.LOC4_KEY, null) == null) &&
                (cachePref.getString(BusinessDirectoryActivity.LOC5_KEY, null) == null)){
            return null;
        }
        else{
            if(cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, null) != null)
                listRecentLocation.add(cachePref.getString(BusinessDirectoryActivity.LOC1_KEY, null));

            if(cachePref.getString(BusinessDirectoryActivity.LOC2_KEY, null) != null)
                listRecentLocation.add(cachePref.getString(BusinessDirectoryActivity.LOC2_KEY, null));

            if(cachePref.getString(BusinessDirectoryActivity.LOC3_KEY, null) != null)
                listRecentLocation.add(cachePref.getString(BusinessDirectoryActivity.LOC3_KEY, null));

            if(cachePref.getString(BusinessDirectoryActivity.LOC4_KEY, null) != null)
                listRecentLocation.add(cachePref.getString(BusinessDirectoryActivity.LOC4_KEY, null));

            if(cachePref.getString(BusinessDirectoryActivity.LOC5_KEY, null) != null)
                listRecentLocation.add(cachePref.getString(BusinessDirectoryActivity.LOC5_KEY, null));


            return listRecentLocation;
        }
    }


    public void writeCachedBusinessSearchData(Business recentData){
        SharedPreferences cachePref = null;

        cachePref = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_BUSINESSSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        String name = recentData.getName();
        String type = recentData.getType();
        String id = recentData.getId();

        if(cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY, null) == null){
            editor.putString(BusinessDirectoryActivity.BIZ1_NAME_KEY, name);
            editor.putString(BusinessDirectoryActivity.BIZ1_TYPE_KEY, type);
            editor.putString(BusinessDirectoryActivity.BIZ1_ID_KEY, id);
        }
        else{

            if(!cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY, null).equalsIgnoreCase(name)){
                editor.putString(BusinessDirectoryActivity.BIZ2_NAME_KEY,
                        cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY, ""));
                editor.putString(BusinessDirectoryActivity.BIZ1_NAME_KEY, name);

                editor.putString(BusinessDirectoryActivity.BIZ2_TYPE_KEY,
                        cachePref.getString(BusinessDirectoryActivity.BIZ1_TYPE_KEY, ""));
                editor.putString(BusinessDirectoryActivity.BIZ1_TYPE_KEY, type);

                editor.putString(BusinessDirectoryActivity.BIZ2_ID_KEY,
                        cachePref.getString(BusinessDirectoryActivity.BIZ1_ID_KEY, ""));
                editor.putString(BusinessDirectoryActivity.BIZ1_ID_KEY, name);
            }

        }

        editor.commit();

    }

    public List<Business> getCachedBusinessSearchData(){
        SharedPreferences cachePref = null;
        List<Business> listRecentBusiness = new ArrayList<Business>();

        cachePref = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_BUSINESSSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = cachePref.edit();

        if((cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY, null) == null)
                && (cachePref.getString(BusinessDirectoryActivity.BIZ2_NAME_KEY, null) == null)){
            return null;
        }
        else{
            if(cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY, null) != null)
                listRecentBusiness.add(new Business(cachePref.getString(BusinessDirectoryActivity.BIZ1_NAME_KEY,
                        null),
                        cachePref.getString(BusinessDirectoryActivity.BIZ1_TYPE_KEY, null),
                        cachePref.getString(BusinessDirectoryActivity.BIZ1_ID_KEY, null)));

            if(cachePref.getString(BusinessDirectoryActivity.BIZ2_NAME_KEY, null) != null)
                listRecentBusiness.add(new Business(cachePref.getString(BusinessDirectoryActivity.BIZ2_NAME_KEY,
                        null),
                        cachePref.getString(BusinessDirectoryActivity.BIZ2_TYPE_KEY, null),
                        cachePref.getString(BusinessDirectoryActivity.BIZ2_ID_KEY, null)));

            return listRecentBusiness;
        }
    }


    private void clearCacheSharedPreference() {
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_BUSINESSSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(BusinessDirectoryActivity.BIZ1_NAME_KEY);
        editor.remove(BusinessDirectoryActivity.BIZ1_TYPE_KEY);
        editor.remove(BusinessDirectoryActivity.BIZ1_ID_KEY);
        editor.remove(BusinessDirectoryActivity.BIZ2_NAME_KEY);
        editor.remove(BusinessDirectoryActivity.BIZ2_TYPE_KEY);
        editor.remove(BusinessDirectoryActivity.BIZ2_ID_KEY);

        editor.commit();

        SharedPreferences prefLocation = getSharedPreferences(BusinessDirectoryActivity.MOSTRECENT_LOCATIONSEARCH_SHARED_PREF,
                MODE_PRIVATE);
        editor = prefLocation.edit();

        editor.remove(BusinessDirectoryActivity.LOC2_KEY);
        editor.remove(BusinessDirectoryActivity.LOC3_KEY);
        editor.remove(BusinessDirectoryActivity.LOC4_KEY);
        editor.remove(BusinessDirectoryActivity.LOC5_KEY);
        editor.remove(BusinessDirectoryActivity.LOC1_KEY);

        editor.commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
        if(start != null & finish != null){

            float yDistance = Math.abs(finish.getY() - start.getY());

            if((finish.getRawX()>start.getRawX()) && (yDistance < 10)){
                float xDistance = Math.abs(finish.getRawX() - start.getRawX());

                if(xDistance > 100){
                    finish();
                    return true;
                }
            }

        }
        return false;
    }
}
