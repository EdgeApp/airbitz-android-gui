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

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;

/**
 * Created on 2/10/14.
 */
public class SpendingLimitsFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private EditText mPasswordEditText;
    private View mPasswordRedRing;
    private Button mSaveButton;
    private HighlightOnPressImageButton mHelpButton;
    private TextView mTitleTextView;
    private Switch mDailySwitch;
    private EditText mDailyEditText;
    private TextView mDailyDenominationTextView;
    private Switch mPINSwitch;
    private EditText mPINEditText;
    private TextView mPINDenominationTextView;
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_spending_limits, container, false);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_spending_limits_title);

        ImageButton mBackButton = (ImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mDailySwitch = (Switch) mView.findViewById(R.id.fragment_spending_limits_toggle_daily_limit);
        mDailySwitch.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mDailyEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_daily_edittext);
        mDailyEditText.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mDailyDenominationTextView = (TextView) mView.findViewById(R.id.fragment_spending_limits_daily_denomination);
        mDailyDenominationTextView.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mDailySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustTextColors();
            }
        });


        mPINSwitch = (Switch) mView.findViewById(R.id.fragment_spending_limits_toggle_pin_limit);
        mPINSwitch.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mPINEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_pin_edittext);
        mPINEditText.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mPINDenominationTextView = (TextView) mView.findViewById(R.id.fragment_spending_limits_pin_denomination);
        mPINDenominationTextView.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mPINSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustTextColors();
            }
        });

        mSaveButton = (Button) mView.findViewById(R.id.fragment_spending_limits_button_logout);
        mSaveButton.setTypeface(NavigationActivity.montserratRegularTypeFace, Typeface.NORMAL);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSave();
            }
        });

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.pushFragment(new HelpFragment(HelpFragment.SPEND_LIMITS), NavigationActivity.Tabs.SETTING.ordinal());
            }
        });


        mPasswordRedRing = mView.findViewById(R.id.fragment_spending_limits_password__redring);

        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_password_edittext);
        mPasswordEditText.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                } else {
                }
            }
        });
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (mPasswordEditText.getText().toString().length() < 10 || mPasswordEditText.getText().toString().trim().length() < 10) {
                    mPasswordRedRing.setVisibility(View.VISIBLE);
                } else {
                    mPasswordRedRing.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return mView;
    }

    private void adjustTextColors() {
        if(mDailySwitch.isChecked()) {
            mDailyDenominationTextView.setTextColor(getResources().getColor(android.R.color.white));
            mDailyEditText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mDailyDenominationTextView.setTextColor(getResources().getColor(R.color.text_hint));
            mDailyEditText.setTextColor(getResources().getColor(R.color.text_hint));
        }
        if(mPINSwitch.isChecked()) {
            mPINDenominationTextView.setTextColor(getResources().getColor(android.R.color.white));
            mPINEditText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            mPINDenominationTextView.setTextColor(getResources().getColor(R.color.text_hint));
            mPINEditText.setTextColor(getResources().getColor(R.color.text_hint));
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mDailySwitch.setChecked(mCoreAPI.GetDailySpendLimitSetting());
        mDailyEditText.setText(mCoreAPI.formatSatoshi(mCoreAPI.GetDailySpendLimit(), false));
        mDailyDenominationTextView.setText(mCoreAPI.getUserBTCSymbol());
        mPINSwitch.setChecked(mCoreAPI.GetPINSpendLimitSetting());
        mPINEditText.setText(mCoreAPI.formatSatoshi(mCoreAPI.GetPINSpendLimit(), false));
        mPINDenominationTextView.setText(mCoreAPI.getUserBTCSymbol());
        adjustTextColors();
    }


    private void goSave() {
        if(mPasswordEditText.getText().toString().equals(AirbitzApplication.getPassword())) {
            if(mDailySwitch.isChecked()) {
                mCoreAPI.SetDailySpendLimitSetting(true);
                mCoreAPI.SetDailySpendSatoshis(mCoreAPI.denominationToSatoshi(mDailyEditText.getText().toString()));
            } else {
                mCoreAPI.SetDailySpendLimitSetting(false);
            }
            if(mPINSwitch.isChecked()) {
                mCoreAPI.SetPINSpendLimitSetting(true);
                mCoreAPI.SetPINSpendSatoshis(mCoreAPI.denominationToSatoshi(mPINEditText.getText().toString()));
            } else {
                mCoreAPI.SetPINSpendLimitSetting(false);
            }
            mActivity.popFragment();
        } else {
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_spending_limits_incorrect_password),
                    getResources().getString(R.string.fragment_spending_limits_incorrect_password));
        }
    }
}
