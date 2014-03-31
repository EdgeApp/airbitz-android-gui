
package com.airbitz.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Hour;
import com.airbitz.models.Location;
import com.airbitz.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class DirectoryDetailActivity extends Activity implements GestureDetector.OnGestureListener {

    private static final String TAG = DirectoryDetailActivity.class.getSimpleName();
    private EditText mAboutField;

    private Intent mIntent;

    private RelativeLayout mParentLayout;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private BusinessDetail mDetail;
    private ImageView backImage;

    private TextView mTitleView;

    private double mLat;
    private double mLon;

    private Button mAddressButton;
    private Button mPhoneButton;
    private Button mWebButton;
    private Button mHourButton;
    private TextView mBusinessNameText;
    private String mBusinessId;

    private TextView mTypeAndDiscountTextView;
    private TextView mDistanceTextView;

    private GetBusinessDetailTask mTask;

    private GestureDetector mGestureDetector;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_detail);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        mGestureDetector = new GestureDetector(this);

        mBusinessId = getIntent().getExtras().getString("bizId");
        String businessName = getIntent().getExtras().getString("bizName");
        mParentLayout = (RelativeLayout) findViewById(R.id.layout_parent);

        mBusinessNameText = (TextView) findViewById(R.id.textview_business_name);
        mTypeAndDiscountTextView = (TextView) findViewById(R.id.textview_discount);
        mDistanceTextView = (TextView) findViewById(R.id.textview_distance);

        setDistance(getIntent().getStringExtra("bizDistance"));

        if (businessName != null && !businessName.equalsIgnoreCase("")) {
            mBusinessNameText.setText(businessName);
        }

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mTask = new GetBusinessDetailTask(DirectoryDetailActivity.this);
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

        mAddressButton = (Button) findViewById(R.id.button_address);
        mPhoneButton = (Button) findViewById(R.id.button_phone);
        mWebButton = (Button) findViewById(R.id.button_web);
        mHourButton = (Button) findViewById(R.id.button_hour);

        mAboutField = (EditText) findViewById(R.id.edittext_about);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);
        mTitleView = (TextView) findViewById(R.id.textview_title);
        mTitleView.setText(businessName);

        mTitleView.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);

        mAddressButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mPhoneButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mWebButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);
        mHourButton.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);

        TextView aboutTextView = (TextView) findViewById(R.id.textview_about);
        EditText aboutEditText = (EditText) findViewById(R.id.edittext_about);
        aboutTextView.setTypeface(BusinessDirectoryActivity.montserratBoldTypeFace);
        aboutEditText.setTypeface(BusinessDirectoryActivity.montserratRegularTypeFace);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Common.showHelpInfoDialog(DirectoryDetailActivity.this, "Info", "Business directory info");
            }
        });

    }

    @Override protected void onResume() {
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        super.onResume();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onStop() {
        super.onStop();
    }

    private void getMapLink() {
        String address = mDetail.getAddress();

        String daddr = buildLatLonToStr(String.valueOf(mLat), String.valueOf(mLon));
        String saddr = buildLatLonToStr(String.valueOf(getLatFromSharedPreference()),
                                        String.valueOf(getLonFromSharedPreference()));

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                   Uri.parse("http://maps.google.com/maps?saddr=" + saddr
                                             + "&daddr="
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
                businessDistance = Math.ceil(businessDistance * 10) / 10;
                String distanceString = "" + businessDistance;
                distanceString = distanceString.substring(1, distanceString.length());
                mDistanceTextView.setText(distanceString + " miles");
            } else if (businessDistance >= 1000) {
                int distanceInInt = (int) businessDistance;
                mDistanceTextView.setText(String.valueOf(distanceInInt) + " miles");
            } else {
                businessDistance = Math.ceil(businessDistance * 10) / 10;
                mDistanceTextView.setText(String.valueOf(businessDistance) + " miles");
            }
        } catch (Exception e) {
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
            return mApi.getBusinessById(params[0]);
        }

        @Override protected void onCancelled() {
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Timeout retrieving data",
                           Toast.LENGTH_LONG).show();
            super.onCancelled();
        }

        @Override protected void onPostExecute(String results) {
            try {
                mDetail = new BusinessDetail(new JSONObject(results));
                Location location = mDetail.getLocationObjectArray();
                mLat = location.getLatitude();
                mLon = location.getLongitude();

                mBusinessNameText = (TextView) findViewById(R.id.textview_business_name);
                mTypeAndDiscountTextView = (TextView) findViewById(R.id.textview_discount);
                mDistanceTextView = (TextView) findViewById(R.id.textview_distance);
                mAddressButton = (Button) findViewById(R.id.button_address);
                mPhoneButton = (Button) findViewById(R.id.button_phone);
                mWebButton = (Button) findViewById(R.id.button_web);
                mHourButton = (Button) findViewById(R.id.button_hour);
                backImage = (ImageView) findViewById(R.id.imageview_business);

                setDistance(mDetail.getDistance());

                if ((mDetail.getAddress().length() == 0) || mDetail == null) {
                    if (location != null) {
                        mAddressButton.setText("Directions");
                    } else {
                        mAddressButton.setText(Common.UNAVAILABLE);
                    }
                }
                else {
                    mAddressButton.setText(mDetail.getAddress() + ", "
                                           + mDetail.getCity()
                                           + ", "
                                           + mDetail.getState()
                                           + ", "
                                           + mDetail.getPostalCode());
                }

                if ((mDetail.getPhone().length() == 0) || mDetail.getPhone() == null) {
                    mPhoneButton.setText(Common.UNAVAILABLE);
                    mPhoneButton.setVisibility(View.GONE);
                }
                else {
                    mPhoneButton.setText(mDetail.getPhone());
                    mPhoneButton.setVisibility(View.VISIBLE);
                }

                if ((mDetail.getWebsite().length() == 0) || mDetail.getWebsite() == null) {
                    mWebButton.setText(Common.UNAVAILABLE);
                }
                else {
                    mWebButton.setText(mDetail.getWebsite());
                }

                if ((mDetail.getHourObjectArray().size() == 0) || mDetail.getHourObjectArray() == null) {
                    mHourButton.setText(Common.UNAVAILABLE);
                }
                else {
                    mHourButton.setText(createScheduleString(mDetail.getHourObjectArray()));
                }

                if ((mDetail.getName().length() == 0) || mDetail.getName() == null) {
                    // mBusinessNameText.setText(Common.UNAVAILABLE);
                }
                else {
                    mBusinessNameText.setText(mDetail.getName());
                }

                if ((mDetail.getDescription().length() == 0) || mDetail.getDescription() == null) {
                    mAboutField.setText(Common.UNAVAILABLE);
                }
                else {
                    mAboutField.setText(mDetail.getDescription());
                }

                String discount = mDetail.getFlagBitcoinDiscount();

                double discountDouble = 0;
                try {
                    discountDouble = Double.parseDouble(discount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int discountInt = (int) (discountDouble * 100);

                if (discountInt == 0) {
                    mTypeAndDiscountTextView.setText(mDetail.getCategoryObject().get(0).getCategoryName());
                } else {
                    mTypeAndDiscountTextView.setText(mDetail.getCategoryObject().get(0).getCategoryName() +
                                                     " | Disc. " + discountInt + "%");
                }

                GetBackgroundImageTask task = new GetBackgroundImageTask(backImage);
                task.execute(mDetail.getImages().get(0).getPhotoLink());

                mAddressButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        getMapLink();
                    }
                });
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
                mHourButton.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {

                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                mAddressButton.setText(Common.UNAVAILABLE);
                mPhoneButton.setText(Common.UNAVAILABLE);
                mWebButton.setText(Common.UNAVAILABLE);
                mHourButton.setText(Common.UNAVAILABLE);
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                               Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                mAddressButton.setText(Common.UNAVAILABLE);
                mPhoneButton.setText(Common.UNAVAILABLE);
                mWebButton.setText(Common.UNAVAILABLE);
                mHourButton.setText(Common.UNAVAILABLE);
                Toast.makeText(getApplicationContext(), "Can not retrieve data",
                               Toast.LENGTH_LONG).show();
            }
            mProgressDialog.dismiss();
        }

        String createHourString(List<Hour> hours) {
            String dayOfWeek = hours.get(0).getDayOfWeek();
            String dayOfWeek2 = hours.get(hours.size() - 1).getDayOfWeek();
            String hourStart = hours.get(0).getHourStart();
            String hourEnd = hours.get(0).setHourEnd();
            if (hours.get(0).getDayOfWeek().length() == 0 || hours.get(0).getDayOfWeek() == null) {
                dayOfWeek = Common.UNAVAILABLE;
            }
            if (hours.get(hours.size() - 1).getDayOfWeek().length() == 0 || hours.get(hours.size() - 1)
                                                                                 .getDayOfWeek() == null) {
                dayOfWeek2 = Common.UNAVAILABLE;
            }
            if (hours.get(0).getHourStart().length() == 0 || hours.get(0).getHourStart() == null) {
                hourStart = Common.UNAVAILABLE;
            }
            if (hours.get(0).setHourEnd().length() == 0 || hours.get(0).setHourEnd() == null) {
                hourEnd = Common.UNAVAILABLE;
            }

            return "Hours: " + dayOfWeek + " - " + dayOfWeek2 + " " + hourStart + " - " + hourEnd;
        }

        String createScheduleString(List<Hour> hours) {
            String schedule = "";
            for (Hour hour : hours) {
                String startHour = hour.getHourStart();
                String endHour = hour.getHourEnd();
                SimpleDateFormat militaryFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat amPmFormat = new SimpleDateFormat("hh:mm a");

                String hourString = "";
                try {
                    Date hourStartDateMil = militaryFormat.parse(startHour);
                    startHour = amPmFormat.format(hourStartDateMil);
                    Date hourEndDateMil = militaryFormat.parse(endHour);
                    endHour = amPmFormat.format(hourEndDateMil);

                    hourString = startHour + " - " + endHour;

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (startHour.equalsIgnoreCase("null")) {
                    if (!endHour.equalsIgnoreCase("null")) {
                        hourString = endHour;
                    }
                } else if (endHour.equalsIgnoreCase("null")) {
                    hourString = startHour;
                }

                schedule += hour.getDayOfWeek() + " " + hourString + "\n";
            }
            schedule = schedule.substring(0, schedule.length() - 1);
            return schedule;
        }
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
        SharedPreferences pref = getSharedPreferences(BusinessDirectoryActivity.PREF_NAME,
                                                      Context.MODE_PRIVATE);
        return pref.getFloat(key, -1);
    }

    private double getLatFromSharedPreference() {
        return (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LAT_KEY);
    }

    private double getLonFromSharedPreference() {
        return (double) getStateFromSharedPreferences(BusinessDirectoryActivity.LON_KEY);
    }

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
