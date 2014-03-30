
package com.airbitz.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.LruCache;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.activities.DirectoryDetailActivity;
import com.airbitz.activities.MapBusinessDirectoryActivity;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.BusinessSearchResult;
import com.airbitz.models.SearchResult;
import com.airbitz.utils.ListViewUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class VenueFragment extends Fragment implements
                                           GestureDetector.OnGestureListener,
                                           BusinessDirectoryActivity.BusinessScrollListener {

    public static final String TAG = VenueFragment.class.getSimpleName();
    private ListView mVenueListView;
    private List<BusinessSearchResult> mVenues;
    private List<BusinessSearchResult> mTempVenues;
    private boolean mLoadFlag = false;
    private String mNextUrl = "null";
    private boolean isGettingMoreVenueFinished = true;
    private boolean isFirstLoad = true;
    private VenueAdapter mVenueAdapter;
    private TextView mNoResultView;
//    private View mLoadingFooterView;

    private String mLocationName;
    private String mBusinessName;
    private String mBusinessType;

    private GetVenuesTask mGetVenuesTask;
    private GestureDetector mGestureDetector;

    private LruCache<String, Bitmap> mMemoryCache;

    private boolean mIsInBusinessDirectory = false;

    public int getMemoryCacheSize() {
        return mMemoryCache.size();
    }

    public boolean getIsBusinessDirectory() {
        return mIsInBusinessDirectory;
    }

    public boolean isMemoryCacheFull(int byteCount) {
        int allowedSize = byteCount / 1024;
        return mMemoryCache.maxSize() - mMemoryCache.size() <= allowedSize;
    }

    public ListView getVenueListView() {
        return mVenueListView;
    }

    public List<BusinessSearchResult> getVenues() {
        return mVenues;
    }

    public void setVenues(List<BusinessSearchResult> venues) {
        mVenues = venues;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_venue, container, false);
        mGestureDetector = new GestureDetector(this);

        // Set-up list
        mVenueListView = (ListView) view.findViewById(R.id.listView);
        mNoResultView = (TextView) view.findViewById(R.id.no_result_view);
//        mLoadingFooterView = inflater.inflate(R.layout.loading_indicator, null, false);

        // Set-up list adapter
//        mVenueListView.addFooterView(mLoadingFooterView);
        mVenues = new ArrayList<BusinessSearchResult>();
        mVenueAdapter = new VenueAdapter(getActivity(), mVenues, VenueFragment.this);
        mVenueListView.setAdapter(mVenueAdapter);
//        mVenueListView.removeFooterView(mLoadingFooterView);

        mVenueListView.setVisibility(View.INVISIBLE);
        mVenueListView.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        if (getActivity().getClass().toString().equalsIgnoreCase(BusinessDirectoryActivity.class.toString())) {
            String latLon = "" + getLatFromSharedPreference() + "," + getLonFromSharedPreference();

            mIsInBusinessDirectory = true;
            mGetVenuesTask = new GetVenuesTask(getActivity());
            mGetVenuesTask.execute(latLon);
            BusinessDirectoryActivity businessDirectoryActivity = (BusinessDirectoryActivity) getActivity();
            businessDirectoryActivity.setBusinessScrollListener(this);
        } else if (getActivity().getClass()
                                .toString()
                                .equalsIgnoreCase(MapBusinessDirectoryActivity.class.toString())) {

            mIsInBusinessDirectory = false;
            mLocationName = getActivity().getIntent().getStringExtra(BusinessDirectoryActivity.LOCATION);
            mBusinessType = getActivity().getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESSTYPE);
            mBusinessName = getActivity().getIntent().getStringExtra(BusinessDirectoryActivity.BUSINESS);

            mGetVenuesTask = new GetVenuesTask(getActivity());
            mGetVenuesTask.execute(mBusinessName, mLocationName, mBusinessType);
            // if(mLocationName.length()>0){
            // mGetVenuesTask.execute(mLocationName);
            // } else if(mCategoryName.length()>0){
            // mGetVenuesTask.execute(mCategoryName);
            // } else if(mBusinessName.length()>0){
            // mGetVenuesTask.execute(mBusinessName);
            // } else {
            // String latlong =
            // ""+getLatFromSharedPreference()+","+getLonFromSharedPreference();
            // mGetVenuesTask.execute(latlong);
            // }
        } else {
            mIsInBusinessDirectory = false;
            mGetVenuesTask = new GetVenuesTask(getActivity());
            String latlong = "" + getLatFromSharedPreference() + "," + getLonFromSharedPreference();
            mGetVenuesTask.execute(latlong);
        }

        int timeout = 15000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override public void run() {
                if (mGetVenuesTask.getStatus() == AsyncTask.Status.RUNNING)
                    mGetVenuesTask.cancel(true);
            }
        }, timeout);

        ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);

        return view;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override public void onScrollEnded() {
        if (isFirstLoad) {
            isFirstLoad = false;
            GetRemainingFirstVenuesTask getRemainingFirstVenuesTask = new GetRemainingFirstVenuesTask(getActivity());
            getRemainingFirstVenuesTask.execute("");
        } else {
            if (isGettingMoreVenueFinished) {
                isGettingMoreVenueFinished = false;

                GetMoreVenuesTask getMoreVenuesTask = new GetMoreVenuesTask(getActivity());
                getMoreVenuesTask.execute(mNextUrl);
            }
        }
    }

    private class GetVenuesTask extends AsyncTask<String, Void, String> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;

        public GetVenuesTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
