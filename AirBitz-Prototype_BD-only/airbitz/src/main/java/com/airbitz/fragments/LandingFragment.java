package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.ForgotPasswordActivity;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.activities.SignUpActivity;
import com.airbitz.api.SWIGTYPE_p_void;
import com.airbitz.api.core;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_RequestResults;

public class LandingFragment extends Fragment {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private RelativeLayout mLandingLayout;

    private ImageView mLogoImageView;

    private TextView mDetailTextView;
    private LinearLayout mSwipeTextLayout;

    private EditText mUserNameEditText;
    private EditText mPasswordEditText;

    private View mProgressView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(AirbitzApplication.isLoggedIn()) { gotoBusinessDirectoryLoggedIn(); }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mProgressView = view.findViewById(R.id.fragment_landing_login_progressbar);
        mLandingLayout = (RelativeLayout) view.findViewById(R.id.fragment_landing_main_layout);

        mLogoImageView = (ImageView) view.findViewById(R.id.fragment_landing_logo_imageview);

        mDetailTextView = (TextView) view.findViewById(R.id.fragment_landing_detail_textview);
        mSwipeTextLayout = (LinearLayout) view.findViewById(R.id.fragment_landing_swipe_layout);
        TextView mSwipeTextView = (TextView) view.findViewById(R.id.fragment_landing_swipe_textview);

        mUserNameEditText = (EditText) view.findViewById(R.id.fragment_landing_username_edittext);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_landing_password_edittext);
        Button mSignInButton = (Button) view.findViewById(R.id.fragment_landing_signin_button);
        Button mSignUpButton = (Button) view.findViewById(R.id.fragment_landing_signup_button);
        ImageView mForgotImageView = (ImageView) view.findViewById(R.id.fragment_landing_forgot_password_imageview);
        TextView mForgotPasswordTextView = (TextView) view.findViewById(R.id.fragment_landing_forgot_password_textview);

        mDetailTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mSwipeTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignInButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignUpButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mForgotPasswordTextView.setTypeface(NavigationActivity.latoRegularTypeFace);


        final View activityRootView = view.findViewById(R.id.fragment_landing_container);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100) { // if more than 100 pixels, its probably a keyboard...
                    mDetailTextView.setVisibility(View.GONE);
                    mSwipeTextLayout.setVisibility(View.GONE);
                    if(activityRootView.getHeight() < (int)getActivity().getResources().getDimension(R.dimen.fragment_landing_content_total_height)){
                        mLogoImageView.setVisibility(View.GONE);
                    }
                } else {
                    mDetailTextView.setVisibility(View.VISIBLE);
                    mSwipeTextLayout.setVisibility(View.VISIBLE);
                    mLogoImageView.setVisibility(View.VISIBLE);
                }
            }
        });


        mForgotImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserNameEditText.getText().toString().isEmpty()){
                    showAlertDialog();
                }else {
                    System.out.println("THis is me: "+mUserNameEditText.getText().toString());
                    Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
                    startActivity(intent);
                }
            }
        });

        mForgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserNameEditText.getText().toString().isEmpty()){
                    showAlertDialog();
                }else {
                    Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
                    intent.putExtra(SignUpActivity.KEY_USERNAME, mUserNameEditText.getText().toString());
                    startActivity(intent);
                }
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(!mPasswordEditText.getText().toString().isEmpty() && !mUserNameEditText.getText().toString().isEmpty()){
                    mgr.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);
                    mgr.hideSoftInputFromWindow(mUserNameEditText.getWindowToken(), 0);
                }
                attemptLogin();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(mUserNameEditText.getWindowToken(), 0);
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
            }
        });

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
            SWIGTYPE_p_void pVoid = core.requestResultsp_to_voidp(pResults);

            tABC_CC result = core.ABC_SignIn(mUsername, mPassword, null, pVoid, pError);

            return result == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            showProgress(false);
            if (success){
                AirbitzApplication.Login(mUsername, mPassword);
                gotoBusinessDirectoryLoggedIn();
            } else {
                showErrorDialog();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void gotoBusinessDirectoryLoggedIn() {
        ((NavigationActivity) getActivity()).setLoginView(false);
        ((NavigationActivity) getActivity()).onNavBarSelected(0);
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
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_field_required));
            focusView = mUserNameEditText;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_username));
            focusView = mUserNameEditText;
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            if(null == focusView) {
                focusView = mPasswordEditText;
                cancel = true;
            }
        } else if (!isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            if(null == focusView) {
                focusView = mPasswordEditText;
                cancel = true;
            }
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
        alertDialogBuilder.setMessage(getResources().getString(R.string.fragment_forgot_no_username_details))
                .setCancelable(false)
                .setNeutralButton(getResources().getString(R.string.string_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}
