package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends Fragment {

    private static final int MAX_TIME_VALUE = 60;

    private RelativeLayout mCategoryContainer;

    private ImageButton mBackButton;
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
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;

    private HighlightOnPressButton mAutoLogoffButton;
    private HighlightOnPressButton mDefaultCurrencyButton;

    private TextView mAccountTitle;

    private HighlightOnPressButton mUSDollarButton;
    private HighlightOnPressButton mCanadianDollarButton;
    private HighlightOnPressButton mEuroButton;
    private HighlightOnPressButton mPesoButton;
    private HighlightOnPressButton mYuanButton;

    private HighlightOnPressButton mLogoutButton;

    private NumberPicker mNumberPicker;
    private NumberPicker mTextPicker;
    private int mNumberSelection;
    private int mTextSelection;
    private int mAutoLogoffMinutes;
    private String[] mAutoLogoffStrings = { "Day(s)", "Hour(s)", "Minute(s)" };

    private String[] mCurrencyItems;
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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mCoreAPI = CoreAPI.getApi();
        mCoreSettings = mCoreAPI.loadAccountSettings();
        mCurrencyItems = mCoreAPI.getCurrencyAcronyms();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_setting, container, false);
        } else {
            ((ViewGroup) mView.getParent()).removeView(mView);
            return mView;
        }

        mBackButton = (ImageButton) mView.findViewById(R.id.settings_button_back);
        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.settings_button_help);
        mTitleTextView = (TextView) mView.findViewById(R.id.settings_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mCategoryContainer = (RelativeLayout) mView.findViewById(R.id.category_container);

        mAccountTitle = (TextView) mView.findViewById(R.id.settings_account_title);

        mDenominationGroup = (RadioGroup) mView.findViewById(R.id.settings_denomination_denomination_group);
        mBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) mView.findViewById(R.id.settings_denomination_buttons_ubitcoin);

        mChangePasswordButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_recovery);

        mSendNameSwitch = (Switch) mView.findViewById(R.id.settings_toggle_send_user_info);
        mFirstEditText = (EditText) mView.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) mView.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) mView.findViewById(R.id.settings_edit_nick_name);
        mAutoLogoffButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_auto_logoff);
        mDefaultCurrencyButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_currency);

        mUSDollarButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_usd);
        mCanadianDollarButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_canadian);
        mEuroButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_euro);
        mPesoButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_peso);
        mYuanButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_yuan);

        mLogoutButton = (HighlightOnPressButton) mView.findViewById(R.id.settings_button_logout);

        mCategoryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new CategoryFragment();
                ((NavigationActivity)getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SETTING.ordinal());
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.SETTINGS), NavigationActivity.Tabs.SETTING.ordinal());
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SignUpFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PASSWORD);
                fragment.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SETTING.ordinal());
            }
        });

        mChangePINButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SignUpFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(SignUpFragment.MODE, SignUpFragment.CHANGE_PIN);
                fragment.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SETTING.ordinal());
            }
        });

        mChangeRecoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new PasswordRecoveryFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PasswordRecoveryFragment.MODE, PasswordRecoveryFragment.CHANGE_QUESTIONS);
                fragment.setArguments(bundle);
                ((NavigationActivity)getActivity()).pushFragment(fragment, NavigationActivity.Tabs.SETTING.ordinal());
            }
        });

        mSendNameSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                setUserNameState(isChecked);
            }
        });

        mAutoLogoffButton.setText("1 Hour");
        mAutoLogoffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAutoLogoffDialog();
            }
        });

        mDefaultCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mDefaultCurrencyButton, mCurrencyItems, "Select an item", mCoreAPI.SettingsCurrencyIndex());
            }
        });

        mUSDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mUSDollarButton, mUSDExchangeItems, "Select an item", 0);
            }
        });

        mCanadianDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mCanadianDollarButton, mCanadianExchangeItems, "Select an item", 0);
            }
        });

        mEuroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mEuroButton, mEuroExchangeItems, "Select an item", 0);
            }
        });

        mPesoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mPesoButton, mPesoExchangeItems, "Select an item", 0);
            }
        });

        mYuanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mYuanButton, mYuanExchangeItems, "Select an item", 0);
            }
        });

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCurrentSettings();
                ((NavigationActivity) getActivity()).Logout();
            }
        });

        mAccountTitle.setText(mAccountTitle.getText().toString()+": "+AirbitzApplication.getUsername());
        try {
            String s = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            TextView debugInfo = (TextView) mView.findViewById(R.id.fragment_settings_debug_info);
            debugInfo.setText(s);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        setUserNameState(mSendNameSwitch.isChecked());

        return mView;
    }

    private void loadSettings(tABC_AccountSettings settings) {
        //Bitcoin denomination
        tABC_BitcoinDenomination denomination = settings.getBitcoinDenomination();
        if(denomination != null) {
            if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_BTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_bitcoin);
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_MBTC) {
                mDenominationGroup.check(R.id.settings_denomination_buttons_mbitcoin);
            } else if(denomination.getDenominationType()==CoreAPI.ABC_DENOMINATION_UBTC) {
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
        mAutoLogoffMinutes = settings.getMinutesAutoLogout();
        int amount;
        String strType;
        if (mAutoLogoffMinutes < MAX_TIME_VALUE) {
            strType = "minute";
            amount = mAutoLogoffMinutes;
        }
        else if (mAutoLogoffMinutes < 24 * MAX_TIME_VALUE) {
            strType = "hour";
            amount = mAutoLogoffMinutes / 60;
        }
        else {
            strType = "day";
            amount = mAutoLogoffMinutes / (24*MAX_TIME_VALUE);
        }

        String timeText = amount + " " + strType;
        if (amount != 1) {
            timeText += "s";
        }
        mAutoLogoffButton.setText(timeText);

        // Default Currency
        mCurrencyNum = mCoreSettings.getCurrencyNum();
        mDefaultCurrencyButton.setText(mCoreAPI.getUserCurrencyAcronym());

        //Default Exchange
        mExchanges = mCoreAPI.getExchangeRateSources(settings.getExchangeRateSources());
        Set<String> exchangeSources = new HashSet<String>();
        for(int i=0; i<mExchanges.size(); i++) {
            exchangeSources.add(mExchanges.get(i).getSource());
        }
        String[] exchanges = exchangeSources.toArray(new String[exchangeSources.size()]);

        mUSDollarButton.setText(exchangeForCurrencyNum(840));
        mUSDExchangeItems = exchanges;
        mCanadianDollarButton.setText(exchangeForCurrencyNum(124));
        mCanadianExchangeItems = exchanges;;
        mEuroButton.setText(exchangeForCurrencyNum(978));
        mEuroExchangeItems = exchanges;;
        mPesoButton.setText(exchangeForCurrencyNum(484));
        mPesoExchangeItems = exchanges;;
        mYuanButton.setText(exchangeForCurrencyNum(156));
        mYuanExchangeItems = exchanges;;
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

    // sets the exchange for the given currency in the settings
    void setExchange(String szSourceSel, int currencyNum) {
            // if there are currently any sources
            if (mExchanges.size() > 0) {
                boolean bReplaced = false;
                // look through all the sources
                for (CoreAPI.ExchangeRateSource source : mExchanges) {
                    if (source.getmCurrencyNum() == currencyNum) {
                        source.setSzSource(szSourceSel);
                        bReplaced = true;
                        break;
                    }
                }
                if (!bReplaced) { // add the source
                    mCoreAPI.addExchangeRateSource(szSourceSel, currencyNum);
                }
            } else {
                // add our first and only source
                mCoreAPI.addExchangeRateSource(szSourceSel, currencyNum);
            }
    }

    private void saveCurrentSettings() {
        //Bitcoin denomination
        tABC_BitcoinDenomination denomination = mCoreSettings.getBitcoinDenomination();
        if(denomination != null) {
            if(mmBitcoinButton.isChecked()) {
                denomination.setDenominationType(CoreAPI.ABC_DENOMINATION_MBTC);
                SWIGTYPE_p_int64_t amt = core.new_int64_tp();
                core.longp_assign(core.p64_t_to_long_ptr(amt), 100000);
                denomination.setSatoshi(amt);
            } else if(muBitcoinButton.isChecked()) {
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
        mCoreSettings.setMinutesAutoLogout(mAutoLogoffMinutes);

        // Default Currency
        mCoreSettings.setCurrencyNum(mCurrencyNum);

        //Default Exchanges TODO
//            mCoreSettings.set(USD_EXCHANGE, mUSDollarButton.getText().toString());

        //Advanced Settings TODO

        mCoreAPI.saveAccountSettings(mCoreSettings);
    }


    private void setUserNameState(boolean on) {
        if(on) {
            mFirstEditText.setEnabled(true);
            mFirstEditText.setTextColor(getResources().getColor(android.R.color.black));
            mFirstEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
            mFirstEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_white));
            mLastEditText.setEnabled(true);
            mLastEditText.setTextColor(getResources().getColor(android.R.color.black));
            mLastEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_white));
            mLastEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
            mNicknameEditText.setEnabled(true);
            mNicknameEditText.setTextColor(getResources().getColor(android.R.color.black));
            mNicknameEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_white));
            mNicknameEditText.setHintTextColor(getResources().getColor(R.color.enabled_hint_color));
        } else {
            mFirstEditText.setEnabled(false);
            mFirstEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mFirstEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_dark));
            mFirstEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
            mLastEditText.setEnabled(false);
            mLastEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mLastEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_dark));
            mLastEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
            mNicknameEditText.setEnabled(false);
            mNicknameEditText.setTextColor(getResources().getColor(R.color.disabled_color));
            mNicknameEditText.setBackground(getResources().getDrawable(R.drawable.emboss_down_dark));
            mNicknameEditText.setHintTextColor(getResources().getColor(R.color.disabled_hint_color));
        }
    }

    private void showAutoLogoffDialog() {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        View blankView = new View(getActivity());
        LinearLayout.LayoutParams bLP = new LinearLayout.LayoutParams((int)getResources().getDimension(R.dimen.two_mm), ViewGroup.LayoutParams.MATCH_PARENT);
        blankView.setLayoutParams(bLP);
        LinearLayout.LayoutParams lLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mNumberPicker = new NumberPicker(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustomLight));
        mTextPicker = new NumberPicker(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustomLight));

        mTextPicker.setMaxValue(2);
        mTextPicker.setMinValue(0);
        mTextPicker.setDisplayedValues( mAutoLogoffStrings);
        mTextPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mNumberPicker.setMaxValue(MAX_TIME_VALUE);
        mNumberPicker.setMinValue(1);

        String[] current = mAutoLogoffButton.getText().toString().split(" ");
        if(current[0]!=null && current[1]!=null) {
            mNumberPicker.setValue(Integer.valueOf(current[0]));
            String temp = current[1];
            for(int i=0; i<mAutoLogoffStrings.length; i++) {
                if(mAutoLogoffStrings[i].contains(temp+"(s)")) {
                    mTextPicker.setValue(i);
                }
            }
        }

        linearLayout.addView(mNumberPicker);
        linearLayout.addView(blankView);
        linearLayout.addView(mTextPicker);

        AlertDialog frag = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom))
                .setTitle(getResources().getString(R.string.dialog_title))
                .setView(linearLayout)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mNumberSelection = mNumberPicker.getValue();
                                mTextSelection = mTextPicker.getValue();
                                if(mTextSelection==0)
                                    mAutoLogoffMinutes = mNumberSelection * 60 * 24;
                                else if (mTextSelection==1)
                                    mAutoLogoffMinutes = mNumberSelection * 60;
                                else if (mTextSelection==2)
                                    mAutoLogoffMinutes = mNumberSelection;

                                mAutoLogoffButton.setText(mNumberSelection + " " +mAutoLogoffStrings[mTextSelection].substring(0, mAutoLogoffStrings[mTextSelection].indexOf('(')));
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
        frag.show();
    }

    AlertDialog mDefaultExchangeDialog;
    private void showSelectorDialog(final Button button, final String[] items, String title, int index) {
        if(mDefaultExchangeDialog!=null && mDefaultExchangeDialog.isShowing())
            return;

        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lLP);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mTextPicker = new NumberPicker(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustomLight));

        mTextPicker.setMaxValue(items.length - 1);
        mTextPicker.setMinValue(0);
        mTextPicker.setValue(index);
        mTextPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        mTextPicker.setDisplayedValues(items);
        mTextPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                if(Arrays.equals(mCurrencyItems, items)) {
                    mCurrencyNum = mCoreAPI.getCurrencyNumbers()[newVal];
                    mCoreAPI.SaveCurrencyNumber(mCurrencyNum);
                    mDefaultCurrencyButton.setText(mCoreAPI.getUserCurrencyAcronym());
                }
            }
        });

        linearLayout.addView(mTextPicker);

        mDefaultExchangeDialog = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom))
                .setTitle(title)
                .setView(linearLayout)
                .setCancelable(false)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                  int num = mTextPicker.getValue();
                                  button.setText(items[num]);
//                                if(mCurrencyItems.equals(items)) {//TODO?
//                                    mCurrencyNum = mCoreAPI.getCurrencyNumbers()[num];
//                                    mCoreAPI.SaveCurrencyNumber(mCurrencyNum);
//                                    mDefaultCurrencyButton.setText(mCoreAPI.getUserCurrencyAcronym());
//                                }
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
        mDefaultExchangeDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSettings(mCoreSettings);
    }

    @Override
    public void onPause() {
        super.onPause();
        saveCurrentSettings();
    }

}
