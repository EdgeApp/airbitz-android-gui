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

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.adapters.MoreCategoryAdapter;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.models.Business;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.Categories;
import com.airbitz.models.Category;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.utils.CacheUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class BusinessDirectoryFragment extends BaseFragment implements
        CurrentLocationManager.OnCurrentLocationChange {

    String TAG = getClass().getSimpleName();

    static int CATEGORY_TIMEOUT = 15000;
    static int LOCATION_TIMEOUT = 10000;
    static float LOCATION_ACCURACY_METERS = 100.0f;

    public static final String LOCATION = "LOCATION";
    public static final String BUSINESS = "BUSINESS";
    public static final String BUSINESSTYPE = "BUSINESSTYPE";

    static final int MAX_VENUES = 500;
    static final int PAGE_SIZE = 20;
    static final int VENUE_LOAD_AHEAD = 3;

    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;

    private static String mLocationWords = "";
    private static String mBusinessType = "business";

    Handler mHandler = new Handler();
    View view;
    private Categories mCategories;
    private LinearLayout mNearYouLayout;
    private View mFragHeader;
    private ViewGroup mBusinessLayout;
    private TextView mRestaurantButton;
    private TextView mGiftButton;
    private TextView mCoffeeButton;
    private TextView mElecButton;
    private TextView mAtmButton;
    private TextView mShoppingButton;
    private TextView mMoreButton;
    private boolean locationEnabled = false;
    private List<BusinessSearchResult> mVenuesLoaded;
    private TextView mTitleView;
    private ListView mVenueListView;
    private VenueAdapter mVenueAdapter;
    private Spinner mMoreSpinner;
    private View mSearchLoading;
    private CurrentLocationManager mLocationManager;
    private ViewGroup mViewGroupLoading;
    private TextView mNoResultView;
    private String mNextUrl = "null";
    private MoreCategoryAdapter mMoreCategoryAdapter;
    private BusinessCategoryAsyncTask mBusinessCategoryAsynctask;
    private VenuesTask mVenuesTask;
    private boolean mFirstLoad = true;
    private ProgressDialog mMoreCategoriesProgressDialog;
    private Handler mVenueHandler = new Handler();
    private Location mCurrentLocation = null;

    Runnable mProgressTimeout = new Runnable() {
        @Override
        public void run() {
            if (mMoreCategoriesProgressDialog != null && mMoreCategoriesProgressDialog.isShowing()) {
                mMoreCategoriesProgressDialog.dismiss();
            }
            mMoreCategoriesProgressDialog = null;
        }
    };

    Runnable mLocationTimeout = new Runnable() {
        @Override
        public void run() {
            hideLoadingIndicator();
            queryWithoutLocation();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mVenuesLoaded == null) {
            mVenuesLoaded = new ArrayList<BusinessSearchResult>();
        }
        if (mLocationManager == null) {
            mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_business_directory, container, false);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        getBaseActivity().setSupportActionBar(toolbar);

        latoBlackTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Montserrat-Bold.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/Lato-RegIta.ttf");
        latoRegularTypeFace = Typeface.createFromAsset(getActivity().getAssets(), "font/HelveticaNeue.ttf");

        mVenueListView = (ListView) view.findViewById(R.id.fragment_layout);
        mNearYouLayout = (LinearLayout) view.findViewById(R.id.layout_near_you_sticky);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mTitleView.setText(R.string.directory_title);

        // Add a header
        mBusinessLayout = (ViewGroup) inflater.inflate(R.layout.inc_directory_categories, null, false);
        mVenueListView.addHeaderView(mBusinessLayout, null, false);

        // Add a footer
        mViewGroupLoading = (ViewGroup) inflater.inflate(R.layout.loading_indicator, null, false);
        mVenueListView.addFooterView(mViewGroupLoading);

        // Setup venues adapter and listview
        mVenueAdapter = new VenueAdapter(getActivity(), mVenuesLoaded);
        mVenueListView.setAdapter(mVenueAdapter);
        mVenueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int newIdx = i - 1;
                if(newIdx >= 0 && newIdx < mVenuesLoaded.size()) {
                    showDirectoryDetailFragment(mVenuesLoaded.get(newIdx).getId(),
                            mVenuesLoaded.get(newIdx).getName(),
                            mVenuesLoaded.get(newIdx).getDistance());
                }
            }
        });

        mFragHeader = view.findViewById(R.id.toolbar);

        mVenueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (!mNextUrl.equalsIgnoreCase("null")) {
                    if (firstVisibleItem + visibleItemCount + VENUE_LOAD_AHEAD >= totalItemCount && totalItemCount != 0) {
                        if (mVenuesTask == null && mVenuesLoaded.size() <= MAX_VENUES) {
                            mVenuesTask = new VenuesTask(getActivity(), null);
                            mVenuesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mNextUrl);
                        }
                    }
                }
                updateNearYouSticky();
            }
        });

        mRestaurantButton = (TextView) mBusinessLayout.findViewById(R.id.button_restaurant);
        mGiftButton = (TextView) mBusinessLayout.findViewById(R.id.button_gift_cards);
        mCoffeeButton = (TextView) mBusinessLayout.findViewById(R.id.button_coffee_tea);
        mElecButton = (TextView) mBusinessLayout.findViewById(R.id.button_electronics);
        mAtmButton = (TextView) mBusinessLayout.findViewById(R.id.button_atms);
        mShoppingButton = (TextView) mBusinessLayout.findViewById(R.id.button_shopping);
        mMoreButton = (TextView) mBusinessLayout.findViewById(R.id.button_more);
        mMoreButton.setClickable(false);
        mMoreSpinner = (Spinner) mBusinessLayout.findViewById(R.id.spinner_more_categories);
        mSearchLoading = view.findViewById(R.id.business_directory_search_loading);

        mNoResultView = (TextView) view.findViewById(R.id.business_fragment_no_result_view);
        mNearYouLayout = (LinearLayout) view.findViewById(R.id.layout_near_you_sticky);

        ((TextView)view.findViewById(R.id.textview_nearyou_sticky)).setTypeface(latoRegularTypeFace);

        mMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMoreCategoryAdapter != null) {
                    mMoreSpinner.setVisibility(View.INVISIBLE);
                    mMoreSpinner.performClick();
                } else {
                    mMoreCategoriesProgressDialog = new ProgressDialog(getActivity());
                    mMoreCategoriesProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mMoreCategoriesProgressDialog.setMessage("Retrieving data...");
                    mMoreCategoriesProgressDialog.setIndeterminate(true);
                    mMoreCategoriesProgressDialog.setCancelable(false);
                    mMoreCategoriesProgressDialog.show();
                }
            }
        });

        mRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mActivity.getString(R.string.restaurant_and_food_trucks),
                    mActivity.getString(R.string.current_location),
                    "category");
            }
        });

        mGiftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mGiftButton.getText().toString(),
                    mActivity.getString(R.string.on_the_web),
                    "category");
            }
        });

        mCoffeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mCoffeeButton.getText().toString(),
                    mActivity.getString(R.string.current_location),
                    "category");
            }
        });

        mElecButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mElecButton.getText().toString(),
                    mActivity.getString(R.string.on_the_web),
                    "category");
            }
        });

        mAtmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mAtmButton.getText().toString(),
                    mActivity.getString(R.string.current_location),
                    "category");
            }
        });

        mShoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapBusinessDirectoryFragment.pushFragment(mActivity,
                    mShoppingButton.getText().toString(),
                    mActivity.getString(R.string.current_location),
                    "category");
            }
        });

        mNoResultView.setVisibility(View.GONE);
        checkLocationManager();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_directory, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_help:
            mActivity.pushFragment(
                new HelpFragment(HelpFragment.INFO), NavigationActivity.Tabs.BD.ordinal());
            return true;
        case R.id.action_search:
            BusinessSearchFragment.pushFragment(mActivity);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void updateNearYouSticky() {
        int firstVisibleItem = mVenueListView.getFirstVisiblePosition();
        View first = mVenueListView.getChildAt(1); // first item not the header
        if(first != null && firstVisibleItem==0) {
            mNearYouLayout.setY(first.getY() + mFragHeader.getMeasuredHeight());
        }
        else if(firstVisibleItem > 0) {
            mNearYouLayout.setY(mVenueListView.getY() + mFragHeader.getMeasuredHeight());
        }
        mNearYouLayout.invalidate();
    }

    public void queryWithoutLocation() {
        Log.d(TAG, "Query without location");
        if (mVenuesTask != null && mVenuesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mVenuesTask.cancel(true);
        }
        mVenuesTask = new VenuesTask(getActivity(), ""); // pass in empty lat/lng
        mVenuesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mHandler.removeCallbacks(mLocationTimeout);
        if (location != null && location.getAccuracy() < LOCATION_ACCURACY_METERS) {
            mCurrentLocation = location;
            String latLon = "";
            if (location != null) {
                latLon = "" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
            }
            Log.d(TAG, "LocationManager Location = " + latLon);

            mVenueHandler.removeCallbacks(null);
            if (mVenuesTask != null && mVenuesTask.getStatus() == AsyncTask.Status.RUNNING) {
                mVenuesTask.cancel(true);
            }
            mVenuesTask = new VenuesTask(getActivity(), latLon);
            mVenuesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onResume() {
        updateNearYouSticky();
        if (mCategories != null) {
            updateMoreSpinner(mCategories);
        }
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        if (mSearchLoading != null) {
            mSearchLoading.setVisibility(View.GONE);
        }
        mCurrentLocation = mLocationManager.getLocation();
        checkLocationManager();
        mLocationManager.addLocationChangeListener(this);
        if (!locationEnabled) {
            // if no venues, then request location
            queryWithoutLocation();
        } else {
            mHandler.postDelayed(mLocationTimeout, LOCATION_TIMEOUT);
        }
        // If we don't have categories, fetch them
        if (mCategories == null || mCategories.getCountValue() == 0) {
            try {
                mBusinessCategoryAsynctask = new BusinessCategoryAsyncTask();
                mBusinessCategoryAsynctask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "level");
                mHandler.postDelayed(mProgressTimeout, CATEGORY_TIMEOUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

    @Override
    public void onStop() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        if (mMoreSpinner != null) {
            mMoreSpinner.setVisibility(View.GONE);
        }
        mLocationManager.removeLocationChangeListener(this);
        if (mVenuesTask != null && mVenuesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mVenuesTask.cancel(true);
        }
        mFirstLoad = true;
        super.onPause();
    }

    private void updateMoreSpinner(Categories categories) {
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
            mCategories = categories;

            mMoreCategoryAdapter = new MoreCategoryAdapter(getActivity(), mCategories);
            mMoreSpinner.setAdapter(mMoreCategoryAdapter);
            mMoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView,
                                           View view, int position, long l) {

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

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            mMoreButton.setClickable(true);

            // If we are displaying a dialog, open up the spinner
            if (mMoreCategoriesProgressDialog != null && mMoreCategoriesProgressDialog.isShowing()) {
                if (categories == null) {
                    if(getActivity() != null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.fragment_business_cannot_retrieve_data), Toast.LENGTH_LONG).show();
                    }
                }
                mMoreCategoriesProgressDialog.dismiss();
                mMoreCategoriesProgressDialog = null;

                mMoreSpinner.setVisibility(View.INVISIBLE);
                mMoreSpinner.performClick();
            }

            // Change the more button to open up categories
            mMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMoreSpinner.setVisibility(View.INVISIBLE);
                    mMoreSpinner.performClick();
                }
            });
        } else {
            mMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getActivity() != null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                getString(R.string.fragment_business_no_categories_retreived), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void hideLoadingIndicator() {
        if (mViewGroupLoading != null) {
            mViewGroupLoading.setVisibility(View.GONE);
        }
    }

    public void showLoadingIndicator() {
        if (mViewGroupLoading != null) {
            mViewGroupLoading.setVisibility(View.VISIBLE);
        }
    }

    public Categories getMoreBusinessCategory(Categories initial, String link) {
        while (!link.equalsIgnoreCase("null")) {

            String jSOnString = AirbitzAPI.getApi().getRequest(link);
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
        if (mBusinessCategoryAsynctask != null) {
            mBusinessCategoryAsynctask.cancel(true);
        }
    }

    private void checkLocationManager() {
        locationEnabled = CurrentLocationManager.locationEnabled(getActivity());
        if(!locationEnabled) {
            if(AirbitzApplication.getLocationWarn() && getActivity() != null) {
                Toast.makeText(getActivity(), getString(R.string.fragment_business_enable_location_services), Toast.LENGTH_SHORT).show();
                AirbitzApplication.setLocationWarn(false);
            }
        } else {
            AirbitzApplication.setLocationWarn(true);
        }
    }

    private void showDirectoryDetailFragment(String id, String name, String distance) {
        DirectoryDetailFragment.pushFragment(mActivity, id, name, distance);
    }

    public void setVenueListView(List<BusinessSearchResult> venues) {
        if (venues != null) {
            mVenueListView.setVisibility(View.VISIBLE);
            mNoResultView.setVisibility(View.GONE);
        } else {
            mVenueListView.setVisibility(View.GONE);
            mNoResultView.setVisibility(View.VISIBLE);
            mSearchLoading.setVisibility(View.GONE);
        }
        if (!venues.isEmpty()) {
            mVenuesLoaded.addAll(venues);
            if (venues.size() <= PAGE_SIZE) {
                mVenueAdapter.warmupCache(venues);
            }
            mVenueAdapter.notifyDataSetChanged();
        }
    }

    class BusinessCategoryAsyncTask extends AsyncTask<String, Integer, Categories> {
        private AirbitzAPI api = AirbitzAPI.getApi();

        @Override
        protected Categories doInBackground(String... strings) {
            Categories jsonParsingResult = null;
            try {
                jsonParsingResult = api.getHttpCategories(strings[0]);
                String nextUrl = jsonParsingResult.getNextLink();
                mCategories = jsonParsingResult;
                getMoreBusinessCategory(mCategories, nextUrl);
            } catch (Exception e) {

            }
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(Categories categories) {
            if (getActivity() == null) {
                return;
            }
            updateMoreSpinner(categories);
        }
    }

    private class VenuesTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        String mLatLng;

        public VenuesTask(Context context, String latlng) {
            mContext = context;
            mLatLng = latlng;
            showLoadingIndicator();
        }

        @Override
        protected String doInBackground(String... params) {
            if (mLatLng != null && !mLatLng.isEmpty()) {
                return mApi.getSearchByLatLong(mLatLng, String.valueOf(PAGE_SIZE), "", "1");
            } else if (params.length > 0 && !params[0].equalsIgnoreCase("null")) {
                return mApi.getRequest(params[0]);
            } else {
                return mApi.getSearchByTerm("", String.valueOf(PAGE_SIZE), "", "1");
            }
        }

        @Override
        protected void onCancelled() {
            mNoResultView.setVisibility(View.VISIBLE);
            mSearchLoading.setVisibility(View.GONE);
            hideLoadingIndicator();
            mVenuesTask = null;
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String searchResult) {
            if (!searchResult.isEmpty()) {
                try {
                    SearchResult results = new SearchResult(new JSONObject(searchResult));
                    mNextUrl = results.getNextLink();
                    setVenueListView(results.getBusinessSearchObjectArray());
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
            if (mVenuesLoaded.size() >= MAX_VENUES) {
                hideLoadingIndicator();
            }
            mVenuesTask = null;
        }
    }
}
