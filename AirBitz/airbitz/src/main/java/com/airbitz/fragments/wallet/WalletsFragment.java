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
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressImageButton;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

public class WalletsFragment extends WalletBaseFragment implements
    DynamicListView.OnListReordering,
    WalletAdapter.OnHeaderButtonPress {

    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";
    public static final String ARCHIVE_HEADER_STATE = "archiveClosed";

    public final String TAG = getClass().getSimpleName();

    private View mWalletsHeader;
    private View mArchiveHeader;
    private ImageView mWalletsHeaderImage;
    private ImageView mArchiveMovingHeaderImage;
    private DynamicListView mWalletListView;
    private WalletAdapter mWalletAdapter;
    private List<Wallet> mLatestWalletList = new ArrayList<Wallet>();
    private boolean mArchiveClosed = false;

    private TextView mHeaderTotal;
    private SegmentedGroup mModeSelector;
    private Button mFiatSelect;
    private Button mBitcoinSelect;

    @Override
    protected void setupWalletViews(View view) {
        mWalletsContainer = view.findViewById(R.id.wallets_container);

        mWalletAdapter = new WalletAdapter(mActivity, mLatestWalletList);
        mWalletAdapter.setHeaderButtonListener(this);

        mWalletsHeader = view.findViewById(R.id.fragment_wallets_wallets_header);
        mWalletsHeader.setVisibility(View.GONE);

        mWalletsHeaderImage = (ImageView) view.findViewById(R.id.item_listview_wallets_header_image);
        mWalletsHeaderImage.setVisibility(View.GONE);

        mArchiveHeader = view.findViewById(R.id.fragment_wallets_archive_header);
        mArchiveHeader.setVisibility(View.GONE);
        mArchiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mWalletAdapter.notifyDataSetChanged();
            }
        });

        mArchiveMovingHeaderImage = (ImageView) view.findViewById(R.id.item_listview_wallets_archive_header_image);
        mArchiveMovingHeaderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mWalletAdapter.notifyDataSetChanged();
            }
        });

        mWalletListView = (DynamicListView) view.findViewById(R.id.fragment_wallets_listview);
        mWalletListView.setVisibility(View.GONE);
        mWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isAdded()) {
                    return;
                }
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                Wallet wallet = a.getList().get(i);
                if (wallet.isHeader()) {
                } else if (wallet.isArchiveHeader()) {
                    mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_wallets_archive_help), 2000);
                } else {
                    AirbitzApplication.setCurrentWallet(wallet.getUUID());
                    walletChanged(wallet);
                }
            }
        });
        mWalletListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                View drag = arg1.findViewById(R.id.drag_container);
                if (drag.getX() <= mWalletListView.getDownX()) {
                    return mWalletListView.startDrag(arg0, arg1, pos, id);
                } else {
                    Wallet wallet = mLatestWalletList.get(pos);
                    showRenameDialog(wallet);
                    return true;
                }
            }
        });
        mHeaderTotal = (TextView) view.findViewById(R.id.total);

        mModeSelector = (SegmentedGroup) view.findViewById(R.id.fiat_btc_select);
        mFiatSelect = (Button) mModeSelector.findViewById(R.id.fiat);
        mFiatSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode();
            }
        });

        mBitcoinSelect = (Button) mModeSelector.findViewById(R.id.bitcoin);
        mBitcoinSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode();
            }
        });
        updateModeButtons();
    }

    private void toggleMode() {
        mOnBitcoinMode = !mOnBitcoinMode;
        updateModeButtons();

        updateWalletList(mArchiveClosed);
        mWalletAdapter.notifyDataSetChanged();
    }

    private void updateModeButtons() {
        if (mOnBitcoinMode) {
            mModeSelector.check(mBitcoinSelect.getId());
        } else {
            mModeSelector.check(mFiatSelect.getId());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isMenuExpanded()) {
            inflater.inflate(R.menu.menu_wallets, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isMenuExpanded()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
        case R.id.action_add:
            WalletAddFragment.pushFragment(mActivity);
            return true;
        case R.id.action_export:
            ExportFragment.pushFragment(mActivity);
            return true;
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.WALLETS));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void checkWalletListVisibility() {
        if (mWalletListView.getVisibility() != View.VISIBLE && mLatestWalletList.size() >= 3) {
            mWalletListView.setVisibility(View.VISIBLE);
            mWalletsHeader.setVisibility(View.VISIBLE);
            mArchiveHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onListReordering(boolean started) {
        mCoreApi.setWalletOrder(mLatestWalletList);
        mCoreApi.reloadWallets();
    }

    @Override
    protected void loadWallets() {
        updateWalletList(mArchiveClosed);
        checkWalletListVisibility();
    }

    @Override
    public void OnHeaderButtonPressed() {
        mArchiveClosed = !mArchiveClosed;
        updateWalletList(mArchiveClosed);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        mArchiveClosed = prefs.getBoolean(ARCHIVE_HEADER_STATE, false);

        setupLatestWalletListView();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences prefs = mActivity.getSharedPreferences(AirbitzApplication.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(ARCHIVE_HEADER_STATE, mArchiveClosed).apply();
    }

    @Override
    protected void fetchWallets() {
        mWallets = mCoreApi.getCoreWallets(false);
    }

    private void setupLatestWalletListView() {
        mWalletListView.setAdapter(mWalletAdapter);
        mWalletListView.setWalletList(mLatestWalletList);
        mWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mWalletListView.setHeaders(mWalletsHeader, mArchiveHeader);
        mWalletListView.setArchiveClosed(mArchiveClosed);
        mWalletListView.setHeaderVisibilityOnReturn();
        mWalletListView.setOnListReorderedListener(this);
    }

    private void updateWalletList(boolean archiveClosed) {
        List<Wallet> walletList = getWallets(archiveClosed);
        if (walletList != null && !walletList.isEmpty()) {
            mLatestWalletList.clear();
            mLatestWalletList.addAll(walletList);
        }
        mWalletAdapter.swapWallets();
        mWalletAdapter.setIsBitcoin(mOnBitcoinMode);
        mWalletAdapter.setArchiveButtonState(!archiveClosed);
        mWalletListView.setArchiveClosed(archiveClosed);
        mWalletAdapter.notifyDataSetChanged();

        if (mHeaderTotal != null && null != mWallet) {
            long totalSatoshis = mWallet.getBalanceSatoshi();
            mFiatSelect.setText(mCoreApi.currencyCodeLookup(mWallet.getCurrencyNum()));
            mBitcoinSelect.setText(mCoreApi.getDefaultBTCDenomination());
            if (mOnBitcoinMode) {
                mHeaderTotal.setText(mCoreApi.formatSatoshi(totalSatoshis, true));
            } else {
                mHeaderTotal.setText(mCoreApi.FormatCurrency(totalSatoshis, mWallet.getCurrencyNum(), false, true));
            }
        }
    }

    private List<Wallet> getWallets(boolean archiveClosed) {
        List<Wallet> list = new ArrayList<Wallet>();
        List<Wallet> coreList = mWallets;

        if (coreList == null) {
            return null;
        }

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
            for (Wallet wallet : coreList) {
                if (wallet.isArchived() && wallet.getName() != null) {
                    list.add(wallet);
                }
            }
        }
        return list;
    }

    @Override
    protected int getAnimDuration() {
        return 200;
    }

    public void showRenameDialog(final Wallet wallet) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.alert_rename_wallet, null);
        final EditText editText = (EditText) view.findViewById(R.id.wallet_name);
        editText.setText(wallet.getName());
        editText.setSelection(wallet.getName().length());

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        builder.setTitle(getResources().getString(R.string.fragment_wallets_rename_wallet))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.string_done),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String walletName = editText.getText().toString();
                                if (TextUtils.isEmpty(walletName)) {
                                    editText.setError(getString(R.string.fragment_wallets_wallet_name_required));
                                } else {
                                    wallet.setName(walletName);
                                    mCoreApi.renameWallet(wallet);
                                    mCoreApi.reloadWallets();
                                    dialog.dismiss();
                                }
                            }
                        })
                .setNegativeButton(R.string.string_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        // this changes the colors of the system's UI buttons we're using
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
        dialog.show();
    }

}
