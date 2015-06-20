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

package com.airbitz.fragments.wallet;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.CurrencyAdapter;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.OfflineWalletFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WalletsFragment extends BaseFragment implements DynamicListView.OnListReordering,
        CoreAPI.OnWalletLoaded,
        NavigationActivity.OnWalletUpdated,
        WalletAdapter.OnHeaderButtonPress {
    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";
    public static final String ARCHIVE_HEADER_STATE = "archiveClosed";

    public final String TAG = getClass().getSimpleName();

    private View walletsHeader;
    private ImageView walletsHeaderImage;
    private ImageView archiveMovingHeaderImage;
    private View archiveHeader;
    private TextView mBalanceLabel;
    private boolean mArchiveClosed = false;
    private DynamicListView mLatestWalletListView;
    private HighlightOnPressImageButton mHelpButton;
    private ImageView mMoverCoin;
    private Bundle bundle;
    private WalletAdapter mLatestWalletAdapter;
    private boolean mOnBitcoinMode = true;
    private TextView mTitleView;

    private List<Wallet> mLatestWalletList = new ArrayList<Wallet>();
    private CoreAPI mCoreAPI;
    private NavigationActivity mActivity;
    private View mView;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_wallets, container, false);
        }

        Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        getBaseActivity().setSupportActionBar(toolbar);
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);

        mTitleView = (TextView) mView.findViewById(R.id.title);
        mTitleView.setText(R.string.fragment_wallets_title);

        mLatestWalletAdapter = new WalletAdapter(mActivity, mLatestWalletList);
        mLatestWalletAdapter.setHeaderButtonListener(this);

        walletsHeader = mView.findViewById(R.id.fragment_wallets_wallets_header);
        walletsHeader.setVisibility(View.GONE);
        walletsHeaderImage = (ImageView) mView.findViewById(R.id.item_listview_wallets_header_image);
        walletsHeaderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WalletAddFragment.pushFragment(mActivity);
            }
        });

        archiveHeader = mView.findViewById(R.id.fragment_wallets_archive_header);
        archiveHeader.setVisibility(View.GONE);
        archiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mLatestWalletAdapter.notifyDataSetChanged();
            }
        });

        archiveMovingHeaderImage = (ImageView) mView.findViewById(R.id.item_listview_wallets_archive_header_image);
        archiveMovingHeaderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mLatestWalletAdapter.notifyDataSetChanged();
            }
        });

        mLatestWalletListView = (DynamicListView) mView.findViewById(R.id.fragment_wallets_listview);
        mLatestWalletListView.setVisibility(View.GONE);
        mLatestWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isAdded()) {
                    return;
                }
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                Wallet wallet = a.getList().get(i);
                if (wallet.isArchiveHeader()) {
                    mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_wallets_archive_help), 2000);
                } else {
                    if (!wallet.isArchived()) {
                        AirbitzApplication.setCurrentWallet(wallet.getUUID());
                    }
                    WalletsFragment.popFragment(mActivity);
                }
            }
        });
        return mView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            WalletsFragment.popFragment(mActivity);
            return true;
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.WALLETS));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupLatestWalletListView() {
        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mLatestWalletListView.setWalletList(mLatestWalletList);
        mLatestWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mLatestWalletListView.setHeaders(walletsHeader, archiveHeader);
        mLatestWalletListView.setArchiveClosed(mArchiveClosed);
        mLatestWalletListView.setHeaderVisibilityOnReturn();
        mLatestWalletListView.setOnListReorderedListener(this);
    }

    private void checkWalletListVisibility() {
        if(mLatestWalletListView.getVisibility() != View.VISIBLE && mLatestWalletList.size() >= 3) {
            mLatestWalletListView.setVisibility(View.VISIBLE);
            walletsHeader.setVisibility(View.VISIBLE);
            archiveHeader.setVisibility(View.VISIBLE);
        }
    }

    // Callback when the listview was reordered by the user
    @Override
    public void onListReordering(boolean started) {
        Log.d(TAG, "List reordering is " + started);
        if(started) {
            mCoreAPI.stopAllAsyncUpdates();
        }
        else {
            mCoreAPI.startAllAsyncUpdates();
            mCoreAPI.setWalletOrder(mLatestWalletList);
            reloadWallets();
        }
    }

    @Override
    public void onWalletUpdated() {
        Log.d(TAG, "wallet list updated");
        reloadWallets();
    }

    private void reloadWallets() {
        mCoreAPI.reloadWallets(); // async call return as onWalletsLoaded
    }

    @Override
    public void onWalletsLoaded() {
        Log.d(TAG, "wallet loaded");
        updateWalletList(mArchiveClosed);
        checkWalletListVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mArchiveClosed = prefs.getBoolean(ARCHIVE_HEADER_STATE, false);

        mActivity.setOnWalletUpdated(this);
        mCoreAPI.setOnWalletLoadedListener(this); // this kicks off reading wallets

        mOnBitcoinMode = AirbitzApplication.getBitcoinSwitchMode();

        setupLatestWalletListView();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ARCHIVE_HEADER_STATE, mArchiveClosed).apply();
        mActivity.setOnWalletUpdated(null);
        mCoreAPI.setOnWalletLoadedListener(null);
    }

    public void updateWalletList(boolean archiveClosed) {
        List<Wallet> walletList = getWallets(archiveClosed);
        if(walletList != null && !walletList.isEmpty()) {
            mLatestWalletList.clear();
            mLatestWalletList.addAll(walletList);
        }
        mLatestWalletAdapter.swapWallets();
        mLatestWalletAdapter.setIsBitcoin(mOnBitcoinMode);
        mLatestWalletAdapter.setArchiveButtonState(!archiveClosed);
        mLatestWalletListView.setArchiveClosed(archiveClosed);
        mLatestWalletAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnHeaderButtonPressed() {
        mArchiveClosed = !mArchiveClosed;
        updateWalletList(mArchiveClosed);
    }

    public List<Wallet> getWallets(boolean archiveClosed) {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = mCoreAPI.getCoreWallets(false);

        if (coreList == null)
            return null;

        Wallet headerWallet = new Wallet(Wallet.WALLET_HEADER_ID);
        headerWallet.setUUID(Wallet.WALLET_HEADER_ID);
        list.add(headerWallet);//Wallet HEADER
        // Loop through and find non-archived wallets first
        for (Wallet wallet : coreList) {
            if (!wallet.isArchived() && wallet.getName() != null)
                list.add(wallet);
        }
        Wallet archiveWallet = new Wallet(Wallet.WALLET_ARCHIVE_HEADER_ID);
        archiveWallet.setUUID(Wallet.WALLET_ARCHIVE_HEADER_ID);
        list.add(archiveWallet); //Archive HEADER

        if (!archiveClosed) {
            // Loop through and add archived wallets now
            for (Wallet wallet : coreList) {
                if (wallet.isArchived() && wallet.getName() != null)
                    list.add(wallet);
            }
        }
        return list;
    }

    public static void pushFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_top, R.animator.fade_out);

        Fragment fragment = new WalletsFragment();
        mActivity.pushFragment(fragment, transaction);
    }

    public static void popFragment(NavigationActivity mActivity) {
        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.fade_in, R.animator.slide_out_top);

        mActivity.popFragment(transaction);
    }
}
