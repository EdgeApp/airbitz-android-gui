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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletChoiceAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Wallet;

import java.util.ArrayList;
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
    protected View mTitleFrame;
    protected View mSearchLayout;
    protected EditText mSearch;
    protected View mCloseSearch;
    protected TextView mTitleView;
    protected TextView mSubtitleView;
    protected ImageView mDropdownIcon;
    protected CoreAPI mCoreApi;
    protected boolean mSearching = false;
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
        mOnBitcoinMode = AirbitzApplication.getBitcoinSwitchMode();
        mLoading = true;
        mSearching = false;
        // Check for cached wallets
        if (null == mWallets) {
            mWallets = mCoreApi.getCoreActiveWallets();
        }
        // Create empty list
        if (null == mWallets) {
            mWallets = new ArrayList<Wallet>();
        }
        setDefaultWallet();
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

        mTitleFrame = view.findViewById(R.id.title_frame);
        mTitleView = (TextView) view.findViewById(R.id.title);
        mSearchLayout = view.findViewById(R.id.toolbar_search_layout);
        if (mSearchLayout != null) {
            mSearch = (EditText) view.findViewById(R.id.toolbar_search);
            mSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    onSearchQuery(editable.toString());
                }
            });
            mSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        mActivity.hideSoftKeyboard(mSearch);
                        return true;
                    }
                    return false;
                }
            });
            mCloseSearch = view.findViewById(R.id.search_close_btn);
            if (mCloseSearch != null) {
                mCloseSearch.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(mSearch.getText())) {
                            hideSearch();
                        } else {
                            mSearch.setText("");
                        }
                    }
                });
            }
        }
        mSubtitleView = (TextView) view.findViewById(R.id.subtitle);
        mDropdownIcon = (ImageView) view.findViewById(R.id.dropdown_icon);
        updateTitle();
        if (mDropDownEnabled) {
            mTitleFrame.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    toggleWallets();
                }
            });
            setupWalletViews(view);
            if (mExpanded) {
                finishShowWallets();
            } else {
                finishHideWallets();
            }
            mDropdownIcon.setVisibility(View.VISIBLE);
        } else {
            mDropdownIcon.setVisibility(View.GONE);
        }
        updateTitle();
        if (mSearching) {
            showSearch();
        } else {
            hideSearch();
        }
    }

    protected void onSearchQuery(String query) {
    }

    protected void fetchWallets() {
        List<Wallet> tmp = mCoreApi.getCoreActiveWallets();
        if (tmp != null) {
            mWallets.clear();
            mWallets.addAll(tmp);
        }
    }

    protected boolean getForceDefaultWallet() {
        return true;
    }

    private void setDefaultWallet() {
        if (mWallet == null || getForceDefaultWallet()) {
            String uuid = AirbitzApplication.getCurrentWallet();
            if (uuid == null) {
                if (mWallets != null && mWallets.size() > 0) {
                    uuid = mWallets.get(0).getUUID();
                    AirbitzApplication.setCurrentWallet(uuid);
                }
            }
            if (uuid != null && null != mWallets) {
                for (Wallet w : mWallets) {
                    if (!w.getUUID().equals(uuid)) {
                        continue;
                    }
                    mWallet = w;
                    break;
                }
            }
        }
        // If the user archives the selected wallet:
        //     change the default wallet for other screens
        if (mWallet != null && mWallet.isArchived()) {
            if (mWallets != null && mWallets.size() > 0) {
                String uuid = mWallets.get(0).getUUID();
                AirbitzApplication.setCurrentWallet(uuid);
            }
        }
        if (mWallet != null) {
            mLoading = mWallet.getCurrencyNum() == -1 ? true : false;
        }
    }

    @Override
    public void onWalletsLoaded() {
        fetchWallets();
        if (mLoading) {
            mWallet = null;
        }
        setDefaultWallet();
        if (mWallet != null) {
            loadWallets();
            updateTitle();
        }
    }

    @Override
    public void onWalletUpdated() {
        mCoreApi.reloadWallets();
    }

    @Override
    public void OnExchangeRatesChange() {
        onExchangeRatesChange();
    }

    protected void onExchangeRatesChange() {
        if (!mLoading) {
            fetchWallets();
            loadWallets();
        }
    }

    private WalletChoiceAdapter mAdapter;
    protected void loadWallets() {
        if (mDropDownEnabled) {
            mAdapter.notifyDataSetChanged();
        }
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
        mAdapter = new WalletChoiceAdapter(mActivity, mWallets);
        mWalletList.setAdapter(mAdapter);
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
    }

    @Override
    public void onPause() {
        super.onPause();

        mCoreApi.setOnWalletLoadedListener(null);
        mCoreApi.removeExchangeRateChangeListener(this);
        mActivity.setOnWalletUpdated(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isMenuExpanded()) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        } else if (isSearching()) {
            inflater.inflate(R.menu.menu_empty, menu);
            return;
        }
        onAddOptions(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected void onAddOptions(Menu menu, MenuInflater inflater) {
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
                mTitleView.setText(mWallet.getName());
            }
        }
        if (mSubtitleView != null && !TextUtils.isEmpty(getSubtitle())) {
            mSubtitleView.setText(getSubtitle());
            mSubtitleView.setVisibility(View.VISIBLE);
        }
    }

    protected void walletChanged(Wallet newWallet) {
        mWallet = newWallet;
        if (!newWallet.isArchived()) {
            AirbitzApplication.setCurrentWallet(mWallet.getUUID());
        }
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
        mTitleFrame.setVisibility(View.GONE);
    }

    public void showTitleView() {
        mTitleFrame.setVisibility(View.VISIBLE);
    }

    public boolean isSearching() {
        return mSearching;
    }

    public boolean showSearch() {
        if (mSearchLayout.getVisibility() != View.VISIBLE) {
            showArrow();
            mSearchLayout.setVisibility(View.VISIBLE);
            mTitleFrame.setVisibility(View.INVISIBLE);
            mSearch.requestFocus();
            mActivity.showSoftKeyboard(mSearch);

            mSearching = true;
            mActivity.invalidateOptionsMenu();
            return true;
        }
        return false;
    }

    public boolean hideSearch() {
        if (mSearchLayout.getVisibility() == View.VISIBLE) {
            showBurger();
            mSearchLayout.setVisibility(View.GONE);
            mTitleFrame.setVisibility(View.VISIBLE);
            mActivity.hideSoftKeyboard(mSearchLayout);

            mSearching = false;
            mActivity.invalidateOptionsMenu();
            return true;
        }
        return false;
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
        } else if (isSearching()) {
            hideSearch();
        } else {
            super.onNavigationClick();
        }
    }
}
