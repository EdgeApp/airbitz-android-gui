package com.airbitz.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.utils.Common;

public class LandingFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private ImageView mLogoImageView;

    private TextView mDetailTextView;
    private LinearLayout mSwipeTextLayout;

    private ImageView mRightArrow;
    private ImageView mLeftArrow;

    private EditText mUserNameEditText;
    private EditText mPasswordEditText;

    private CoreAPI mCoreAPI;
    View activityRootView;

    boolean keyboardUp = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        mLogoImageView = (ImageView) view.findViewById(R.id.fragment_landing_logo_imageview);

        mDetailTextView = (TextView) view.findViewById(R.id.fragment_landing_detail_textview);
        mSwipeTextLayout = (LinearLayout) view.findViewById(R.id.fragment_landing_swipe_layout);
        TextView mSwipeTextView = (TextView) view.findViewById(R.id.fragment_landing_swipe_textview);

        mUserNameEditText = (EditText) view.findViewById(R.id.fragment_landing_username_edittext);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_landing_password_edittext);
        HighlightOnPressButton mSignInButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_signin_button);
        HighlightOnPressButton mSignUpButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_signup_button);
        HighlightOnPressButton mForgotPasswordButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_forgot_password_button);

        mRightArrow = (ImageView) view.findViewById(R.id.fragment_landing_arrowright_imageview);
        mLeftArrow = (ImageView) view.findViewById(R.id.fragment_landing_arrowleft_imageview);

        mDetailTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);
        mSwipeTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignInButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignUpButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        activityRootView = view.findViewById(R.id.fragment_landing_container);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean oldKeyboardState = keyboardUp;
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if(heightDiff < 100) {
                    keyboardUp = false;
                } else {
                    keyboardUp = true;
                }
                if(oldKeyboardState && !keyboardUp) {
                    collapseVertically(false);
                } else {

                }
            }
        });

        mUserNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                collapseVertically(hasFocus);
            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                collapseVertically(hasFocus);
            }
        });

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()  {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    collapseVertically(false);
                }
                return false;
            }
        });

        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserNameEditText.getText().toString().isEmpty()) {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog("",
                            getResources().getString(R.string.fragment_forgot_no_username_title));
                } else {
                    attemptForgotPassword();
                }
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).hideSoftKeyboard(mPasswordEditText);
                ((NavigationActivity)getActivity()).hideSoftKeyboard(mUserNameEditText);
                attemptLogin();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( ((NavigationActivity)getActivity()).networkIsAvailable()) {
                    ((NavigationActivity) getActivity()).startSignUp();
                } else {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog("", getActivity().getString(R.string.string_no_connection_message));
                }
            }
        });

        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUserNameEditText.setText(prefs.getString(AirbitzApplication.LOGIN_NAME, ""));

        ObjectAnimator leftBounce = ObjectAnimator.ofFloat(mLeftArrow, "translationX", 0, -50);
        leftBounce.setRepeatCount(ValueAnimator.INFINITE);
        leftBounce.setDuration(500);
        leftBounce.setRepeatMode(ValueAnimator.REVERSE);
        leftBounce.start();
        ObjectAnimator rightBounce = ObjectAnimator.ofFloat(mRightArrow,"translationX", 0, 50);
        rightBounce.setRepeatCount(ValueAnimator.INFINITE);
        rightBounce.setDuration(500);
        rightBounce.setRepeatMode(ValueAnimator.REVERSE);
        rightBounce.start();

        return view;
    }

    void collapseVertically(boolean collapse) {
        if (collapse) {
            mDetailTextView.setVisibility(View.GONE);
            mSwipeTextLayout.setVisibility(View.GONE);
            if (activityRootView.getHeight() < (int) getActivity().getResources().getDimension(R.dimen.fragment_landing_content_total_height)) {
                mLogoImageView.setVisibility(View.GONE);
            }
        } else {
            mDetailTextView.setVisibility(View.VISIBLE);
            mSwipeTextLayout.setVisibility(View.VISIBLE);
            mLogoImageView.setVisibility(View.VISIBLE);
        }
    }

    private void attemptForgotPassword() {
        mRecoveryQuestionsTask = new GetRecoveryQuestionsTask();
        mRecoveryQuestionsTask.execute(mUserNameEditText.getText().toString());
    }

    /**
     * Represents an asynchronous question fetch task
     */
    private GetRecoveryQuestionsTask mRecoveryQuestionsTask;
    public class GetRecoveryQuestionsTask extends AsyncTask<String, Void, String> {

        @Override
        public void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return mCoreAPI.GetRecoveryQuestionsForUser(params[0]);
        }

        @Override
        protected void onPostExecute(String questionString) {
            ((NavigationActivity)getActivity()).showModalProgress(false);

            mRecoveryQuestionsTask = null;

            if(questionString==null) {
                ((NavigationActivity)getActivity()).ShowOkMessageDialog(getString(R.string.fragment_forgot_no_recovery_questions_title),
                        getString(R.string.fragment_forgot_no_recovery_questions_text));
            } else { // Some message or questions
                String[] questions = questionString.split("\n");
                if(questions.length > 1) { // questions came back
                    ((NavigationActivity) getActivity()).startRecoveryQuestions(questionString, mUserNameEditText.getText().toString());
                } else if(questions.length == 1) { // Error string
                    Common.LogD(TAG, questionString);
                    ((NavigationActivity)getActivity()).ShowOkMessageDialog(getString(R.string.fragment_forgot_no_account_title), questions[0]);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRecoveryQuestionsTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
        }

    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid mUsername.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_field_required));
            focusView = mUserNameEditText;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_credentials));
            focusView = mUserNameEditText;
            cancel = true;
        }

        // Check for a valid mPassword.
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            if(null == focusView) {
                focusView = mPasswordEditText;
                cancel = true;
            }
        } else if (!isPasswordValid(password)) {
            mPasswordEditText.setError(getString(R.string.error_invalid_credentials));
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
            ((NavigationActivity) getActivity()).attemptLogin(username, password);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO real logic for good mUsername
        return !username.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        //TODO real logic for good mPassword
        return !password.isEmpty();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }
}
