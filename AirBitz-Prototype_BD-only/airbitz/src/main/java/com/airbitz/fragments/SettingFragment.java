package com.airbitz.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.LandingActivity;
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

    private Switch mSendNameToggle;
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;

    private Button mAutoLogoffButton;
    private Spinner mLanguageSpinner;
    private Spinner mCurrencySpinner;

    private Spinner mUSDollarSpinner;
    private Spinner mCanadianDollarSpinner;
    private Spinner mEuroSpinner;
    private Spinner mPesoSpinner;
    private Spinner mYuanSpinner;

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
        mTitleTextView.setTypeface(LandingActivity.montserratBoldTypeFace);

        mBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (RadioButton) view.findViewById(R.id.settings_denomination_buttons_ubitcoin);
        mBitcoinButton.setChecked(true);

        mChangePasswordButton = (Button) view.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (Button) view.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (Button) view.findViewById(R.id.settings_button_recovery);

        mSendNameToggle = (Switch) view.findViewById(R.id.settings_toggle_send_user_info);
        mFirstEditText = (EditText) view.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) view.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) view.findViewById(R.id.settings_edit_nick_name);

        mAutoLogoffButton = (Button) view.findViewById(R.id.settings_button_auto_logoff);
        mLanguageSpinner = (Spinner) view.findViewById(R.id.settings_spinner_language);
        mCurrencySpinner = (Spinner) view.findViewById(R.id.settings_spinner_currency);

        mUSDollarSpinner = (Spinner) view.findViewById(R.id.settings_spinner_usd);
        mCanadianDollarSpinner = (Spinner) view.findViewById(R.id.settings_spinner_canadian);
        mEuroSpinner = (Spinner) view.findViewById(R.id.settings_spinner_euro);
        mPesoSpinner = (Spinner) view.findViewById(R.id.settings_spinner_peso);
        mYuanSpinner = (Spinner) view.findViewById(R.id.settings_spinner_yuan);

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

        mSendNameToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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


        String[] language_items = getResources().getStringArray(R.array.language_array);
        ArrayAdapter<String> language_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, language_items);
        mLanguageSpinner.setAdapter(language_adapter);

        String[] currency_items = getResources().getStringArray(R.array.default_currency_array);
        ArrayAdapter<String> currency_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, currency_items);
        mCurrencySpinner.setAdapter(currency_adapter);

        String[] usd_items = getResources().getStringArray(R.array.usd_exchange_array);
        ArrayAdapter<String> usd_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, usd_items);
        mUSDollarSpinner.setAdapter(usd_adapter);

        String[] canadian_items = getResources().getStringArray(R.array.canadian_exchange_array);
        ArrayAdapter<String> canadian_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, canadian_items);
        mCanadianDollarSpinner.setAdapter(canadian_adapter);

        String[] euro_items = getResources().getStringArray(R.array.euro_exchange_array);
        ArrayAdapter<String> euro_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, euro_items);
        mEuroSpinner.setAdapter(euro_adapter);

        String[] peso_items = getResources().getStringArray(R.array.peso_exchange_array);
        ArrayAdapter<String> peso_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, peso_items);
        mPesoSpinner.setAdapter(peso_adapter);

        String[] yuan_items = getResources().getStringArray(R.array.yuan_exchange_array);
        ArrayAdapter<String> yuan_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, yuan_items);
        mYuanSpinner.setAdapter(yuan_adapter);

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
