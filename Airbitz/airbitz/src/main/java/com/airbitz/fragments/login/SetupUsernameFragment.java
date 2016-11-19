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

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.AirbitzCore;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.settings.PasswordRecoveryFragment;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on 2/26/15.
 */
public class SetupUsernameFragment extends BaseFragment implements NavigationActivity.OnBackPress {

    private final String TAG = getClass().getSimpleName();

    public static int USERNAME_MIN_LENGTH = 3;

    private EditText mUserNameEditText;
    private Button mNextButton;
    private CheckUsernameTask mCheckUsernameTask;
    private AirbitzCore mCoreAPI;
    private View mView;
    private Handler mHandler = new Handler();
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = AirbitzCore.getApi();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_username, container, false);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

//        mUserNameRedRingCover = mView.findViewById(R.id.fragment_setup_username_redring);
//        mUserNameRedRingCover.setVisibility(View.GONE);

//        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
//        mTitleTextView.setTypeface(NavigationActivity.latoBlackTypeFace);
//        mTitleTextView.setText(R.string.fragment_setup_titles);

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.fragment_setup_titles);
        getBaseActivity().setSupportActionBar(mToolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

//        mBackButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_back);
//        mBackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getActivity().onBackPressed();
//            }
//        });

        mNextButton = (Button) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        mUserNameEditText = (EditText) mView.findViewById(R.id.fragment_setup_username_edittext);
        mUserNameEditText.setTypeface(NavigationActivity.latoRegularTypeFace);
        mUserNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        mUserNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mUserNameEditText);
                }
            }
        });

        mUserNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    goNext();
                    return true;
                }
                return false;
            }
        });
        return mView;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onBackPress();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goNext() {
        mActivity.hideSoftKeyboard(mUserNameEditText);

        if (mCheckUsernameTask != null) {
            return;
        }
        else if(mUserNameEditText.getText().toString().length() < USERNAME_MIN_LENGTH) {
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.activity_signup_insufficient_username_title),
                    getResources().getString(R.string.activity_signup_insufficient_username_message));
        }
        else {
            // Reset errors.
            mUserNameEditText.setError(null);

            mCheckUsernameTask = new CheckUsernameTask();
            mCheckUsernameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUserNameEditText.getText().toString());
        }
    }

    private void launchSetupPin() {
        SetupPinFragment fragment = new SetupPinFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SetupWriteItDownFragment.USERNAME, mUserNameEditText.getText().toString());
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);

        // Save username if the user hits back
        getArguments().putString(SetupWriteItDownFragment.USERNAME, mUserNameEditText.getText().toString());
    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        mActivity.noSignup();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        enableNextButton(true);
        if (bundle.containsKey(SetupWriteItDownFragment.USERNAME)) {
            mUserNameEditText.setText(bundle.getString(SetupWriteItDownFragment.USERNAME));
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mUserNameEditText.setSelection(mUserNameEditText.getText().length());
                mUserNameEditText.requestFocus();
            }
        });
    }

    /**
     * Determine if username available
     */
    public class CheckUsernameTask extends AsyncTask<String, Void, Boolean> {
        AirbitzException mFailureException;
        CheckUsernameTask() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            try {
                return mCoreAPI.isUsernameAvailable(username);
            } catch (AirbitzException e) {
                AirbitzCore.logi("CheckUsernameTask error:");
                mFailureException = e;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            onCancelled();
            if (result) {
                launchSetupPin();
            } else {
                mActivity.ShowFadingDialog(
                    Common.errorMap(mActivity, mFailureException));
            }
        }

        @Override
        protected void onCancelled() {
            mCheckUsernameTask = null;
            mActivity.showModalProgress(false);
        }
    }
}
