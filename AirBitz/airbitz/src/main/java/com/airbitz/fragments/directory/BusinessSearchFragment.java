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
import android.app.FragmentTransaction;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.BusinessSearchAdapter;
import com.airbitz.adapters.LocationAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.models.Business;
import com.airbitz.models.LocationSearchResult;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.utils.CacheUtil;

import java.util.List;
import java.util.LinkedList;

public class BusinessSearchFragment extends BaseFragment implements
        NavigationActivity.OnBackPress,
        CurrentLocationManager.OnCurrentLocationChange {

    String TAG = getClass().getSimpleName();

    private ImageButton mSearchButton;
    private View mLoading;
    private EditText mQueryField;
    private EditText mLocationField;
    private LocationAdapter mLocationAdapter;
    private BusinessSearchAdapter mBusinessSearchAdapter;
    private BusinessAutoCompleteAsynctask mBusinessAutoCompleteAsyncTask;
    private LocationAutoCompleteAsynctask mLocationAutoCompleteAsyncTask;
    private CurrentLocationManager mLocationManager;
    private List<LocationSearchResult> mLocationList;
    private List<Business> mBusinessList;
    private ListView mSearchListView;
    private Location mCurrentLocation = null;
    private String mBusinessType = "business";
    private String mLocationWords = "";
    private boolean locationEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mBusinessList == null) {
            mBusinessList = new LinkedList<Business>();
        }
        if (mLocationList == null) {
            mLocationList = new LinkedList<LocationSearchResult>();
        }
        if (mLocationManager == null) {
            mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        }
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_business_search, container, false);

        mSearchButton = (ImageButton) view.findViewById(R.id.search_button);
        mQueryField = (EditText) view.findViewById(R.id.query);
        mLocationField = (EditText) view.findViewById(R.id.location);
        mLocationField.setText(R.string.current_location);
        mSearchListView = (ListView) view.findViewById(R.id.list);
        mLoading = view.findViewById(R.id.empty);

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });
        mQueryField.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    String latLong = "";
                    if (locationEnabled) {
                        Location currentLoc = mLocationManager.getLocation();
                        latLong = String.valueOf(currentLoc.getLatitude());
                        latLong += "," + String.valueOf(currentLoc.getLongitude());
                    }
                    // Only include cached searches if text is empty.
                    final String query = editable.toString();
                    List<Business> cachedBusinesses = (TextUtils.isEmpty(query)
                            ? CacheUtil.getCachedBusinessSearchData(getActivity())
                            : null);
                    if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mBusinessAutoCompleteAsyncTask.cancel(true);
                    }
                    mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusinesses);
                    mBusinessAutoCompleteAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query, mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mLocationField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mSearchListView.setAdapter(mLocationAdapter);
                    try {
                        String latLong = "";
                        if (locationEnabled) {
                            Location currentLoc = mLocationManager.getLocation();
                            latLong = String.valueOf(currentLoc.getLatitude());
                            latLong += "," + String.valueOf(currentLoc.getLongitude());
                        }
                        mLocationWords = "";
                        if (mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                            mLocationAutoCompleteAsyncTask.cancel(true);
                        }
                        mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(CacheUtil.getCachedLocationSearchData(getActivity()));
                        mLocationAutoCompleteAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationWords, latLong);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mQueryField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    return;
                }
                mSearchListView.setAdapter(mBusinessSearchAdapter);
                try {
                    final String text = mQueryField.getText().toString();
                    final List<Business> cachedBusiness = (!TextUtils.isEmpty(text)
                            ? null
                            : CacheUtil.getCachedBusinessSearchData(getActivity()));
                    if (mBusinessAutoCompleteAsyncTask != null && mBusinessAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mBusinessAutoCompleteAsyncTask.cancel(true);
                    }
                    String latLong = getCurrentLocationString();
                    mBusinessAutoCompleteAsyncTask = new BusinessAutoCompleteAsynctask(cachedBusiness);
                    mBusinessAutoCompleteAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text, mLocationWords, latLong);
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
            }
        });
        mQueryField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mActivity.hideSoftKeyboard(textView);
                    submitForm();
                    return true;
                }
                return false;
            }
        });
        mLocationField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mActivity.hideSoftKeyboard(textView);
                    submitForm();
                    return true;
                }
                return false;
            }
        });

        mLocationField.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                String latLong = getCurrentLocationString();
                try {
                    mLocationWords = editable.toString();

                    List<LocationSearchResult> cachedLocationSearch = (TextUtils.isEmpty(mLocationWords)
                            ? CacheUtil.getCachedLocationSearchData(getActivity())
                            : null);
                    if (mLocationAutoCompleteAsyncTask != null && mLocationAutoCompleteAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mLocationAutoCompleteAsyncTask.cancel(true);
                    }
                    mLocationAutoCompleteAsyncTask = new LocationAutoCompleteAsynctask(cachedLocationSearch);
                    mLocationAutoCompleteAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLocationWords, latLong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long l) {
                boolean locationFieldShouldFocus = false;

                if (mQueryField.isFocused()) {
                    final BusinessSearchAdapter businessSearchAdapter = (BusinessSearchAdapter) mSearchListView.getAdapter();
                    final Business business = businessSearchAdapter.getItem(position);

                    mQueryField.setText(business.getName());
                    mBusinessType = business.getType();

                    if ("business".equalsIgnoreCase(mBusinessType)) {
                        DirectoryDetailFragment.pushFragment(mActivity, business.getId(), business.getName(), null);
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
                    mQueryField.requestFocus();
                }
            }
        });
        mBusinessSearchAdapter = new BusinessSearchAdapter(mActivity, mBusinessList);
        mLocationAdapter = new LocationAdapter(mActivity, mLocationList);
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
        BusinessSearchFragment.popFragment(mActivity);
        return true;
    }

    @Override
    public void OnCurrentLocationChange(Location location) {
        mCurrentLocation = location;
        // if query is focuses, requery
    }

    @Override
    public void onResume() {
        super.onResume();
        locationEnabled = CurrentLocationManager.locationEnabled(mActivity);
        mCurrentLocation = mLocationManager.getLocation();
        mLocationManager.addLocationChangeListener(this);

        mQueryField.requestFocus();
        mActivity.showSoftKeyboard(mQueryField);
        if (!TextUtils.isEmpty(mQueryField.getText().toString())) {
            mQueryField.setSelection(mQueryField.getText().toString().length());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBusinessAutoCompleteAsyncTask != null) {
            mBusinessAutoCompleteAsyncTask.cancel(true);
        }
        if (mLocationAutoCompleteAsyncTask != null) {
            mLocationAutoCompleteAsyncTask.cancel(true);
        }
        mLocationManager.removeLocationChangeListener(this);
    }

    private void submitForm() {
        MapBusinessDirectoryFragment.pushFragment(
            mActivity, mQueryField.getText().toString(),
            mLocationField.getText().toString(), mBusinessType);
    }

    class BusinessAutoCompleteAsynctask extends AsyncTask<String, Integer, List<Business>> {
        private AirbitzAPI api = AirbitzAPI.getApi();
        private List<Business> mCacheData = null;

        public BusinessAutoCompleteAsynctask(List<Business> cacheData) {
            mCacheData = cacheData;
        }

        @Override
        protected void onPreExecute() {
            setLoading(true);
        }

        @Override
        protected List<Business> doInBackground(String... strings) {
            List<Business> jsonParsingResult =
                api.getHttpAutoCompleteBusiness(strings[0], strings[1], strings[2]);
            return jsonParsingResult;
        }

        @Override
        protected void onPostExecute(List<Business> businesses) {
            if (mActivity == null) {
                return;
            }
            setLoading(false);

            mBusinessList.clear();
            if (businesses == null) {
                mBusinessList.add(new Business("No Results Found", "", ""));
            } else {
                mBusinessList.addAll(businesses);
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
        protected void onCancelled(List<Business> jSONResult) {
            mBusinessAutoCompleteAsyncTask = null;
            super.onCancelled();
        }
    }

    class LocationAutoCompleteAsynctask extends AsyncTask<String, Integer, List<LocationSearchResult>> {

        private List<LocationSearchResult> mCacheData = null;
        private AirbitzAPI api = AirbitzAPI.getApi();

        public LocationAutoCompleteAsynctask(List<LocationSearchResult> cacheData) {
            mCacheData = cacheData;
        }

        @Override
        protected void onPreExecute() {
            setLoading(true);
        }

        @Override
        protected List<LocationSearchResult> doInBackground(String... strings) {
            return api.getHttpAutoCompleteLocation(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(List<LocationSearchResult> result) {
            if (mActivity == null) {
                return;
            }
            setLoading(false);

            mLocationList.clear();
            mLocationList.add(new LocationSearchResult(getString(R.string.current_location), false));
            mLocationList.add(new LocationSearchResult(getString(R.string.on_the_web), false));

            if (result == null) {
                mLocationList.add(new LocationSearchResult("No Results Found", false));
            } else {
                if (mCacheData != null) {
                    for (LocationSearchResult location : mCacheData) {
                        if (!mLocationList.contains(location)) {
                            mLocationList.add(0, location);
                        }
                    }
                }
                for (LocationSearchResult l : result) {
                    if (!mLocationList.contains(l)) {
                        mLocationList.add(l);
                    }
                }
            }
            mLocationAdapter.notifyDataSetChanged();
            mLocationAutoCompleteAsyncTask = null;
        }

        @Override
        protected void onCancelled(List<LocationSearchResult> JSONResult) {
            super.onCancelled();
            mLocationAutoCompleteAsyncTask = null;
        }
    }

    private String getCurrentLocationString() {
        String latLong = "";
        if (locationEnabled) {
            if (mCurrentLocation == null) {
                mCurrentLocation = mLocationManager.getLocation();
            }
            if (mCurrentLocation != null) {
                latLong = String.valueOf(mCurrentLocation.getLatitude());
                latLong += "," + String.valueOf(mCurrentLocation.getLongitude());
            }
        }
        return latLong;
    }

    private void setLoading(boolean loading) {
        mSearchListView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        mLoading.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }

    static class TextWatcherAdapter implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    public static void pushFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        BusinessSearchFragment fragment = new BusinessSearchFragment();
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.fade_out);

        mActivity.popFragment(transaction);
        mActivity.getFragmentManager().executePendingTransactions();
    }
}
