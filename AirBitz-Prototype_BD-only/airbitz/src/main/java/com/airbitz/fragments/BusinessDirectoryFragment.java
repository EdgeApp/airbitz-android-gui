package com.airbitz.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.Business;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ObservableScrollView;
import com.airbitz.utils.CacheUtil;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class BusinessDirectoryFragment extends Fragment implements
        GestureDetector.OnGestureListener,
        ObservableScrollView.ScrollViewListener,
        CurrentLocationManager.OnLocationChange {

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
    private TextView mNearYouTextViewSticky;

    private boolean wentSettings = false;

    private LinearLayout mBusinessLayout;
    private LinearLayout mNearYouContainer;

    private boolean mLoadingVisible = true;
    private boolean mSearchVisible = true;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private TextView mRestaurantButton;
    private TextView mBarButton;
    private TextView mCoffeeButton;
    private TextView mMoreButton;

    private RelativeLayout mParentLayout;

    private RelativeLayout mVenueFragmentLayout;

    private LinearLayout mDummyFocusLayout;

    private LinearLayout mStickyLayout;

    private LinearLayout mVenueFragmentLayoutInner;

    private Spinner mMoreSpinner;

    //private LocationManager mLocationManager;

    private CurrentLocationManager mLocationManager;

    private ObservableScrollView mScrollView;

    private ViewGroup mViewGroupLoading;
    private TextView mLoadingText;

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

    private VenueFragment mVenueFragment;

    private AirbitzAPI api = AirbitzAPI.getApi();

    private BusinessCategoryAsyncTask mBusinessCategoryAsynctask;
    private boolean mFirstLoad = true;

    private TextView businessHint;
    private TextView locationHint;

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

    public boolean getSearchVisible(){ return mSearchVisible; }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_directory, container, false);

        checkLocationManager();

        mGestureDetector = new GestureDetector(this);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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

        businessHint = (TextView) view.findViewById(R.id.business_hint);
        businessHint.setTypeface(montserratRegularTypeFace);
        locationHint = (TextView) view.findViewById(R.id.location_hint);
        locationHint.setTypeface(montserratRegularTypeFace);

        Log.d("TAG_LOC", "CUR LOC: ");

        mRestaurantButton = (TextView) view.findViewById(R.id.button_restaurant);
        mBarButton = (TextView) view.findViewById(R.id.button_bar);
        mCoffeeButton = (TextView) view.findViewById(R.id.button_coffee_tea);
        mMoreButton = (TextView) view.findViewById(R.id.button_more);
        mMoreButton.setClickable(false);

        mDummyFocusLayout = (LinearLayout) view.findViewById(R.id.dummy_focus);

        mStickyLayout = (LinearLayout) view.findViewById(R.id.layout_near_you_sticky);

        mNearYouContainer = (LinearLayout) view.findViewById(R.id.layout_near_you);
        mVenueFragmentLayout = (RelativeLayout) view.findViewById(R.id.fragment_layout_container);
        mVenueFragmentLayoutInner = (LinearLayout) view.findViewById(R.id.fragment_layout);

        if(mVenueFragment == null) {
            mVenueFragment = new VenueFragment();
        }
        if(mVenueFragmentLayoutInner.getChildCount()<=0) {
            if(getChildFragmentManager().findFragmentByTag("venue") == null) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_layout, mVenueFragment, "venue").commit();
            }
        }

        mScrollView = (ObservableScrollView) view.findViewById(R.id.scroll_view);
        mScrollView.setScrollViewListener(this);
        mScrollView.setContext(getActivity());
        mScrollView.setSticky(mStickyLayout);

        mMoreSpinner = (Spinner) view.findViewById(R.id.spinner_more_categories);

        mBusinessLayout = (LinearLayout) view.findViewById(R.id.layout_listview_business);

        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mSearchField = (ClearableEditText) view.findViewById(R.id.edittext_search);
        mLocationField = (ClearableEditText) view.findViewById(R.id.edittext_location);
        mSearchListView = (ListView) view.findViewById(R.id.listview_search);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mNearYouTextView = (TextView) view.findViewById(R.id.textview_nearyou);
        mNearYouTextViewSticky = (TextView) view.findViewById(R.id.textview_nearyou_sticky);
        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);

        mViewGroupLoading = (ViewGroup) view.findViewById(R.id.ViewGroup_loading);
        mLoadingText = (TextView) view.findViewById(R.id.TextView_loading);

        mTitleTextView.setTypeface(montserratBoldTypeFace);
        mSearchField.setTypeface(montserratRegularTypeFace);
        mLocationField.setTypeface(montserratRegularTypeFace);

        mRestaurantButton.setTypeface(montserratRegularTypeFace);
        mBarButton.setTypeface(montserratRegularTypeFace);
        mCoffeeButton.setTypeface(montserratRegularTypeFace);
        mMoreButton.setTypeface(montserratRegularTypeFace);
        mNearYouTextView.setTypeface(montserratRegularTypeFace);
        mNearYouTextViewSticky.setTypeface(montserratRegularTypeFace);
        mLoadingText.setTypeface(montserratRegularTypeFace);

        mBusinessCategoryAsynctask = new BusinessCategoryAsyncTask();
        mMoreCategoriesProgressDialog = new ProgressDialog(getActivity());

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

        mRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(BUSINESS, ((TextView) view).getText().toString());
                bundle.putString(LOCATION, "");
                bundle.putString(BUSINESSTYPE, "category");
                Fragment fragment = new MapBusinessDirectoryFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment);
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
                ((NavigationActivity) getActivity()).pushFragment(fragment);
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
                ((NavigationActivity) getActivity()).pushFragment(fragment);
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
                    mSearchVisible=true;


                    if(mSearchField.getText().toString().isEmpty()) {
                        mSearchField.setText(" ");
                        mSearchField.setSelection(mSearchField.getText().toString().length());
                    }else{
                        if(mSearchField.getText().toString().charAt(0)!=' '){
                            mSearchField.setText(" " + mSearchField.getText().toString());
                        }
                        mSearchField.setSelection(1,mSearchField.getText().toString().length());
                    }

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
                        final String text;
                        if(mSearchField.getText().toString().charAt(0)==' '){
                            text = mSearchField.getText().toString().substring(1);
                        }else{
                            text = mSearchField.getText().toString();
                        }
                        final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                                ? null
                                : CacheUtil.getCachedBusinessSearchData(getActivity()));
                        Location currentLoc = mLocationManager.getLocation();
                        String latLong = String.valueOf(currentLoc.getLatitude());
                        latLong += "," + String.valueOf(currentLoc.getLongitude());
                        new BusinessAutoCompleteAsynctask(cachedBusiness).execute(text
                                        .toString(),
                                mLocationWords,
                                latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    if(!mLocationField.hasFocus()){
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                    /*if(!mSearchField.getText().toString().isEmpty() && mSearchField.getText().toString().charAt(0)==' ') {
                        mSearchField.setText(mSearchField.getText().toString().substring(1));
                    }*/
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
                mSearchVisible=true;
                mBackButton.setVisibility(View.VISIBLE);
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mViewGroupLoading.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                try {
                    Location currentLoc = mLocationManager.getLocation();
                    String latLong = String.valueOf(currentLoc.getLatitude());
                    latLong += "," + String.valueOf(currentLoc.getLongitude());

                    // Only include cached searches if text is empty.
                    final String query;
                    if(!editable.toString().isEmpty() && editable.toString().charAt(0)==' ') {
                        query = editable.toString().substring(1);
                    }else{
                        query = editable.toString();
                    }
                    List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(getActivity())
                            : null);
                    new BusinessAutoCompleteAsynctask(cachedBusinesses).execute(query,
                            mLocationWords,
                            latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(editable.toString().isEmpty() && mSearchField.hasFocus()){
                    editable.append(' ');
                }else if(!editable.toString().isEmpty() && editable.toString().charAt(0)!=' '){
                    mSearchField.setText(" "+editable.toString());
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

                if( ( editable.toString().compareTo(" ")==0)){
                    businessHint.setVisibility(View.VISIBLE);
                    mSearchField.setSelection(1);
                }else{
                    businessHint.setVisibility(View.INVISIBLE);
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
                    mSearchVisible=true;

                    if(mLocationField.getText().toString().isEmpty()) {
                        mLocationField.setText(" ");
                        mLocationField.setSelection(mLocationField.getText().toString().length());
                    }else{
                        if(mLocationField.getText().toString().charAt(0)!=' '){
                            mLocationField.setText(" " + mLocationField.getText().toString());
                        }
                        mLocationField.setSelection(1,mLocationField.getText().toString().length());
                    }

                    // Search
                    Location currentLoc = mLocationManager.getLocation();
                    String latLong = String.valueOf(currentLoc.getLatitude());
                    latLong += "," + String.valueOf(currentLoc.getLongitude());
                    mLocationWords = "";

                    try {
                        new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(getActivity())).execute(mLocationWords,
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
                    if(!mLocationField.getText().toString().isEmpty() && mLocationField.getText().toString().charAt(0)==' ') {
                        mLocationField.setText(mLocationField.getText().toString().substring(1));
                    }
                }

            }
        });

        final View.OnKeyListener keyListener =
                (new View.OnKeyListener() {
                    @Override public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        int keyAction = keyEvent.getAction();
                        if (keyAction == KeyEvent.ACTION_UP) {
                            switch (keyCode) {
                                case KeyEvent.FLAG_EDITOR_ACTION:
                                case KeyEvent.KEYCODE_ENTER:
                                    Bundle bundle = new Bundle();
                                    if(!mSearchField.getText().toString().isEmpty() && mSearchField.getText().toString().charAt(0)==' ') {
                                        bundle.putString(BUSINESS, mSearchField.getText().toString().substring(1));
                                    }else{
                                        bundle.putString(BUSINESS, mSearchField.getText().toString());
                                    }
                                    if(!mLocationField.getText().toString().isEmpty() && mLocationField.getText().toString().charAt(0)==' '){;
                                        bundle.putString(LOCATION, mLocationField.getText().toString().substring(1));
                                    }else{
                                        bundle.putString(LOCATION, mLocationField.getText().toString());
                                    }
                                    bundle.putString(BUSINESSTYPE, mBusinessType);
                                    Fragment fragment = new MapBusinessDirectoryFragment();
                                    fragment.setArguments(bundle);
                                    ((NavigationActivity) getActivity()).pushFragment(fragment);

                                    if (mBusinessLayout.getVisibility() == View.GONE) {
                                        mDummyFocusLayout.requestFocus();
                                        mLocationField.setVisibility(View.GONE);
                                        mSearchListView.setVisibility(View.GONE);
                                        mSearchVisible=false;
                                        mBackButton.setVisibility(View.GONE);
                                        mBusinessLayout.setVisibility(View.VISIBLE);
                                        mNearYouContainer.setVisibility(View.VISIBLE);
                                        if(mLoadingVisible){
                                            mViewGroupLoading.setVisibility(View.VISIBLE);
                                        }
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
                mBackButton.setVisibility(View.VISIBLE);
                mSearchVisible=true;
                mBusinessLayout.setVisibility(View.GONE);
                mNearYouContainer.setVisibility(View.GONE);
                mViewGroupLoading.setVisibility(View.GONE);
                mVenueFragmentLayout.setVisibility(View.GONE);

                Location currentLoc = mLocationManager.getLocation();
                String latLong = String.valueOf(currentLoc.getLatitude());
                latLong += "," + String.valueOf(currentLoc.getLongitude());
                if(!editable.toString().isEmpty() && editable.toString().charAt(0)==' ') {
                    mLocationWords = editable.toString().substring(1);
                }else{
                    mLocationWords = editable.toString();
                }

                try {
                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(getActivity())
                            : null);

                    new LocationAutoCompleteAsynctask(cachedLocationSearch).execute(mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(editable.toString().isEmpty() && mLocationField.hasFocus()){
                    editable.append(' ');
                }else if(!editable.toString().isEmpty() && editable.toString().charAt(0)!=' '){
                    mLocationField.setText(" "+editable.toString());
                }

                if( editable.toString().compareTo(" ")==0){
                    locationHint.setVisibility(View.VISIBLE);
                    mLocationField.setSelection(1);
                }else{
                    locationHint.setVisibility(View.GONE);
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
                        ((NavigationActivity) getActivity()).pushFragment(fragment);
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
                    mLocationField.setSelection(1,mLocationField.length());
                    mLocationField.setSelected(false);
                } else {
                    mSearchField.requestFocus();
                    mSearchField.setSelection(1,mSearchField.length());
                    mSearchField.setSelected(false);
                }
            }
        });

        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.INVISIBLE);
        }
        mBackButton.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        // TODO - update directory?
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
            if(getActivity()==null)
                return;

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

//    @Override
    public void onBackPressed() {
        System.out.println("Back Pressed");
        mLocationWords = "";
        if (mBusinessLayout.getVisibility() == View.GONE) {
            System.out.println("Backing out of Search");
            mDummyFocusLayout.requestFocus();
            mLocationField.setVisibility(View.GONE);
            mSearchListView.setVisibility(View.GONE);
            mSearchVisible=false;
            mBusinessLayout.setVisibility(View.VISIBLE);
            mNearYouContainer.setVisibility(View.VISIBLE);
            mBackButton.setVisibility(View.GONE);
            locationHint.setVisibility(View.GONE);
            if(mLoadingVisible){
                mViewGroupLoading.setVisibility(View.VISIBLE);
            }
            mVenueFragmentLayout.setVisibility(View.VISIBLE);
        } else {
            System.out.println("Backing out of App");
//            super.onBackPressed();
        }
    }

    @Override public void onResume() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        if(wentSettings){
            wentSettings = false;
            checkLocationManager();
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
                            ((NavigationActivity) getActivity()).pushFragment(fragment);
                        }
                    }

                    @Override public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
                mMoreButton.setClickable(true);

                if (mIsMoreCategoriesProgressRunning) {
                    if (categories == null) {
                        Toast.makeText(getActivity().getApplicationContext(), "Can not retrieve data",
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

//    private float getStateFromSharedPreferences(String key) {
//        Activity activity = getActivity();
//        if(activity!=null) {
//            SharedPreferences pref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
//            return pref.getFloat(key, -1);
//        }
//        return -1;
//    }
//
//    public double getLatFromSharedPreference() {
//        return (double) getStateFromSharedPreferences(this.LAT_KEY);
//    }
//
//    public double getLonFromSharedPreference() {
//        return (double) getStateFromSharedPreferences(this.LON_KEY);
//    }

    private void checkLocationManager() {

        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());

        //Criteria cri = new Criteria();

        /*final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!gpsEnabled){
            String provider = mLocationManager.getBestProvider(cri, true);
            mCurrentLocation = mLocationManager.getLastKnownLocation(provider);
        }
        if (mCurrentLocation != null) {
            clearSharedPreference();
            writeLatLonToSharedPreference();
        }

        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is disabled. Please Enable GPS to find business near your location.")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            wentSettings = true;
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (getLatFromSharedPreference() == -1 && getLonFromSharedPreference() == -1) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setMessage("Getting location...");
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }*/

    }

//    private void writeLatLonToSharedPreference() {
//        writeValueToSharedPreference(LAT_KEY, (float) mCurrentLocation.getLatitude());
//        writeValueToSharedPreference(LON_KEY, (float) mCurrentLocation.getLongitude());
//    }
//
//    private void writeValueToSharedPreference(String key, float value) {
//        Activity activity = getActivity();
//        if(activity!=null) {
//            SharedPreferences pref = activity.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putFloat(key, value);
//            editor.commit();
//        }
//    }
//
//    private void clearSharedPreference() {
//        Activity activity = getActivity();
//        if(activity!=null) {
//            SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            editor.remove(LAT_KEY);
//            editor.remove(LON_KEY);
//            editor.commit();
//        }
//    }
//

    @Override
    public void onStart(){
        super.onStart();
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

//    @Override public boolean onTouchEvent(MotionEvent event) {
//        return mGestureDetector.onTouchEvent(event);
//    }

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
//                    finish();
                    return true;
                }
            }

        }

        return false;
    }
}
