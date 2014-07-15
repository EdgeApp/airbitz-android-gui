package com.airbitz.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
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
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_RequestResults;

import java.util.regex.Pattern;

/**
 * Created on 2/10/14.
 */
public class SignUpActivity extends Activity {
    public static final int DOLLAR_CURRENCY_NUMBER = 840;

    private RelativeLayout mParentLayout;

    private View mProgressView;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private EditText mPasswordConfirmationEditText;
    private EditText mWithdrawalPinEditText;

    private LinearLayout mPopupContainer;
    private ImageView mSwitchImage1;
    private ImageView mSwitchImage2;
    private ImageView mSwitchImage3;
    private ImageView mSwitchImage4;
    private ImageView mSwitchImage5;

    private View mUserNameRedRingCover;
    private View mPinRedRingCover;
    private View mDummyFocus;


    private static final String specialChar = "~`!@#$%^&*()-_+=,.?/<>:;'][{}|\\\"";
    private static final String passwordPattern = ".*[" + Pattern.quote(specialChar) + "].*";

    private CreateAccountTask mAuthTask;

    private CreateFirstWalletTask mCreateFirstWalletTask;

    public static String KEY_USERNAME = "KEY_USERNAME";
    public static String KEY_PASSWORD = "KEY_PASSWORD";
    public static String KEY_WITHDRAWAL = "KEY_WITHDRAWAL";

    private CoreAPI mCoreAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();

