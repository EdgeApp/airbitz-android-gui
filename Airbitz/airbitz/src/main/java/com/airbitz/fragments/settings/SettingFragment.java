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
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import static android.provider.Settings.ACTION_BLUETOOTH_SETTINGS;

import co.airbitz.bitbeacon.BleUtil;
import co.airbitz.core.Account;
import co.airbitz.core.AirbitzCore;
import co.airbitz.core.AirbitzException;
import co.airbitz.core.BitcoinDenomination;
import co.airbitz.core.CoreCurrency;
import co.airbitz.core.Settings;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreWrapper;
import com.airbitz.api.DirectoryWrapper;
import com.airbitz.api.directory.DirectoryApi;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.login.SignUpFragment;
import com.airbitz.fragments.settings.CurrencyFragment.OnCurrencySelectedListener;
import com.airbitz.fragments.settings.twofactor.TwoFactorShowFragment;
import com.airbitz.objects.PinChangeTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

public class SettingFragment extends BaseFragment implements CurrencyFragment.OnCurrencySelectedListener {
    private static final String MERCHANT_MODE_PREF = "MerchantMode";
    private static final String DISTANCE_PREF = "DistancePref";
    private static final String NFC_PREF = "NFCPref";
    private static final String BLE_PREF = "BLEPref";
    public static final String START_RECOVERY_PASSWORD = "StartRecoveryPassword";
    public static final String START_CHANGE_PASSWORD = "com.airbitz.fragments.settingfragment.StartChangePassword";
    public static final String START_CHANGE_PIN = "com.airbitz.fragments.settingfragment.StartChangePin";
    private final String TAG = getClass().getSimpleName();
    Dialog mDefaultExchangeDialog;
    Dialog mDistanceDialog;
    private Button mCategoryContainer;
    private Button mSpendingLimitContainer;
    private Button mTwoFactorContainer;
    private RadioGroup mDenominationGroup;
    private RadioButton mBitcoinButton;
    private RadioButton mmBitcoinButton;
    private RadioButton muBitcoinButton;
    private Button mChangePasswordButton;
    private Button mChangePINButton;
    private Button mChangeRecoveryButton;
    private Switch mSendNameSwitch;
    private Switch mMerchantModeSwitch;
    private Switch mPinReloginSwitch;
    private Switch mNFCSwitch;
    private Switch mBLESwitch;
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;
    private Button mAutoLogoffButton;
    private Button mDebugButton;
    private Button mDefaultCurrencyButton;
    private Button mDefaultDistanceButton;
    private TextView mAccountTitle;
    private Button mExchangeButton;
    private AutoLogoffDialogManager mAutoLogoffManager;
    private List<String> mDistanceItems;
    private String mCurrencyCode;
    private List<String> mExchanges;
    private AirbitzCore mCoreAPI;
    private Account mAccount;
    private View mView;
    private Settings mCoreSettings;
    private NavigationActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCoreAPI = AirbitzCore.getApi();
        mAccount = AirbitzApplication.getAccount();
        mActivity = ((NavigationActivity)getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    protected String getTitle() {
        return mActivity.getString(R.string.settings_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            LayoutInflater i = getThemedInflater(inflater, R.style.AppTheme_Blue);
            mView = i.inflate(R.layout.fragment_setting, container, false);
        }

        mDistanceItems = Arrays.asList(getResources().getStringArray(R.array.distance_list));
        mAccountTitle = (TextView) mView.findViewById(R.id.settings_account_title);

        mBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_ubitcoin);
        mDenominationGroup = (RadioGroup) mView.findViewById(R.id.settings_denomination_denomination_group);
        mDenominationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveDenomination();
            }
        });

        mChangePasswordButton = (Button) mView.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (Button) mView.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (Button) mView.findViewById(R.id.settings_button_recovery);

        mFirstEditText = (EditText) mView.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) mView.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) mView.findViewById(R.id.settings_edit_nick_name);
        mAutoLogoffButton = (Button) mView.findViewById(R.id.settings_button_auto_logoff);
        mAutoLogoffManager = new AutoLogoffDialogManager(mAutoLogoffButton, getActivity());
        mDefaultCurrencyButton = (Button) mView.findViewById(R.id.settings_button_currency);
        mDefaultDistanceButton = (Button) mView.findViewById(R.id.settings_button_distance);

        mExchangeButton = (Button) mView.findViewById(R.id.settings_button_default_exchange);

        mDebugButton = (Button) mView.findViewById(R.id.settings_button_debug);
        mDebugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new DebugFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mCategoryContainer = (Button) mView.findViewById(R.id.settings_button_category);
        mCategoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CategoryFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mSpendingLimitContainer = (Button) mView.findViewById(R.id.settings_button_spending_limits);
        mSpendingLimitContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SpendingLimitsFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mTwoFactorContainer = (Button) mView.findViewById(R.id.settings_button_two_factor_authentication);
        mTwoFactorContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new TwoFactorShowFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
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
                bundle.putInt(PasswordRecoveryFragment.TYPE, PasswordRecoveryFragment.RECOVERY_TYPE_2);
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
                boolean alreadySet = mAccount.hasPinLogin();
                if (!alreadySet && isChecked) {
                    new PinChangeTask(mActivity, mAccount, true).execute();
                } else if (alreadySet && !isChecked) {
                    new PinChangeTask(mActivity, mAccount, false).execute();
                }
            }
        });

        mNFCSwitch = (Switch) mView.findViewById(R.id.settings_toggle_nfc);
        if(isNFCcapable()) {
            mNFCSwitch.setVisibility(View.VISIBLE);
            mNFCSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !NfcAdapter.getDefaultAdapter(getActivity()).isEnabled()) {
                        ShowNFCMessageDialog();
                    }
                    saveNFCPref(isChecked);
                }
            });
        }

        mBLESwitch = (Switch) mView.findViewById(R.id.settings_toggle_ble);
        if(isBLEcapable()) {
            mBLESwitch.setVisibility(View.VISIBLE);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !BleUtil.isBleAdvertiseAvailable(getActivity())) {
                mBLESwitch.setText(getString(R.string.settings_title_ble_send_only));
            }
            mBLESwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        ShowBLEMessageDialog();
                    }
                    saveBLEPref(isChecked);
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

        Settings settings = mAccount.settings();
        mCurrencyCode = settings.currency().code;

        mDefaultCurrencyButton.setText(mCurrencyCode);
        mDefaultCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrencyFragment fragment = new CurrencyFragment();
                fragment.setSelected(mCurrencyCode);
                fragment.setOnCurrencySelectedListener(SettingFragment.this);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.MORE.ordinal());
            }
        });

        mDefaultDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDistanceDialog(mDefaultDistanceButton, mDistanceItems);
            }
        });

        mExchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExchangeDialog(mExchangeButton, mExchanges);
            }
        });

        mAccountTitle.setText(getString(R.string.settings_account_title) + ": " + AirbitzApplication.getUsername());

        setUserNameState(mSendNameSwitch.isChecked());

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
        case R.id.action_help:
            ((NavigationActivity) getActivity()).pushFragment(
                new HelpFragment(HelpFragment.SETTINGS), NavigationActivity.Tabs.MORE.ordinal());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadSettings(Settings settings) {
        //Bitcoin denomination
        BitcoinDenomination denomination = settings.bitcoinDenomination();
        if (denomination != null) {
            if (denomination.type() == BitcoinDenomination.BTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_bitcoin);
            } else if (denomination.type() == BitcoinDenomination.MBTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_mbitcoin);
            } else if (denomination.type() == BitcoinDenomination.UBTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_ubitcoin);
            }
        }

        //User Name
        mSendNameSwitch.setChecked(settings.nameOnPayments());
        mFirstEditText.setText(settings.firstName());
        mLastEditText.setText(settings.lastName());
        mNicknameEditText.setText(settings.nickName());


        //Options
        //Autologoff
        mAutoLogoffManager.setSeconds(settings.secondsAutoLogout());
        // Pin Relogin
        if (mAccount.hasPinLogin()) {
            mPinReloginSwitch.setChecked(true);
        } else {
            mPinReloginSwitch.setChecked(false);
        }
        // NFC
        if (mNFCSwitch.getVisibility() == View.VISIBLE) {
            mNFCSwitch.setChecked(getNFCPref());
        }
        // BLE
        if (mBLESwitch.getVisibility() == View.VISIBLE) {
            mBLESwitch.setChecked(getBLEPref());
        }

        mMerchantModeSwitch.setChecked(getMerchantModePref());

        mDefaultDistanceButton.setText(getResources().getStringArray(R.array.distance_list)[getDistancePref()]);

        //Default Exchange
        mExchanges = mCoreAPI.exchangeCache().exchangeRateSources();
        mExchangeButton.setText(mCoreSettings.exchangeRateSource());
    }

    private void saveDenomination() {
        if (mCoreSettings == null) {
            mCoreSettings = mAccount.settings();
        }
        //Bitcoin denomination
        BitcoinDenomination denomination = mCoreSettings.bitcoinDenomination();
        if (denomination != null) {
            if (mmBitcoinButton.isChecked()) {
                denomination.setDenominationType(BitcoinDenomination.MBTC);
            } else if (muBitcoinButton.isChecked()) {
                denomination.setDenominationType(BitcoinDenomination.UBTC);
            } else {
                denomination.setDenominationType(BitcoinDenomination.BTC);
            }
        }
    }

    private void saveCurrentSettings() {
        mCoreSettings = mAccount.settings();
        if (mCoreSettings == null) {
            return;
        }

        saveDenomination();

        //Credentials - N/A

        //User Name
        mCoreSettings.showNameOnPayments(mSendNameSwitch.isChecked());
        mCoreSettings.firstName(mFirstEditText.getText().toString());
        mCoreSettings.lastName(mLastEditText.getText().toString());
        mCoreSettings.nickName(mNicknameEditText.getText().toString());

        //Options
        //Autologoff
        mCoreSettings.secondsAutoLogout(mAutoLogoffManager.getSeconds());

        //PinRelogin is saved during click

        //NFC
        saveNFCPref(mNFCSwitch.isChecked());

        //BLE
        saveBLEPref(mBLESwitch.isChecked());

        // Default Currency
        mCoreSettings.currency(mCurrencyCode);
        mCoreSettings.exchangeRateSource(mExchangeButton.getText().toString());

        if (AirbitzApplication.isLoggedIn()) {
            try {
                mCoreSettings.save();
            } catch (AirbitzException e) {
                AirbitzCore.logi("SettingFragment saveCurrentSettings error");
            }
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

    private AlertDialogWrapper.Builder defaultDialogLayout(final List<String> items, int index) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lLP =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        NumberPicker mTextPicker =
                new NumberPicker(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustomLight));
        mTextPicker.setMaxValue(items.size() - 1);
        mTextPicker.setId(R.id.dialog_number_picker);
        mTextPicker.setMinValue(0);
        mTextPicker.setValue(index);
        mTextPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mTextPicker.setDisplayedValues(items.toArray(new String[items.size()]));
        linearLayout.addView(mTextPicker);

        return new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom))
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

    private void showDistanceDialog(final Button button, final List<String> items) {
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
                                mDefaultDistanceButton.setText(mDistanceItems.get(index));
                                saveCurrentSettings();
                            }
                        }
                )
                .create();
        mDistanceDialog.show();
    }

    private void showExchangeDialog(final Button button, final List<String> items) {
        if (mDefaultExchangeDialog != null && mDefaultExchangeDialog.isShowing()) {
            return;
        }
        int index = findInArray(button, items.toArray(new String[items.size()]));
        mDefaultExchangeDialog = defaultDialogLayout(items, index)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                NumberPicker picker = (NumberPicker) mDefaultExchangeDialog.findViewById(R.id.dialog_number_picker);
                                int num = picker.getValue();
                                button.setText(items.get(num));
                                saveCurrentSettings();
                                mAccount.updateExchangeRates();
                            }
                        }
                )
                .create();
        mDefaultExchangeDialog.show();
    }

    private int findInArray(Button button, String[] arr) {
        for(int i = 0; i<arr.length; i++) {
            if(button.getText().toString().equals(arr[i].substring(0,3))) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getBoolean(START_RECOVERY_PASSWORD)) {
            mChangeRecoveryButton.performClick();
            bundle.putBoolean(START_RECOVERY_PASSWORD, false);
        }

        if (bundle != null && bundle.getBoolean(START_CHANGE_PASSWORD)) {
            mChangePasswordButton.performClick();
            bundle.putBoolean(START_CHANGE_PASSWORD, false);
        }

        if (bundle != null && bundle.getBoolean(START_CHANGE_PIN)) {
            mChangePINButton.performClick();
            bundle.putBoolean(START_CHANGE_PIN, false);
        }

        mPinReloginSwitch.setEnabled(mAccount.passwordExists());

        mCoreSettings = mAccount.settings();
        loadSettings(mCoreSettings);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (AirbitzApplication.isLoggedIn()) {
            saveCurrentSettings();
        }
    }

    @Override
    public void onCurrencySelected(CoreCurrency currency) {
        mCurrencyCode = currency.code;
        saveCurrentSettings();
        mDefaultCurrencyButton.setText(mCurrencyCode);
        mActivity.ShowFadingDialog(
            getString(R.string.settings_currency_change_note_popup),
            getResources().getInteger(R.integer.alert_hold_time_help_popups));
    }

    static class AutoLogoffDialogManager {
        static final int MAX_TIME_VALUE = 60;
        List<String> mAutoLogoffStrings = new ArrayList<String>();

        private int mNumberSelection;
        private int mTextSelection;
        private Dialog mDialog;
        private Button mButton;
        private Activity mActivity;
        private int mSeconds;
        private NumberPicker mNumberPicker;
        private NumberPicker mTextPicker;

        AutoLogoffDialogManager(Button button, Activity activity) {
            this.mButton = button;
            this.mActivity = activity;
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_days));
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_hours));
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_minutes));
            mAutoLogoffStrings.add(mActivity.getString(R.string.settings_seconds));
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

        public int getSeconds() {
            return mSeconds;
        }

        public void setSeconds(int seconds) {
            this.mSeconds = seconds;
            if (mSeconds < 60) {
                mNumberSelection = mSeconds;
                mTextSelection = 3;
            } else if (mSeconds < 60 * 60) {
                mNumberSelection = mSeconds / 60;
                mTextSelection = 2;
            } else if (mSeconds < 60 * 60 * 24) {
                mNumberSelection = mSeconds / 60 / 60;
                mTextSelection = 1;
            } else {
                mNumberSelection = mSeconds / 60 / 60 / 24;
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

            mTextPicker.setMaxValue(3);
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

            mDialog = new AlertDialogWrapper.Builder(new ContextThemeWrapper(mActivity, R.style.AlertDialogCustom))
                    .setTitle(mActivity.getResources().getString(R.string.dialog_title))
                    .setView(linearLayout)
                    .setPositiveButton(R.string.string_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mNumberSelection = mNumberPicker.getValue();
                                    mTextSelection = mTextPicker.getValue();
                                    if (mTextSelection == 0) {
                                        mSeconds = mNumberSelection * 60 * 60 * 24;
                                    } else if (mTextSelection == 1) {
                                        mSeconds = mNumberSelection * 60 * 60;
                                    } else if (mTextSelection == 2) {
                                        mSeconds = mNumberSelection * 60;
                                    } else if (mTextSelection == 3) {
                                        mSeconds = mNumberSelection;
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

    private boolean isNFCcapable() {
        final NfcManager nfcManager = (NfcManager) getActivity().getSystemService(Context.NFC_SERVICE);
        return nfcManager.getDefaultAdapter() != null;
    }

    private void saveNFCPref(boolean state) {
        SharedPreferences.Editor editor = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
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
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
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
        SharedPreferences.Editor editor = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(BLE_PREF, state);
        editor.apply();
    }

    static public boolean getBLEPref() {
        SharedPreferences prefs = AirbitzApplication.getContext().getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(BLE_PREF, true);
    }

    public void ShowBLEMessageDialog() {
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setMessage(getString(R.string.settings_ble_turn_on_message))
                .setTitle(getString(R.string.settings_ble_turn_on_title))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (SettingFragment.this.isAdded()) {
                                    startActivity(new Intent(ACTION_BLUETOOTH_SETTINGS));
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
