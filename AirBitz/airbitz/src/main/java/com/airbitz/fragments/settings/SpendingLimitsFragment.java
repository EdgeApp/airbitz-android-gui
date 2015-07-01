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

package com.airbitz.fragments.settings;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.objects.HighlightOnPressImageButton;

/**
 * Created on 2/10/14.
 */
public class SpendingLimitsFragment extends BaseFragment
    implements CoreAPI.OnPasswordCheckListener {
    private final String TAG = getClass().getSimpleName();

    private EditText mPasswordEditText;
    private View mPasswordRedRing;
    private Button mSaveButton;
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

        setHasOptionsMenu(true);
        setDrawerEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_spending_limits, container, false);

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fragment_spending_limits_title);
        getBaseActivity().setSupportActionBar(toolbar);
        getBaseActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getBaseActivity().getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDailySwitch = (Switch) mView.findViewById(R.id.fragment_spending_limits_toggle_daily_limit);
        mDailySwitch.setTypeface(Typeface.DEFAULT);
        mDailyEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_daily_edittext);
        mDailyEditText.setTypeface(Typeface.DEFAULT);
        mDailyDenominationTextView = (TextView) mView.findViewById(R.id.fragment_spending_limits_daily_denomination);
        mDailyDenominationTextView.setTypeface(Typeface.DEFAULT);
        mDailySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustTextColors();
            }
        });

        mPINSwitch = (Switch) mView.findViewById(R.id.fragment_spending_limits_toggle_pin_limit);
        mPINSwitch.setTypeface(Typeface.DEFAULT);
        mPINEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_pin_edittext);
        mPINEditText.setTypeface(Typeface.DEFAULT);
        mPINDenominationTextView = (TextView) mView.findViewById(R.id.fragment_spending_limits_pin_denomination);
        mPINDenominationTextView.setTypeface(Typeface.DEFAULT);
        mPINSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adjustTextColors();
            }
        });

        mSaveButton = (Button) mView.findViewById(R.id.fragment_spending_limits_button_logout);
        mSaveButton.setTypeface(Typeface.DEFAULT);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSave();
            }
        });

        mPasswordEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_password_edittext);
        mPasswordEditText.setTypeface(Typeface.DEFAULT);
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_standard, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.action_help:
                ((NavigationActivity) getActivity()).pushFragment(
                    new HelpFragment(HelpFragment.SPEND_LIMITS), NavigationActivity.Tabs.MORE.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

        if(!mCoreAPI.PasswordExists()) {
            mPasswordEditText.setVisibility(View.GONE);
        }

        mDailyEditText.setText(mCoreAPI.formatSatoshi(mCoreAPI.GetDailySpendLimit(), false));
        mDailySwitch.setChecked(mCoreAPI.GetDailySpendLimitSetting());
        mDailyDenominationTextView.setText(mCoreAPI.getUserBTCSymbol());

        mPINSwitch.setChecked(mCoreAPI.GetPINSpendLimitSetting());
        mPINEditText.setText(mCoreAPI.formatSatoshi(mCoreAPI.GetPINSpendLimit(), false));
        mPINDenominationTextView.setText(mCoreAPI.getUserBTCSymbol());
        adjustTextColors();
    }


    private void goSave() {
        mActivity.showModalProgress(true);
        mCoreAPI.SetOnPasswordCheckListener(this, mPasswordEditText.getText().toString());
    }

    @Override
    public void onPasswordCheck(boolean passwordOkay) {
        mActivity.showModalProgress(false);
        if(passwordOkay) {
            long satoshis = mCoreAPI.denominationToSatoshi(mDailyEditText.getText().toString());
            mCoreAPI.SetDailySpendSatoshis(satoshis);
            mCoreAPI.SetDailySpendLimitSetting(mDailySwitch.isChecked());

            mCoreAPI.SetPINSpendSatoshis(mCoreAPI.denominationToSatoshi(mPINEditText.getText().toString()));
            mCoreAPI.SetPINSpendLimitSetting(mPINSwitch.isChecked());

            mActivity.popFragment();
        } else {
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_spending_limits_incorrect_password),
                    getResources().getString(R.string.fragment_spending_limits_incorrect_password));
        }
    }
}
