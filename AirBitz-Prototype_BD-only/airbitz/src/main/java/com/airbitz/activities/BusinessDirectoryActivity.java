
package com.airbitz.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ObservableScrollView;
import com.airbitz.utils.CacheUtil;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/10/14.
 */
public class BusinessDirectoryActivity extends Activity implements
                                                       GestureDetector.OnGestureListener,
                                                       ObservableScrollView.ScrollViewListener {

    public static final String LAT_KEY = "LAT_KEY";
    public static final String LON_KEY = "LON_KEY";
    public static final String PREF_NAME = "PREF_NAME";
    public static final String LOCATION_CACHE_SHARED_PREF = "LOCATION_CACHE_PREF";
    public static final String BUSINESS_CACHE_SHARED_PREF = "BUSINESS_CACHE_PREF";

    private Categories mCategories;

    private ClearableEditText mSearchField;
    private ClearableEditText mLocationField;
    private ListView mSearchListView;
    private TextView mTitleTextView;

    private TextView mNearYouTextView;

    private LinearLayout mBusinessLayout;
    private LinearLayout mNearYouContainer;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private Button mRestaurantButton;
    private Button mBarButton;
    private Button mCoffeeButton;
    private Button mMoreButton;

    private RelativeLayout mParentLayout;

    private LinearLayout mVenueFragmentLayout;

    private LinearLayout mDummyFocusLayout;

    private Spinner mMoreSpinner;

    private LocationManager mLocationManager;

    private ObservableScrollView mScrollView;

    private static String mLocationWords = "";
    private static String mBusinessType = "business";

    private Intent mIntent;

    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private LocationAdapter mLocationAdapter;

    public final static String CATEGORY = "CATEGORY";
    public final static String LOCATION = "LOCATION";
    public final static String BUSINESS = "BUSINESS";
    public final static String BUSINESSTYPE = "BUSINESSTYPE";

    private ArrayList<LocationSearchResult> mLocationList;
    private ArrayList<Business> mBusinessList;

    private String mNextUrl = "null";

    private MoreCategoryAdapter mMoreCategoryAdapter;

    private AirbitzAPI api = AirbitzAPI.getApi();

    private BusinessCategoryAsyncTask mBusinessCategoryAsynctask;
    private boolean mFirstLoad = true;

    private Location mCurrentLocation;

    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;

    private ProgressDialog mProgressDialog;
    private ProgressDialog mMoreCategoriesProgressDialog;
    private boolean mIsMoreCategoriesProgressRunning = false;

    private GestureDetector mGestureDetector;

    private BusinessScrollListener mBusinessScrollListener;

    public interface BusinessScrollListener {
        void onScrollEnded();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationManager();

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);

        setContentView(R.layout.activity_business_directory);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        montserratBoldTypeFace = Typeface.createFromAsset(getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getAssets(), "font/Lato-RegIta.ttf");
        helveticaNeueTypeFace = Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");

        mBusinessList = new ArrayList<Business>();
        mLocationList = new ArrayList<LocationSearchResult>();

        Log.d("TAG_LOC", "CUR LOC: ");

        mRestaurantButton = (Button) findViewById(R.id.button_restaurant);
        mBarButton = (Button) findViewById(R.id.button_bar);
        mCoffeeButton = (Button) findViewById(R.id.button_coffee_tea);
        mMoreButton = (Button) findViewById(R.id.button_more);
        mMoreButton.setClickable(false);

        mDummyFocusLayout = (LinearLayout) findViewById(R.id.dummy_focus);

        mNearYouContainer = (LinearLayout) findViewById(R.id.layout_near_you);
        mVenueFragmentLayout = (LinearLayout) findViewById(R.id.fragment_layout);

        mScrollView = (ObservableScrollView) findViewById(R.id.scroll_view);
        mScrollView.setScrollViewListener(this);

        mMoreSpinner = (Spinner) findViewById(R.id.spinner_more_categories);
        mMoreSpinner.setAdapter(mMoreCategoryAdapter);

        mBusinessLayout = (LinearLayout) findViewById(R.id.layout_listview_business);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);
        mSearchField = (ClearableEditText) findViewById(R.id.edittext_search);
        mLocationField = (ClearableEditText) findViewById(R.id.edittext_location);
        mSearchListView = (ListView) findViewById(R.id.listview_search);
        mTitleTextView = (TextView) findViewById(R.id.textview_title);

        mNearYouTextView = (TextView) findViewById(R.id.textview_nearyou);
        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);

        mTitleTextView.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        mSearchField.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mLocationField.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);

        mRestaurantButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mBarButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mCoffeeButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mMoreButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mNearYouTextView.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);

        mBusinessCategoryAsynctask = new BusinessCategoryAsyncTask();
        mMoreCategoriesProgressDialog = new ProgressDialog(BusinessDirectoryActivity.this);

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mIsMoreCategoriesProgressRunning = true;
                mMoreCategoriesProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mMoreCategoriesProgressDialog.setMessage("Retrieving data...");
                mMoreCategoriesProgressDialog.setIndeterminate(true);
                mMoreCategoriesProgressDialog.setCancelable(false);
                mMoreCategoriesProgressDialog.show();
            }
        });

        try {
            mBusinessCategoryAsynctask.execute("level");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int timeout = 15000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override public void run() {
                if (mIsMoreCategoriesProgressRunning) {
                    mMoreCategoriesProgressDialog.dismiss();
                }
                // mMoreButton.setOnClickListener(new View.OnClickListener() {
                // @Override public void onClick(View view) {
                // Toast.makeText(getApplicationContext(),
                // "No categories retrieved from server",
                // Toast.LENGTH_LONG).show();
                // }
                // });
            }
        }, timeout);

        mMoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                if (mFirstLoad) {
                    mFirstLoad = false;
                }
                else {

                    Intent intent = new Intent(BusinessDirectoryActivity.this,
                                               MapBusinessDirectoryActivity.class);
                    intent.putExtra(BUSINESS, mMoreCategoryAdapter.getListItemName(position)
                                                                  .getCategoryName());
                    intent.putExtra(LOCATION, "");
                    intent.putExtra(BUSINESSTYPE, "category");
                    startActivity(intent);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mIntent = new Intent(BusinessDirectoryActivity.this, MapBusinessDirectoryActivity.class);
                mIntent.putExtra(BUSINESS, ((Button) view).getText().toString());
                mIntent.putExtra(LOCATION, "");
                mIntent.putExtra(BUSINESSTYPE, "category");
                startActivity(mIntent);
            }
        });

        mBarButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mIntent = new Intent(BusinessDirectoryActivity.this, MapBusinessDirectoryActivity.class);
                mIntent.putExtra(BUSINESS, ((Button) view).getText().toString());
                mIntent.putExtra(LOCATION, "");
                mIntent.putExtra(BUSINESSTYPE, "category");
                startActivity(mIntent);
            }
        });

        mCoffeeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mIntent = new Intent(BusinessDirectoryActivity.this, MapBusinessDirectoryActivity.class);
                mIntent.putExtra(BUSINESS, ((Button) view).getText().toString());
                mIntent.putExtra(LOCATION, "");
                mIntent.putExtra(BUSINESSTYPE, "category");
                startActivity(mIntent);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                finish();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Common.showHelpInfoDialog(BusinessDirectoryActivity.this, "Info", "Business directory info");
            }
        });

        mBusinessSearchAdapter = new BusinessSearchAdapter(BusinessDirectoryActivity.this, mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {

                    mSearchListView.setAdapter(mBusinessSearchAdapter);
                    mBusinessLayout.setVisibility(View.GONE);
                    mNearYouContainer.setVisibility(View.GONE);
                    mVenueFragmentLayout.setVisibility(View.GONE);
                    mLocationField.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.VISIBLE);

                    // mBusinessList.clear();
                    // mBusinessSearchAdapter.notifyDataSetChanged();

                    // if (getCachedBusinessSearchData() != null) {
                    // // mBusinessList.clear();
                    // final List<Business> cachedBusinesses =
                    // getCachedBusinessSearchData();
                    // for (Business business : cachedBusinesses) {
                    // if (!mBusinessList.contains(business)) {
                    // mBusinessList.add(business);
                    // }
                    // }
                    // mBusinessSearchAdapter.notifyDataSetChanged();
                    // ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
                    // }

                    // Start search
                    try {
                        final String text = mSearchField.getText().toString();
                        final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                                ? null
                                : CacheUtil.getCachedBusinessSearchData(BusinessDirectoryActivity.this));
                        String latLong = String.valueOf(getLatFromSharedPreference());
                        latLong += "," + String.valueOf(getLonFromSharedPreference());
                        new BusinessAutoCompleteAsynctask(cachedBusiness).execute(mSearchField.getText()
                                                                                              .toString(),
                                                                                  mLocationWords,
                                                                                  latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mSearchField.onTextChanged();
            }

            @Override public void afterTextChanged(Editable editable) {

                // if (editable.toString().length() > 0) {

                if (mSearchListView.getVisibility() == View.GONE) {
                    return;
                }

                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationField.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.VISIBLE);
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                try {
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());

                    // Only include cached searches if text is empty.
                    final String query = editable.toString();
                    final List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(BusinessDirectoryActivity.this)
                            : null);
                    new BusinessAutoCompleteAsynctask(cachedBusinesses).execute(query,
                                                                                mLocationWords,
                                                                                latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // } else {
                // mDummyFocusLayout.requestFocus();
                // if (mSearchField.getText().toString().length() <= 0) {
                // mLocationField.setVisibility(View.GONE);
                // }
                // mBusinessLayout.setVisibility(View.VISIBLE);
                // mNearYouContainer.setVisibility(View.VISIBLE);
                // mVenueFragmentLayout.setVisibility(View.VISIBLE);
                // mSearchListView.setVisibility(View.GONE);
                // mCurrentLocationButton.setVisibility(View.GONE);
                // mOnTheWebButton.setVisibility(View.GONE);
                // mCurrentLayoutSeparator.setVisibility(View.GONE);
                // mOnTheWebSeparator.setVisibility(View.GONE);
                // }

            }
        });

        mLocationAdapter = new LocationAdapter(BusinessDirectoryActivity.this, mLocationList);
        mSearchListView.setAdapter(mLocationAdapter);

        mLocationField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mBusinessLayout.setVisibility(View.GONE);
                    mNearYouContainer.setVisibility(View.GONE);
                    mVenueFragmentLayout.setVisibility(View.GONE);
                    mSearchListView.setAdapter(mLocationAdapter);
                    mSearchListView.setVisibility(View.VISIBLE);

                    // Search
                    String latLong = String.valueOf(getLatFromSharedPreference());
                    latLong += "," + String.valueOf(getLonFromSharedPreference());
                    mLocationWords = "";

                    try {
                        new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(BusinessDirectoryActivity.this)).execute(mLocationWords,
                                                                                                                                         latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // // Show cached search
                    // if (getCachedLocationSearchData() != null) {
                    // mLocationList.clear();
                    // mLocationList.addAll(getCachedLocationSearchData());
                    // mLocationAdapter.notifyDataSetChanged();
                    // ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
                    // mSearchListView.setVisibility(View.VISIBLE);
                    // mBusinessLayout.setVisibility(View.GONE);
                    // mNearYouContainer.setVisibility(View.GONE);
                    // mVenueFragmentLayout.setVisibility(View.GONE);
                    // }

                } else {

                    mDummyFocusLayout.requestFocus();
                    mBusinessLayout.setVisibility(View.VISIBLE);
                    mNearYouContainer.setVisibility(View.VISIBLE);
                    mVenueFragmentLayout.setVisibility(View.VISIBLE);
                }

            }
        });

        final View.OnKeyListener keyListener =
                (new View.OnKeyListener() {
                    @Override public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        int keyAction = keyEvent.getAction();
                        mIntent = new Intent(BusinessDirectoryActivity.this,
                                             MapBusinessDirectoryActivity.class);
                        if (keyAction == KeyEvent.ACTION_UP) {
                            switch (keyCode) {
                                case KeyEvent.FLAG_EDITOR_ACTION:
                                case KeyEvent.KEYCODE_ENTER:
                                    mIntent.putExtra(BUSINESS, mSearchField.getText().toString());
                                    mIntent.putExtra(LOCATION, mLocationField.getText().toString());
                                    mIntent.putExtra(BUSINESSTYPE, mBusinessType);
                                    startActivity(mIntent);

                                    if (mBusinessLayout.getVisibility() == View.GONE) {
                                        mDummyFocusLayout.requestFocus();
                                        mLocationField.setVisibility(View.GONE);
                                        mSearchListView.setVisibility(View.GONE);
                                        mBusinessLayout.setVisibility(View.VISIBLE);
                                        mNearYouContainer.setVisibility(View.VISIBLE);
                                        mVenueFragmentLayout.setVisibility(View.VISIBLE);
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                        return false;
                    }
                });

        mSearchField.setOnKeyListener(keyListener);
        mLocationField.setOnKeyListener(keyListener);

        mLocationField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mLocationField.onTextChanged();
            }

            @Override public void afterTextChanged(Editable editable) {

                if (mSearchListView.getVisibility() == View.GONE) {
                    return;
                }

                // if (editable.toString().length() > 0) {
                mSearchListView.setAdapter(mLocationAdapter);
                mSearchListView.setVisibility(View.VISIBLE);
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                String latLong = String.valueOf(getLatFromSharedPreference());
                latLong += "," + String.valueOf(getLonFromSharedPreference());
                mLocationWords = editable.toString();

                try {
                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(BusinessDirectoryActivity.this)
                            : null);

                    new LocationAutoCompleteAsynctask(cachedLocationSearch).execute(mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //
                // } else {
                //
                // mBusinessLayout.setVisibility(View.VISIBLE);
                // mNearYouContainer.setVisibility(View.VISIBLE);
                // mVenueFragmentLayout.setVisibility(View.VISIBLE);
                // mSearchListView.setVisibility(View.GONE);
                // mCurrentLocationButton.setVisibility(View.GONE);
                // mOnTheWebButton.setVisibility(View.GONE);
                // mCurrentLayoutSeparator.setVisibility(View.GONE);
                // mOnTheWebSeparator.setVisibility(View.GONE);
                // }
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                boolean locationFieldShouldFocus = false;

                if (mSearchField.isFocused()) {

                    final BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter) mSearchListView.getAdapter();

                    final Business business = businessSearchAdapter.getItem(position);

                    mSearchField.setText(business.getName());
                    mBusinessType = business.getType();

                    if ("business".equalsIgnoreCase(mBusinessType)) {
                        Intent intent = new Intent(BusinessDirectoryActivity.this,
                                                   DirectoryDetailActivity.class);
                        intent.putExtra("bizId", business.getId());
                        intent.putExtra("bizName", business.getName());
                        startActivity(intent);
                    } else {
                        CacheUtil.writeCachedBusinessSearchData(BusinessDirectoryActivity.this,
                                                                businessSearchAdapter.getItem(position));
                        locationFieldShouldFocus = true;
                    }

                } else if (mLocationField.isFocused()) {
                    final LocationAdapter locationAdapter = (LocationAdapter) mSearchListView.getAdapter();
                    final LocationSearchResult location = locationAdapter.getItem(position);
                    mLocationField.setText(location.getLocationName());
                    CacheUtil.writeCachedLocationSearchData(BusinessDirectoryActivity.this,
                                                            location.getLocationName());
                }

                if (locationFieldShouldFocus) {
                    mLocationField.requestFocus();
                    mLocationField.setSelection(mLocationField.length());
                    mLocationField.setSelected(false);
                } else {
                    mSearchField.requestFocus();
                    mSearchField.setSelection(mSearchField.length());
                    mSearchField.setSelected(false);
                }
            }
        });
    }

    @Override public void onScrollEnded(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        if (mBusinessLayout.getVisibility() == View.VISIBLE) {
            mBusinessScrollListener.onScrollEnded();
        }
    }

    public void setBusinessScrollListener(BusinessScrollListener businessScrollListener) {
        mBusinessScrollListener = businessScrollListener;
    }

    class BusinessAutoCompleteAsynctask extends AsyncTask<String, Integer, List<Business>> {

        private AirbitzAPI api = AirbitzAPI.getApi();
        private List<Business> mCacheData = null;

        public BusinessAutoCompleteAsynctask(List<Business> cacheData) {
            mCacheData = cacheData;
        }

        @Override protected List<Business> doInBackground(String... strings) {
            List<Business> jsonParsingResult = api.getHttpAutoCompleteBusiness(strings[0],
                                                                               strings[1],
                                                                               strings[2]);
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
            ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
        }
    }

    @Override public void onBackPressed() {
        mLocationWords = "";
        if (mBusinessLayout.getVisibility() == View.GONE) {
            mDummyFocusLayout.requestFocus();
            mLocationField.setVisibility(View.GONE);
            mSearchListView.setVisibility(View.GONE);
            mBusinessLayout.setVisibility(View.VISIBLE);
            mNearYouContainer.setVisibility(View.VISIBLE);
            mVenueFragmentLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onResume() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.INVISIBLE);
        }

        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override protected void onStop() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.INVISIBLE);
        }
        super.onStop();
    }

    @Override protected void onPause() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.INVISIBLE);
        }
        super.onPause();
    }

    class LocationAutoCompleteAsynctask extends AsyncTask<String, Integer, List<LocationSearchResult>> {

        private List<LocationSearchResult> mCacheData = null;
        private AirbitzAPI api = AirbitzAPI.getApi();

        public LocationAutoCompleteAsynctask(List<LocationSearchResult> cacheData) {
            mCacheData = cacheData;
        }

        @Override protected List<LocationSearchResult> doInBackground(String... strings) {
            return api.getHttpAutoCompleteLocation(strings[0], strings[1]);
        }

        @Override protected void onPostExecute(List<LocationSearchResult> result) {

            mLocationList.clear();

            // Add current location and on the web
            mLocationList.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocationList.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocationList.add(new LocationSearchResult("Result not found", false));
            } else {

                // Add cached location searches
                if (mCacheData != null) {
                    for (LocationSearchResult location : mCacheData) {
                        if (!mLocationList.contains(location)) {
                            mLocationList.add(0, location);
                        }
                    }
                }

                // Add all location results
                for (LocationSearchResult l : result) {
                    if (!mLocationList.contains(l)) {
                        mLocationList.add(l);
                    }
                }
            }
            mLocationAdapter.notifyDataSetChanged();
            ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
        }

    }

    class BusinessCategoryAsyncTask extends AsyncTask<String, Integer, Categories> {

        private AirbitzAPI api = AirbitzAPI.getApi();

        @Override protected Categories doInBackground(String... strings) {
            Categories jsonParsingResult = null;
            try {
                jsonParsingResult = api.getHttpCategories(strings[0]);
                mNextUrl = jsonParsingResult.getNextLink();
                mCategories = jsonParsingResult;
                getMoreBusinessCategory(mCategories, mNextUrl);
            } catch (Exception e) {

            }

            return jsonParsingResult;
        }

        @Override protected void onPostExecute(Categories categories) {

            if (categories != null) {
                ArrayList<Category> catArrayList = new ArrayList<Category>();

                for (Category cat : categories.getBusinessCategoryArray()) {
                    if (!cat.getCategoryLevel().equalsIgnoreCase("1")
                        && !cat.getCategoryLevel().equalsIgnoreCase("2")
                        && !cat.getCategoryLevel().equalsIgnoreCase("3")
                        && !cat.getCategoryLevel().equalsIgnoreCase("null")) {
                        catArrayList.add(cat);
                    }
                }

                categories.removeBusinessCategoryArray();
                categories.setBusinessCategoryArray(catArrayList);

                mMoreCategoryAdapter = new MoreCategoryAdapter(BusinessDirectoryActivity.this, mCategories);
                mMoreSpinner.setAdapter(mMoreCategoryAdapter);
                mMoreButton.setClickable(true);

                if (mIsMoreCategoriesProgressRunning) {
                    if (categories == null) {
                        Toast.makeText(getApplicationContext(), "Can not retrieve data",
                                       Toast.LENGTH_LONG).show();
                    }
                    mIsMoreCategoriesProgressRunning = false;

                    mMoreCategoriesProgressDialog.dismiss();

                    mMoreSpinner.setVisibility(View.INVISIBLE);
                    mMoreSpinner.performClick();
                }

                mMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        mMoreSpinner.setVisibility(View.INVISIBLE);
                        mMoreSpinner.performClick();
                    }
                });
            } else {
                mMoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        Toast.makeText(getApplicationContext(), "No categories retrieved from server",
                                       Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

    public Categories getMoreBusinessCategory(Categories initial, String link) {
        while (!link.equalsIgnoreCase("null")) {

            String jSOnString = api.getRequest(link);
            Categories jsonParsingResult = null;
            try {
                jsonParsingResult = new Categories(new JSONObject(jSOnString));
                link = jsonParsingResult.getNextLink();
                initial.addCategories(jsonParsingResult);
            } catch (Exception e) {
                link = "null";
            }
        }

        return initial;
    }

    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference() {
        return (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LAT_KEY);
    }

    private double getLonFromSharedPreference() {
        return (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LON_KEY);
    }

    private void checkLocationManager() {

        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        Criteria cri = new Criteria();
        String provider = mLocationManager.getBestProvider(cri, true);
        mCurrentLocation = mLocationManager.getLastKnownLocation(provider);
        if (mCurrentLocation != null) {
            clearSharedPreference();
            writeLatLonToSharedPreference();
        }

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("GPS is disabled. Go to Settings and turned on your GPS.")
                   .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                       @Override public void onClick(DialogInterface dialog, int which) {
                           startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       @Override public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                       }
                   });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (getLatFromSharedPreference() == -1 && getLonFromSharedPreference() == -1) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("Getting location...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }

    }

    private final LocationListener listener = new LocationListener() {

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override public void onProviderEnabled(String provider) {

        }

        @Override public void onProviderDisabled(String provider) {

        }

        @Override public void onLocationChanged(android.location.Location location) {

            // Location has changed
            if (mLocationManager != null) {

                mLocationManager.removeUpdates(listener);
            }

            if (location.hasAccuracy()) {
                if (getLatFromSharedPreference() == -1 && getLonFromSharedPreference() == -1) {
                    mProgressDialog.dismiss();
                }
                mCurrentLocation = location;

                Log.d("TAG_LOC",
                      "CUR LOC: " + mCurrentLocation.getLatitude() + "; " + mCurrentLocation.getLongitude());
                clearSharedPreference();
                writeLatLonToSharedPreference();
            }
        }

    };

    private void writeLatLonToSharedPreference() {
        writeValueToSharedPreference(LAT_KEY, (float) mCurrentLocation.getLatitude());
        writeValueToSharedPreference(LON_KEY, (float) mCurrentLocation.getLongitude());
    }

    private void writeValueToSharedPreference(String key, float value) {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    private void clearSharedPreference() {
        SharedPreferences pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(LAT_KEY);
        editor.remove(LON_KEY);
        editor.commit();
    }

    // private void clearCacheSharedPreference() {
    // SharedPreferences pref =
    // getSharedPreferences(MOSTRECENT_BUSINESSSEARCH_SHARED_PREF, MODE_PRIVATE);
    // SharedPreferences.Editor editor = pref.edit();
    // editor.remove(BIZ1_NAME_KEY);
    // editor.remove(BIZ1_TYPE_KEY);
    // editor.remove(BIZ1_ID_KEY);
    // editor.remove(BIZ2_NAME_KEY);
    // editor.remove(BIZ2_TYPE_KEY);
    // editor.remove(BIZ2_ID_KEY);
    //
    // editor.commit();
    //
    // SharedPreferences prefLocation =
    // getSharedPreferences(MOSTRECENT_LOCATIONSEARCH_SHARED_PREF,
    // MODE_PRIVATE);
    // editor = prefLocation.edit();
    //
    // editor.remove(LOC2_KEY);
    // editor.remove(LOC3_KEY);
    // editor.remove(LOC4_KEY);
    // editor.remove(LOC5_KEY);
    // editor.remove(LOC1_KEY);
    //
    // editor.commit();
    // }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
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
