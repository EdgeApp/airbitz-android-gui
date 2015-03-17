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

package com.airbitz.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;

import java.util.List;

/**
 * Created by Thomas Baker on 4/22/14.
 */
public class NavigationBarFragment extends BaseFragment {

    private RelativeLayout mDirectoryButton;
    private ImageView mDirectoryImage;
    private TextView mDirectoryText;
    private RelativeLayout mSendButton;
    private ImageView mSendImage;
    private TextView mSendText;
    private RelativeLayout mRequestButton;
    private ImageView mRequestImage;
    private TextView mRequestText;
    private RelativeLayout mWalletButton;
    private ImageView mWalletImage;
    private TextView mWalletText;
    private RelativeLayout mSettingButton;
    private ImageView mSettingImage;
    private TextView mSettingText;


    private RelativeLayout mPopupDirectoryButton;
    private RelativeLayout mPopupSendButton;
    private RelativeLayout mPopupRequestButton;
    private RelativeLayout mPopupWalletButton;
    private RelativeLayout mPopupSettingButton;

    private TextView mPopupDirectoryText;
    private TextView mPopupSendText;
    private TextView mPopupRequestText;
    private TextView mPopupWalletText;
    private TextView mPopupSettingText;

    private View mView, mButtons;
    private int selectedTab = 0;
    private int mLastTab = 0;

