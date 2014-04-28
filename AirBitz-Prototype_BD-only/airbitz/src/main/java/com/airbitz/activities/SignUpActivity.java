package com.airbitz.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbitz.R;
import com.airbitz.utils.Common;

import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class SignUpActivity extends Activity {

    private Button mNextButton;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private EditText mWithdrawalPinEditText;
    private TextView mTitleTextView;
    private TextView mHintTextView;
    private View mProgressView;
    private View mLoginView;
    private UserLoginTask mAuthTask;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private static final String specialChar = "~`!@#$%^&*()-_+=,.?/<>:;'][{}|\\\"";
    private static final String passwordPattern = ".*[" + Pattern.quote(specialChar) + "].*";


    private Intent mIntent;

//    private GestureDetector mGestureDetector;

    public static String KEY_USERNAME = "KEY_USERNAME";
    public static String KEY_PASSWORD = "KEY_PASSWORD";
    public static String KEY_WITHDRAWAL = "KEY_WITHDRAWAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        mLoginView = (View) findViewById(R.id.layout_signup);
        mProgressView = (View) findViewById(R.id.layout_progress);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mUsernameEditText = (EditText) findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText) findViewById(R.id.edittext_password);
        mPasswordConfirmationEditText = (EditText) findViewById(R.id.edittext_repassword);
        mWithdrawalPinEditText = (EditText) findViewById(R.id.edittext_withdrawalpin);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mHintTextView = (TextView) findViewById(R.id.textview_pass_hint);
        TextView withdrawalTextView = (TextView) findViewById(R.id.textview_withdrawal);

        withdrawalTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mUsernameEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mPasswordEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mPasswordConfirmationEditText.setTypeface(LandingActivity.montserratRegularTypeFace);
        mHintTextView.setTypeface(LandingActivity.montserratRegularTypeFace);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(SignUpActivity.this, "Info", "Business directory info");
            }
        });

    }

    private boolean goodUsername(String name) {
        return name.length() != 0;
    }

    private boolean goodPassword(String password) {
        if (password.length() >= 10 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(passwordPattern)) {
                return true;
            }
        return false;
    }

    private boolean goodConfirmation(String password, String confirmation) {
        return password.equals(confirmation);
    }

    private boolean goodPin(String pin) {
        return pin.matches("[0-9]+");
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mPin;

        UserLoginTask(String email, String password, String pin) {
            mUsername = email;
            mPassword = password;
            mPin = pin;
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

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                mIntent = new Intent(SignUpActivity.this, PasswordRecoveryActivity.class);
                mIntent.putExtra(KEY_USERNAME, mUsername);
                mIntent.putExtra(KEY_PASSWORD, mPassword);
                mIntent.putExtra(KEY_WITHDRAWAL, mPin);
                startActivity(mIntent);
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

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Store values at the time of the login attempt.
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmation = mPasswordConfirmationEditText.getText().toString();
        String pin = mWithdrawalPinEditText.getText().toString();

        // Reset errors.
        mPasswordEditText.setError(null);
        mUsernameEditText.setError(null);
        mPasswordConfirmationEditText.setError(null);
        mWithdrawalPinEditText.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (!goodUsername(username)) {
            mUsernameEditText.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameEditText;
            cancel = true;
        }

        // Check for a valid password.
        else if (!goodPassword(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        }

        // Check for a valid confirmation.
        else if (!goodConfirmation(password, confirmation)) {
            mPasswordConfirmationEditText.setError(getString(R.string.error_invalid_confirmation));
            focusView = mPasswordConfirmationEditText;
            cancel = true;
        }

        // Check for a valid confirmation.
        else if (!goodPin(pin)) {
            mWithdrawalPinEditText.setError(getString(R.string.error_invalid_pin));
            focusView = mWithdrawalPinEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, pin);
            mAuthTask.execute((Void) null);
        }
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
