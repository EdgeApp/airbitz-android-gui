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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import co.airbitz.api.AirbitzException;
import co.airbitz.api.CoreAPI;
import co.airbitz.api.PasswordRule;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.objects.HighlightOnPressButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/26/15.
 */
public class SetupPinFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    private final String TAG = getClass().getSimpleName();

    public static final int MIN_PIN_LENGTH = 4;
    public static String PIN = "com.airbitz.setuppin.pin";

    private String mUsername;
    private EditText mWithdrawalPinEditText;
    private Button mNextButton;
    private CoreAPI mCoreAPI;
    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_pin, container, false);

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mToolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.fragment_setup_titles);
        getBaseActivity().setSupportActionBar(mToolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mNextButton = (Button) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.fragment_password_pin_edittext);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.latoRegularTypeFace);

        mWithdrawalPinEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final TextWatcher mPINTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() >= 4) {
                    mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
                }
            }
        };
        mWithdrawalPinEditText.addTextChangedListener(mPINTextWatcher);

        mWithdrawalPinEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mWithdrawalPinEditText);
                } else {
                }
            }
        });

        mWithdrawalPinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mNextButton.requestFocus();
                    goNext();
                    return true;
                }
                return false;
            }
        });
        return mView;
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
        mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
        // if they entered a valid mUsername or old mPassword
        if (pinFieldIsValid()) {
            launchSetupPassword();
        }
    }

    private void launchSetupPassword() {
        SetupPasswordFragment fragment = new SetupPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SetupWriteItDownFragment.USERNAME, mUsername);
        bundle.putString(SetupWriteItDownFragment.PIN, mWithdrawalPinEditText.getText().toString());
        fragment.setArguments(bundle);
        mActivity.pushFragment(fragment);

        // Save username if the user hits back
        getArguments().putString(SetupPinFragment.PIN, mWithdrawalPinEditText.getText().toString());
    }

    private boolean pinFieldIsValid() {
        boolean bpinNameFieldIsValid = true;

        // if the pin isn't long enough
        if (mWithdrawalPinEditText.getText().toString().length() < MIN_PIN_LENGTH) {
            bpinNameFieldIsValid = false;
            mActivity.ShowFadingDialog(getResources().getString(R.string.activity_signup_insufficient_pin));
        }

        return bpinNameFieldIsValid;
    }

    @Override
    public boolean onBackPress() {
        mActivity.hideSoftKeyboard(getView());
        mActivity.popFragment();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        enableNextButton(true);

        Bundle bundle = getArguments();
        mUsername = bundle.getString(SetupWriteItDownFragment.USERNAME);

        mWithdrawalPinEditText.requestFocus();
    }
}