//            mVenueListView.addFooterView(mLoadingFooterView);
            ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);
        }

        @Override protected String doInBackground(String... params) {
            String result = "";
            if (getActivity().getClass()
                             .toString()
                             .equalsIgnoreCase(BusinessDirectoryActivity.class.toString())) {

                result = mApi.getSearchByLatLong(params[0], "", "", "1");

            } else if (getActivity().getClass()
                                    .toString()
                                    .equalsIgnoreCase(MapBusinessDirectoryActivity.class.toString())) {
                // if(mLocationName.length()>0){
                // result = mApi.getSearchByLocation(params[0],"","","");
                // } else if(mCategoryName.length()>0){
                // result = mApi.getSearchByCategory(params[0],"","","");
                // } else if(mBusinessName.length()>0){
                // result = mApi.getSearchByTerm(params[0],"","","");
                // } else {
                // result = mApi.getSearchByLatLong(params[0],"","","");
                // }
                String latlong = "" + getLatFromSharedPreference() + "," + getLonFromSharedPreference();
                result = mApi.getSearchByCategoryOrBusinessAndLocation(params[0], params[1], "", "",
                                                                       "1", params[2], latlong);
            }

            return result;
        }

        @Override protected void onCancelled() {
//            mVenueListView.removeFooterView(mLoadingFooterView);
            mNoResultView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, "Can not retrieve data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
//            mProgressDialog.dismiss();
            try {
                SearchResult results = new SearchResult(new JSONObject(searchResult));

                mNextUrl = "null";
                mTempVenues = new ArrayList<BusinessSearchResult>();
                if (results != null) {
                    mNextUrl = results.getNextLink();
                    mTempVenues = results.getBusinessSearchObjectArray();
                }

                if (mTempVenues.isEmpty()) {
                    mNoResultView.setVisibility(View.VISIBLE);
                } else {

                    if (mIsInBusinessDirectory) {

                        if (mTempVenues.size() >= 5) {
                            for (int i = 0; i < 5; i++) {
                                mVenues.add(mTempVenues.get(i));
                            }

                            for (int i = 4; i >= 0; i--) {
                                mTempVenues.remove(i);
                            }
                        } else {
                            for (int i = 0; i < mTempVenues.size(); i++) {
                                mVenues.add(mTempVenues.get(i));
                            }
                            for (int i = mTempVenues.size() - 1; i >= 0; i--) {
                                mTempVenues.remove(i);
                            }
                        }
                    } else {
                        mVenues = mTempVenues;
                    }
                    mNoResultView.setVisibility(View.GONE);
                }

                mVenueListView.setVisibility(View.VISIBLE);
                mVenueAdapter.notifyDataSetChanged();

                mVenueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override public void onScrollStateChanged(AbsListView absListView, int i) {

                    }

                    @Override public void onScroll(AbsListView view,
                                                   int firstVisibleItem,
                                                   int visibleItemCount,
                                                   int totalItemCount) {

                        if (!mNextUrl.equalsIgnoreCase("null")) {
                            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                                if (mLoadFlag == false) {
                                    mLoadFlag = true;
                                    if (!mIsInBusinessDirectory) {

                                        GetMoreVenuesTask getMoreVenuesTask = new GetMoreVenuesTask(getActivity());
                                        getMoreVenuesTask.execute(mNextUrl);
                                    }
                                }
                            }
                        }
                    }
                });

                mVenueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getActivity(), DirectoryDetailActivity.class);
                        int firstVisiblePosition = mVenueListView.getFirstVisiblePosition();
                        intent.putExtra("bizId", mVenues.get(i).getId());
                        intent.putExtra("bizName", mVenues.get(i).getName());
                        intent.putExtra("bizDistance", mVenues.get(i).getDistance());
                        startActivity(intent);
                    }
                });
                if (mIsInBusinessDirectory) {
                    ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);
                }
            } catch (JSONException e) {
                mNoResultView.setVisibility(View.VISIBLE);
                e.printStackTrace();
                this.cancel(true);
            } catch (Exception e) {
                mNoResultView.setVisibility(View.VISIBLE);
                e.printStackTrace();
                this.cancel(true);
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
//            mVenueListView.addFooterView(mLoadingFooterView);
            ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);
        }

        @Override protected String doInBackground(String... params) {
            if (params[0].equalsIgnoreCase("null")) {
                return "";
            }
            return mApi.getRequest(params[0]);
        }

        @Override protected void onCancelled() {
//            mVenueListView.removeFooterView(mLoadingFooterView);
            mNoResultView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, "Can not retrieve data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String searchResult) {
            if (!searchResult.isEmpty()) {
                try {
                    mLoadFlag = false;
                    SearchResult results = new SearchResult(new JSONObject(searchResult));
                    mNextUrl = results.getNextLink();
                    mVenues.addAll(results.getBusinessSearchObjectArray());
                    mVenueAdapter.notifyDataSetChanged();
                    mVenueListView.setVisibility(View.VISIBLE);

                    mVenueListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override public void onScrollStateChanged(AbsListView absListView, int i) {

                        }

                        @Override public void onScroll(AbsListView view,
                                                       int firstVisibleItem,
                                                       int visibleItemCount,
                                                       int totalItemCount) {

                            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                                if (mLoadFlag == false) {
                                    mLoadFlag = true;
                                    if (!mIsInBusinessDirectory) {

                                        GetMoreVenuesTask getMoreVenuesTask = new GetMoreVenuesTask(getActivity());
                                        getMoreVenuesTask.execute(mNextUrl);
                                    }
                                }
                            }
                        }
                    });

                    mVenueListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Intent intent = new Intent(getActivity(), DirectoryDetailActivity.class);
                            int firstVisiblePosition = mVenueListView.getFirstVisiblePosition();
                            intent.putExtra("bizId", mVenues.get(i).getId());
                            intent.putExtra("bizName", mVenues.get(i).getName());
                            intent.putExtra("bizDistance", mVenues.get(i).getDistance());
                            startActivity(intent);
                        }
                    });
