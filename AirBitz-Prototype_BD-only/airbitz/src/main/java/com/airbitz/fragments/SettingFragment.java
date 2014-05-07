package com.airbitz.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends Fragment {

    public static final String SETTINGS_NAME = "Settings";
    public static final String DENOMINATION = "Denomination";
    public static final String NAME_SWITCH = "SendName";
    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";
    public static final String NICK_NAME = "Nickname";
    public static final String AUTO_LOGOFF = "Logoff";
    public static final String LANGUAGE = "Language";
    public static final String CURRENCY = "Currency";
    public static final String USD_EXCHANGE = "USD_EXCHANGE";
    public static final String CANADIAN_EXCHANGE = "CANADIAN_EXCHANGE";
    public static final String EURO_EXCHANGE = "EURO_EXCHANGE";
    public static final String PESO_EXCHANGE = "PESO_EXCHANGE";
    public static final String YUAN_EXCHANGE = "YUAN_EXCHANGE";

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private TextView mTitleTextView;

    private RadioGroup mDenominationGroup;
    private RadioButton mBitcoinButton;
    private RadioButton mmBitcoinButton;
    private RadioButton muBitcoinButton;

    private Button mChangePasswordButton;
    private Button mChangePINButton;
    private Button mChangeRecoveryButton;

    private Switch mSendNameSwitch;
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;

    private Button mAutoLogoffButton;
    private Button mLanguageButton;
    private Button mCurrencyButton;

    private Button mUSDollarButton;
    private Button mCanadianDollarButton;
    private Button mEuroButton;
    private Button mPesoButton;
    private Button mYuanButton;

    private NumberPicker mNumberPicker;
    private NumberPicker mTextPicker;
    private int mNumberSelection;
    private int mTextSelection;
    private String[] mAutoLogoffStrings = { "Day", "Hour", "Minute" };

    private String[] mLanguageItems;
    private String[] mCurrencyItems;
    private String[] mUSDExchangeItems;
    private String[] mCanadianExchangeItems;
    private String[] mEuroExchangeItems;
    private String[] mPesoExchangeItems;
    private String[] mYuanExchangeItems;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLanguageItems = getResources().getStringArray(R.array.language_array);
        mCurrencyItems = getResources().getStringArray(R.array.default_currency_array);
        mUSDExchangeItems = getResources().getStringArray(R.array.usd_exchange_array);
        mCanadianExchangeItems = getResources().getStringArray(R.array.canadian_exchange_array);
        mEuroExchangeItems = getResources().getStringArray(R.array.euro_exchange_array);
        mPesoExchangeItems = getResources().getStringArray(R.array.peso_exchange_array);
        mYuanExchangeItems = getResources().getStringArray(R.array.yuan_exchange_array);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mBackButton = (ImageButton) view.findViewById(R.id.settings_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.settings_button_help);
        mTitleTextView = (TextView) view.findViewById(R.id.settings_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mDenominationGroup = (RadioGroup) view.findViewById(R.id.settings_denomination_denomination_group);
        mBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_ubitcoin);

        mChangePasswordButton = (Button) view.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (Button) view.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (Button) view.findViewById(R.id.settings_button_recovery);

        mSendNameSwitch = (Switch) view.findViewById(R.id.settings_toggle_send_user_info);
        mFirstEditText = (EditText) view.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) view.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) view.findViewById(R.id.settings_edit_nick_name);

        mAutoLogoffButton = (Button) view.findViewById(R.id.settings_button_auto_logoff);
        mLanguageButton = (Button) view.findViewById(R.id.settings_button_language);
        mCurrencyButton = (Button) view.findViewById(R.id.settings_button_currency);

        mUSDollarButton = (Button) view.findViewById(R.id.settings_button_usd);
        mCanadianDollarButton = (Button) view.findViewById(R.id.settings_button_canadian);
        mEuroButton = (Button) view.findViewById(R.id.settings_button_euro);
        mPesoButton = (Button) view.findViewById(R.id.settings_button_peso);
        mYuanButton = (Button) view.findViewById(R.id.settings_button_yuan);

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Business directory info");
            }
        });

        mChangePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mChangePINButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mChangeRecoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        mLanguageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mLanguageButton, mLanguageItems, "Select an item");
            }
        });

        mCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mCurrencyButton, mCurrencyItems, "Select an item");
            }
        });

        mUSDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mUSDollarButton, mUSDExchangeItems, "Select an item");
            }
        });

        mCanadianDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mCanadianDollarButton, mCanadianExchangeItems, "Select an item");
            }
        });

        mEuroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mEuroButton, mEuroExchangeItems, "Select an item");
            }
        });

        mPesoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mPesoButton, mPesoExchangeItems, "Select an item");
            }
        });

        mYuanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectorDialog(mYuanButton, mYuanExchangeItems, "Select an item");
            }
        });

        //TODO populate from PREFS

        return view;
    }

    private void setUserNameState(boolean on) {
        if(on) {
            mFirstEditText.setEnabled(true);
            mFirstEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent_30_padded);
            mLastEditText.setEnabled(true);
            mLastEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent_30_padded);
            mNicknameEditText.setEnabled(true);
            mNicknameEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent_30_padded);
        } else {
            mFirstEditText.setEnabled(false);
            mFirstEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent);
            mLastEditText.setEnabled(false);
            mLastEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent);
            mNicknameEditText.setEnabled(false);
            mNicknameEditText.setBackgroundResource(R.drawable.rounded_edge_black_transparent);
        }
    }

    private void showAutoLogoffDialog() {
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View npView = inflater.inflate(R.layout.dialog_auto_logoff, null);
        mNumberPicker = (NumberPicker) npView.findViewById(R.id.dialog_auto_logout_number_picker);
        mTextPicker = (NumberPicker) npView.findViewById(R.id.dialog_auto_logout_text_picker);

        mTextPicker.setMaxValue(2);
        mTextPicker.setMinValue(0);
        mTextPicker.setDisplayedValues( mAutoLogoffStrings);
        mNumberPicker.setMaxValue(60);
        mNumberPicker.setMinValue(0);

        String[] current = mAutoLogoffButton.getText().toString().split(" ");
        if(current[0]!=null && current[1]!=null) {
            mNumberPicker.setValue(Integer.valueOf(current[0]));
            String temp = current[1];
            for(int i=0; i<mAutoLogoffStrings.length; i++) {
                if(mAutoLogoffStrings[i].contains(temp)) {
                    mTextPicker.setValue(i);
                }
            }
        }

        AlertDialog frag = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.dialog_title))
                .setView(npView)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mNumberSelection = mNumberPicker.getValue();
                                mTextSelection = mTextPicker.getValue();
                                mAutoLogoffButton.setText(mNumberSelection + " " +mAutoLogoffStrings[Integer.valueOf(mTextSelection)]);
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

    private void showSelectorDialog(final Button button, final String[] items, String title) {
        LayoutInflater inflater = (LayoutInflater)
                getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View npView = inflater.inflate(R.layout.dialog_text_selector, null);
        mTextPicker = (NumberPicker) npView.findViewById(R.id.dialog_text_picker);

        mTextPicker.setMaxValue(items.length - 1);
        mTextPicker.setMinValue(0);
        mTextPicker.setDisplayedValues(items);

        AlertDialog frag = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(npView)
                .setPositiveButton(R.string.string_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                button.setText(items[Integer.valueOf(mTextPicker.getValue())]);
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

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();
    }

    private void loadPreferences() {
        Activity activity = getActivity();
        if(activity!=null) {
            SharedPreferences pref = activity.getSharedPreferences(SETTINGS_NAME, Activity.MODE_PRIVATE);
            int selected = pref.getInt(DENOMINATION, 0);
            switch(selected) {
                case 0:
                    mBitcoinButton.setChecked(true);
                    break;
                case 1:
                    mmBitcoinButton.setChecked(true);
                    break;
                case 2:
                    muBitcoinButton.setChecked(true);
                    break;
                default:
                    break;
            }
            mSendNameSwitch.setChecked(pref.getBoolean(NAME_SWITCH, false));
            mFirstEditText.setText(pref.getString(FIRST_NAME, ""));
            mLastEditText.setText(pref.getString(LAST_NAME, ""));
            mNicknameEditText.setText(pref.getString(NICK_NAME, ""));
            mAutoLogoffButton.setText(pref.getString(AUTO_LOGOFF, "1 Hour"));
            mLanguageButton.setText(pref.getString(LANGUAGE, mLanguageItems[0]));
            mCurrencyButton.setText(pref.getString(CURRENCY, mCurrencyItems[0]));
            mUSDollarButton.setText(pref.getString(USD_EXCHANGE, mUSDExchangeItems[0]));
            mCanadianDollarButton.setText(pref.getString(CANADIAN_EXCHANGE, mCanadianExchangeItems[0]));
            mEuroButton.setText(pref.getString(EURO_EXCHANGE, mEuroExchangeItems[0]));
            mPesoButton.setText(pref.getString(PESO_EXCHANGE, mPesoExchangeItems[0]));
            mYuanButton.setText(pref.getString(YUAN_EXCHANGE, mYuanExchangeItems[0]));
        }
    }

    private void savePreferences() {
        Activity activity = getActivity();
        if(activity!=null) {
            SharedPreferences pref = activity.getSharedPreferences(SETTINGS_NAME, Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            int selected = 0;
            if(mmBitcoinButton.isChecked()) selected=1;
            else if(muBitcoinButton.isChecked()) selected=2;
            editor.putInt(DENOMINATION, selected);
            editor.putBoolean(NAME_SWITCH, mSendNameSwitch.isChecked());
            editor.putString(FIRST_NAME, mFirstEditText.getText().toString());
            editor.putString(LAST_NAME, mLastEditText.getText().toString());
            editor.putString(NICK_NAME, mNicknameEditText.getText().toString());
            editor.putString(AUTO_LOGOFF, mAutoLogoffButton.getText().toString());
            editor.putString(LANGUAGE, mLanguageButton.getText().toString());
            editor.putString(CURRENCY, mCurrencyButton.getText().toString());
            editor.putString(USD_EXCHANGE, mUSDollarButton.getText().toString());
            editor.putString(CANADIAN_EXCHANGE, mCanadianDollarButton.getText().toString());
            editor.putString(EURO_EXCHANGE, mEuroButton.getText().toString());
            editor.putString(PESO_EXCHANGE, mPesoButton.getText().toString());
            editor.putString(YUAN_EXCHANGE, mYuanButton.getText().toString());
            editor.commit();
        }
    }


}
