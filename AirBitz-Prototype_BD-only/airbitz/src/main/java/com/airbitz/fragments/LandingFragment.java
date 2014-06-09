package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.ForgotPasswordActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.SignUpActivity;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_RequestResults;
import com.airbitz.api.tABC_RequestType;

public class LandingFragment extends Fragment {

    public static final String LAT_KEY = "LATITUDE_KEY";
    public static final String LON_KEY = "LONGITUDE_KEY";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private EditText mUserNameField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;
    private ImageView mForgotImageView;
    private ImageView mLogoImageView;
    private GestureDetector mGestureDetector;
    private TextView mForgotPasswordTextView;
    private int mDisplayWidth;
    private Location mLocation;
    private View mProgressView;


    private TextView mDetailTextView;
    private LinearLayout mSwipeLayout;

    private LocationManager mLocationManager;

    private RelativeLayout mLandingLayout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mProgressView = view.findViewById(R.id.login_progress);
        mLandingLayout = (RelativeLayout) view.findViewById(R.id.landing_main_layout);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUserNameField = (EditText) view.findViewById(R.id.userNameField);
        mPasswordField = (EditText) view.findViewById(R.id.passwordField);
        mSignInButton = (Button) view.findViewById(R.id.signinButton);
        mSignUpButton = (Button) view.findViewById(R.id.signUpButton);
        mForgotImageView = (ImageView) view.findViewById(R.id.forgotPassImage);
        mLogoImageView = (ImageView) view.findViewById(R.id.imageView);
        mForgotPasswordTextView = (TextView) view.findViewById(R.id.forgotPassText);
        mSwipeLayout = (LinearLayout) view.findViewById(R.id.swipeLayout);
        mDetailTextView = (TextView) view.findViewById(R.id.detail_text);

        mUserNameField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mPasswordField.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mSignInButton.setTypeface(NavigationActivity.latoBlackTypeFace);
        mSignUpButton.setTypeface(NavigationActivity.latoBlackTypeFace);
        mForgotPasswordTextView.setTypeface(NavigationActivity.latoBlackTypeFace);

        TextView swipeText = (TextView) view.findViewById(R.id.swipe_text);

        swipeText.setTypeface(NavigationActivity.montserratRegularTypeFace);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;

        final View activityRootView = view.findViewById(R.id.container);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    mDetailTextView.setVisibility(View.GONE);
                    mSwipeLayout.setVisibility(View.GONE);
                } else {
                    mDetailTextView.setVisibility(View.VISIBLE);
                    mSwipeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
//        mLogoImageView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                return mGestureDetector.onTouchEvent(motionEvent);
//            }
//        });'


        mForgotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserNameField.getText().toString().isEmpty()){
                    showAlertDialog();
                }else {
                    System.out.println("THis is me: "+mUserNameField.getText().toString());
                    Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            }
        });

        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserNameField.getText().toString().isEmpty()){
                    showAlertDialog();
                }else {
                    //System.out.println("THis is me: "+mUserNameField.getText().toString());
                    Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(!mPasswordField.getText().toString().isEmpty() && !mUserNameField.getText().toString().isEmpty()){
                    mgr.hideSoftInputFromWindow(mPasswordField.getWindowToken(), 0);
                    mgr.hideSoftInputFromWindow(mUserNameField.getWindowToken(), 0);
                }
                attemptLogin();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mPasswordField.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(mUserNameField.getWindowToken(), 0);
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        checkLocationManager();

        return view;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_Error pError = new tABC_Error();
            tABC_RequestResults pResults = new tABC_RequestResults();

            tABC_CC code = core.ABC_SignIn(mUsername, mPassword, null, pResults, pError);
            tABC_RequestType type = pResults.getRequestType();

            boolean success = type == tABC_RequestType.ABC_RequestType_AccountSignIn? true: false;
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                ((NavigationActivity) getActivity()).setUserLoggedIn(true);
                ((NavigationActivity) getActivity()).setLoginView(false);
                ((NavigationActivity) getActivity()).onNavBarSelected(0);
                final View activityRootView = getActivity().findViewById(R.id.container);
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                }
            } else {
                showProgress(false);
                showErrorDialog();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPasswordField.setError(null);
        mUserNameField.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameField.getText().toString();
        String password = mPasswordField.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUserNameField.setError(getString(R.string.error_field_required));
            focusView = mUserNameField;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameField.setError(getString(R.string.error_invalid_username));
            focusView = mUserNameField;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.error_field_required));
            focusView = mPasswordField;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordField.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordField;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO real logic for good username
        return !username.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        //TODO real logic for good password
        return !password.isEmpty();
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLandingLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mLandingLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLandingLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLandingLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.error_invalid_credentials))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void showAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(getResources().getString(R.string.activity_forgot_no_username_details))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void checkLocationManager() {

        mLocationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("GPS is disabled. Go to Settings and turned on your GPS.")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }

    }


    private final LocationListener listener = new LocationListener() {


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


        @Override
        public void onProviderEnabled(String provider) {

        }


        @Override
        public void onProviderDisabled(String provider) {

        }


        @Override
        public void onLocationChanged(Location location) {

            if (mLocationManager != null) {
                mLocationManager.removeUpdates(listener);
            }

            if (location.hasAccuracy()) {
                mLocation = location;
                clearSharedPreference();
                writeLatLonToSharedPreference();
            }
        }
    };


    private void writeValueToSharedPreference(String key, float value) {
        SharedPreferences.Editor editor = getActivity().getPreferences(Activity.MODE_PRIVATE).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    private void writeLatLonToSharedPreference(){
        writeValueToSharedPreference(LAT_KEY, (float)mLocation.getLatitude());
        writeValueToSharedPreference(LON_KEY, (float)mLocation.getLongitude());
    }


    private float getStateFromSharedPreferences(String key) {
        SharedPreferences prefs = getActivity().getPreferences(Activity.MODE_PRIVATE);
        return prefs.getFloat(key, -1);
    }


    private void setLatLonFromSharedPreference() {
        double lat = (double)getStateFromSharedPreferences(LAT_KEY);
        double lon = (double)getStateFromSharedPreferences(LON_KEY);
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lon);
    }

    private void clearSharedPreference(String key) {
        SharedPreferences.Editor editor = getActivity().getPreferences(Activity.MODE_PRIVATE).edit();
        editor.remove(key);
        editor.commit();
    }

    private void clearSharedPreference() {
        SharedPreferences.Editor editor = getActivity().getPreferences(Activity.MODE_PRIVATE).edit();
        editor.remove(LAT_KEY);
        editor.remove(LON_KEY);
        editor.commit();
    }
}