    private NavigationActivity mActivity;
    private CoreAPI mCoreAPI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (NavigationActivity) getActivity();
        mCoreAPI = CoreAPI.getApi();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView != null)
            return mView;

        mView = inflater.inflate(R.layout.fragment_navigation_bar, container, false);

        mButtons = mView.findViewById(R.id.normal_navigation_bar);

        mDirectoryButton = (RelativeLayout) mView.findViewById(R.id.directory_button);
        mDirectoryImage = (ImageView) mView.findViewById(R.id.nav_bar_directory_image);
        mDirectoryText = (TextView) mView.findViewById(R.id.nav_bar_directory_text);

        mRequestButton = (RelativeLayout) mView.findViewById(R.id.request_button);
        mRequestImage = (ImageView) mView.findViewById(R.id.nav_bar_request_image);
        mRequestText = (TextView) mView.findViewById(R.id.nav_bar_request_text);

        mSendButton = (RelativeLayout) mView.findViewById(R.id.send_button);
        mSendImage = (ImageView) mView.findViewById(R.id.nav_bar_send_image);
        mSendText = (TextView) mView.findViewById(R.id.nav_bar_send_text);

        mWalletButton = (RelativeLayout) mView.findViewById(R.id.wallet_button);
        mWalletImage = (ImageView) mView.findViewById(R.id.nav_bar_wallets_image);
        mWalletText = (TextView) mView.findViewById(R.id.nav_bar_wallets_text);

        mSettingButton = (RelativeLayout) mView.findViewById(R.id.setting_button);
        mSettingImage = (ImageView) mView.findViewById(R.id.nav_bar_settings_image);
        mSettingText = (TextView) mView.findViewById(R.id.nav_bar_settings_text);

        mPopupDirectoryButton = (RelativeLayout) mView.findViewById(R.id.popup_directory_button);
        mPopupDirectoryText = (TextView) mView.findViewById(R.id.popup_directory_text);

        mPopupSendButton = (RelativeLayout) mView.findViewById(R.id.popup_send_button);
        mPopupSendText = (TextView) mView.findViewById(R.id.popup_send_text);

        mPopupRequestButton = (RelativeLayout) mView.findViewById(R.id.popup_request_button);
        mPopupRequestText = (TextView) mView.findViewById(R.id.popup_request_text);

        mPopupWalletButton = (RelativeLayout) mView.findViewById(R.id.popup_wallet_button);
        mPopupWalletText = (TextView) mView.findViewById(R.id.popup_wallet_text);

        mPopupSettingButton = (RelativeLayout) mView.findViewById(R.id.popup_setting_button);
        mPopupSettingText = (TextView) mView.findViewById(R.id.popup_setting_text);

        mButtons.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() > 1)
                    return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        checkTabs(event);
                        return true;
                    case MotionEvent.ACTION_UP:
                        clearPopups();
                        return true;
                    default:
                        return false;
                }
            }
        });

        return mView;
    }

    private void checkTabs(MotionEvent ev) {
        selectedTab = getTabNum(ev);
        if (selectedTab == -1)
            return;

        // ignore Send and Request until wallets are loaded
        if((selectedTab == NavigationActivity.Tabs.REQUEST.ordinal() ||
                selectedTab == NavigationActivity.Tabs.SEND.ordinal()) &&
                AirbitzApplication.isLoggedIn() && mCoreAPI.walletsStillLoading()) {
            selectedTab = NavigationActivity.Tabs.WALLET.ordinal();
            mActivity.ShowFadingDialog(getString(R.string.wait_until_wallets_loaded));
            return;
        }
        else if(selectedTab == mLastTab) {
            displayPopup(ev);
        }
        else if(selectedTab != NavigationActivity.Tabs.MORE.ordinal()) {
            displayPopup(ev);
            selectTab(selectedTab);
            unselectTab(mLastTab);
            mLastTab = selectedTab;
        }

        if (mActivity != null)
            mActivity.onNavBarSelected(selectedTab);
    }

    private int getTabNum(MotionEvent ev) {
        if (isOverView(ev.getRawX(), ev.getRawY(), mDirectoryButton)) {
            return 0;
        } else if (isOverView(ev.getRawX(), ev.getRawY(), mRequestButton)) {
            return 1;
        } else if (isOverView(ev.getRawX(), ev.getRawY(), mSendButton)) {
            return 2;
        } else if (isOverView(ev.getRawX(), ev.getRawY(), mWalletButton)) {
            return 3;
        } else if (isOverView(ev.getRawX(), ev.getRawY(), mSettingButton)) {
            return 4;
        } else return -1;
    }

    public void selectTab(int position) {
        selectedTab = position;
        switch (position) {
            case 0:
                mDirectoryImage.setBackgroundResource(R.drawable.ico_nav_directory_selected);
                mDirectoryText.setTypeface(NavigationActivity.latoRegularTypeFace);
                mDirectoryText.setTextColor(getResources().getColor(android.R.color.white));
                mDirectoryButton.setBackgroundResource(R.drawable.tab_selected);
                break;
            case 1:
                mRequestImage.setBackgroundResource(R.drawable.ico_nav_request_selected);
                mRequestText.setTypeface(NavigationActivity.latoRegularTypeFace);
                mRequestText.setTextColor(getResources().getColor(android.R.color.white));
                mRequestButton.setBackgroundResource(R.drawable.tab_selected);
                break;
            case 2:
                mSendImage.setBackgroundResource(R.drawable.ico_nav_send_selected);
                mSendText.setTypeface(NavigationActivity.latoRegularTypeFace);
                mSendText.setTextColor(getResources().getColor(android.R.color.white));
                mSendButton.setBackgroundResource(R.drawable.tab_selected);
                break;
            case 3:
                mWalletImage.setBackgroundResource(R.drawable.ico_nav_wallets_selected);
                mWalletText.setTypeface(NavigationActivity.latoRegularTypeFace);
                mWalletText.setTextColor(getResources().getColor(android.R.color.white));
                mWalletButton.setBackgroundResource(R.drawable.tab_selected);
                break;
            case 4:
                mSettingImage.setBackgroundResource(R.drawable.ico_nav_more_selected);
                mSettingText.setTypeface(NavigationActivity.latoRegularTypeFace);
                mSettingText.setTextColor(getResources().getColor(android.R.color.white));
                mSettingButton.setBackgroundResource(R.drawable.tab_selected);
                break;
            default:
                break;
        }
    }

    public void unselectTab(int position) {
        mLastTab = position;
        switch (position) {
            case 0:
                mDirectoryImage.setBackgroundResource(R.drawable.ico_nav_directory);
                mDirectoryText.setTextColor(getResources().getColor(R.color.navbar_text));
                mDirectoryButton.setBackgroundResource(android.R.color.transparent);
                break;
            case 1:
                mRequestImage.setBackgroundResource(R.drawable.ico_nav_request);
                mRequestText.setTextColor(getResources().getColor(R.color.navbar_text));
                mRequestButton.setBackgroundResource(android.R.color.transparent);
                break;
            case 2:
                mSendImage.setBackgroundResource(R.drawable.ico_nav_send);
                mSendText.setTextColor(getResources().getColor(R.color.navbar_text));
                mSendButton.setBackgroundResource(android.R.color.transparent);
                break;
            case 3:
                mWalletImage.setBackgroundResource(R.drawable.ico_nav_wallets);
                mWalletText.setTextColor(getResources().getColor(R.color.navbar_text));
                mWalletButton.setBackgroundResource(android.R.color.transparent);
                break;
            case 4:
                mSettingImage.setBackgroundResource(R.drawable.ico_nav_more);
                mSettingText.setTextColor(getResources().getColor(R.color.navbar_text));
                mSettingButton.setBackgroundResource(android.R.color.transparent);
                break;
            default:
                break;
        }
    }

    private void clearPopups() {
        mPopupDirectoryButton.setVisibility(View.INVISIBLE);
        mPopupRequestButton.setVisibility(View.INVISIBLE);
        mPopupSendButton.setVisibility(View.INVISIBLE);
        mPopupWalletButton.setVisibility(View.INVISIBLE);
        mPopupSettingButton.setVisibility(View.INVISIBLE);
    }

    private void displayPopup(MotionEvent ev) {
        clearPopups();
        if (isOverView(ev.getRawX(), ev.getRawY(), mDirectoryButton)) {
            mPopupDirectoryButton.setVisibility(View.VISIBLE);
        }
        if (isOverView(ev.getRawX(), ev.getRawY(), mRequestButton)) {
            mPopupRequestButton.setVisibility(View.VISIBLE);
        }
        if (isOverView(ev.getRawX(), ev.getRawY(), mSendButton)) {
            mPopupSendButton.setVisibility(View.VISIBLE);
        }
        if (isOverView(ev.getRawX(), ev.getRawY(), mWalletButton)) {
            mPopupWalletButton.setVisibility(View.VISIBLE);
        }
        if (isOverView(ev.getRawX(), ev.getRawY(), mSettingButton)) {
            mPopupSettingButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Determines if given points are inside view
     *
     * @param x    - x coordinate of point
     * @param y    - y coordinate of point
     * @param view - view object to compare
     * @return true if the points are within view bounds, false otherwise
     */
    private boolean isOverView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        //point is inside view bounds
        if ((x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    public void initializeElements() {
        mDirectoryText.setTypeface(NavigationActivity.latoRegularTypeFace);
        mRequestText.setTypeface(NavigationActivity.latoRegularTypeFace);
        mSendText.setTypeface(NavigationActivity.latoRegularTypeFace);
        mWalletText.setTypeface(NavigationActivity.latoRegularTypeFace);
        mSettingText.setTypeface(NavigationActivity.latoRegularTypeFace);

        for(int i=0; i<5; i++) {
            unselectTab(i);
        }
        clearPopups();
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeElements();
        mLastTab = selectedTab;
        selectTab(selectedTab);
    }

    //Callbacks for containing Activity to implement
    public interface OnScreenSelectedListener {
        public void onNavBarSelected(int position);
    }
}
