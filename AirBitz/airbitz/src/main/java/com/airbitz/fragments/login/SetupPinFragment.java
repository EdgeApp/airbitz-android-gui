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
 * Created on 2/10/14.
 */
public class SetupPinFragment extends BaseFragment implements NavigationActivity.OnBackPress {
    public static final int MIN_PIN_LENGTH = 4;
    public static final String USERNAME = "com.airbitz.setuppin.username";
    public static final String PASSWORD = "com.airbitz.setuppin.password";

    private final String TAG = getClass().getSimpleName();

    private EditText mWithdrawalPinEditText;
    private HighlightOnPressButton mNextButton;
    private HighlightOnPressButton mBackButton;
    private TextView mTitleTextView;
    private CoreAPI mCoreAPI;
    private View mView;
    private NavigationActivity mActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_setup_pin, container, false);

        mActivity = (NavigationActivity) getActivity();

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_setup_titles);

        mBackButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mNextButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_setup_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        mWithdrawalPinEditText = (EditText) mView.findViewById(R.id.fragment_setup_pin_edittext);
        mWithdrawalPinEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        mWithdrawalPinEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
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
                    mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
                    enableNextButton(true);
                }
            }
        };
        mWithdrawalPinEditText.addTextChangedListener(mPINTextWatcher);

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

    private void goNext() {
        mActivity.hideSoftKeyboard(mWithdrawalPinEditText);
        // if they entered a valid mUsername or old mPassword
        if (pinFieldIsValid()) {
            String username = getArguments().getString(USERNAME);
            char[] password = getArguments().getString(PASSWORD).toCharArray();
            String pin = mWithdrawalPinEditText.getText().toString();

            // Reset errors.
            mWithdrawalPinEditText.setError(null);

            // TODO launch WriteItDown screen
        }
    }

    // checks the pin field
    // returns YES if field is good
    // if the field is bad, an appropriate message box is displayed
    // note: this function is aware of the 'mode' of the view controller and will check and display appropriately
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
        enableNextButton(false);
    }
}
