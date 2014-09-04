package com.airbitz.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.objects.ObservableScrollView;
import com.airbitz.utils.CacheUtil;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tom on 4/22/14.
 */
public class BusinessDirectoryFragment extends Fragment implements
        NavigationActivity.OnBackPress,
        ObservableScrollView.ScrollViewListener,
        CurrentLocationManager.OnLocationChange {

    public static final String LAT_KEY = "LAT_KEY";
    public static final String LON_KEY = "LON_KEY";
    public static final String PREF_NAME = "PREF_NAME";
    public static final String LOCATION_CACHE_SHARED_PREF = "LOCATION_CACHE_PREF";
    public static final String BUSINESS_CACHE_SHARED_PREF = "BUSINESS_CACHE_PREF";

    private Categories mCategories;

    private EditText mSearchField;
    private EditText mLocationField;
    private ListView mSearchListView;
    private TextView mTitleTextView;

    private TextView mNearYouTextView;
    private TextView mNearYouTextViewSticky;


    private LinearLayout mBusinessLayout;
    private LinearLayout mNearYouContainer;

    private boolean mLoadingVisible = true;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mRestaurantButton;
    private TextView mBarButton;
    private TextView mCoffeeButton;
    private TextView mMoreButton;

    private boolean locationEnabled = false;


    private RelativeLayout mVenueFragmentLayout;
    List<BusinessSearchResult> mVenuesLoaded;
    private List<BusinessSearchResult> mTempVenues;

    private LinearLayout mDummyFocusLayout;

    private LinearLayout mStickyLayout;

    private ListView mVenueListView;
    private VenueAdapter mVenueAdapter;

    private Spinner mMoreSpinner;

    private CurrentLocationManager mLocationManager;

    private ObservableScrollView mScrollView;

    private ViewGroup mViewGroupLoading;
    private TextView mNoResultView;

    private static String mLocationWords = "";
    private static String mBusinessType = "business";


    private ArrayAdapter<Business> mBusinessSearchAdapter;
    private LocationAdapter mLocationAdapter;

    public final static String LOCATION = "LOCATION";
    public final static String BUSINESS = "BUSINESS";
    public final static String BUSINESSTYPE = "BUSINESSTYPE";

    private ArrayList<LocationSearchResult> mLocationList;
    private ArrayList<Business> mBusinessList;

    private String mNextUrl = "null";

    private MoreCategoryAdapter mMoreCategoryAdapter;

    private AirbitzAPI api = AirbitzAPI.getApi();

    private BusinessCategoryAsyncTask mBusinessCategoryAsynctask;
    private BusinessAutoCompleteAsynctask mBusinessAutoCompleteAsyncTask;
    private LocationAutoCompleteAsynctask mLocationAutoCompleteAsyncTask;

    private GetVenuesTask mGetVenuesTask;
    private GetMoreVenuesTask mGetMoreVenuesTask;
    private GetRemainingFirstVenuesTask mGetRemainingFirstVenuesTask;

    private boolean isGettingMoreVenueFinished = true;
    private boolean mFirstLoad = true;
    private int venueAmount = 0;
    private boolean mLoadFlag = false;
    private boolean isFirstLoad = true;

    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;

    private ProgressDialog mMoreCategoriesProgressDialog;

    protected static int CATEGORY_TIMEOUT = 15000;
    Handler mHandler = new Handler();
    boolean alreadyLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(mLocationManager==null) {
            alreadyLoaded = false;
            mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
            mLocationManager.addLocationChangeListener(this);
        } else {
            alreadyLoaded = true;
        }
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view==null) {
            view = inflater.inflate(R.layout.fragment_business_directory, container, false);
        } else {
            ((ViewGroup) view.getParent()).removeView(view);
            showLoadingIndicator();
            mNoResultView.setVisibility(View.GONE);
            return view;
        }

        checkLocationManager();

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        montserratBoldTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Lato-RegIta.ttf");
        helveticaNeueTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/HelveticaNeue.ttf");

        mBusinessList = new ArrayList<Business>();
        mLocationList = new ArrayList<LocationSearchResult>();

        Log.d("TAG_LOC", "CUR LOC: ");

        mRestaurantButton = (TextView) view.findViewById(R.id.button_restaurant);
        mBarButton = (TextView) view.findViewById(R.id.button_bar);
        mCoffeeButton = (TextView) view.findViewById(R.id.button_coffee_tea);
        mMoreButton = (TextView) view.findViewById(R.id.button_more);
        mMoreButton.setClickable(false);

        mDummyFocusLayout = (LinearLayout) view.findViewById(R.id.fragment_businessdirectory_dummy_focus);

        mStickyLayout = (LinearLayout) view.findViewById(R.id.layout_near_you_sticky);

        mNearYouContainer = (LinearLayout) view.findViewById(R.id.layout_near_you);
        mVenueFragmentLayout = (RelativeLayout) view.findViewById(R.id.fragment_layout_container);
        mVenueListView = (ListView) view.findViewById(R.id.fragment_layout);
        mVenueAdapter = new VenueAdapter(getActivity(), mVenuesLoaded);
        mNoResultView = (TextView) view.findViewById(R.id.business_fragment_no_result_view);

        mScrollView = (ObservableScrollView) view.findViewById(R.id.scroll_view);
        mScrollView.setScrollViewListener(this);
        mScrollView.setContext(getActivity());
        mScrollView.setSticky(mStickyLayout);

        mMoreSpinner = (Spinner) view.findViewById(R.id.spinner_more_categories);

        mBusinessLayout = (LinearLayout) view.findViewById(R.id.layout_listview_business);

        mBackButton = (ImageButton) view.findViewById(R.id.fragment_category_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.fragment_category_button_help);
        mHelpButton.setVisibility(View.GONE);
        mSearchField = (EditText) view.findViewById(R.id.edittext_search);
        mLocationField = (EditText) view.findViewById(R.id.edittext_location);
        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mTitleTextView = (TextView) view.findViewById(R.id.fragment_category_textview_title);

        mNearYouTextView = (TextView) view.findViewById(R.id.textview_nearyou);
        mNearYouTextViewSticky = (TextView) view.findViewById(R.id.textview_nearyou_sticky);

        mViewGroupLoading = (ViewGroup) view.findViewById(R.id.ViewGroup_loading);

        mTitleTextView.setTypeface(montserratBoldTypeFace);
        mSearchField.setTypeface(montserratRegularTypeFace);
        mLocationField.setTypeface(montserratRegularTypeFace);

        mRestaurantButton.setTypeface(montserratRegularTypeFace);
        mBarButton.setTypeface(montserratRegularTypeFace);
        mCoffeeButton.setTypeface(montserratRegularTypeFace);
        mMoreButton.setTypeface(montserratRegularTypeFace);
        mNearYouTextView.setTypeface(montserratRegularTypeFace);
        mNearYouTextViewSticky.setTypeface(montserratRegularTypeFace);

        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mMoreCategoriesProgressDialog = new ProgressDialog(getActivity());
                mMoreCategoriesProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mMoreCategoriesProgressDialog.setMessage("Retrieving data...");
                mMoreCategoriesProgressDialog.setIndeterminate(true);
                mMoreCategoriesProgressDialog.setCancelable(false);
                mMoreCategoriesProgressDialog.show();
            }
        });

        try {
            mBusinessCategoryAsynctask = new BusinessCategoryAsyncTask();
            mBusinessCategoryAsynctask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "level");
            mHandler.postDelayed(mProgressTimeout, CATEGORY_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(BUSINESS, ((TextView) view).getText().toString());
                bundle.putString(LOCATION, "");
                bundle.putString(BUSINESSTYPE, "category");
                Fragment fragment = new MapBusinessDirectoryFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
            }
        });

        mBarButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(BUSINESS, ((TextView) view).getText().toString());
                bundle.putString(LOCATION, "");
                bundle.putString(BUSINESSTYPE, "category");
                Fragment fragment = new MapBusinessDirectoryFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
            }
        });

        mCoffeeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(BUSINESS, ((TextView) view).getText().toString());
                bundle.putString(LOCATION, "");
                bundle.putString(BUSINESSTYPE, "category");
                Fragment fragment = new MapBusinessDirectoryFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
