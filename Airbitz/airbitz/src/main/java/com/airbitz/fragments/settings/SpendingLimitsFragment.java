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
import android.os.Handler;
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

import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.Utils;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreWrapper;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.PasswordCheckRunnable;
import com.airbitz.objects.UserReview;

public class SpendingLimitsFragment extends BaseFragment
    implements PasswordCheckRunnable.OnPasswordCheckListener {
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
    private AirbitzCore mCoreAPI;
    private Account mAccount;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccount = AirbitzApplication.getAccount();
        mCoreAPI = AirbitzCore.getApi();

        setHasOptionsMenu(true);
        setDrawerEnabled(false);
        setBackEnabled(true);
    }

    @Override
    public String getTitle() {
        return mActivity.getString(R.string.fragment_spending_limits_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
        View mView = i.inflate(R.layout.fragment_spending_limits, container, false);

        mDailySwitch = (Switch) mView.findViewById(R.id.fragment_spending_limits_toggle_daily_limit);
        mDailySwitch.setTypeface(Typeface.DEFAULT);
        mDailyEditText = (EditText) mView.findViewById(R.id.fragment_spending_limits_daily_edittext);
        mDailyEditText.setTypeface(Typeface.DEFAULT);
        mDailyDenominationTextView = (TextView) mView.findViewById(R.id.fragment_spending_limits_daily_denomination);
        mDailyDenominationTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
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
        mPINDenominationTextView.setTypeface(NavigationActivity.latoRegularTypeFace);
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
        if (mDailySwitch.isChecked()) {
            mDailyDenominationTextView.setEnabled(true);
            mDailyEditText.setEnabled(true);
        } else {
            mDailyDenominationTextView.setEnabled(false);
            mDailyEditText.setEnabled(false);
        }
        if (mPINSwitch.isChecked()) {
            mPINDenominationTextView.setEnabled(true);
            mPINEditText.setEnabled(true);
        } else {
            mPINDenominationTextView.setEnabled(false);
            mPINEditText.setEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mAccount.passwordExists()) {
            mPasswordEditText.setVisibility(View.GONE);
        }

        mDailyEditText.setText(Utils.formatSatoshi(mAccount, CoreWrapper.getDailySpendLimit(mActivity, mAccount), false));
        mDailySwitch.setChecked(CoreWrapper.getDailySpendLimitSetting(mActivity, mAccount));
        mDailyDenominationTextView.setText(CoreWrapper.userBtcSymbol(mAccount));

        mPINSwitch.setChecked(CoreWrapper.getPinSpendLimitSetting(mAccount));
        mPINEditText.setText(Utils.formatSatoshi(mAccount, CoreWrapper.getPinSpendLimit(mAccount), false));
        mPINDenominationTextView.setText(CoreWrapper.userBtcSymbol(mAccount));
        adjustTextColors();

        if (!mAccount.settings().dailySpendLimit()) {
            mActivity.ShowFadingDialog(getString(R.string.fragment_spending_limits_warning), 10000);
        }
    }


    private void goSave() {
        mActivity.showModalProgress(true);
        mHandler.post(
            new PasswordCheckRunnable(mAccount, mPasswordEditText.getText().toString(), this));
    }

    @Override
    public void onPasswordCheck(boolean passwordOkay) {
        mActivity.showModalProgress(false);
        if(passwordOkay) {
            long satoshis = Utils.btcStringToSatoshi(mAccount, mDailyEditText.getText().toString());
            CoreWrapper.setDailySpendSatoshis(mActivity, mAccount, satoshis);
            CoreWrapper.setDailySpendLimitSetting(mActivity, mAccount, mDailySwitch.isChecked());
            CoreWrapper.setPinSpendSatoshis(mActivity, mAccount, Utils.btcStringToSatoshi(mAccount, mPINEditText.getText().toString()));
            CoreWrapper.setPinSpendLimitSetting(mActivity, mAccount, mPINSwitch.isChecked());
            UserReview.passwordUsed();

            mActivity.popFragment();
        } else {
            mActivity.ShowOkMessageDialog(getResources().getString(R.string.fragment_spending_limits_incorrect_password),
                    getResources().getString(R.string.fragment_spending_limits_incorrect_password));
        }
    }
}
