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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletChoiceAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.models.Wallet;

import java.util.List;

public class WalletBaseFragment extends BaseFragment implements
        CoreAPI.OnWalletLoaded,
        CoreAPI.OnExchangeRatesChange,
        NavigationActivity.OnBackPress,
        NavigationActivity.OnWalletUpdated {

    private final String TAG = WalletBaseFragment.class.getSimpleName();

    private View mBlocker;

    protected Handler mHandler = new Handler();
    protected List<Wallet> mWallets;
    protected Wallet mWallet;
    protected ListView mWalletList;
    protected View mWalletsContainer;
    protected TextView mTitleView;
    protected CoreAPI mCoreApi;
    protected boolean mHomeEnabled = true;
    protected boolean mDrawerEnabled = false;
    protected boolean mDropDownEnabled = true;
    protected boolean mOnBitcoinMode = true;
    protected boolean mExpanded = false;
    protected boolean mLoading = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreApi = CoreAPI.getApi();

        String uuid = AirbitzApplication.getCurrentWallet();
        if (uuid != null) {
            mLoading = false;
            mWallet = mCoreApi.getWalletFromUUID(uuid);
        } else {
            mLoading = true;
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        mBlocker = view.findViewById(R.id.toolbar_blocker);
        mBlocker.setVisibility(View.INVISIBLE);
        mBlocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mExpanded) {
                    hideWalletList();
                }
            }
        });

        mTitleView = (TextView) view.findViewById(R.id.title);
        updateTitle();
        if (mDropDownEnabled) {
            mTitleView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    toggleWallets();
                }
            });
        }
        setupWalletViews(view);
        if (mExpanded) {
            finishShowWallets();
        } else {
            finishHideWallets();
        }
        updateTitle();
    }

    protected void fetchWallets() {
        mWallets = mCoreApi.getCoreActiveWallets();
    }

    @Override
    public void onWalletsLoaded() {
        fetchWallets();
        if (mWallet == null) {
            String uuid = AirbitzApplication.getCurrentWallet();
            if (uuid == null) {
                uuid = mCoreApi.loadWalletUUIDs().get(0);
                AirbitzApplication.setCurrentWallet(uuid);
            }
            mWallet = mCoreApi.getWalletFromUUID(uuid);
        }
        mLoading = mWallet.getCurrencyNum() == -1 ? true : false;
        loadWallets();
        updateTitle();
    }

    @Override
    public void onWalletUpdated() {
        mLoading = mWallet.getCurrencyNum() == -1 ? true : false;
        fetchWallets();
        loadWallets();
        updateTitle();
    }

    @Override
    public void OnExchangeRatesChange() {
        onExchangeRatesChange();
    }

    protected void onExchangeRatesChange() {
        fetchWallets();
        loadWallets();
    }

    protected void loadWallets() {
        WalletChoiceAdapter adapter = new WalletChoiceAdapter(mActivity, mWallets);
        adapter.setDropDownViewResource(R.layout.item_request_wallet_spinner_dropdown);
        mWalletList.setAdapter(adapter);
    }

    protected void setupWalletViews(View view) {
        mWalletList = (ListView) view.findViewById(R.id.wallet_choices);
        mWalletList.setVisibility(View.GONE);
        mWalletList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                walletChanged(mWallets.get(i));
            }
        });
        mWalletsContainer = mWalletList;
    }

    @Override
    public boolean onBackPress() {
        if (mActivity.isDrawerOpen()) {
            mActivity.closeDrawer();
            return true;
        } else if (isMenuExpanded()) {
            hideWalletList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mCoreApi.setOnWalletLoadedListener(this);
        mCoreApi.addExchangeRateChangeListener(this);
        mActivity.setOnWalletUpdated(this);

        mOnBitcoinMode = AirbitzApplication.getBitcoinSwitchMode();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCoreApi.setOnWalletLoadedListener(null);
        mCoreApi.removeExchangeRateChangeListener(this);
        mActivity.setOnWalletUpdated(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    protected void updateTitle() {
        if (mTitleView != null) {
            if (mLoading) {
                mTitleView.setText(R.string.string_loading);
            } else {
                mTitleView.setText(mWallet.getName() + " ▼");
            }
        }
    }

    protected void walletChanged(Wallet newWallet) {
        mWallet = newWallet;
        AirbitzApplication.setCurrentWallet(mWallet.getUUID());
        updateTitle();
        hideWalletList();
    }

    protected void setHomeEnabled(boolean enabled) {
        mHomeEnabled = enabled;
    }

    protected void setDropdownEnabled(boolean enabled) {
        mDropDownEnabled = enabled;
    }

    public void hideTitleView() {
        mTitleView.setVisibility(View.GONE);
    }

    public void showTitleView() {
        mTitleView.setVisibility(View.VISIBLE);
    }

    public void toggleWallets() {
        if (isMenuExpanded()) {
            hideWalletList();
        } else {
            showWalletList();
        }
    }

    public boolean isMenuExpanded() {
        return mExpanded;
    }

    protected int getAnimDuration() {
        return 100;
    }

    public void showWalletList() {
        ObjectAnimator blocker = ObjectAnimator.ofFloat(mBlocker, "alpha", 0f, 1f);
        ObjectAnimator key = ObjectAnimator.ofFloat(mWalletsContainer, "translationY", -mWalletsContainer.getHeight(), 0f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(getAnimDuration());
        set.playTogether(key, blocker);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator aniamtor) {
                finishShowWallets();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                mBlocker.setVisibility(View.VISIBLE);
                mWalletsContainer.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    public void finishShowWallets() {
        mWalletsContainer.setVisibility(View.VISIBLE);
        mBlocker.setVisibility(View.VISIBLE);
        mActivity.invalidateOptionsMenu();
        mExpanded = true;

        showArrow();
    }

    public void hideWalletList() {

        ObjectAnimator blocker = ObjectAnimator.ofFloat(mBlocker, "alpha", 1f, 0f);
        ObjectAnimator key = ObjectAnimator.ofFloat(mWalletsContainer, "translationY", 0f, -mWalletsContainer.getHeight());

        AnimatorSet set = new AnimatorSet();
        set.setDuration(getAnimDuration());
        set.playTogether(key, blocker);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                finishHideWallets();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                mWalletsContainer.setVisibility(View.VISIBLE);
                mBlocker.setVisibility(View.VISIBLE);
            }
        });
        set.start();
    }

    public void finishHideWallets() {
        mWalletsContainer.setVisibility(View.INVISIBLE);
        mBlocker.setVisibility(View.INVISIBLE);
        mActivity.invalidateOptionsMenu();
        mExpanded = false;

        updateNavigationIcon();

        showBurger();
    }


    @Override
    protected void onNavigationClick() {
        if (isMenuExpanded()) {
            hideWalletList();
        } else {
            super.onNavigationClick();
        }
    }
}
