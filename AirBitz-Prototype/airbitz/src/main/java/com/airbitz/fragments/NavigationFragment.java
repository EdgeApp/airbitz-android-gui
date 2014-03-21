package com.airbitz.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.airbitz.R;
import com.airbitz.activities.BusinessDirectoryActivity;
import com.airbitz.activities.DirectoryDetailActivity;
import com.airbitz.activities.DisplayActivity;
import com.airbitz.activities.ExportActivity;
import com.airbitz.activities.ExportSavingOptionActivity;
import com.airbitz.activities.ImportActivity;
import com.airbitz.activities.MapBusinessDirectoryActivity;
import com.airbitz.activities.OfflineWalletActivity;
import com.airbitz.activities.ReceivedSuccessActivity;
import com.airbitz.activities.RequestActivity;
import com.airbitz.activities.SecurityActivity;
import com.airbitz.activities.SendActivity;
import com.airbitz.activities.SendConfirmationActivity;
import com.airbitz.activities.SettingActivity;
import com.airbitz.activities.TransactionActivity;
import com.airbitz.activities.TransactionDetailActivity;
import com.airbitz.activities.WalletActivity;
import com.airbitz.activities.WalletPasswordActivity;

/**
 * Created on 2/18/14.
 */
public class NavigationFragment extends Fragment {

