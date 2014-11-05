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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;

public class LandingFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    View activityRootView;
    private TextView mDetailTextView;
    private ImageView mRightArrow;
    private EditText mUserNameEditText;
    private View mPasswordLayout;
    private EditText mPasswordEditText;
    private EditText mPinEditText;
    private View mPinLayout;
    
    private HighlightOnPressButton mCreateAccountButton;
    private TextView mCurrentUserTextView;
    private TextView mForgotTextView;

    private String mUsername;
    private String mPinScratchpad;
    
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    /**
     * Represents an asynchronous question fetch task
     */
    private GetRecoveryQuestionsTask mRecoveryQuestionsTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);

        activityRootView = view.findViewById(R.id.fragment_landing_container);

        mDetailTextView = (TextView) view.findViewById(R.id.fragment_landing_detail_textview);
        mDetailTextView.setTypeface(NavigationActivity.montserratRegularTypeFace);

        TextView mSwipeTextView = (TextView) view.findViewById(R.id.fragment_landing_swipe_textview);

        mUserNameEditText = (EditText) view.findViewById(R.id.fragment_landing_username_edittext);
        mPasswordEditText = (EditText) view.findViewById(R.id.fragment_landing_password_edittext);

        mRightArrow = (ImageView) view.findViewById(R.id.fragment_landing_arrowright_imageview);

        mSwipeTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
        mUserNameEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordLayout = view.findViewById(R.id.fragment_landing_password_layout);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mCreateAccountButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_create_account);
        mCreateAccountButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((NavigationActivity) getActivity()).networkIsAvailable()) {
//                    ((NavigationActivity) getActivity()).startSignUp();
                } else {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog("", getActivity().getString(R.string.string_no_connection_message));
                }
            }
        });

        mCurrentUserTextView = (TextView) view.findViewById(R.id.fragment_landing_current_user);
        mPinLayout = view.findViewById(R.id.fragment_landing_pin_entry_layout);

        mPinEditText = (EditText) view.findViewById(R.id.fragment_landing_pin_edittext);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 4) {
                    mActivity.hideSoftKeyboard(mPinEditText);
                    mPinEditText.clearFocus();
                }
            }
        };
        mPinEditText.addTextChangedListener(mPINTextWatcher);
        mPinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mPinEditText);
                }
            }
        });


        mForgotTextView = (TextView) view.findViewById(R.id.fragment_landing_forgot_text);

        LinearLayout mForgotPasswordButton = (LinearLayout) view.findViewById(R.id.fragment_landing_forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserNameEditText.getText().toString().isEmpty()) {
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog("",
                            getResources().getString(R.string.fragment_forgot_no_username_title));
                } else {
                    attemptForgotPasswordOrPin();
                }
            }
        });

        HighlightOnPressButton mSignInButton = (HighlightOnPressButton) view.findViewById(R.id.fragment_landing_signin_button);
        mSignInButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).hideSoftKeyboard(mPasswordEditText);
                ((NavigationActivity) getActivity()).hideSoftKeyboard(mUserNameEditText);
                attemptPasswordLogin();
            }
        });

        SharedPreferences prefs = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mUsername = prefs.getString(AirbitzApplication.LOGIN_NAME, "");
        mUserNameEditText.setText(mUsername);

        ObjectAnimator rightBounce = ObjectAnimator.ofFloat(mRightArrow, "translationX", 0, 50);
        rightBounce.setRepeatCount(3);
        rightBounce.setDuration(500);
        rightBounce.setRepeatMode(ValueAnimator.REVERSE);
        rightBounce.start();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCoreAPI.PinLoginExists(mUsername)) {
            mPasswordLayout.setVisibility(View.GONE);
            mPinLayout.setVisibility(View.VISIBLE);

            String out = String.format(getString(R.string.fragment_landing_current_user), mUsername);
            int start = out.indexOf(mUsername);

            SpannableStringBuilder s = new SpannableStringBuilder();
            s.append(out).setSpan(new ForegroundColorSpan(Color.BLUE), start, mUsername.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mCurrentUserTextView.setText(s);
            mCreateAccountButton.setText(getString(R.string.fragment_landing_switch_user));
            mForgotTextView.setText(getString(R.string.fragment_landing_forgot_pin));
        }
    }

    private void attemptForgotPasswordOrPin() {
        if(mCoreAPI.PinLoginExists(mUsername)) {
            //TODO Pin forgot attempt
        } else {
            mRecoveryQuestionsTask = new GetRecoveryQuestionsTask();
            mRecoveryQuestionsTask.execute(mUserNameEditText.getText().toString());
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptPasswordLogin() {

        // Reset errors.
        mPasswordEditText.setError(null);
        mUserNameEditText.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameEditText.getText().toString();
        Editable pass = mPasswordEditText.getText();
        char[] password = new char[pass.length()];
        pass.getChars(0, pass.length(), password, 0);

        boolean cancel = false;
        View focusView = null;

        // Check for empty username.
        if (TextUtils.isEmpty(username)) {
            mUserNameEditText.setError(getString(R.string.error_invalid_credentials));
            focusView = mUserNameEditText;
            cancel = true;
        }

        // Check for empty password.
        if (password.length<1) {
            mPasswordEditText.setError(getString(R.string.error_invalid_credentials));
            if (null == focusView) {
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

    public class GetRecoveryQuestionsTask extends AsyncTask<String, Void, String> {

        @Override
        public void onPreExecute() {
            ((NavigationActivity) getActivity()).showModalProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return mCoreAPI.GetRecoveryQuestionsForUser(params[0]);
        }

        @Override
        protected void onPostExecute(String questionString) {
            ((NavigationActivity) getActivity()).showModalProgress(false);

            mRecoveryQuestionsTask = null;

            if (questionString == null) {
                ((NavigationActivity) getActivity()).ShowOkMessageDialog(getString(R.string.fragment_forgot_no_recovery_questions_title),
                        getString(R.string.fragment_forgot_no_recovery_questions_text));
            } else { // Some message or questions
                String[] questions = questionString.split("\n");
                if (questions.length > 1) { // questions came back
                    ((NavigationActivity) getActivity()).startRecoveryQuestions(questionString, mUserNameEditText.getText().toString());
                } else if (questions.length == 1) { // Error string
                    Log.d(TAG, questionString);
                    ((NavigationActivity) getActivity()).ShowOkMessageDialog(getString(R.string.fragment_forgot_no_account_title), questions[0]);
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRecoveryQuestionsTask = null;
            ((NavigationActivity) getActivity()).showModalProgress(false);
        }

    }
}