//                finish();
                onBackPressed();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Common.showHelpInfoDialog(getActivity(), "Info", "Business directory info");
            }
        });

        mBusinessSearchAdapter = new BusinessSearchAdapter(getActivity(), mBusinessList);
        mSearchListView.setAdapter(mBusinessSearchAdapter);

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {

                    mSearchListView.setAdapter(mBusinessSearchAdapter);
                    mBusinessLayout.setVisibility(View.GONE);
                    mNearYouContainer.setVisibility(View.GONE);
                    mViewGroupLoading.setVisibility(View.GONE);
                    mVenueFragmentLayout.setVisibility(View.GONE);
                    mLocationField.setVisibility(View.VISIBLE);
                    mSearchListView.setVisibility(View.VISIBLE);
                    mBackButton.setVisibility(View.VISIBLE);

                    // Start search
                    try {
                        final String text = mSearchField.getText().toString();
                        final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                                ? null
                                : CacheUtil.getCachedBusinessSearchData(getActivity()));

                        if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                            mBusinessAutoCompleteAsyncTask.cancel(true);
                        }
                        mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusiness);
                        String latLong = "";
                        if(locationEnabled) {
                            Location currentLoc = mLocationManager.getLocation();
                            latLong = String.valueOf(currentLoc.getLatitude());
                            latLong += "," + String.valueOf(currentLoc.getLongitude());
                        }
                        mBusinessAutoCompleteAsyncTask.execute(text, mLocationWords, latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    if(!mLocationField.hasFocus()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }


            }
        });

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override public void afterTextChanged(Editable editable) {

                // if (editable.toString().length() > 0) {

                if (mSearchListView.getVisibility() == View.GONE) {
                    return;
                }

                mSearchListView.setAdapter(mBusinessSearchAdapter);
                mLocationField.setVisibility(View.VISIBLE);
                mSearchListView.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mViewGroupLoading.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                try {
                    String latLong = "";
                    if(locationEnabled) {
                        Location currentLoc = mLocationManager.getLocation();
                        latLong = String.valueOf(currentLoc.getLatitude());
                        latLong += "," + String.valueOf(currentLoc.getLongitude());
                    }
                    // Only include cached searches if text is empty.
                    final String query = editable.toString();
                    List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(getActivity())
                            : null);
                    if(mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus()== AsyncTask.Status.RUNNING){
                        mBusinessAutoCompleteAsyncTask.cancel(true);
                    }
                    mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusinesses);
                    mBusinessAutoCompleteAsyncTask.execute(query,mLocationWords,latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        mLocationAdapter = new LocationAdapter(getActivity(), mLocationList);
        mSearchListView.setAdapter(mLocationAdapter);

        mLocationField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {

                if (hasFocus) {
                    mBusinessLayout.setVisibility(View.GONE);
                    mNearYouContainer.setVisibility(View.GONE);
                    mVenueFragmentLayout.setVisibility(View.GONE);
                    mViewGroupLoading.setVisibility(View.GONE);
                    mSearchListView.setAdapter(mLocationAdapter);
                    mSearchListView.setVisibility(View.VISIBLE);
                    mBackButton.setVisibility(View.VISIBLE);

                    // Search

                    try {
                        String latLong = "";
                        if(locationEnabled) {
                            Location currentLoc = mLocationManager.getLocation();
                            latLong = String.valueOf(currentLoc.getLatitude());
                            latLong += "," + String.valueOf(currentLoc.getLongitude());
                        }
                        mLocationWords = "";
                        if(mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus()== AsyncTask.Status.RUNNING){
                            mLocationAutoCompleteAsyncTask.cancel(true);
                        }
                        mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(getActivity()));
                        mLocationAutoCompleteAsyncTask.execute(mLocationWords,latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    if(!mSearchField.hasFocus()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                    mDummyFocusLayout.requestFocus();
                    mBusinessLayout.setVisibility(View.VISIBLE);//TODO do we really need this?
                    mNearYouContainer.setVisibility(View.VISIBLE);
                    if(mLoadingVisible){
                        mViewGroupLoading.setVisibility(View.VISIBLE);
                    }
                    mVenueFragmentLayout.setVisibility(View.VISIBLE);
                }

            }
        });

        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    Bundle bundle = new Bundle();
                    bundle.putString(BUSINESS, mSearchField.getText().toString());
                    bundle.putString(LOCATION, mLocationField.getText().toString());
                    bundle.putString(BUSINESSTYPE, mBusinessType);
                    Fragment fragment = new MapBusinessDirectoryFragment();
                    fragment.setArguments(bundle);
                    ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());

                    if (mBusinessLayout.getVisibility() == View.GONE) {
                        mDummyFocusLayout.requestFocus();
                        mLocationField.setVisibility(View.GONE);
                        mSearchListView.setVisibility(View.GONE);
                        mBackButton.setVisibility(View.GONE);
                        mBusinessLayout.setVisibility(View.VISIBLE);
                        mNearYouContainer.setVisibility(View.VISIBLE);
                        if(mLoadingVisible){
                            mViewGroupLoading.setVisibility(View.VISIBLE);
                        }
                        mVenueFragmentLayout.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });

        mLocationField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    Bundle bundle = new Bundle();
                    bundle.putString(BUSINESS, mSearchField.getText().toString());
                    bundle.putString(LOCATION, mLocationField.getText().toString());
                    bundle.putString(BUSINESSTYPE, mBusinessType);
                    Fragment fragment = new MapBusinessDirectoryFragment();
                    fragment.setArguments(bundle);
                    ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());

                    if (mBusinessLayout.getVisibility() == View.GONE) {
                        mDummyFocusLayout.requestFocus();
                        mLocationField.setVisibility(View.GONE);
                        mSearchListView.setVisibility(View.GONE);
                        mBackButton.setVisibility(View.GONE);
                        mBusinessLayout.setVisibility(View.VISIBLE);
                        mNearYouContainer.setVisibility(View.VISIBLE);
                        if(mLoadingVisible){
                            mViewGroupLoading.setVisibility(View.VISIBLE);
                        }
                        mVenueFragmentLayout.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });

        mLocationField.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override public void afterTextChanged(Editable editable) {

                if (mSearchListView.getVisibility() == View.GONE) {
                    return;
                }

                // if (editable.toString().length() > 0) {
                mSearchListView.setAdapter(mLocationAdapter);
                mSearchListView.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mViewGroupLoading.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                try {
                    String latLong = "";
                    if(locationEnabled) {
                        Location currentLoc = mLocationManager.getLocation();
                        latLong = String.valueOf(currentLoc.getLatitude());
                        latLong += "," + String.valueOf(currentLoc.getLongitude());
                    }
                    mLocationWords = editable.toString();

                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(getActivity())
                            : null);
                    if(mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus()== AsyncTask.Status.RUNNING){
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
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                boolean locationFieldShouldFocus = false;

                if (mSearchField.isFocused()) {

                    final BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter) mSearchListView.getAdapter();

                    final Business business = businessSearchAdapter.getItem(position);

                    mSearchField.setText(business.getName());
                    mBusinessType = business.getType();

                    if ("business".equalsIgnoreCase(mBusinessType)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(DirectoryDetailFragment.BIZID, business.getId());
                        bundle.putString(DirectoryDetailFragment.BIZNAME, business.getName());
                        Fragment fragment = new DirectoryDetailFragment();
                        fragment.setArguments(bundle);
                        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
                    } else {
                        CacheUtil.writeCachedBusinessSearchData(getActivity(),
                                businessSearchAdapter.getItem(position));
                        locationFieldShouldFocus = true;
                    }

                } else if (mLocationField.isFocused()) {
                    final LocationAdapter locationAdapter = (LocationAdapter) mSearchListView.getAdapter();
                    final LocationSearchResult location = locationAdapter.getItem(position);
                    mLocationField.setText(location.getLocationName());
                    CacheUtil.writeCachedLocationSearchData(getActivity(),
                            location.getLocationName());
                }

                if (locationFieldShouldFocus) {
                    mLocationField.requestFocus();
                } else {
                    mSearchField.requestFocus();
                }
            }
        });

        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.INVISIBLE);
        }
        mBackButton.setVisibility(View.GONE);

        return view;
    }

    Runnable mProgressTimeout = new Runnable()
    {
        @Override public void run() {
            if (mMoreCategoriesProgressDialog!=null && mMoreCategoriesProgressDialog.isShowing()) {
                mMoreCategoriesProgressDialog.dismiss();
            }
            mMoreCategoriesProgressDialog=null;
        }
    };

    @Override
    public void OnCurrentLocationChange(Location location) {
        String latLon = "";
        if (location != null)
            latLon = "" + location.getLatitude() + "," + location.getLongitude();

        if (mGetVenuesTask != null && mGetVenuesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mGetVenuesTask.cancel(true);
        }
        mGetVenuesTask = new GetVenuesTask(getActivity());
        mGetVenuesTask.execute(latLon);

        mLocationManager.removeLocationChangeListener(this);
    }

    @Override public void onScrollEnded(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        if (isFirstLoad) {
            isFirstLoad = false;
            if(mGetRemainingFirstVenuesTask != null && mGetRemainingFirstVenuesTask.getStatus() == AsyncTask.Status.RUNNING){
                mGetRemainingFirstVenuesTask.cancel(true);
            }
            mGetRemainingFirstVenuesTask = new GetRemainingFirstVenuesTask(getActivity());
            mGetRemainingFirstVenuesTask.execute("");
            venueAmount = 20;
        } else {
            if( venueAmount < 100) {
                if (isGettingMoreVenueFinished) {
                    isGettingMoreVenueFinished = false;
                    if(mGetMoreVenuesTask != null && mGetMoreVenuesTask.getStatus() == AsyncTask.Status.RUNNING){
                        mGetMoreVenuesTask.cancel(true);
                    }
                    mGetMoreVenuesTask = new GetMoreVenuesTask(getActivity());
                    mGetMoreVenuesTask.execute(mNextUrl);
                    venueAmount += 20;
                }
            }
        }

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
            if(getActivity()==null)
                return;

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
            ListViewUtility.setListViewHeightBasedOnChildren(mSearchListView);
            mBusinessAutoCompleteAsyncTask = null;
        }

        @Override protected void onCancelled(List<Business> jSONResult){
            mBusinessAutoCompleteAsyncTask = null;
            super.onCancelled();
        }
    }

    @Override
    public boolean onBackPress() {
        return onBackPressed();
    }

    //    @Override
    public boolean onBackPressed() {
        System.out.println("Back Pressed");
        mLocationWords = "";
        if (mBusinessLayout.getVisibility() == View.GONE) {
            System.out.println("Backing out of Search");
            mDummyFocusLayout.requestFocus();
            mLocationField.setVisibility(View.GONE);
            mSearchListView.setVisibility(View.GONE);
            mBusinessLayout.setVisibility(View.VISIBLE);
            mNearYouContainer.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.GONE);
            if(mLoadingVisible){
                mViewGroupLoading.setVisibility(View.VISIBLE);
            }
            mVenueFragmentLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override public void onResume() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        checkLocationManager();
        if(alreadyLoaded && mVenueListView!=null && mVenuesLoaded!=null) {
            setVenueListView(mVenuesLoaded);
        } else {
            mVenuesLoaded = new ArrayList<BusinessSearchResult>();
            mLocationManager.addLocationChangeListener(this);
        }
        super.onResume();
    }

    @Override public void onStop() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        super.onStop();
    }

    @Override public void onPause() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        mLocationManager.removeLocationChangeListener(this);

        if(mBusinessAutoCompleteAsyncTask != null){
            mBusinessAutoCompleteAsyncTask.cancel(true);
        }
        if(mLocationAutoCompleteAsyncTask != null){
            mLocationAutoCompleteAsyncTask.cancel(true);
        }
        if(mGetMoreVenuesTask != null && mGetMoreVenuesTask.getStatus() == AsyncTask.Status.RUNNING){
            mGetMoreVenuesTask.cancel(true);
        }
        if(mGetRemainingFirstVenuesTask != null && mGetRemainingFirstVenuesTask.getStatus() == AsyncTask.Status.RUNNING){
            mGetRemainingFirstVenuesTask.cancel(true);
        }
        if(mGetVenuesTask != null && mGetVenuesTask.getStatus() == AsyncTask.Status.RUNNING){
            mGetVenuesTask.cancel(true);
        }
        mFirstLoad = true;

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
            if(getActivity()==null)
                return;

            mLocationList.clear();

            // Add current location and on the web
            mLocationList.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocationList.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocationList.add(new LocationSearchResult("No Results Found", false));
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
            mLocationAutoCompleteAsyncTask = null;
        }

        @Override protected void onCancelled(List<LocationSearchResult> JSONResult){
            super.onCancelled();
            mLocationAutoCompleteAsyncTask = null;
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
            if(getActivity()==null)
                return;

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

                mMoreCategoryAdapter = new MoreCategoryAdapter(getActivity(), mCategories);
                mMoreSpinner.setAdapter(mMoreCategoryAdapter);
                mMoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> adapterView,
                                                         View view,
                                                         int position,
                                                         long l) {

                        if (mFirstLoad) {
                            mFirstLoad = false;
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(BUSINESS, mMoreCategoryAdapter.getListItemName(position)
                                    .getCategoryName());
                            bundle.putString(LOCATION, "");
                            bundle.putString(BUSINESSTYPE, "category");
                            Fragment fragment = new MapBusinessDirectoryFragment();
                            fragment.setArguments(bundle);
                            ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
                        }
                    }

                    @Override public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                mMoreButton.setClickable(true);

                if (mMoreCategoriesProgressDialog!=null && mMoreCategoriesProgressDialog.isShowing()) {
                    if (categories == null) {
                        Toast.makeText(getActivity().getApplicationContext(), "Can not retrieve data",
                                Toast.LENGTH_LONG).show();
                    }
                    mMoreCategoriesProgressDialog.dismiss();
                    mMoreCategoriesProgressDialog = null;


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
                        Toast.makeText(getActivity().getApplicationContext(), "No categories retrieved from server",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

        }
    }

    public void hideLoadingIndicator() {
        mViewGroupLoading.setVisibility(View.GONE);
        mLoadingVisible = false;
    }

    public void showLoadingIndicator() {
        mViewGroupLoading.setVisibility(View.VISIBLE);
        mLoadingVisible = true;
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

    @Override
    public void onDetach() {
        super.onDetach();
        if(mBusinessCategoryAsynctask!=null)
            mBusinessCategoryAsynctask.cancel(true);

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

    private void checkLocationManager() {
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for better results", Toast.LENGTH_SHORT).show();
        }else{
            locationEnabled = true;
        }
    }

    private class GetVenuesTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetVenuesTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
        }

        @Override protected String doInBackground(String... params) {
            String result = "";
            result = mApi.getSearchByLatLong(params[0], "", "", "1");

            return result;
        }

        @Override protected void onCancelled() {
            mNoResultView.setVisibility(View.VISIBLE);
            hideLoadingIndicator();
            mGetVenuesTask = null;
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            try {
                mVenuesLoaded.clear();
                processSearchResults(searchResult);
                setVenueListView(mVenuesLoaded);
            } catch (JSONException e) {
                mNoResultView.setVisibility(View.VISIBLE);
                hideLoadingIndicator();
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                mNoResultView.setVisibility(View.VISIBLE);
                hideLoadingIndicator();
                e.printStackTrace();
                this.cancel(true);
            }
            mGetVenuesTask = null;
        }
    }

    private void showDirectoryDetailFragment(String id, String name, String distance) {
        Bundle bundle = new Bundle();
        bundle.putString(DirectoryDetailFragment.BIZID, id);
        bundle.putString("", name);
        bundle.putString("", distance);
        Fragment fragment = new DirectoryDetailFragment();
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.BD.ordinal());
    }

    public void setVenueListView(List<BusinessSearchResult> venues) {
        if(venues!=null) {
            mVenueListView.setVisibility(View.VISIBLE);
            mNoResultView.setVisibility(View.GONE);
        } else {
            mVenueListView.setVisibility(View.GONE);
            mNoResultView.setVisibility(View.VISIBLE);
        }
        if(mVenuesLoaded.isEmpty())
            mVenuesLoaded.addAll(venues);

        mVenueAdapter = new VenueAdapter(getActivity(), mVenuesLoaded);
        mVenueListView.setAdapter(mVenueAdapter);
        ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView, mVenueListView.getContext());

        preloadVenueImages();

        mVenueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDirectoryDetailFragment(mVenuesLoaded.get(i).getId(), mVenuesLoaded.get(i).getName(), mVenuesLoaded.get(i).getDistance());
            }
        });

        mVenueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView absListView, int i) { }

            @Override public void onScroll(AbsListView view,
                                           int firstVisibleItem,
                                           int visibleItemCount,
                                           int totalItemCount) {

                if (!mNextUrl.equalsIgnoreCase("null")) {
                    if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                        if (!mLoadFlag) {
                            mLoadFlag = true;
                        }
                    }
                }
            }
        });
    }

    void processSearchResults(String searchResult) throws JSONException {

        SearchResult results = new SearchResult(new JSONObject(searchResult));

        mNextUrl = "null";
        mTempVenues = new ArrayList<BusinessSearchResult>();
        mNextUrl = results.getNextLink();
        mTempVenues = results.getBusinessSearchObjectArray();

        if (mTempVenues.isEmpty() && mVenuesLoaded.isEmpty()) {

        } else {

                if (mTempVenues.size() >= 5) {
                    for (int i = 0; i < 5; i++) {
                        mVenuesLoaded.add(mTempVenues.get(i));
                    }

                    for (int i = 4; i >= 0; i--) {
                        mTempVenues.remove(i);
                    }
                } else {
                    for (int i = 0; i < mTempVenues.size(); i++) {
                        mVenuesLoaded.add(mTempVenues.get(i));
                    }
                    for (int i = mTempVenues.size() - 1; i >= 0; i--) {
                        mTempVenues.remove(i);
                    }
                }
        }
    }

    private void preloadVenueImages() {
        if (mVenuesLoaded != null) {
            for (BusinessSearchResult venue : mVenuesLoaded) {
                Picasso.with(getActivity()).load(venue.getProfileImage().getImageThumbnail()).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) { }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) { }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) { }
                });
            }
        }
    }
    private class GetMoreVenuesTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetMoreVenuesTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
        }

        @Override protected String doInBackground(String... params) {
            if (params[0].equalsIgnoreCase("null")) {
                return "";
            }
            return mApi.getRequest(params[0]);
        }

        @Override protected void onCancelled() {
            mNoResultView.setVisibility(View.VISIBLE);
            hideLoadingIndicator();
            mGetMoreVenuesTask = null;
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            if (!searchResult.isEmpty()) {
                try {
                    mLoadFlag = false;
                    SearchResult results = new SearchResult(new JSONObject(searchResult));
                    mNextUrl = results.getNextLink();
                    mVenuesLoaded.addAll(results.getBusinessSearchObjectArray());
                    setVenueListView(mVenuesLoaded);
                } catch (JSONException e) {
                    e.printStackTrace();
                    this.cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.cancel(true);

                }
            } else {
                hideLoadingIndicator();
            }
            if(venueAmount >= 100){
                hideLoadingIndicator();
            }
            isGettingMoreVenueFinished = true;
            mGetMoreVenuesTask = null;
        }
    }    private class GetRemainingFirstVenuesTask extends AsyncTask<String, Void, List<BusinessSearchResult>> {

        Context mContext;

        public GetRemainingFirstVenuesTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
        }

        @Override protected List<BusinessSearchResult> doInBackground(String... params) {
            return mTempVenues;
        }

        @Override protected void onCancelled() {
            mNoResultView.setVisibility(View.VISIBLE);
            hideLoadingIndicator();
            mGetRemainingFirstVenuesTask = null;
            super.onCancelled();
        }

        @Override protected void onPostExecute(List<BusinessSearchResult> searchResult) {
            if (!searchResult.isEmpty()) {
                refreshVenueList();
                mVenuesLoaded.addAll(searchResult);
            }
            mGetRemainingFirstVenuesTask = null;
        }

    }

    public void refreshVenueList() {
        mVenueAdapter.notifyDataSetChanged();
        preloadVenueImages();
        ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView, mVenueListView.getContext());
    }
}