    private ImageButton mDirectoryButton;
    private ImageButton mSendButton;
    private ImageButton mRequestButton;
    private ImageButton mWalletButton;
    private ImageButton mSettingButton;
    private ImageButton mPopupDirectoryButton;
    private ImageButton mPopupSendButton;
    private ImageButton mPopupRequestButton;
    private ImageButton mPopupWalletButton;
    private ImageButton mPopupSettingButton;
    private static ImageButton mCurrentActivityButton;
    private View mView;
    private Intent mIntent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_navigation, container, false);

        mDirectoryButton = (ImageButton) mView.findViewById(R.id.directory_button);
        mSendButton = (ImageButton) mView.findViewById(R.id.send_button);
        mRequestButton = (ImageButton) mView.findViewById(R.id.request_button);
        mWalletButton = (ImageButton) mView.findViewById(R.id.wallet_button);
        mSettingButton = (ImageButton) mView.findViewById(R.id.setting_button);
        mPopupDirectoryButton = (ImageButton) mView.findViewById(R.id.popup_directory_button);
        mPopupSendButton = (ImageButton) mView.findViewById(R.id.popup_send_button);
        mPopupRequestButton = (ImageButton) mView.findViewById(R.id.popup_request_button);
        mPopupWalletButton = (ImageButton) mView.findViewById(R.id.popup_wallet_button);
        mPopupSettingButton = (ImageButton) mView.findViewById(R.id.popup_setting_button);

        mDirectoryButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPopupDirectoryButton.setVisibility(View.VISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(BusinessDirectoryActivity.class.toString())) {
                            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory_white);
                            mDirectoryButton.setBackgroundResource(R.drawable.bg_selected_tab);
                            mSendButton.setEnabled(false);
                            mRequestButton.setEnabled(false);
                            mWalletButton.setEnabled(false);
                            mSettingButton.setEnabled(false);

                            unFocusOtherButton();

                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                        mPopupDirectoryButton.setVisibility(View.INVISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(BusinessDirectoryActivity.class.toString())) {
                            mIntent = new Intent(getActivity(), BusinessDirectoryActivity.class);
                            startActivity(mIntent);
                            getActivity().finish();
                            mSendButton.setEnabled(true);
                            mRequestButton.setEnabled(true);
                            mWalletButton.setEnabled(true);
                            mSettingButton.setEnabled(true);

                            return true;
                        } else {
                            return false;
                        }
                }
                return false;
            }
        });


        mSendButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPopupSendButton.setVisibility(View.VISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(SendActivity.class.toString())) {
                            unFocusOtherButton();
                            mSendButton.setImageResource(R.drawable.ico_tab_send_white);
                            mSendButton.setBackgroundResource(R.drawable.bg_selected_tab);
                            mDirectoryButton.setEnabled(false);
                            mRequestButton.setEnabled(false);
                            mWalletButton.setEnabled(false);
                            mSettingButton.setEnabled(false);
                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                        mPopupSendButton.setVisibility(View.INVISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(SendActivity.class.toString())) {
                            mIntent = new Intent(getActivity(), SendActivity.class);
                            startActivity(mIntent);
                            getActivity().finish();
                            mDirectoryButton.setEnabled(true);
                            mRequestButton.setEnabled(true);
                            mWalletButton.setEnabled(true);
                            mSettingButton.setEnabled(true);
                            return true;
                        } else {
                            return false;
                        }
                }
                return false;
            }
        });


        mRequestButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPopupRequestButton.setVisibility(View.VISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(RequestActivity.class.toString())) {
                            mRequestButton.setImageResource(R.drawable.ico_tab_request_white);
                            mRequestButton.setBackgroundResource(R.drawable.bg_selected_tab);
                            unFocusOtherButton();
                            mDirectoryButton.setEnabled(false);
                            mSendButton.setEnabled(false);
                            mWalletButton.setEnabled(false);
                            mSettingButton.setEnabled(false);
                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                        mPopupRequestButton.setVisibility(View.INVISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(RequestActivity.class.toString())) {
                            mIntent = new Intent(getActivity(), RequestActivity.class);
                            startActivity(mIntent);
                            getActivity().finish();
                            mDirectoryButton.setEnabled(true);
                            mSendButton.setEnabled(true);
                            mWalletButton.setEnabled(true);
                            mSettingButton.setEnabled(true);
                            return true;
                        } else {
                            return false;
                        }
                }
                return false;
            }
        });


        mWalletButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPopupWalletButton.setVisibility(View.VISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(WalletActivity.class.toString())) {
                            unFocusOtherButton();
                            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
                            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
                            mDirectoryButton.setEnabled(false);
                            mRequestButton.setEnabled(false);
                            mSendButton.setEnabled(false);
                            mSettingButton.setEnabled(false);
                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                        mPopupWalletButton.setVisibility(View.INVISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(WalletActivity.class.toString())) {
                            mIntent = new Intent(getActivity(), WalletActivity.class);
                            startActivity(mIntent);
                            getActivity().finish();
                            mDirectoryButton.setEnabled(true);
                            mRequestButton.setEnabled(true);
                            mSendButton.setEnabled(true);
                            mSettingButton.setEnabled(true);
                            return true;
                        } else {
                            return false;
                        }
                }
                return false;
            }
        });


        mSettingButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPopupSettingButton.setVisibility(View.VISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(SettingActivity.class.toString())) {
                            unFocusOtherButton();
                            mSettingButton.setImageResource(R.drawable.ico_tab_settings_white);
                            mSettingButton.setBackgroundResource(R.drawable.bg_selected_tab);
                            mDirectoryButton.setEnabled(false);
                            mRequestButton.setEnabled(false);
                            mSendButton.setEnabled(false);
                            mWalletButton.setEnabled(false);
                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                        mPopupDirectoryButton.setVisibility(View.INVISIBLE);
                        if (!getActivity().getClass().toString().equalsIgnoreCase(SettingActivity.class.toString())) {
                            mIntent = new Intent(getActivity(), SettingActivity.class);
                            startActivity(mIntent);
                            getActivity().finish();
                            mDirectoryButton.setEnabled(true);
                            mRequestButton.setEnabled(true);
                            mSendButton.setEnabled(true);
                            mSettingButton.setEnabled(true);
                            return true;
                        } else {
                            return false;
                        }
                }
                return false;
            }
        });
        return mView;

    }

    public void setActivityRelatedButtonToPresseState() {

        if (getActivity().getClass().toString().equalsIgnoreCase(BusinessDirectoryActivity.class.toString())) {
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory_white);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mDirectoryButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(MapBusinessDirectoryActivity.class.toString())) {
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory_white);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mDirectoryButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(DirectoryDetailActivity.class.toString())) {
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory_white);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mDirectoryButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(RequestActivity.class.toString())) {

            mRequestButton.setImageResource(R.drawable.ico_tab_request_white);
            mRequestButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mRequestButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(ImportActivity.class.toString())) {

            mRequestButton.setImageResource(R.drawable.ico_tab_request_white);
            mRequestButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mRequestButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(WalletPasswordActivity.class.toString())) {

            mRequestButton.setImageResource(R.drawable.ico_tab_request_white);
            mRequestButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mRequestButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(SendActivity.class.toString())) {

            mSendButton.setImageResource(R.drawable.ico_tab_send_white);
            mSendButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSendButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(SendConfirmationActivity.class.toString())) {

            mSendButton.setImageResource(R.drawable.ico_tab_send_white);
            mSendButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSendButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(ReceivedSuccessActivity.class.toString())) {

            mSendButton.setImageResource(R.drawable.ico_tab_send_white);
            mSendButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSendButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(WalletActivity.class.toString())) {

            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(SettingActivity.class.toString())) {

            mSettingButton.setImageResource(R.drawable.ico_tab_settings_white);
            mSettingButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSettingButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(DisplayActivity.class.toString())) {

            mSettingButton.setImageResource(R.drawable.ico_tab_settings_white);
            mSettingButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSettingButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(SecurityActivity.class.toString())) {

            mSettingButton.setImageResource(R.drawable.ico_tab_settings_white);
            mSettingButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mSettingButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(TransactionActivity.class.toString())) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(TransactionDetailActivity.class.toString())) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(ExportActivity.class.toString())) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(ExportSavingOptionActivity.class.toString())) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        } else if (getActivity().getClass().toString().equalsIgnoreCase(OfflineWalletActivity.class.toString())) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet_white);
            mWalletButton.setBackgroundResource(R.drawable.bg_selected_tab);
            mCurrentActivityButton = mWalletButton;
        }
    }

    public void unFocusOtherButton() {
        if (mCurrentActivityButton == mDirectoryButton) {
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mCurrentActivityButton == mSendButton) {
            mSendButton.setImageResource(R.drawable.ico_tab_send);
            mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mCurrentActivityButton == mRequestButton) {
            mRequestButton.setImageResource(R.drawable.ico_tab_request);
            mRequestButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mCurrentActivityButton == mWalletButton) {
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
            mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mCurrentActivityButton == mSettingButton) {
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
        }
    }

    public void setOtherButtonToUnclickState() {

        if (mDirectoryButton.isFocused()) {
            mSendButton.setImageResource(R.drawable.ico_tab_send);
            mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mRequestButton.setImageResource(R.drawable.ico_tab_request);
            mRequestButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
            mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mRequestButton.isFocused()) {

            mSendButton.setImageResource(R.drawable.ico_tab_send);
            mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
            mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (getActivity().getClass().toString().equalsIgnoreCase(SendActivity.class.toString())) {
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
            mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mWalletButton.isFocused()) {

            mSendButton.setImageResource(R.drawable.ico_tab_send);
            mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mSettingButton.setImageResource(R.drawable.ico_tab_settings);
            mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mRequestButton.setImageResource(R.drawable.ico_tab_request);
            mRequestButton.setBackgroundResource(R.drawable.bg_tab_bar);
        } else if (mSettingButton.isFocused()) {

            mSendButton.setImageResource(R.drawable.ico_tab_send);
            mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
            mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
            mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
            mRequestButton.setImageResource(R.drawable.ico_tab_request);
            mRequestButton.setBackgroundResource(R.drawable.bg_tab_bar);
        }
    }

    public void initializeElements() {

        mDirectoryButton.setImageResource(R.drawable.ico_tab_directory);
        mDirectoryButton.setBackgroundResource(R.drawable.bg_tab_bar);
        mDirectoryButton.invalidate();
        mSendButton.setImageResource(R.drawable.ico_tab_send);
        mSendButton.setBackgroundResource(R.drawable.bg_tab_bar);
        mSendButton.invalidate();
        mRequestButton.setImageResource(R.drawable.ico_tab_request);
        mRequestButton.setBackgroundResource(R.drawable.bg_tab_bar);
        mRequestButton.invalidate();
        mWalletButton.setImageResource(R.drawable.ico_tab_wallet);
        mWalletButton.setBackgroundResource(R.drawable.bg_tab_bar);
        mWalletButton.invalidate();
        mSettingButton.setImageResource(R.drawable.ico_tab_settings);
        mSettingButton.setBackgroundResource(R.drawable.bg_tab_bar);
        mSettingButton.invalidate();

        mPopupDirectoryButton.setVisibility(View.INVISIBLE);
        mPopupSendButton.setVisibility(View.INVISIBLE);
        mPopupRequestButton.setVisibility(View.INVISIBLE);
        mPopupWalletButton.setVisibility(View.INVISIBLE);
        mPopupSettingButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeElements();

        setActivityRelatedButtonToPresseState();
    }
}