//                    mVenueListView.removeFooterView(mLoadingFooterView);

                    if (mIsInBusinessDirectory) {
                        ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    this.cancel(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    this.cancel(true);

                }
            }
            isGettingMoreVenueFinished = true;
        }
    }

    private class GetRemainingFirstVenuesTask extends AsyncTask<String, Void, List<BusinessSearchResult>> {

        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetRemainingFirstVenuesTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting more venues list...");
            mProgressDialog.setIndeterminate(true);
            // mProgressDialog.setCancelable(false);
            if (mIsInBusinessDirectory) {
                mProgressDialog.show();
            } else {
                mProgressDialog.show();
            }
        }

        @Override protected List<BusinessSearchResult> doInBackground(String... params) {
            return mTempVenues;
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            mNoResultView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, "Can not retrieve data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(List<BusinessSearchResult> searchResult) {
            if (!searchResult.isEmpty()) {

                mVenues.addAll(mTempVenues);
                mVenueAdapter.notifyDataSetChanged();
                ListViewUtility.setListViewHeightBasedOnChildren(mVenueListView);

            }
            mProgressDialog.dismiss();
        }
    }

    private float getStateFromSharedPreferences(String key) {
        SharedPreferences pref = getActivity().getSharedPreferences(BusinessDirectoryActivity.PREF_NAME,
                                                                    Context.MODE_PRIVATE);
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
                    getActivity().finish();
                    return true;
                }
            }

        }
        return false;
    }

}
