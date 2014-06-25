package com.airbitz.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_RequestResults;
import com.airbitz.utils.Common;

import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class SignUpActivity extends Activity {
    public static final int DOLLAR_CURRENCY_NUMBER = 840;

    static {
        System.loadLibrary("airbitz");
    }

    private Button mNextButton;

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private EditText mWithdrawalPinEditText;
    private TextView mTitleTextView;
    private TextView mHintTextView;
    private View mProgressView;
    private View mLoginView;
    private CreateAccountTask mAuthTask;

    private LinearLayout popupContainer;
    private ImageView switchImage1;
    private ImageView switchImage2;
    private ImageView switchImage3;
    private ImageView switchImage4;
    private ImageView switchImage5;

    private View redRingDummy;

    private ImageButton mBackButton;
    private ImageButton mHelpButton;

    private static final String specialChar = "~`!@#$%^&*()-_+=,.?/<>:;'][{}|\\\"";
    private static final String passwordPattern = ".*[" + Pattern.quote(specialChar) + "].*";


    private Intent mIntent;

    private CreateFirstWalletTask mCreateFirstWalletTask;
    private CoreAPI mAPI;

//    private GestureDetector mGestureDetector;

    public static String KEY_USERNAME = "KEY_USERNAME";
    public static String KEY_PASSWORD = "KEY_PASSWORD";
    public static String KEY_WITHDRAWAL = "KEY_WITHDRAWAL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        mAPI = CoreAPI.getApi();

        mLoginView = (View) findViewById(R.id.layout_signup);
        mProgressView = (View) findViewById(R.id.layout_progress);

        redRingDummy = findViewById(R.id.red_ring);

        mUsernameEditText = (EditText) findViewById(R.id.edittext_username);
        mPasswordEditText = (EditText) findViewById(R.id.edittext_password);
        mPasswordConfirmationEditText = (EditText) findViewById(R.id.edittext_repassword);
        mWithdrawalPinEditText = (EditText) findViewById(R.id.edittext_withdrawalpin);

        mTitleTextView = (TextView) findViewById(R.id.textview_title);
        mHintTextView = (TextView) findViewById(R.id.textview_pass_hint);
        TextView withdrawalTextView = (TextView) findViewById(R.id.textview_withdrawal);

        withdrawalTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mUsernameEditText.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mPasswordEditText.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mPasswordConfirmationEditText.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mHintTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.montserratRegularTypeFace);

        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mHelpButton = (ImageButton) findViewById(R.id.button_help);

        switchImage1 = (ImageView) findViewById(R.id.switch_image_1);
        switchImage2 = (ImageView) findViewById(R.id.switch_image_2);
        switchImage3 = (ImageView) findViewById(R.id.switch_image_3);
        switchImage4 = (ImageView) findViewById(R.id.switch_image_4);
        switchImage5 = (ImageView) findViewById(R.id.switch_image_5);

        popupContainer = (LinearLayout) findViewById(R.id.popup_container_signup);

        /*mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });*///TODO implement moving to next screen on done key

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

        mWithdrawalPinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if ( actionId == EditorInfo.IME_ACTION_DONE){
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if(mUsernameEditText.getText().toString().length() < 4){
                    redRingDummy.setVisibility(View.VISIBLE);
                }else{
                    redRingDummy.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                String password = mPasswordEditText.getText().toString();

                if(password.length() >= 10){
                    switchImage5.setImageResource(R.drawable.green_check);
                }else{
                    switchImage5.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*[A-Z].*")){
                    switchImage1.setImageResource(R.drawable.green_check);
                }else{
                    switchImage1.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*[a-z].*")){
                    switchImage2.setImageResource(R.drawable.green_check);
                }else{
                    switchImage2.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*\\d.*")){
                    switchImage3.setImageResource(R.drawable.green_check);
                }else{
                    switchImage3.setImageResource(R.drawable.red_x);
                }
                if(password.matches(passwordPattern)){
                    switchImage4.setImageResource(R.drawable.green_check);
                }else{
                    switchImage4.setImageResource(R.drawable.red_x);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    popupContainer.setVisibility(View.VISIBLE);
                }else{
                    popupContainer.setVisibility(View.GONE);
                }
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
     * Represents an asynchronous account creation task
     */
    public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mPin;
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pData = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pData);

        CreateAccountTask(String email, String password, String pin) {
            mUsername = email;
            mPassword = password;
            mPin = pin;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            tABC_CC code = core.ABC_CreateAccount(mUsername, mPassword, mPin, null, pVoid, pError);
            return code == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                mCreateFirstWalletTask = new CreateFirstWalletTask(mUsername, mPassword, mPin);
                mCreateFirstWalletTask.execute((Void) null);
            } else {
                showProgress(false);
                ShowReasonAlert(getResources().getString(R.string.error_invalid_credentials));
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    public class CreateFirstWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername, mPassword, mPin;

        CreateFirstWalletTask(String username, String password, String pin) {
            mUsername = username;
            mPassword = password;
            mPin = pin;
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String walletName = getResources().getString(R.string.activity_recovery_first_wallet_name);
            return mAPI.createWallet(walletName, mUsername, mPassword, DOLLAR_CURRENCY_NUMBER);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateFirstWalletTask = null;
            showProgress(false);
            if (!success) {
                ShowReasonAlert("Create wallet failed");
            } else {
                mIntent = new Intent(SignUpActivity.this, PasswordRecoveryActivity.class);
                mIntent.putExtra(KEY_USERNAME, mUsername);
                mIntent.putExtra(KEY_PASSWORD, mPassword);
                mIntent.putExtra(KEY_WITHDRAWAL, mPin);
                startActivity(mIntent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateFirstWalletTask = null;
        }
    }

    private void ShowReasonAlert(String reason) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(reason)
                .setTitle(getResources().getString(R.string.activity_recovery_alert_title))
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
            mAuthTask = new CreateAccountTask(username, password, pin);
            mAuthTask.execute((Void) null);
        }
    }

    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            /*mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });*/

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
            //mLoginView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


}
