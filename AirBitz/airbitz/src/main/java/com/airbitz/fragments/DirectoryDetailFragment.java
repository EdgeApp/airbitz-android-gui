package com.airbitz.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Category;
import com.airbitz.models.CurrentLocationManager;
import com.airbitz.models.Hour;
import com.airbitz.models.Location;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class DirectoryDetailFragment extends Fragment {

    public static final String BIZID = "bizId";
    public static final String BIZNAME = "bizName";
    public static final String BIZDISTANCE = "bizDistance";
    private static final String TAG = DirectoryDetailFragment.class.getSimpleName();
    View mView;
    private boolean locationEnabled;
    private TextView mAboutField;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBusinessId = getArguments().getString(BIZID);
        mBusinessName = getArguments().getString(BIZNAME);
        mBusinessDistance = getArguments().getString(BIZDISTANCE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_business_detail, container, false);
        } else {
            return mView;
        }

        mLocationManager = CurrentLocationManager.getLocationManager(getActivity());
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationEnabled = false;
            Toast.makeText(getActivity(), getString(R.string.fragment_business_enable_location_services), Toast.LENGTH_SHORT).show();
        } else {
            locationEnabled = true;
        }

        Log.d(TAG, "Business ID: " + mBusinessId);

        mCategoriesTextView = (TextView) mView.findViewById(R.id.textview_categories);
        mDiscountTextView = (TextView) mView.findViewById(R.id.textview_discount);
        mDistanceTextView = (TextView) mView.findViewById(R.id.textview_distance);
        mDistanceTextView.setVisibility(View.GONE);

        if (mBusinessDistance != null && mBusinessDistance != "null") {
            setDistance(mBusinessDistance);
        }

        mTask = new GetBusinessDetailTask(getActivity());
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mBusinessId);

        int timeout = 5000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
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
        mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mHelpButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });


        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);

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

        return mView;
    }

    private void getMapLink() {
        String address = mDetail.getAddress();
        String daddr = buildLatLonToStr(String.valueOf(mLat), String.valueOf(mLon));

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?"
                        + "daddr="
                        + daddr
                        + "&dirflg=d")
        );
        intent.setComponent(new ComponentName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"));
        startActivity(intent);
    }

    private void setDistance(String strDistance) {
        double businessDistance = 0;
        try {
            businessDistance = Double.parseDouble(strDistance);

            mDistanceTextView.setText(VenueAdapter.getDistanceString(businessDistance));
            mDistanceTextView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            mDistanceTextView.setVisibility(View.INVISIBLE);
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
        Activity mActivity;

        public GetBusinessDetailTask(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            ((NavigationActivity) mActivity).showModalProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String latLong = "";
            android.location.Location currentLoc = mLocationManager.getLocation();
            if (locationEnabled && null != currentLoc) {
                latLong = String.valueOf(currentLoc.getLatitude());
                latLong += "," + String.valueOf(currentLoc.getLongitude());
            }
            return mApi.getBusinessByIdAndLatLong(params[0], latLong);
        }

        @Override
        protected void onCancelled() {
            ((NavigationActivity) mActivity).showModalProgress(false);
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String results) {
            try {
                mDetail = new BusinessDetail(new JSONObject(results));
                Location location = mDetail.getLocationObjectArray();
                mLat = location.getLatitude();
                mLon = location.getLongitude();

                setDistance(mDetail.getDistance());
                if (mLat == 0 && mLon == 0) {
                    mAddressButton.setClickable(false);
                }

                if ((mDetail.getAddress().length() == 0) || mDetail == null) {
                    if (mLat != 0 && mLon != 0) {
                        mAddressButton.setText(getString(R.string.fragment_directory_detail_directions));
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
                if (mAddressButton.getVisibility() == View.VISIBLE) {
                    if (mPhoneButton.getVisibility() == View.GONE && mWebButton.getVisibility() == View.GONE && mHourContainer.getVisibility() == View.GONE && mAboutField.getVisibility() == View.GONE) {
                        mAddressButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                    }
                }
                //Phone Number
                if (mPhoneButton.getVisibility() == View.VISIBLE) {
                    if (mWebButton.getVisibility() == View.GONE && mHourContainer.getVisibility() == View.GONE && mAboutField.getVisibility() == View.GONE) {
                        if (mAddressButton.getVisibility() == View.GONE) {
                            mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                        } else {
                            mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_bottom);
                        }
                    } else if (mAddressButton.getVisibility() == View.GONE) {
                        mPhoneButton.setBackgroundResource(R.drawable.transparent_until_pressed_top);
                    }
                }
                //Web Button
                if (mWebButton.getVisibility() == View.VISIBLE) {
                    if (mHourContainer.getVisibility() == View.GONE && mAboutField.getVisibility() == View.GONE) {
                        if (mAddressButton.getVisibility() == View.GONE && mPhoneButton.getVisibility() == View.GONE) {
                            mWebButton.setBackgroundResource(R.drawable.transparent_until_pressed_both);
                        } else {
                            mWebButton.setBackgroundResource(R.drawable.transparent_until_pressed_bottom);
                        }
                    } else if (mAddressButton.getVisibility() == View.GONE && mPhoneButton.getVisibility() == View.GONE) {
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
                    mDiscountTextView.setText(getString(R.string.fragment_directory_detail_discount) + (int) (discountDouble * 100) + "%");
                    mDiscountTextView.setVisibility(View.VISIBLE);
                } else {
                    mDiscountTextView.setVisibility(View.GONE);
                }

                // Set photo
                String imgUrl = mDetail.getPrimaryImage().getPhotoThumbnailLink();
                Picasso.with(getActivity()).load(imgUrl).into(mBackImage);

                if (mLat != 0 && mLon != 0) {
                    mAddressButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getMapLink();
                        }
                    });
                }
                mPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if ((mDetail.getPhone().length() != 0) && mDetail.getPhone() != null) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + mDetail.getPhone()));
                            startActivity(intent);
                        }

                    }
                });
                mWebButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((mDetail.getWebsite().length() != 0) && mDetail.getWebsite() != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(mDetail.getWebsite()));
                            startActivity(intent);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                mAddressButton.setVisibility(View.GONE);
                mPhoneButton.setVisibility(View.GONE);
                mWebButton.setVisibility(View.GONE);
                mHourContainer.setVisibility(View.GONE);
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.fragment_business_cannot_retrieve_data),
                        Toast.LENGTH_LONG).show();
            }
            ((NavigationActivity) mActivity).showModalProgress(false);
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
    }
}
