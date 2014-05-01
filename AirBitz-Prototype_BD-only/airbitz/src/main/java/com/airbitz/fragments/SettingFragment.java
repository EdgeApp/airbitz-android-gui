package com.airbitz.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.airbitz.R;
import com.airbitz.activities.DisplayActivity;
import com.airbitz.activities.LandingActivity;
import com.airbitz.activities.SecurityActivity;
import com.airbitz.utils.Common;

/**
 * Created on 2/12/14.
 */
public class SettingFragment extends Fragment {

    private ImageButton mBackButton;
    private ImageButton mHelpButton;
    private TextView mTitleTextView;

    private Button mBitcoinButton;
    private Button mmBitcoinButton;
    private Button muBitcoinButton;

    private Button mChangePasswordButton;
    private Button mChangePINButton;
    private Button mChangeRecoveryButton;

    private ToggleButton mSendNameToggle;
    private EditText mFirstEditText;
    private EditText mLastEditText;
    private EditText mNicknameEditText;

    private Spinner mAutoLogoffSpinner;
    private Spinner mLanguageSpinner;
    private Spinner mCurrencySpinner;

    private Spinner mUSDollarSpinner;
    private Spinner mCanadianDollarSpinner;
    private Spinner mEuroSpinner;
    private Spinner mPesoSpinner;
    private Spinner mYuanSpinner;


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

        mBitcoinButton = (Button) view.findViewById(R.id.settings_denomination_buttons_bitcoin);
        mmBitcoinButton = (Button) view.findViewById(R.id.settings_denomination_buttons_mbitcoin);
        muBitcoinButton = (Button) view.findViewById(R.id.settings_denomination_buttons_ubitcoin);

        mChangePasswordButton = (Button) view.findViewById(R.id.settings_button_change_password);
        mChangePINButton = (Button) view.findViewById(R.id.settings_button_pin);
        mChangeRecoveryButton = (Button) view.findViewById(R.id.settings_button_recovery);

        mSendNameToggle = (ToggleButton) view.findViewById(R.id.settings_toggle_send_user_info);
        mFirstEditText = (EditText) view.findViewById(R.id.settings_edit_first_name);
        mLastEditText = (EditText) view.findViewById(R.id.settings_edit_last_name);
        mNicknameEditText = (EditText) view.findViewById(R.id.settings_edit_nick_name);

        mAutoLogoffSpinner = (Spinner) view.findViewById(R.id.settings_spinner_auto_logoff);
        mLanguageSpinner = (Spinner) view.findViewById(R.id.settings_spinner_language);
        mCurrencySpinner = (Spinner) view.findViewById(R.id.settings_spinner_currency);

        mUSDollarSpinner = (Spinner) view.findViewById(R.id.settings_spinner_usd);
        mCanadianDollarSpinner = (Spinner) view.findViewById(R.id.settings_spinner_canadian);
        mEuroSpinner = (Spinner) view.findViewById(R.id.settings_spinner_euro);
        mPesoSpinner = (Spinner) view.findViewById(R.id.settings_spinner_peso);
        mYuanSpinner = (Spinner) view.findViewById(R.id.settings_spinner_yuan);

//        mDisplayButton = (Button) view.findViewById(R.id.button_display);
//        mCategoriesButton.setTypeface(LandingActivity.latoBlackTypeFace);
//
//        mLanguageTextView.setTypeface(LandingActivity.latoBlackTypeFace, Typeface.ITALIC);
//
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

        mBitcoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        mmBitcoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        muBitcoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        //TODO setup spinners
        String[] auto_logoff_items = getResources().getStringArray(R.array.auto_logoff_array);
        ArrayAdapter<String> auto_logoff_adapter = new ArrayAdapter<String>(getActivity(),  R.layout.item_setting_spinner, auto_logoff_items);
        mAutoLogoffSpinner.setAdapter(auto_logoff_adapter);

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
