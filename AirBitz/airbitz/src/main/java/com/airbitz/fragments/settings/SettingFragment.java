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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.SWIGTYPE_p_int64_t;
import com.airbitz.api.core;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.api.tABC_BitcoinDenomination;
import com.airbitz.api.tABC_CC;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.login.SignUpFragment;
import com.airbitz.fragments.settings.twofactor.TwoFactorShowFragment;
import com.airbitz.objects.BleUtil;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends BaseFragment {
    static final int USD_NUM = 840;
    static final int CAD_NUM = 124;
    static final int EUR_NUM = 978;
    static final int PESO_NUM = 484;
    static final int YUAN_NUM = 156;
    static final String[] USD_EXCHANGES = new String[]{"Bitstamp", "BraveNewCoin", "Coinbase"};
    static final String[] OTHER_EXCHANGES = new String[]{"BraveNewCoin", "Coinbase"};
    private static final String MERCHANT_MODE_PREF = "MerchantMode";
    private static final String DISTANCE_PREF = "DistancePref";
    private static final String NFC_PREF = "NFCPref";
    private static final String BLE_PREF = "BLEPref";
    public static final String START_RECOVERY_PASSWORD = "StartRecoveryPassword";
    private final String TAG = getClass().getSimpleName();
    AlertDialog mCurrencyDialog;
    AlertDialog mDefaultExchangeDialog;
    AlertDialog mDistanceDialog;
    private RelativeLayout mCategoryContainer;
    private RelativeLayout mSpendingLimitContainer;
    private RelativeLayout mTwoFactorContainer;
    private View mNFCSwitchLayout;
    private View mBLESwitchLayout;
    private HighlightOnPressImageButton mHelpButton;
    private TextView mTitleTextView;
    private RadioGroup mDenominationGroup;
    private RadioButton mBitcoinButton;
    private RadioButton mmBitcoinButton;
    private RadioButton muBitcoinButton;
    private HighlightOnPressButton mChangePasswordButton;
    private HighlightOnPressButton mChangePINButton;
    private HighlightOnPressButton mChangeRecoveryButton;
    private Switch mSendNameSwitch;
    private Switch mMerchantModeSwitch;
    private Switch mPinReloginSwitch;
    private Switch mNFCSwitch;
    private Switch mBLESwitch;
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;
    private HighlightOnPressButton mAutoLogoffButton;
    private HighlightOnPressButton mDebugButton;
    private HighlightOnPressButton mDefaultCurrencyButton;
    private HighlightOnPressButton mDefaultDistanceButton;
    private TextView mAccountTitle;
    private HighlightOnPressButton mUSDollarButton;
    private HighlightOnPressButton mCanadianDollarButton;
    private HighlightOnPressButton mEuroButton;
    private HighlightOnPressButton mPesoButton;
    private HighlightOnPressButton mYuanButton;
    private HighlightOnPressButton mLogoutButton;
    private AutoLogoffDialogManager mAutoLogoffManager;
    private String[] mCurrencyItems;
    private String[] mDistanceItems;
    private int mCurrencyNum;
    private List<CoreAPI.ExchangeRateSource> mExchanges;
    private String[] mUSDExchangeItems;
    private String[] mCanadianExchangeItems;
    private String[] mEuroExchangeItems;
    private String[] mPesoExchangeItems;
    private String[] mYuanExchangeItems;
    private CoreAPI mCoreAPI;
    private View mView;
    private tABC_AccountSettings mCoreSettings;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mActivity = ((NavigationActivity)getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_setting, container, false);
        }

        mCurrencyItems = mCoreAPI.getCurrencyAcronyms();
        mDistanceItems = getResources().getStringArray(R.array.distance_list);

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.settings_title);

        mAccountTitle = (TextView) mView.findViewById(R.id.settings_account_title);

        mDenominationGroup = (RadioGroup) mView.findViewById(R.id.settings_denomination_denomination_group);
        mBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_ubitcoin);

        mChangePasswordButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_recovery);

        mFirstEditText = (EditText) mView.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) mView.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) mView.findViewById(R.id.settings_edit_nick_name);
        mAutoLogoffButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_auto_logoff);
        mAutoLogoffManager = new AutoLogoffDialogManager(mAutoLogoffButton, getActivity());
        mDefaultCurrencyButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_currency);
        mDefaultDistanceButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_distance);

        mUSDollarButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_usd);
        mCanadianDollarButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_canadian);
        mEuroButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_euro);
        mPesoButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_peso);
        mYuanButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_yuan);

        mLogoutButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentSettings();
                ((NavigationActivity) getActivity()).Logout(false);
            }
        });

        mDebugButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_debug);
        mDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DebugFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mCategoryContainer = (RelativeLayout) mView.findViewById(R.id.category_container);
        mCategoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CategoryFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mSpendingLimitContainer = (RelativeLayout) mView.findViewById(R.id.settings_spending_limits_container);
        mSpendingLimitContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SpendingLimitsFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mTwoFactorContainer = (RelativeLayout) mView.findViewById(R.id.settings_two_factor_container);
        mTwoFactorContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new TwoFactorShowFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).pushFragment(new HelpFragment(HelpFragment.SETTINGS), NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SignUpFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PASSWORD);
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mChangePINButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SignUpFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PIN);
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mChangeRecoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new PasswordRecoveryFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PasswordRecoveryFragment.MODE, PasswordRecoveryFragment.CHANGE_QUESTIONS);
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mSendNameSwitch = (Switch) mView.findViewById(R.id.settings_toggle_send_user_info);
        mSendNameSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                setUserNameState(isChecked);
            }
        });

        mMerchantModeSwitch = (Switch) mView.findViewById(R.id.settings_toggle_merchant_mode);
        mMerchantModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                saveMerchantModePref(isChecked);
            }
        });

        mPinReloginSwitch = (Switch) mView.findViewById(R.id.settings_toggle_pin_login);
        mPinReloginSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                if(isChecked && mCoreSettings.getBDisablePINLogin()) {
                    Log.d(TAG, "Enabling PIN");
                    new mPinSetupTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
                } else if(!isChecked) {
                    Log.d(TAG, "Disabling PIN");
                    mCoreSettings.setBDisablePINLogin(true);
                    mCoreAPI.saveAccountSettings(mCoreSettings);
                    mCoreAPI.PINLoginDelete(AirbitzApplication.getUsername());
                }
            }
        });

        mNFCSwitchLayout = mView.findViewById(R.id.settings_nfc_layout);
        mNFCSwitch = (Switch) mView.findViewById(R.id.settings_toggle_nfc);
        if(isNFCcapable()) {
            mNFCSwitchLayout.setVisibility(View.VISIBLE);
            mNFCSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !NfcAdapter.getDefaultAdapter(getActivity()).isEnabled()) {
                        ShowNFCMessageDialog();
                    }
                }
            });
        }

        mBLESwitchLayout = mView.findViewById(R.id.settings_ble_layout);
        mBLESwitch = (Switch) mView.findViewById(R.id.settings_toggle_ble);
        if(isBLEcapable()) {
            mBLESwitchLayout.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !BleUtil.isBleAdvertiseAvailable(getActivity())) {
                mBLESwitch.setText(getString(R.string.settings_title_ble_send_only));
            }
            mBLESwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        ShowBLEMessageDialog();
                    }
                }
            });
        }

        mAutoLogoffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutoLogoffManager.create();
                mAutoLogoffManager.show();
            }
        });

        mDefaultCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCurrencyDialog(mDefaultCurrencyButton, mCurrencyItems);
            }
        });

        mDefaultDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDistanceDialog(mDefaultDistanceButton, mDistanceItems);
            }
        });

        mUSDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mUSDollarButton, mUSDExchangeItems);
            }
        });

        mCanadianDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mCanadianDollarButton, mCanadianExchangeItems);
            }
        });

        mEuroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mEuroButton, mEuroExchangeItems);
            }
        });

        mPesoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mPesoButton, mPesoExchangeItems);
            }
        });

        mYuanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mYuanButton, mYuanExchangeItems);
            }
        });

        mAccountTitle.setText(getString(R.string.settings_account_title) + ": " + AirbitzApplication.getUsername());

        setUserNameState(mSendNameSwitch.isChecked());

        return mView;
    }

    private void loadSettings(tABC_AccountSettings settings) {
        //Bitcoin denomination
        tABC_BitcoinDenomination denomination = settings.getBitcoinDenomination();
        if (denomination != null) {
            if (denomination.getDenominationType() == CoreAPI.ABC_DENOMINATION_BTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_bitcoin);
            } else if (denomination.getDenominationType() == CoreAPI.ABC_DENOMINATION_MBTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_mbitcoin);
            } else if (denomination.getDenominationType() == CoreAPI.ABC_DENOMINATION_UBTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_ubitcoin);
            }
        }

        //Credentials

        //User Name
        mSendNameSwitch.setChecked(settings.getBNameOnPayments());
        mFirstEditText.setText(settings.getSzFirstName());
        mLastEditText.setText(settings.getSzLastName());
        mNicknameEditText.setText(settings.getSzNickname());


        //Options
        //Autologoff
        mAutoLogoffManager.setMinutes(settings.getMinutesAutoLogout());
        // Pin Relogin
        mPinReloginSwitch.setChecked(!settings.getBDisablePINLogin());
        // NFC
        if(mNFCSwitchLayout.getVisibility() == View.VISIBLE) {
            mNFCSwitch.setChecked(getNFCPref());
        }
        // BLE
        if(mBLESwitchLayout.getVisibility() == View.VISIBLE) {
            mBLESwitch.setChecked(getBLEPref());
        }

        // Default Currency
        mCurrencyNum = mCoreSettings.getCurrencyNum();
        mDefaultCurrencyButton.setText(mCoreAPI.getUserCurrencyAcronym());

        mMerchantModeSwitch.setChecked(getMerchantModePref());

        mDefaultDistanceButton.setText(getResources().getStringArray(R.array.distance_list)[getDistancePref()]);

        //Default Exchange
        mExchanges = mCoreAPI.getExchangeRateSources(settings.getExchangeRateSources());

        mUSDollarButton.setText(exchangeForCurrencyNum(USD_NUM));
        mUSDExchangeItems = USD_EXCHANGES;
        mCanadianDollarButton.setText(exchangeForCurrencyNum(CAD_NUM));
        mCanadianExchangeItems = OTHER_EXCHANGES;
        mEuroButton.setText(exchangeForCurrencyNum(EUR_NUM));
        mEuroExchangeItems = OTHER_EXCHANGES;
        mPesoButton.setText(exchangeForCurrencyNum(PESO_NUM));
        mPesoExchangeItems = OTHER_EXCHANGES;
        mYuanButton.setText(exchangeForCurrencyNum(YUAN_NUM));
        mYuanExchangeItems = OTHER_EXCHANGES;
    }

    // searches the exchanges in the settings for the exchange associated with the given currency number
    // NULL is returned if none can be found
    String exchangeForCurrencyNum(int currencyNum) {
        String szRetVal = "";
        // look through all the sources
        for (CoreAPI.ExchangeRateSource source : mExchanges) {
            if (source.getmCurrencyNum() == currencyNum) {
                szRetVal = source.getSource();
                break;
            }
        }
        return szRetVal;
    }

    private void saveCurrentSettings() {
        mCoreSettings = mCoreAPI.newCoreSettings();
        if(mCoreSettings == null) {
            return;
        }
        //Bitcoin denomination
        tABC_BitcoinDenomination denomination = mCoreSettings.getBitcoinDenomination();
        if (denomination != null) {
            if (mmBitcoinButton.isChecked()) {
                denomination.setDenominationType(CoreAPI.ABC_DENOMINATION_MBTC);
                SWIGTYPE_p_int64_t amt = core.new_int64_tp();
                core.longp_assign(core.p64_t_to_long_ptr(amt), 100000);
                denomination.setSatoshi(amt);
            } else if (muBitcoinButton.isChecked()) {
                denomination.setDenominationType(CoreAPI.ABC_DENOMINATION_UBTC);
                SWIGTYPE_p_int64_t amt = core.new_int64_tp();
                core.longp_assign(core.p64_t_to_long_ptr(amt), 100);
                denomination.setSatoshi(amt);
            } else {
                denomination.setDenominationType(CoreAPI.ABC_DENOMINATION_BTC);
                SWIGTYPE_p_int64_t amt = core.new_int64_tp();
                core.longp_assign(core.p64_t_to_long_ptr(amt), 100000000);
                denomination.setSatoshi(amt);
            }
        }

        //Credentials - N/A

        //User Name
        mCoreSettings.setBNameOnPayments(mSendNameSwitch.isChecked());
        mCoreSettings.setSzFirstName(mFirstEditText.getText().toString());
        mCoreSettings.setSzLastName(mLastEditText.getText().toString());
        mCoreSettings.setSzNickname(mNicknameEditText.getText().toString());

        //Options
        //Autologoff
        mCoreSettings.setMinutesAutoLogout(mAutoLogoffManager.getMinutes());

        //PinRelogin is saved during click

        //NFC
        saveNFCPref(mNFCSwitch.isChecked());

        //BLE
        saveBLEPref(mBLESwitch.isChecked());

        // Default Currency
        mCoreSettings.setCurrencyNum(mCurrencyNum);

        mExchanges = mCoreAPI.getExchangeRateSources(
                        mCoreSettings.getExchangeRateSources());

        // Default Exchanges
        for (CoreAPI.ExchangeRateSource source : mExchanges) {
            switch (source.getCurrencyNum()) {
                case USD_NUM:
                    source.setSzSource(mUSDollarButton.getText().toString());
                    break;
                case CAD_NUM:
                    source.setSzSource(mCanadianDollarButton.getText().toString());
                    break;
                case EUR_NUM:
                    source.setSzSource(mEuroButton.getText().toString());
                    break;
                case PESO_NUM:
                    source.setSzSource(mPesoButton.getText().toString());
                    break;
                case YUAN_NUM:
                    source.setSzSource(mYuanButton.getText().toString());
                    break;
            }
        }

        if (AirbitzApplication.isLoggedIn()) {
            mCoreAPI.saveAccountSettings(mCoreSettings);
        }
    }

    static public boolean getMerchantModePref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(MERCHANT_MODE_PREF, false);
    }

    private void saveMerchantModePref(boolean state) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(MERCHANT_MODE_PREF, state);
        editor.apply();
    }

    private void setUserNameState(boolean on) {
        if (on) {
            mFirstEditText.setEnabled(true);
            mFirstEditText.setTextColor(getResources().getColor(R.color.settings_edit_text_field));
            mFirstEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
            mLastEditText.setEnabled(true);
            mLastEditText.setTextColor(getResources().getColor(R.color.settings_edit_text_field));
            mLastEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
            mNicknameEditText.setEnabled(true);
            mNicknameEditText.setTextColor(getResources().getColor(R.color.settings_edit_text_field));
            mNicknameEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
        } else {
            mFirstEditText.setEnabled(false);
            mFirstEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mFirstEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
            mLastEditText.setEnabled(false);
            mLastEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mLastEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
            mNicknameEditText.setEnabled(false);
            mNicknameEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mNicknameEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
        }
    }

    private AlertDialog.Builder defaultDialogLayout(final String[] items, int index) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lLP =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        NumberPicker mTextPicker =
                new NumberPicker(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustomLight));
        mTextPicker.setMaxValue(items.length - 1);
        mTextPicker.setId(R.id.dialog_number_picker);
        mTextPicker.setMinValue(0);
        mTextPicker.setValue(index);
        mTextPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mTextPicker.setDisplayedValues(items);
        linearLayout.addView(mTextPicker);

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom))
                .setTitle(R.string.dialog_select_an_item)
                .setView(linearLayout)
                .setNegativeButton(R.string.string_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                );
    }

    private void saveDistancePref(int selection) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putInt(DISTANCE_PREF, selection);
        editor.apply();
    }

    static public int getDistancePref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(DISTANCE_PREF, 0); // default to Automatic
    }

    private void showDistanceDialog(final Button button, final String[] items) {
        if (mDistanceDialog != null && mDistanceDialog.isShowing()) {
            return;
        }
        mDistanceDialog = defaultDialogLayout(items, getDistancePref())
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                NumberPicker picker = (NumberPicker) mDistanceDialog.findViewById(R.id.dialog_number_picker);
                                int index = picker.getValue();
                                saveDistancePref(index);
                                mDefaultDistanceButton.setText(mDistanceItems[index]);
                                saveCurrentSettings();
                            }
                        }
                )
                .create();
        mDistanceDialog.show();
    }

    private void showCurrencyDialog(final Button button, final String[] items) {
        if (mCurrencyDialog != null && mCurrencyDialog.isShowing()) {
            return;
        }
        mCurrencyDialog = defaultDialogLayout(items, mCoreAPI.SettingsCurrencyIndex())
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                NumberPicker picker = (NumberPicker) mCurrencyDialog.findViewById(R.id.dialog_number_picker);
                                mCurrencyNum = mCoreAPI.getCurrencyNumbers()[picker.getValue()];
                                mDefaultCurrencyButton.setText(mCoreAPI.getCurrencyAcronym(mCurrencyNum));
                                saveCurrentSettings();
                            }
                        }
                )
                .create();
        mCurrencyDialog.show();
    }

    private void showExchangeDialog(final Button button, final String[] items) {
        if (mDefaultExchangeDialog != null && mDefaultExchangeDialog.isShowing()) {
            return;
        }
        int index = findInArray(button, items);
        mDefaultExchangeDialog = defaultDialogLayout(items, index)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                NumberPicker picker = (NumberPicker) mDefaultExchangeDialog.findViewById(R.id.dialog_number_picker);
                                int num = picker.getValue();
                                button.setText(items[num]);
                                saveCurrentSettings();
                                mCoreAPI.updateExchangeRates();
                            }
                        }
                )
                .create();
        mDefaultExchangeDialog.show();
    }

    private int findInArray(Button val, String[] arr) {
        return Math.max(0, Arrays.asList(arr).indexOf(val.getText().toString()));
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getBoolean(START_RECOVERY_PASSWORD)) {
            mChangeRecoveryButton.performClick();
            bundle.putBoolean(START_RECOVERY_PASSWORD, false);
        }

        mCoreSettings = mCoreAPI.newCoreSettings();
        loadSettings(mCoreSettings);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (AirbitzApplication.isLoggedIn()) {
            saveCurrentSettings();
        }
    }

    static class AutoLogoffDialogManager {
        static final int MAX_TIME_VALUE = 60;
        List<String> mAutoLogoffStrings = new ArrayList<String>();

        private int mNumberSelection;
        private int mTextSelection;
        private AlertDialog mDialog;
        private Button mButton;
        private Activity mActivity;
        private int mMinutes;
        private NumberPicker mNumberPicker;
        private NumberPicker mTextPicker;

        AutoLogoffDialogManager(Button button, Activity activity) {
            this.mButton = button;
            this.mActivity = activity;
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_days));
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_hours));
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_minutes));
        }

        private void setButtonText() {
            String strType = mAutoLogoffStrings.get(mTextSelection);
            String timeText = mNumberSelection + " " + strType;
            if (mNumberSelection == 1) {
                timeText = timeText.replaceAll("\\(s\\)", "");
            } else {
                timeText = timeText.replaceAll("\\(|\\)", "");
            }
            mButton.setText(timeText);
        }

        public int getMinutes() {
            return mMinutes;
        }

        public void setMinutes(int minutes) {
            this.mMinutes = minutes;
            if (mMinutes < MAX_TIME_VALUE) {
                mNumberSelection = mMinutes;
                mTextSelection = 2;
            } else if (mMinutes < 24 * MAX_TIME_VALUE) {
                mNumberSelection = mMinutes / 60;
                mTextSelection = 1;
            } else {
                mNumberSelection = mMinutes / (24 * MAX_TIME_VALUE);
                mTextSelection = 0;
            }
            setButtonText();
        }

        private void createNumberPicker() {
            mNumberPicker = new NumberPicker(
                    new ContextThemeWrapper(mActivity, R.style.AlertDialogCustomLight));
            mNumberPicker.setMaxValue(MAX_TIME_VALUE);
            mNumberPicker.setMinValue(1);
            mNumberPicker.setValue(mNumberSelection);
        }

        private void createTextPicker() {
            mTextPicker = new NumberPicker(
                    new ContextThemeWrapper(mActivity, R.style.AlertDialogCustomLight));

            mTextPicker.setMaxValue(2);
            mTextPicker.setMinValue(0);
            mTextPicker.setDisplayedValues(mAutoLogoffStrings.toArray(new String[mAutoLogoffStrings.size()]));
            mTextPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            mTextPicker.setValue(mTextSelection);
        }

        public void create() {
            if (mDialog != null) {
                mTextPicker.setValue(mTextSelection);
                mNumberPicker.setValue(mNumberSelection);
                return;
            }
            LinearLayout linearLayout = new LinearLayout(mActivity);
            View blankView = new View(mActivity);

            LinearLayout.LayoutParams bLP =
                    new LinearLayout.LayoutParams(
                            (int) mActivity.getResources().getDimension(R.dimen.two_mm),
                            ViewGroup.LayoutParams.MATCH_PARENT);
            blankView.setLayoutParams(bLP);

            LinearLayout.LayoutParams lLP =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.setLayoutParams(lLP);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            createNumberPicker();
            createTextPicker();

            linearLayout.addView(mNumberPicker);
            linearLayout.addView(blankView);
            linearLayout.addView(mTextPicker);

            mDialog = new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom))
                    .setTitle(mActivity.getResources().getString(R.string.dialog_title))
                    .setView(linearLayout)
                    .setPositiveButton(R.string.string_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mNumberSelection = mNumberPicker.getValue();
                                    mTextSelection = mTextPicker.getValue();
                                    if (mTextSelection == 0) {
                                        mMinutes = mNumberSelection * 60 * 24;
                                    } else if (mTextSelection == 1) {
                                        mMinutes = mNumberSelection * 60;
                                    } else if (mTextSelection == 2) {
                                        mMinutes = mNumberSelection;
                                    }
                                    setButtonText();
                                }
                            }
                    )
                    .setNegativeButton(R.string.string_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }
                    )
                    .create();
        }

        public void show() {
            mDialog.show();
        }
    }

    /**
     * Save Questions if Signing up or Changing questions
     */
    public class mPinSetupTask extends AsyncTask<Void, Void, Void> {

        mPinSetupTask() { }

        @Override
        public void onPreExecute() {
            mActivity.showModalProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mCoreAPI.PinSetupBlocking();
            mCoreSettings.setBDisablePINLogin(false);
            mCoreAPI.saveAccountSettings(mCoreSettings);
            return null;
        }

        @Override
        protected void onPostExecute(final Void success) {
            onCancelled();
        }

        @Override
        protected void onCancelled() {
            mActivity.showModalProgress(false);
        }
    }


    private boolean isNFCcapable() {
        final NfcManager nfcManager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        return nfcManager.getDefaultAdapter() != null;
    }

    private void saveNFCPref(boolean state) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(NFC_PREF, state);
        editor.apply();
    }

    static public boolean getNFCPref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        boolean nfc;
        try {
            nfc = prefs.getBoolean(NFC_PREF, true);
            return nfc;
        }
        catch (ClassCastException e) {
            SharedPreferences.Editor editor = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
            editor.putBoolean(NFC_PREF, true);
            editor.apply();
            return true;
        }
    }

    public void ShowNFCMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.settings_nfc_turn_on_message))
                .setTitle(getString(R.string.settings_nfc_turn_on_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (SettingFragment.this.isAdded()) {
                                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                                }
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.string_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mNFCSwitch.setChecked(false);
                                dialog.cancel();
                            }
                        });
        builder.create().show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isBLEcapable() {
        return BleUtil.isBleAvailable(getActivity());
    }

    private void saveBLEPref(boolean state) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(BLE_PREF, state);
        editor.apply();
    }

    static public boolean getBLEPref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(BLE_PREF, true);
    }

    public void ShowBLEMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.settings_ble_turn_on_message))
                .setTitle(getString(R.string.settings_ble_turn_on_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (SettingFragment.this.isAdded()) {
                                    startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                                }
                                dialog.cancel();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.string_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mBLESwitch.setChecked(false);
                                dialog.cancel();
                            }
                        });
        builder.create().show();
    }
}
