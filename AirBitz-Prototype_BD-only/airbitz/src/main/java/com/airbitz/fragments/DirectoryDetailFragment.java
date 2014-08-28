package com.airbitz.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Category;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.Hour;
import com.airbitz.models.Location;
import com.airbitz.utils.Common;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class DirectoryDetailFragment extends Fragment {

    private static final String TAG = DirectoryDetailFragment.class.getSimpleName();
    public static final String BIZID = "bizId";
    public static final String BIZNAME = "bizName";
    public static final String BIZDISTANCE = "bizDistance";

    private boolean locationEnabled;

    private TextView mAboutField;

    private Intent mIntent;

    private RelativeLayout mParentLayout;

    private CurrentLocationManager mLocationManager;

    private TextView mTitleTextView;
    private ImageView mLogo;
    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private BusinessDetail mDetail;
    private ImageView mBackImage;

    private double mLat;
    private double mLon;

    private LinearLayout mHourContainer;
    private TextView mDaysTextView;
    private TextView mHoursTextView;
    private Button mAddressButton;
    private Button mPhoneButton;
    private Button mWebButton;

    private String mBusinessId;
    private String mBusinessName;
    private String mBusinessDistance;

    private TextView mCategoriesTextView;
    private TextView mDiscountTextView;
    private TextView mDistanceTextView;

    private GetBusinessDetailTask mTask;

    View mView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mBusinessId = getArguments().getString(BIZID);
        mBusinessName = getArguments().getString(BIZNAME);
        mBusinessDistance = getArguments().getString(BIZDISTANCE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_business_detail, container, false);
        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.layout_parent);

        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), "Enable location services for better results", Toast.LENGTH_SHORT).show();
        }else{
            locationEnabled = true;
        }

        Log.d(TAG, "Business ID: " + mBusinessId);

        mCategoriesTextView = (TextView) mView.findViewById(R.id.textview_categories);
        mDiscountTextView = (TextView) mView.findViewById(R.id.textview_discount);
        mDistanceTextView = (TextView) mView.findViewById(R.id.textview_distance);

        if(mBusinessDistance != null && mBusinessDistance != "null") {
            setDistance(mBusinessDistance);
        }

        mTask = new GetBusinessDetailTask(getActivity());
        mTask.execute(mBusinessId);

        int timeout = 5000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override public void run() {
                if (mTask.getStatus() == AsyncTask.Status.RUNNING)
                    mTask.cancel(true);
            }
        }, timeout);

        mAddressButton = (Button) mView.findViewById(R.id.button_address);
        mPhoneButton = (Button) mView.findViewById(R.id.button_phone);
        mWebButton = (Button) mView.findViewById(R.id.button_web);
        mHourContainer = (LinearLayout) mView.findViewById(R.id.LinearLayout_hourContainer);
        mDaysTextView = (TextView) mView.findViewById(R.id.TextView_days);
        mHoursTextView = (TextView) mView.findViewById(R.id.TextView_hours);
        mBackImage = (ImageView) mView.findViewById(R.id.imageview_business);

        mAboutField = (TextView) mView.findViewById(R.id.edittext_about);

        // Header
        mLogo = (ImageView) mView.findViewById(R.id.logo);
        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_category_textview_title);
        mBackButton = (ImageButton) mView.findViewById(R.id.fragment_category_button_back);
        mHelpButton = (ImageButton) mView.findViewById(R.id.fragment_category_button_help);

        mTitleTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);
        mLogo.setVisibility(View.GONE);
        mBackButton.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(mBusinessName)) {
            mTitleTextView.setText(mBusinessName);
            mTitleTextView.setVisibility(View.VISIBLE);
        }

        mAddressButton.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mPhoneButton.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mWebButton.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mDaysTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mHoursTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mAboutField.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mCategoriesTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);
        mDiscountTextView.setTypeface(BusinessDirectoryFragment.helveticaNeueTypeFace);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
