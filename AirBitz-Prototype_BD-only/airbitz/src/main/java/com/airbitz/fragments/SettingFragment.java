package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends Fragment {

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private TextView mTitleTextView;

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


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        mBackButton = (ImageButton) view.findViewById(R.id.settings_button_back);
        mHelpButton = (ImageButton) view.findViewById(R.id.settings_button_help);
        mTitleTextView = (TextView) view.findViewById(R.id.settings_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

        mBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_ubitcoin);
        mBitcoinButton.setChecked(true);

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

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                finish();
            }
        });
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
                String[] language_items = getResources().getStringArray(R.array.language_array);
                showSelectorDialog(mLanguageButton, language_items, "Select an item");
            }
        });

        mCurrencyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] currency_items = getResources().getStringArray(R.array.default_currency_array);
                showSelectorDialog(mCurrencyButton, currency_items, "Select an item");
            }
        });

        mUSDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] usd_items = getResources().getStringArray(R.array.usd_exchange_array);
                showSelectorDialog(mUSDollarButton, usd_items, "Select an item");
            }
        });

        mCanadianDollarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] canadian_items = getResources().getStringArray(R.array.canadian_exchange_array);
                showSelectorDialog(mCanadianDollarButton, canadian_items, "Select an item");
            }
        });

        mEuroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] euro_items = getResources().getStringArray(R.array.euro_exchange_array);
                showSelectorDialog(mEuroButton, euro_items, "Select an item");
            }
        });

        mPesoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] peso_items = getResources().getStringArray(R.array.peso_exchange_array);
                showSelectorDialog(mPesoButton, peso_items, "Select an item");
            }
        });

        mYuanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] yuan_items = getResources().getStringArray(R.array.yuan_exchange_array);
                showSelectorDialog(mYuanButton, yuan_items, "Select an item");
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
        //TODO load state here
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
