package com.airbitz.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.R;

public class LandingActivity extends Activity implements GestureDetector.OnGestureListener {

    public static final String LAT_KEY = "LATITUDE_KEY";
    public static final String LON_KEY = "LONGITUDE_KEY";

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "air1:hello", "air2:goodbye"
    };

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

    public static Typeface montserratBoldTypeFace;
    public static Typeface montserratRegularTypeFace;
    public static Typeface latoBlackTypeFace;
    public static Typeface latoRegularTypeFace;
    public static Typeface helveticaNeueTypeFace;

    private LocationManager mLocationManager;

    private RelativeLayout mParentLayout;
    private RelativeLayout mAnimationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mParentLayout = (RelativeLayout) findViewById(R.id.container);
        mAnimationLayout = (RelativeLayout) findViewById(R.id.layout_animation);
        mProgressView = findViewById(R.id.login_progress);

        montserratBoldTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Bold.ttf");
        montserratRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Montserrat-Regular.ttf");
        latoBlackTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-Bla.ttf");
        latoRegularTypeFace=Typeface.createFromAsset(getAssets(), "font/Lato-RegIta.ttf");
        helveticaNeueTypeFace=Typeface.createFromAsset(getAssets(), "font/HelveticaNeue.ttf");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUserNameField = (EditText) findViewById(R.id.userNameField);
        mPasswordField = (EditText) findViewById(R.id.passwordField);
        mSignInButton = (Button) findViewById(R.id.signinButton);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mForgotImageView = (ImageView) findViewById(R.id.forgotPassImage);
        mLogoImageView = (ImageView) findViewById(R.id.imageView);
        mForgotPasswordTextView = (TextView) findViewById(R.id.forgotPassText);

        mUserNameField.setTypeface(LandingActivity.montserratRegularTypeFace);
        mPasswordField.setTypeface(LandingActivity.montserratRegularTypeFace);
        mSignInButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mSignUpButton.setTypeface(LandingActivity.latoBlackTypeFace);
        mForgotPasswordTextView.setTypeface(LandingActivity.latoBlackTypeFace);

        TextView swipeText = (TextView) findViewById(R.id.swipe_text);

        swipeText.setTypeface(LandingActivity.montserratRegularTypeFace);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mDisplayWidth = size.x;

        mGestureDetector = new GestureDetector(this,this);

        mLogoImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mParentLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mForgotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandingActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandingActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mPasswordField.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(mUserNameField.getWindowToken(), 0);
                attemptLogin();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LandingActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        checkLocationManager();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service. Remove below code.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                //going to main Navigation
                Intent intent = new Intent(LandingActivity.this, NavigationActivity.class);
                startActivity(intent);
                finish();
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

            mAnimationLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            mAnimationLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimationLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mAnimationLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnimationLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float sensitivity = 30;
        float yDistance = Math.abs(e2.getY() - e1.getY());

        if (e1.getY() - e2.getY() > sensitivity) {
            return true;
        }
        //Swipe Down Check
        else if (e2.getY() - e1.getY() > sensitivity) {
            return true;
        }
        //Swipe Left Check
        else if (e1.getX() - e2.getX() > sensitivity) {
            TranslateAnimation animation = new TranslateAnimation(0, -1*mDisplayWidth, 0, 0);
            animation.setDuration(100);
            animation.setFillAfter(false);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mAnimationLayout.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(LandingActivity.this, NavigationActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mAnimationLayout.startAnimation(animation);


            return true;
        }
        //Swipe Right Check
        else if (e2.getX() - e1.getX() > sensitivity) {
            return true;
        } else if((e2.getRawX()>e1.getRawX()) && (yDistance < 15)){
            float xDistance = Math.abs(e2.getRawX() - e1.getRawX());

            if(xDistance > 50){
                finish();
                return true;
            }
        }else{
            return true;
        }

        return false;
    }

    private void checkLocationManager() {

        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        public void onLocationChanged(android.location.Location location) {

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
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    private void writeLatLonToSharedPreference(){
        writeValueToSharedPreference(LAT_KEY, (float)mLocation.getLatitude());
        writeValueToSharedPreference(LON_KEY, (float)mLocation.getLongitude());
    }


    private float getStateFromSharedPreferences(String key) {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getFloat(key, -1);
    }


    private void setLatLonFromSharedPreference() {
        double lat = (double)getStateFromSharedPreferences(LAT_KEY);
        double lon = (double)getStateFromSharedPreferences(LON_KEY);
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lon);
    }

    private void clearSharedPreference(String key) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.remove(key);
        editor.commit();
    }

    private void clearSharedPreference() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.remove(LAT_KEY);
        editor.remove(LON_KEY);
        editor.commit();
    }
}
