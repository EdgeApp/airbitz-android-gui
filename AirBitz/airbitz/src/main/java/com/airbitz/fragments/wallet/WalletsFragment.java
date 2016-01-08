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
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.AccountSettings;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressImageButton;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;
import java.util.List;

public class WalletsFragment extends WalletBaseFragment implements
    DynamicListView.OnListReordering,
    WalletAdapter.OnHeaderButtonPress {

    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";

    public final String TAG = getClass().getSimpleName();

    private View mWalletsHeader;
    private View mArchiveHeader;
    private ImageView mArchiveMovingHeaderImage;
    private DynamicListView mWalletListView;
    private WalletAdapter mWalletAdapter;
    private View mProgress;
    private List<Wallet> mLatestWalletList = new ArrayList<Wallet>();
    private boolean mArchiveClosed = false;
    private DeleteWalletTask mDeleteTask;

    private TextView mHeaderTotal;
    private Switch mModeSelector;
    private CompoundButton.OnCheckedChangeListener mSwitchChange;
    private View.OnClickListener mModeListener;
    private TextView mFiatSelect;
    private TextView mBitcoinSelect;
    private View mView;

    public WalletsFragment() {
        mAllowArchived = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected String getSubtitle() {
        return mActivity.getString(R.string.fragment_wallets_title);
    }

    @Override
    protected List<Wallet> fetchCoreWallets() {
        return mCoreApi.getCoreWallets(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallets, container, false);

        mWalletAdapter = new WalletAdapter(mActivity, mLatestWalletList);
        mWalletAdapter.setHeaderButtonListener(this);
        mWalletAdapter.setIsBitcoin(mOnBitcoinMode);

        mWalletsHeader = view.findViewById(R.id.fragment_wallets_wallets_header);

        mArchiveHeader = view.findViewById(R.id.fragment_wallets_archive_header);
        mArchiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mWalletAdapter.notifyDataSetChanged();
            }
        });

        mArchiveMovingHeaderImage = (ImageView) mArchiveHeader.findViewById(R.id.item_listview_wallets_archive_header_image);
        mArchiveMovingHeaderImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mArchiveClosed = !mArchiveClosed;
                updateWalletList(mArchiveClosed);
                mWalletAdapter.notifyDataSetChanged();
            }
        });

        mProgress = view.findViewById(R.id.progress_horizontal);
        mWalletListView = (DynamicListView) view.findViewById(R.id.fragment_wallets_listview);
        mWalletListView.setAdapter(mWalletAdapter);
        mWalletListView.setWalletList(mLatestWalletList);
        mWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mWalletListView.setHeaders(mWalletsHeader, mArchiveHeader);
        mWalletListView.setArchiveClosed(mArchiveClosed);
        mWalletListView.setHeaderVisibilityOnReturn();
        mWalletListView.setOnListReorderedListener(this);
        mWalletListView.setEmptyView(mProgress);
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
                    mActivity.ShowFadingDialog(getResources().getString(R.string.fragment_wallets_archive_help), getResources().getInteger(R.integer.alert_hold_time_help_popups));
                } else {
                    a.setSelectedWallet(i);
                    view.setSelected(true);

                    walletChanged(wallet);

                    mActivity.resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.WALLET.ordinal());
                    mActivity.switchFragmentThread(NavigationActivity.Tabs.WALLET.ordinal());
                }
            }
        });
        mWalletListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Wallet wallet = mLatestWalletList.get(pos);
                if (wallet.isHeader() || wallet.isArchiveHeader()) {
                    return true;
                }
                View drag = arg1.findViewById(R.id.drag_container);
                if (drag.getX() <= mWalletListView.getDownX()) {
                    return mWalletListView.startDrag(arg0, arg1, pos, id);
                } else {
                    showRenameDialog(wallet);
                    return true;
                }
            }
        });
        mHeaderTotal = (TextView) mWalletsHeader.findViewById(R.id.total);

        mModeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMode();
            }
        };
        mSwitchChange = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleMode();
            }
        };

        mModeSelector = (Switch) mWalletsHeader.findViewById(R.id.fiat_btc_select);
        mFiatSelect = (TextView) mWalletsHeader.findViewById(R.id.fiat);
        mBitcoinSelect = (TextView) mWalletsHeader.findViewById(R.id.bitcoin);
        mFiatSelect.setOnClickListener(mModeListener);
        mBitcoinSelect.setOnClickListener(mModeListener);
        return view;
    }

    @Override
    public void showWalletList() {
        if (getActivity() == null || !isAdded() || !finishedResume()) {
            return;
        }
        mActivity.resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.WALLET.ordinal());
        mActivity.switchFragmentThread(NavigationActivity.Tabs.WALLET.ordinal());
    }

    @Override
    public void hideWalletList() {
    }

    private void toggleMode() {
        mOnBitcoinMode = !mOnBitcoinMode;
        AirbitzApplication.setBitcoinSwitchMode(mOnBitcoinMode);

        updateBalanceBar();
    }

    protected void updateBalanceBar() {
        mModeSelector.setOnCheckedChangeListener(null);
        mModeSelector.setChecked(mOnBitcoinMode);
        mModeSelector.setOnCheckedChangeListener(mSwitchChange);

        updateWalletList(mArchiveClosed);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mArchiveClosed = AirbitzApplication.getArchivedMode();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_wallets, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_add:
            if (!mLoading) {
                WalletAddFragment.pushFragment(mActivity);
            }
            return true;
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.WALLETS));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListReordering(boolean started) {
        if (!started) {
            mCoreApi.setWalletOrder(mLatestWalletList);
            // Update current wallet incase it became archived
            mWallet = mCoreApi.getWalletFromUUID(AirbitzApplication.getCurrentWallet());
            mCoreApi.reloadWallets();
        }
    }

    @Override
    protected void loadWallets() {
        updateBalanceBar();
    }

    @Override
    public void OnHeaderButtonPressed() {
        mArchiveClosed = !mArchiveClosed;
        AirbitzApplication.setArchivedMode(mArchiveClosed);
        updateBalanceBar();
    }

    private void updateWalletList(boolean archiveClosed) {
        List<Wallet> walletList = getWallets(archiveClosed);
        if (walletList != null && !walletList.isEmpty()) {
            mLatestWalletList.clear();
            mLatestWalletList.addAll(walletList);
        }
        long totalSatoshis = 0;
        int currencyNum;
        AccountSettings settings = mCoreApi.coreSettings();
        if (settings != null)
            currencyNum = settings.getCurrencyNum();
        else
            currencyNum = mCoreApi.defaultCurrencyNum();

        for (Wallet w : walletList) {
            if (!w.isArchived()) {
                totalSatoshis += w.getBalanceSatoshi();
            }
        }
        mWalletAdapter.swapWallets();
        mWalletAdapter.setIsBitcoin(mOnBitcoinMode);
        mWalletAdapter.setCurrencyNum(currencyNum);
        mWalletAdapter.setArchiveButtonState(!archiveClosed);
        mWalletListView.setArchiveClosed(archiveClosed);
        mWalletAdapter.notifyDataSetChanged();

        if (mHeaderTotal != null && null != mWallet) {
            mFiatSelect.setText(mCoreApi.currencyCodeLookup(currencyNum));
            mBitcoinSelect.setText(mCoreApi.getDefaultBTCDenomination());
            if (mOnBitcoinMode) {
                mHeaderTotal.setText(mCoreApi.formatSatoshi(totalSatoshis, true));
            } else {
                mHeaderTotal.setText(mCoreApi.FormatCurrency(totalSatoshis, currencyNum, false, true));
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

        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title(getResources().getString(R.string.fragment_wallets_rename_wallet))
               .titleColorRes(R.color.colorPrimaryDark)
               .cancelable(false)
               .customView(view, false)
               .positiveText(getResources().getString(R.string.string_done))
               .positiveColor(getResources().getColor(R.color.colorPrimaryDark))
               .negativeText(getResources().getString(R.string.string_cancel))
               .negativeColor(getResources().getColor(R.color.colorPrimaryDark))
               .neutralText(getResources().getString(R.string.string_delete))
               .neutralColor(getResources().getColor(R.color.colorPrimaryDark))
               .theme(Theme.LIGHT)
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String walletName = editText.getText().toString();
                        if (TextUtils.isEmpty(walletName)) {
                            editText.setError(getString(R.string.fragment_wallets_wallet_name_required));
                        } else {
                            if (wallet.getUUID().equals(mWallet.getUUID())) {
                                mWallet = wallet;
                            }
                            wallet.setName(walletName);
                            mCoreApi.renameWallet(wallet);
                            mCoreApi.reloadWallets();
                            dialog.dismiss();
                            updateTitle();
                        }
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        deleteWalletConfirm(wallet);
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void deleteWalletConfirm(final Wallet wallet) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title(getResources().getString(R.string.fragment_wallets_delete_wallet_confirm_title))
               .titleColorRes(R.color.colorPrimaryDark)
               .content(String.format(getString(R.string.fragment_wallets_delete_wallet_confirm_message), mWallet.getName()))
               .cancelable(false)
               .positiveText(getResources().getString(R.string.string_delete))
               .positiveColor(getResources().getColor(R.color.colorPrimaryDark))
               .negativeText(getResources().getString(R.string.string_cancel))
               .negativeColor(getResources().getColor(R.color.colorPrimaryDark))
               .theme(Theme.LIGHT)
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteWalletConfirmConfirm(wallet);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void deleteWalletConfirmConfirm(final Wallet wallet) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(mActivity);
        builder.title(String.format(getResources().getString(R.string.fragment_wallets_delete_wallet_second_confirm_title), mWallet.getName()))
               .titleColorRes(R.color.colorPrimaryDark)
               .content(String.format(getString(R.string.fragment_wallets_delete_wallet_second_confirm_message), mWallet.getName()))
               .cancelable(false)
               .positiveText(getResources().getString(R.string.string_delete))
               .positiveColor(getResources().getColor(R.color.colorPrimaryDark))
               .negativeText(getResources().getString(R.string.string_cancel))
               .negativeColor(getResources().getColor(R.color.colorPrimaryDark))
               .theme(Theme.LIGHT)
               .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mDeleteTask = new DeleteWalletTask(wallet);
                        mDeleteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public class DeleteWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final Wallet mWallet;

        DeleteWalletTask(Wallet wallet) {
            mWallet = wallet;
        }

        @Override
        protected void onPreExecute() {
            String msg = mActivity.getString(R.string.fragment_wallets_deleting_wallet);
            int timeout = mActivity.getResources().getInteger(R.integer.alert_hold_time_forever);
            mActivity.ShowFadingDialog(msg, null, timeout, false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreApi.removeWallet(mWallet.getUUID());
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mDeleteTask = null;
            if (!success) {
                mActivity.ShowFadingDialog(getString(R.string.fragment_wallets_unable_to_delete_wallet));
            } else {
                mActivity.ShowFadingDialog(getString(R.string.fragment_wallets_wallet_delete));
            }
        }

        @Override
        protected void onCancelled() {
            mDeleteTask = null;
        }
    }
}
