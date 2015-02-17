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

package com.airbitz.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.airbitz.adapters.ImageViewPagerAdapter;
import com.airbitz.adapters.VenueAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Category;
import com.airbitz.models.Hour;
import com.airbitz.models.Image;
import com.airbitz.models.Location;
import com.airbitz.models.Social;
import com.airbitz.objects.CurrentLocationManager;
import com.airbitz.widgets.TouchImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class DirectoryDetailFragment extends BaseFragment {

    public static final String BIZID = "bizId";
    public static final String BIZNAME = "bizName";
    public static final String BIZDISTANCE = "bizDistance";
    private static final String TAG = DirectoryDetailFragment.class.getSimpleName();
    View mView;
    private boolean locationEnabled;
    private TextView mAboutField;
    private CurrentLocationManager mLocationManager;
    private TextView mTitleTextView;
    private ImageButton mBackButton;
    private BusinessDetail mBusinessDetail;
    private ViewPager mImagePager;
    private List<ImageView> mImageViewList = new ArrayList<ImageView>();
    private double mLat;
    private double mLon;
    private LinearLayout mHourContainer;
    private TextView mDaysTextView;
    private TextView mHoursTextView;
    private Button mAddressButton;
    private Button mPhoneButton;
    private Button mWebButton;
    private Button mShareButton;
    private Button mFacebookButton;
    private String mFacebookURL;
    private Button mTwitterButton;
    private String mTwitterURL;
    private Button mYelpButton;
    private String mYelpURL;
    private Button mFoursquareButton;
    private String mFoursquareURL;
    private String mBusinessId;
    private String mBusinessName;
    private String mBusinessDistance;
    private TextView mCategoriesTextView;
    private TextView mDiscountTextView;
    private TextView mDistanceTextView;
    private GetBusinessDetailTask mTask;
    private NavigationActivity mActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBusinessId = getArguments().getString(BIZID);
        mBusinessName = getArguments().getString(BIZNAME);
        mBusinessDistance = getArguments().getString(BIZDISTANCE);

        mActivity = ((NavigationActivity) getActivity());
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
            if(getActivity() != null) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_business_enable_location_services));
            }
        } else {
            locationEnabled = true;
        }

        Log.d(TAG, "Business ID: " + mBusinessId + ", Business Distance = " + mBusinessDistance);

        mCategoriesTextView = (TextView) mView.findViewById(R.id.textview_categories);
        mCategoriesTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mDiscountTextView = (TextView) mView.findViewById(R.id.textview_discount);
        mDiscountTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mDistanceTextView = (TextView) mView.findViewById(R.id.textview_distance);
        mDistanceTextView.setVisibility(View.GONE);


        if (mBusinessDistance != null && mBusinessDistance != "null") {
            setDistance(mBusinessDistance);
        }

        mTask = new GetBusinessDetailTask();
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
        mAddressButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mPhoneButton = (Button) mView.findViewById(R.id.button_phone);
        mPhoneButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mWebButton = (Button) mView.findViewById(R.id.button_web);
        mWebButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mShareButton = (Button) mView.findViewById(R.id.button_share);
        mShareButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mFacebookButton = (Button) mView.findViewById(R.id.button_facebook);
        mFacebookButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mTwitterButton = (Button) mView.findViewById(R.id.button_twitter);
        mTwitterButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mYelpButton = (Button) mView.findViewById(R.id.button_yelp);
        mYelpButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mFoursquareButton = (Button) mView.findViewById(R.id.button_foursquare);
        mFoursquareButton.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mHourContainer = (LinearLayout) mView.findViewById(R.id.LinearLayout_hourContainer);
        mDaysTextView = (TextView) mView.findViewById(R.id.TextView_days);
        mDaysTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);
        mHoursTextView = (TextView) mView.findViewById(R.id.TextView_hours);
        mHoursTextView.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        mImagePager = (ViewPager) mView.findViewById(R.id.imageview_business);
        final GestureDetector tapGestureDetector = new GestureDetector(getActivity(), new TapGestureListener());
        mImagePager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                tapGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        mAboutField = (TextView) mView.findViewById(R.id.edittext_about);
        mAboutField.setTypeface(BusinessDirectoryFragment.latoRegularTypeFace);

        // Header
        mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });


        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(BusinessDirectoryFragment.montserratBoldTypeFace);

        if (!TextUtils.isEmpty(mBusinessName)) {
            mTitleTextView.setText(mBusinessName);
            mTitleTextView.setVisibility(View.VISIBLE);
        }

        return mView;
    }

    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "image clicked");
            ViewPagerFragment fragment = new ViewPagerFragment();
            fragment.setImages(getTouchImageViewList(mBusinessDetail), mImagePager.getCurrentItem());
            ((NavigationActivity) getActivity()).pushFragment(fragment);
            return true;
        }
    }

    private void getMapLink() {
        String label = mBusinessDetail.getName();
        Uri geoLocation = Uri.parse("geo:" + mLat + "," + mLon
                + "?q=" + mLat + "," + mLon + "(" + label + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
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

    private class GetBusinessDetailTask extends AsyncTask<String, Void, String> {
        AirbitzAPI mApi = AirbitzAPI.getApi();

        public GetBusinessDetailTask() {
        }

        @Override
        protected void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String latLong = "";
            android.location.Location currentLoc = mLocationManager.getLocation();
            if (locationEnabled && null != currentLoc) {
                latLong = String.valueOf(currentLoc.getLatitude());
                latLong += "," + String.valueOf(currentLoc.getLongitude());
            }
            Log.d(TAG, "LocationManager Location = "+latLong);
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
                mBusinessDetail = new BusinessDetail(new JSONObject(results));
                Location location = mBusinessDetail.getLocationObjectArray();
                mLat = location.getLatitude();
                mLon = location.getLongitude();

                setDistance(mBusinessDistance);
                if (mLat == 0 && mLon == 0) {
                    mAddressButton.setClickable(false);
                }

                if ((mBusinessDetail.getAddress().length() == 0) || mBusinessDetail == null) {
                    if (mLat != 0 && mLon != 0) {
                        mAddressButton.setText(getString(R.string.fragment_directory_detail_directions));
                    } else {
                        mAddressButton.setVisibility(View.GONE);
                    }
                } else {
                    mAddressButton.setText(mBusinessDetail.getPrettyAddressString());
                }

                if (TextUtils.isEmpty(mBusinessDetail.getPhone())) {
                    mPhoneButton.setVisibility(View.GONE);
                } else {
                    mPhoneButton.setText(mBusinessDetail.getPhone());
                    mPhoneButton.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(mBusinessDetail.getWebsite())) {
                    mWebButton.setVisibility(View.GONE);
                } else {
                    mWebButton.setText(mBusinessDetail.getWebsite());
                    mWebButton.setVisibility(View.VISIBLE);
                }

                List<Social> socials = mBusinessDetail.getSocialObjectArray();
                for(Social social : socials) {
                    String url = social.getSocialUrl();
                    if(url.toLowerCase().contains("facebook")) {
                        mFacebookButton.setVisibility(View.VISIBLE);
                        mFacebookURL = url;
                    }
                    else if(url.toLowerCase().contains("twitter")) {
                        mTwitterButton.setVisibility(View.VISIBLE);
                        mTwitterURL = url;
                    }
                    else if(url.toLowerCase().contains("yelp")) {
                        mYelpButton.setVisibility(View.VISIBLE);
                        mYelpURL = url;
                    }

                    else if(url.toLowerCase().contains("foursquare")) {
                        mFoursquareButton.setVisibility(View.VISIBLE);
                        mFoursquareURL = url;
                    }
                }

                if (mBusinessDetail.getHourObjectArray() == null || mBusinessDetail.getHourObjectArray().size() == 0) {
                    mHourContainer.setVisibility(View.GONE);
                } else {
                    setSchedule(mBusinessDetail.getHourObjectArray());
                    mHourContainer.setVisibility(View.VISIBLE);
                }

                if ((mBusinessDetail.getName().length() == 0) || mBusinessDetail.getName() == null) {
                    mTitleTextView.setVisibility(View.GONE);
                } else {
                    mTitleTextView.setText(mBusinessDetail.getName());
                    mTitleTextView.setVisibility(View.VISIBLE);
                }

                if (TextUtils.isEmpty(mBusinessDetail.getDescription())) {
                    mAboutField.setVisibility(View.GONE);
                } else {
                    mAboutField.setText(mBusinessDetail.getDescription());
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
                mPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if ((mBusinessDetail.getPhone().length() != 0) && mBusinessDetail.getPhone() != null) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + mBusinessDetail.getPhone()));
                            if(intent.resolveActivity(mActivity.getPackageManager()) != null) {
                                startActivity(intent);
                            }
                            else {
                                mActivity.ShowFadingDialog("No Activity installed to handle a phone call.");
                            }
                        }
                    }
                });

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
                mWebButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if ((mBusinessDetail.getWebsite().length() != 0) && mBusinessDetail.getWebsite() != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(mBusinessDetail.getWebsite()));
                            startActivity(intent);
                        }
                    }
                });

                mShareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            share(mBusinessDetail);
                    }
                });

                mFacebookButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchBrowser(mFacebookURL);
                    }
                });

                mTwitterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchBrowser(mTwitterURL);
                    }
                });

                mYelpButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchBrowser(mYelpURL);
                    }
                });

                mFoursquareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchBrowser(mFoursquareURL);
                    }
                });

                // Set categories text
                final List<Category> categories = mBusinessDetail.getCategoryObject();
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
                String discount = mBusinessDetail.getFlagBitcoinDiscount();
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

                // Set photos
                mImageViewList = getImageViewThumbnailList(mBusinessDetail);
                mImagePager.setAdapter(new ImageViewPagerAdapter(mImageViewList));

                if (mLat != 0 && mLon != 0) {
                    mAddressButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getMapLink();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                mAddressButton.setVisibility(View.GONE);
                mPhoneButton.setVisibility(View.GONE);
                mWebButton.setVisibility(View.GONE);
                mHourContainer.setVisibility(View.GONE);
                mShareButton.setVisibility(View.GONE);
                mFacebookButton.setVisibility(View.GONE);
                mTwitterButton.setVisibility(View.GONE);
                mYelpButton.setVisibility(View.GONE);
                if(getActivity() != null) {
                    Toast.makeText(getActivity().getApplicationContext(), getString(R.string.fragment_business_cannot_retrieve_data),
                            Toast.LENGTH_LONG).show();
                }
            }
            ((NavigationActivity) mActivity).showModalProgress(false);
        }

        private void launchBrowser(String url) {
            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
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

    private List<ImageView> getImageViewThumbnailList(BusinessDetail bd) {
        List<ImageView> imageViews = new ArrayList<ImageView>();
        List<Image> images = bd.getImages();
        if(images != null) {
            for(Image i : images) {
                ImageView imageView = new ImageView(getActivity());
                imageView.setMinimumHeight((int) i.getPhotoHeight());
                imageView.setMinimumWidth((int) i.getPhotoWidth());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.with(getActivity()).load(i.getPhotoThumbnailLink()).into(imageView);
                imageViews.add(imageView);
            }
        }
        return imageViews;
    }

    private List<TouchImageView> getTouchImageViewList(BusinessDetail bd) {
        List<TouchImageView> imageViews = new ArrayList<TouchImageView>();
        List<Image> images = bd.getImages();
        if(images != null) {
            for(Image i : images) {
                TouchImageView imageView = new TouchImageView(getActivity());
                imageView.setMinimumHeight((int) i.getPhotoHeight());
                imageView.setMinimumWidth((int) i.getPhotoWidth());
                Picasso.with(getActivity()).load(i.getPhotoLink()).into(imageView);
                imageViews.add(imageView);
            }
        }
        return imageViews;
    }

    private void share(BusinessDetail detail) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        String text = detail.getName() + " - " + detail.getCity() + " Bitcoin | Airbitz -";
        text = text + "https://airbitz.co/biz/"+detail.getId();
        share.putExtra(Intent.EXTRA_SUBJECT, detail.getName());
        share.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(share, "share link"));
    }
}
