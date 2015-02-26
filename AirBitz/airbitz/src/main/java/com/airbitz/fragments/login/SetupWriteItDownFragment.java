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

package com.airbitz.fragments.login;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.core;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.api.tABC_CC;
import com.airbitz.api.tABC_Error;
import com.airbitz.api.tABC_PasswordRule;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.utils.Common;

import java.util.List;

/**
 * Created on 2/26/15.
 */
public class SetupWriteItDownFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    public static final String USERNAME = "com.airbitz.setupwriteitdown.username";
    public static final String PASSWORD = "com.airbitz.setupwriteitdown.password";
    public static final String PIN = "com.airbitz.setupwriteitdown.pin";

    private String mUsername;
    private char[] mPassword;
    private String mPin;

    private HighlightOnPressButton mNextButton;
    private HighlightOnPressButton mShowButton;
    private boolean mShow = true;
    private LinearLayout mShowContainer;
    private LinearLayout mHideContainer;
    private TextView mTitleTextView;
    private CreateAccountTask mCreateAccountTask;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_writeitdown, container, false);

        mActivity = (NavigationActivity) getActivity();

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mNextButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNextButton.setClickable(false);
                goNext();
            }
        });

        mShowButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_writeitdown_show);
        mShowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShow = !mShow;
                enableShow(mShow);
            }
        });

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.activity_signup_title);

        mShowContainer = (LinearLayout) mView.findViewById(R.id.fragment_setup_writeitdown_show_container);
        mHideContainer = (LinearLayout) mView.findViewById(R.id.fragment_setup_writeitdown_hide_container);

        return mView;
    }

    private void enableShow(boolean show) {
        if(show) {
            mShowButton.setText(R.string.fragment_setup_writeitdown_show);
            mShowContainer.setVisibility(View.VISIBLE);
            mHideContainer.setVisibility(View.GONE);
        }
        else {
            mShowButton.setText(R.string.fragment_setup_writeitdown_hide);
            mShowContainer.setVisibility(View.GONE);
            mHideContainer.setVisibility(View.VISIBLE);
        }
    }

    private void enableNextButton(boolean enable) {
        if(enable) {
            mNextButton.setBackgroundResource(R.drawable.setup_button_green);
            mNextButton.setClickable(true);
        }
        else {
            mNextButton.setBackgroundResource(R.drawable.setup_button_dark_gray);
            mNextButton.setClickable(false);
        }
    }

    private void goNext() {
        AirbitzApplication.Login(mUsername, mPassword);
        mCoreAPI.SetPin(mPin);

        mCoreAPI.setupAccountSettings();
        mCoreAPI.startAllAsyncUpdates();

        tABC_AccountSettings settings = mCoreAPI.coreSettings();
        settings.setRecoveryReminderCount(0);
        mCoreAPI.saveAccountSettings(settings);

        mActivity.UserJustLoggedIn(true);
    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        // Do not go back
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableNextButton(false);
        enableShow(mShow);
        Bundle bundle = getArguments();
        mUsername = bundle.getString(USERNAME);
        mPassword = bundle.getString(PASSWORD).toCharArray();
        mPin = bundle.getString(PIN);

        mCreateAccountTask = new CreateAccountTask();
        mCreateAccountTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    /**
     * Create Account
     */
    public class CreateAccountTask extends AsyncTask<Void, Void, Boolean> {
        tABC_Error pError = new tABC_Error();
        private String mFailureReason;

        CreateAccountTask() {
            enableNextButton(false);
            mActivity.ShowFadingDialog(getString(R.string.fragment_signup_creating_account), 2000000, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            tABC_CC code = core.ABC_CreateAccount(mUsername, String.valueOf(mPassword), pError);
            mFailureReason = Common.errorMap(mActivity, code);
            return code == tABC_CC.ABC_CC_Ok;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            onCancelled();
            if (success) {
                enableNextButton(true);
            } else {
                mActivity.ShowFadingDialog(mFailureReason);
            }
        }

        @Override
        protected void onCancelled() {
            mCreateAccountTask = null;
            mActivity.DismissFadingDialog();
        }
    }
}