        setContentView(R.layout.activity_signup);
        overridePendingTransition(R.anim.slide_in_from_right,R.anim.nothing);

        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_app));

        mParentLayout = (RelativeLayout) findViewById(R.id.activity_signup_parent_layout);

        mProgressView = findViewById(R.id.activity_signup_progressbar);

        mUserNameRedRingCover = findViewById(R.id.activity_signup_username_redring);
        mDummyFocus = findViewById(R.id.activity_signup_dummy_focus);

        mUserNameEditText = (EditText) findViewById(R.id.activity_signup_username_edittext);
        mPasswordEditText = (EditText) findViewById(R.id.activity_signup_password_edittext);
        mPasswordConfirmationEditText = (EditText) findViewById(R.id.activity_signup_repassword_edittext);
        mWithdrawalPinEditText = (EditText) findViewById(R.id.activity_signup_withdrawal_edittext);
        TextView mTitleTextView = (TextView) findViewById(R.id.activity_signup_title_textview);
        TextView mHintTextView = (TextView) findViewById(R.id.activity_signup_password_help);
        TextView withdrawalTextView = (TextView) findViewById(R.id.activity_signup_withdrawal_textview);

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mHintTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordConfirmationEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        withdrawalTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        ImageButton mBackButton = (ImageButton) findViewById(R.id.activity_signup_back_button);

        mSwitchImage1 = (ImageView) findViewById(R.id.activity_signup_switch_image_1);
        mSwitchImage2 = (ImageView) findViewById(R.id.activity_signup_switch_image_2);
        mSwitchImage3 = (ImageView) findViewById(R.id.activity_signup_switch_image_3);
        mSwitchImage4 = (ImageView) findViewById(R.id.activity_signup_switch_image_4);
        mSwitchImage5 = (ImageView) findViewById(R.id.activity_signup_switch_image_5);

        TextView mSwitchTextView1 = (TextView) findViewById(R.id.activity_signup_switch_text_1);
        mSwitchTextView1.setTypeface(NavigationActivity.latoRegularTypeFace);
        TextView mSwitchTextView2 = (TextView) findViewById(R.id.activity_signup_switch_text_2);
        mSwitchTextView2.setTypeface(NavigationActivity.latoRegularTypeFace);
        TextView mSwitchTextView3 = (TextView) findViewById(R.id.activity_signup_switch_text_3);
        mSwitchTextView3.setTypeface(NavigationActivity.latoRegularTypeFace);
        TextView mSwitchTextView4 = (TextView) findViewById(R.id.activity_signup_switch_text_4);
        mSwitchTextView4.setTypeface(NavigationActivity.latoRegularTypeFace);
        TextView mSwitchTextView5 = (TextView) findViewById(R.id.activity_signup_switch_text_5);
        mSwitchTextView5.setTypeface(NavigationActivity.latoRegularTypeFace);
        TextView mTimeTextView = (TextView) findViewById(R.id.activity_signup_time_textview);
        mTimeTextView.setTypeface(NavigationActivity.latoRegularTypeFace);

        mPopupContainer = (LinearLayout) findViewById(R.id.activity_signup_popup_layout);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.nothing, R.anim.slide_out_right);
            }
        });

        mWithdrawalPinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (mUserNameEditText.getText().toString().length() < 3 || mUserNameEditText.getText().toString().trim().length() < 3) {
                    mUserNameRedRingCover.setVisibility(View.VISIBLE);
                } else {
                    mUserNameRedRingCover.setVisibility(View.GONE);
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

                //TODO connect to core
                if(password.length() >= 10){
                    mSwitchImage5.setImageResource(R.drawable.green_check);
                }else{
                    mSwitchImage5.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*[A-Z].*")){
                    mSwitchImage1.setImageResource(R.drawable.green_check);
                }else{
                    mSwitchImage1.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*[a-z].*")){
                    mSwitchImage2.setImageResource(R.drawable.green_check);
                }else{
                    mSwitchImage2.setImageResource(R.drawable.red_x);
                }
                if(password.matches(".*\\d.*")){
                    mSwitchImage3.setImageResource(R.drawable.green_check);
                }else{
                    mSwitchImage3.setImageResource(R.drawable.red_x);
                }
                if(password.matches(passwordPattern)){
                    mSwitchImage4.setImageResource(R.drawable.green_check);
                }else{
                    mSwitchImage4.setImageResource(R.drawable.red_x);
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

                    /*mParentLayout.setLayoutTransition();
                    mParentLayout.animation*/
                    LayoutTransition lt = new LayoutTransition();
                    Animator animator = ObjectAnimator.ofFloat(null,"translationY",-(getResources().getDimension(R.dimen.activity_signup_popup_height)),0);
                    lt.setAnimator(LayoutTransition.APPEARING,animator);
                    lt.setStartDelay(LayoutTransition.APPEARING,0);
                    lt.setDuration(300);
                    mParentLayout.setLayoutTransition(lt);
                    mPopupContainer.setVisibility(View.VISIBLE);
                }else{
                    LayoutTransition lt = new LayoutTransition();
                    Animator animator = ObjectAnimator.ofFloat(null,"translationY",0,-(getResources().getDimension(R.dimen.activity_signup_popup_height)));
                    lt.setAnimator(LayoutTransition.DISAPPEARING,animator);
                    lt.setStartDelay(LayoutTransition.DISAPPEARING,0);
                    lt.setDuration(300);
                    mParentLayout.setLayoutTransition(lt);
                    mPopupContainer.setVisibility(View.GONE);
                }
            }
        });

    }

    private boolean goodUsername(String name) {
        return name.length() >= 3 && name.trim().length() >=3;
    }

    private boolean goodPassword(String password) {
        return password.length() >= 10 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(passwordPattern);
    }

    private boolean goodConfirmation(String password, String confirmation) {
        return password.equals(confirmation);
    }

    private boolean goodPin(String pin) {
        return pin.matches("[0-9]+") && pin.length()==4;
    }

    /**
     * Represents an asynchronous account creation task
     */
    public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mPin;
        private String mFailureReason;
        tABC_Error pError = new tABC_Error();
        tABC_RequestResults pData = new tABC_RequestResults();
        SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pData);

        CreateAccountTask(String email, String password, String pin) {
            mUsername = email;
            mPassword = password;
            mPin = pin;
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            tABC_CC code = core.ABC_CreateAccount(mUsername, mPassword, mPin, null, pVoid, pError);
            mFailureReason = pError.getSzDescription();
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
                ShowReasonAlert(mFailureReason);
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
            return mCoreAPI.createWallet(mUsername, mPassword, walletName, DOLLAR_CURRENCY_NUMBER);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateFirstWalletTask = null;
            showProgress(false);
            if (!success) {
                ShowReasonAlert("Create wallet failed");
            } else {
                AirbitzApplication.Login(mUsername, mPassword);
                Intent mIntent = new Intent(SignUpActivity.this, PasswordRecoveryActivity.class);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
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
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmation = mPasswordConfirmationEditText.getText().toString();
        String pin = mWithdrawalPinEditText.getText().toString();

        // Reset errors.
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);
        mPasswordConfirmationEditText.setError(null);
        mWithdrawalPinEditText.setError(null);

        boolean cancel = false;

        // Check for a valid username.
        if (!goodUsername(username)) {
            showErrorDialog(getResources().getString(R.string.error_invalid_username_details));
            cancel = true;
        }

        // Check for a valid password.
        else if (!goodPassword(password)) {
            String message = getString(R.string.error_invalid_password_details_start);
            if(!password.matches(".*[A-Z].*")){
                message+=getString(R.string.error_invalid_password_details_upper);
            }
            if(!password.matches(".*[a-z].*")){
                message+=getString(R.string.error_invalid_password_details_lower);
            }
            if(!password.matches(".*\\d.*")){
                message+=getString(R.string.error_invalid_password_details_number);
            }
            if(!password.matches(passwordPattern)){
                message+=getString(R.string.error_invalid_password_details_special);
            }
            if(password.length() < 10){
                message+=getString(R.string.error_invalid_password_details_length);
            }
            showErrorDialog(message);
            cancel = true;
        }

        // Check for a valid confirmation.
        else if (!goodConfirmation(password, confirmation)) {
            showErrorDialog(getString(R.string.error_invalid_confirmation_details));
            cancel = true;
        }

        // Check for a valid confirmation.
        else if (!goodPin(pin)) {
            showErrorDialog(getString(R.string.error_invalid_pin_details));
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mDummyFocus.requestFocus();
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

    @Override
    public void onBackPressed(){
        View activityRootView = findViewById(R.id.activity_signup_container_layout);
        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
        if(heightDiff > 100){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.nothing, R.anim.slide_out_right);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.AlertDialogCustom));
        builder.setMessage(message)
                .setTitle(getString(R.string.error_invalid_recovery_title))
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
}