//                Common.showHelpInfoDialog(DirectoryDetailActivity.this, "Info", "Business directory info");
            }
        });

        return mView;
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

    private void getMapLink() {
        String address = mDetail.getAddress();

        String daddr = buildLatLonToStr(String.valueOf(mLat), String.valueOf(mLon));

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?"
                        + "daddr="
                        + daddr
                        + "&dirflg=d"));

        intent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));

        startActivity(intent);
    }

    private void setDistance(String strDistance) {

        double businessDistance = 0;
        try {
            businessDistance = Double.parseDouble(strDistance);
            businessDistance = Common.metersToMiles(businessDistance);
            if (businessDistance < 1) {
                int distFeet = (int) Common.milesToFeet(businessDistance);
                if (distFeet <= 1000) {
                    int intDist = (int) Math.floor(distFeet);
                    String distanceString = "" + intDist;
                    mDistanceTextView.setText(distanceString + " feet");
                } else {
                    businessDistance = Math.ceil(businessDistance * 10) / 10;
                    String distanceString = "" + businessDistance;
                    distanceString = distanceString.substring(1, distanceString.length());
                    mDistanceTextView.setText(distanceString + " miles");
                }

            }else if (businessDistance >= 1000) {
                int distanceInInt = (int) businessDistance;
                mDistanceTextView.setText(String.valueOf(distanceInInt) + " miles");
            } else {
                businessDistance = Math.ceil(businessDistance * 10) / 10;
                mDistanceTextView.setText(String.valueOf(businessDistance) + " miles");
            }
        }catch (Exception e) {
            mDistanceTextView.setText("-");
        }
    }

    private String buildLatLonToStr(String lat, String lon) {
        StringBuilder builder = new StringBuilder();
        builder.append(lat);
        builder.append(",");
        builder.append(lon);

        return builder.toString();
    }

    private class GetBusinessDetailTask extends AsyncTask<String, Void, String> {
        AirbitzAPI mApi = AirbitzAPI.getApi();
        Context mContext;
        ProgressDialog mProgressDialog;

        public GetBusinessDetailTask(Context context) {
            mContext = context;
        }

        @Override protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Getting venue data...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override protected String doInBackground(String... params) {
            String latLong = "";
            if(locationEnabled) {
                android.location.Location currentLoc = mLocationManager.getLocation();
                latLong = String.valueOf(currentLoc.getLatitude());
                latLong += "," + String.valueOf(currentLoc.getLongitude());
            }
            return mApi.getBusinessByIdAndLatLong(params[0],latLong);
        }

        @Override protected void onCancelled() {
            if(null != mProgressDialog) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity().getApplicationContext(), "Timeout retrieving data",
                        Toast.LENGTH_LONG).show();
            }
            super.onCancelled();
        }

        @Override protected void onPostExecute(String results) {
            try {
                mDetail = new BusinessDetail(new JSONObject(results));
                Location location = mDetail.getLocationObjectArray();
                mLat = location.getLatitude();
                mLon = location.getLongitude();

                setDistance(mDetail.getDistance());
                if( mLat== 0 && mLon == 0 ){
                    mAddressButton.setClickable(false);
                }


                if ((mDetail.getAddress().length() == 0) || mDetail == null) {
                    if (mLat != 0 && mLon != 0) {
                        mAddressButton.setText("Directions");
                    } else {
                        mAddressButton.setVisibility(View.GONE);
                    }
                } else {
                    mAddressButton.setText(mDetail.getPrettyAddressString());
                }

                if (TextUtils.isEmpty(mDetail.getPhone())) {
                    mPhoneButton.setVisibility(View.GONE);
                } else {
                    mPhoneButton.setText(mDetail.getPhone());
                    mPhoneButton.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(mDetail.getWebsite())) {
                    mWebButton.setVisibility(View.GONE);
                } else {
                    mWebButton.setText(mDetail.getWebsite());
                    mWebButton.setVisibility(View.VISIBLE);
                }

                if (mDetail.getHourObjectArray() == null || mDetail.getHourObjectArray().size() == 0) {
                    mHourContainer.setVisibility(View.GONE);
                } else {
                    setSchedule(mDetail.getHourObjectArray());
                    mHourContainer.setVisibility(View.VISIBLE);
                }

                if ((mDetail.getName().length() == 0) || mDetail.getName() == null) {
                    mTitleTextView.setVisibility(View.GONE);
                } else {
                    mTitleTextView.setText(mDetail.getName());
                    mTitleTextView.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(mDetail.getDescription())) {
                    mAboutField.setVisibility(View.GONE);
                } else {
                    mAboutField.setText(mDetail.getDescription());
                    mAboutField.setVisibility(View.VISIBLE);
                }

                //set drawables round if others are missing
                //Address
                if(mAddressButton.getVisibility()==View.VISIBLE){
                    if(mPhoneButton.getVisibility()==View.GONE && mWebButton.getVisibility()==View.GONE && mHourContainer.getVisibility()==View.GONE && mAboutField.getVisibility()==View.GONE) {
                        mAddressButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                    }
                }
                //Phone Number
                if(mPhoneButton.getVisibility()==View.VISIBLE){
                    if(mWebButton.getVisibility()==View.GONE && mHourContainer.getVisibility()==View.GONE && mAboutField.getVisibility()==View.GONE){
                        if(mAddressButton.getVisibility()==View.GONE) {
                            mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                        }else{
                            mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_bottom);
                        }
                    }else if(mAddressButton.getVisibility()==View.GONE){
                        mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_top);
                    }
                }
                //Web Button
                if(mWebButton.getVisibility()==View.VISIBLE){
                    if(mHourContainer.getVisibility()==View.GONE && mAboutField.getVisibility()==View.GONE){
                        if(mAddressButton.getVisibility()==View.GONE && mPhoneButton.getVisibility()==View.GONE){
                            mWebButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                        }else{
                            mWebButton.setBackgroundResource(R.drawable.transparent_until_pressed_bottom);
                        }
                    }else if(mAddressButton.getVisibility()==View.GONE && mPhoneButton.getVisibility()==View.GONE){
                        mWebButton.setBackgroundResource(R.drawable.transparent_until_pressed_top);
                    }
                }

                // Set categories text
                final List<Category> categories = mDetail.getCategoryObject();
                if (categories == null || categories.size() == 0) {
                    mCategoriesTextView.setVisibility(View.GONE);
                } else {
                    final StringBuilder sb = new StringBuilder();
                    final Iterator<Category> iter = categories.iterator();
                    while (iter.hasNext()) {
                        final Category category = iter.next();
                        sb.append(category.getCategoryName());
                        if (iter.hasNext()) {
                            sb.append(" | ");
                        }
                    }
                    mCategoriesTextView.setText(sb.toString());
                    mCategoriesTextView.setVisibility(View.VISIBLE);
                }

                // Set discount text
                String discount = mDetail.getFlagBitcoinDiscount();
                double discountDouble = 0;
                try {
                    discountDouble = Double.parseDouble(discount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (discountDouble != 0) {
                    mDiscountTextView.setText("Discount " + (int) (discountDouble * 100) + "%");
                    mDiscountTextView.setVisibility(View.VISIBLE);
                } else {
                    mDiscountTextView.setVisibility(View.GONE);
                }

                // Set photo
                Picasso.with(getActivity()).load(mDetail.getPrimaryImage().getPhotoThumbnailLink()).into(mBackImage);
//                GetBackgroundImageTask task = new GetBackgroundImageTask(mBackImage);
//                task.execute(mDetail.getPrimaryImage().getPhotoLink());

                if (mLat != 0 && mLon != 0) {
                    mAddressButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getMapLink();
                        }
                    });
                }
                mPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {

                        if ((mDetail.getPhone().length() != 0) && mDetail.getPhone() != null) {
                            mIntent = new Intent(Intent.ACTION_CALL);
                            mIntent.setData(Uri.parse("tel:" + mDetail.getPhone()));
                            startActivity(mIntent);
                        }

                    }
                });
                mWebButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        if ((mDetail.getWebsite().length() != 0) && mDetail.getWebsite() != null) {
                            mIntent = new Intent(Intent.ACTION_VIEW);
                            mIntent.setData(Uri.parse(mDetail.getWebsite()));
                            startActivity(mIntent);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mAddressButton.setVisibility(View.GONE);
                mPhoneButton.setVisibility(View.GONE);
                mWebButton.setVisibility(View.GONE);
                mHourContainer.setVisibility(View.GONE);
                Toast.makeText(getActivity().getApplicationContext(), "Can not retrieve data",
                        Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }

        private void setSchedule(List<Hour> hours) {
            final Iterator<Hour> iter = hours.iterator();
            final StringBuilder daysSb = new StringBuilder();
            final StringBuilder hoursSb = new StringBuilder();
            while (iter.hasNext()) {
                final Hour hour = iter.next();

                // Day
                daysSb.append(hour.getDayOfWeek());

                // Hour
                hoursSb.append(hour.getPrettyStartEndHour());

                if (iter.hasNext()) {
                    daysSb.append("\n");
                    hoursSb.append("\n");
                }
            }

            mDaysTextView.setText(daysSb.toString());
            mHoursTextView.setText(hoursSb.toString());
        }

//        String createScheduleString(List<Hour> hours) {
//            String schedule = "";
//            for (Hour hour : hours) {
//                String startHour = hour.getHourStart();
//                String endHour = hour.getHourEnd();
//
//                String hourString = "";
//
//                if (startHour.equalsIgnoreCase("null")) {
//                    if (!endHour.equalsIgnoreCase("null")) {
//                        hourString = endHour;
//                    }
//                } else if (endHour.equalsIgnoreCase("null")) {
//                    hourString = startHour;
//                }
//
//                schedule += hour.getDayOfWeek() + " " + hourString + "\n";
//            }
//            schedule = schedule.substring(0, schedule.length() - 1);
//            return schedule;
//        }
    }

    private class GetBackgroundImageTask extends AsyncTask<String, Void, Bitmap> {

        ImageView mTargetView;

        public GetBackgroundImageTask(ImageView targetView) {
            mTargetView = targetView;
        }

        @Override protected Bitmap doInBackground(String... params) {
            Bitmap image = null;

            try {
                InputStream in = new URL(params[0]).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage());
                e.printStackTrace();
            }

            return image;
        }

        @Override protected void onPostExecute(Bitmap bitmap) {
            mTargetView.setImageBitmap(bitmap);
        }
    }

    private float getStateFromSharedPreferences(String key) {
        Activity activity = getActivity();
        if(activity!=null) {
            SharedPreferences pref = activity.getSharedPreferences(BusinessDirectoryFragment.PREF_NAME,
                    Context.MODE_PRIVATE);
            return pref.getFloat(key, -1);
        }
        return -1;
    }

    private double getLatFromSharedPreference() {
        return mLocationManager.getLocation().getLatitude();
    }

    private double getLonFromSharedPreference() {
        return mLocationManager.getLocation().getLongitude();
    }
}
