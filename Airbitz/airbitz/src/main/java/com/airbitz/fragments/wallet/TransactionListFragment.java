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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.airbitz.core.Account;
import co.airbitz.core.Transaction;
import co.airbitz.core.Utils;
import co.airbitz.core.Wallet;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.BuySellOverrides;
import com.airbitz.api.Constants;
import com.airbitz.api.CoreWrapper;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.WalletBaseFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;
import com.airbitz.utils.RoundedTransformation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;

public class TransactionListFragment extends WalletBaseFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = getClass().getSimpleName();

    private View mRequestButton;
    private View mSendButton;
    private TextView mBitCoinBalance;
    private TextView mFiatBalance;
    private RelativeLayout mHeaderLayout;
    private LinearLayout mBalanceLayout;
    private LinearLayout mShowBalanceLayout;
    private LinearLayout mLoadingLayout;
    private SwipeRefreshLayout mSwipeLayout;
    private boolean mIsAnimating = false;
    private boolean mShowBalance;

    AnimatorListenerAdapter mEndListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            mIsAnimating = false;
            setupBalanceView();
        }

        @Override
        public void onAnimationStart(Animator animator) {
        }
    };
    Runnable animateBalanceHide = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator balances = ObjectAnimator.ofFloat(mBalanceLayout, "alpha", 1, 0);
            ObjectAnimator text = ObjectAnimator.ofFloat(mShowBalanceLayout, "alpha", 0, 1);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(balances, text);
            set.setDuration(250);
            set.addListener(mEndListener);
            set.start();
        }
    };
    Runnable animateBalanceShow = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator balances = ObjectAnimator.ofFloat(mBalanceLayout, "alpha", 0, 1);
            ObjectAnimator text = ObjectAnimator.ofFloat(mShowBalanceLayout, "alpha", 1, 0);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(balances, text);
            set.setDuration(250);
            set.addListener(mEndListener);
            set.start();
        }
    };

    private ListView mListTransaction;
    private ViewGroup mListHeaderView;
    private ViewGroup mListFooterView;
    private TransactionAdapter mTransactionAdapter;
    private List<Transaction> mTransactions = new ArrayList<Transaction>();
    private View mView;
    private TransactionTask mTransactionTask;
    private Handler mHandler = new Handler();

    public TransactionListFragment() {
        mAllowArchived = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mShowBalance = AirbitzApplication.getShowBalanceMode();
    }

    @Override
    protected String getSubtitle() {
        return mActivity.getString(R.string.fragment_wallet_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_transaction_list, container, false);
        }

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.fragment_wallet_swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        mTransactionAdapter = new TransactionAdapter(mActivity, mTransactions);
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
        if (mWallet != null) {
            mTransactionAdapter.setWallet(mWallet);
            mTransactionAdapter.setLoading(false);
        } else {
            mTransactionAdapter.setLoading(true);
        }
        mListTransaction = (ListView) mView.findViewById(R.id.listview_transaction);
        if (mListHeaderView == null) {
            mListHeaderView = (ViewGroup) inflater.inflate(R.layout.custom_transaction_listview_header, null, false);
            mListTransaction.addHeaderView(mListHeaderView, null, false);
        }
        if (mListFooterView == null) {
            mListFooterView = (ViewGroup) inflater.inflate(R.layout.custom_transaction_listview_footer, null, false);
            mListTransaction.addFooterView(mListFooterView, null, false);

            setupFooter(R.id.buy_bitcoin, R.string.transaction_footer_buy_bitcoin, 0, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String code = mAccount.settings().currency().code;
                    String overrideUrl = BuySellOverrides.getCurrencyUrlOverrides(code);
                    if (!TextUtils.isEmpty(overrideUrl)) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(overrideUrl));
                        startActivity(intent);
                    } else if (mActivity.getResources().getBoolean(R.bool.include_buysell)
                            && ("USD".equals(code)
                                || "CAD".equals(code)
                                || "EUR".equals(code))) {
                        mActivity.switchFragmentThread(NavigationActivity.Tabs.BUYSELL.ordinal(), new Bundle());
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.DIRECTORY_CATEGORY_QUERY, "ATM");
                        mActivity.switchFragmentThread(NavigationActivity.Tabs.BD.ordinal(), bundle);
                    }
                }
            });
            setupFooter(R.id.import_bitcoin, R.string.transaction_footer_import_gift_card, Constants.BIZ_ID_AIRBITZ, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal(), new Bundle());
                }
            });
            if (mActivity.getResources().getBoolean(R.bool.include_bitrefill)) {
                setupFooter(R.id.bitrefill, R.string.transaction_footer_bitrefill, Constants.BIZ_ID_BITREFILL, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivity.switchFragmentThread(NavigationActivity.Tabs.SHOP.ordinal(), new Bundle());
                    }
                });
            } else {
                mListFooterView.findViewById(R.id.bitrefill).setVisibility(View.GONE);
            }
            if (mActivity.getResources().getBoolean(R.bool.include_shop_transactions)) {
                setupFooter(R.id.discount_starbucks, R.string.transaction_footer_starbucks_discount, Constants.BIZ_ID_STARBUCKS, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivity.switchFragmentThread(NavigationActivity.Tabs.SHOP.ordinal(), new Bundle());
                    }
                });
                setupFooter(R.id.discount_target, R.string.transaction_footer_target_discount, Constants.BIZ_ID_TARGET, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActivity.switchFragmentThread(NavigationActivity.Tabs.SHOP.ordinal(), new Bundle());
                    }
                });
                setupFooter(R.id.discount_amazon, R.string.transaction_footer_amazon_discount, Constants.BIZ_ID_AMAZON, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("http://bit.ly/AirbitzPurse"));
                        mActivity.startActivity(intent);
                    }
                });
            } else {
                mListFooterView.findViewById(R.id.discount_starbucks).setVisibility(View.GONE);
                mListFooterView.findViewById(R.id.discount_target).setVisibility(View.GONE);
                mListFooterView.findViewById(R.id.discount_amazon).setVisibility(View.GONE);
            }
        }
        mListTransaction.setAdapter(mTransactionAdapter);

        mSendButton = mListHeaderView.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = mListHeaderView.findViewById(R.id.fragment_wallet_request_button);

        mBitCoinBalance = (TextView) mListHeaderView.findViewById(R.id.header_btc_balance);
        mBitCoinBalance.setTypeface(NavigationActivity.latoRegularTypeFace);
        mFiatBalance = (TextView) mListHeaderView.findViewById(R.id.header_fiat_balance);
        mFiatBalance.setTypeface(NavigationActivity.latoRegularTypeFace);

        mLoadingLayout = (LinearLayout) mListHeaderView.findViewById(R.id.loading_layout);
        mLoadingLayout.setVisibility(View.VISIBLE);

        mShowBalanceLayout = (LinearLayout) mListHeaderView.findViewById(R.id.show_balance);
        mBalanceLayout = (LinearLayout) mListHeaderView.findViewById(R.id.balance_layout);
        mHeaderLayout = (RelativeLayout) mListHeaderView.findViewById(R.id.balance_header);
        mHeaderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWallet != null && mWallet.isSynced()) {
                    toggleShowBalance();
                }
            }
        });

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mSendButton);
                mSendButton.setClickable(false);
                Bundle bundle = new Bundle();
                bundle.putString(RequestFragment.FROM_UUID, mWallet.id());
                mActivity.resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.REQUEST.ordinal());
                mActivity.switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal(), bundle);
                mSendButton.setClickable(true);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mSendButton);
                mRequestButton.setClickable(false);
                Bundle bundle = new Bundle();
                bundle.putString(SendFragment.UUID, mWallet.id());
                mActivity.resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.SEND.ordinal());
                mActivity.switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal(), bundle);
                mRequestButton.setClickable(true);
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isAdded()) {
                    return;
                }

                int newIdx = i - 1;
                mActivity.hideSoftKeyboard(mSendButton);
                // Make sure this is not the header view and offset i by 1
                if (i >= 0 && i < mTransactions.size() + 1) {
                    Transaction trans = mTransactions.get(newIdx);
                    mTransactionAdapter.selectItem(view, newIdx);

                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.WALLET_UUID, mWallet.id());
                    bundle.putString(Constants.WALLET_TXID, trans.id());
                    Fragment fragment = new TransactionDetailFragment();
                    fragment.setArguments(bundle);

                    mActivity.pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
                }
            }
        });
        updateBalanceBar();
        updateSendRequestButtons();
        return mView;
    }

    @Override
    public void onResume() {
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.getBoolean(Constants.WALLET_CREATE)) {
            mPositionNavBar = false;
            bundle.putBoolean(Constants.WALLET_CREATE, false);
            buildFragments(bundle);
            super.onResume();
            mPositionNavBar = true;
        } else {
            super.onResume();
        }
    }

    private void setupBalanceView() {
        if (mShowBalance) {
            mShowBalanceLayout.setVisibility(View.INVISIBLE);
            mBalanceLayout.setVisibility(View.VISIBLE);
        } else {
            mShowBalanceLayout.setVisibility(View.VISIBLE);
            mBalanceLayout.setVisibility(View.INVISIBLE);
        }
    }

    private MenuItem mToggle;

    @Override
    protected void onAddOptions(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_transaction_list, menu);
        mToggle = menu.findItem(R.id.action_toggle);
        if (null != mToggle) {
            setMenuItemTitle(mToggle);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (null != mToggle) {
            setMenuItemTitle(mToggle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isMenuExpanded()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
        case android.R.id.home:
            return onBackPress();
        case R.id.action_toggle:
            mOnBitcoinMode = !mOnBitcoinMode;
            AirbitzApplication.setBitcoinSwitchMode(mOnBitcoinMode);
            mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
            mTransactionAdapter.notifyDataSetChanged();
            setMenuItemTitle(item);
            return true;
        case R.id.action_search:
            if (!mLoading) {
                showSearch();
            }
            return true;
        case R.id.action_export:
            ExportFragment.pushFragment(mActivity);
            return true;
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.TRANSACTIONS));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void setMenuItemTitle(MenuItem item) {
        if (!mOnBitcoinMode) {
            item.setTitle(mActivity.getString(R.string.string_balance));
        } else {
            if (mWallet != null) {
                item.setTitle(mWallet.currency().code);
            } else {
                item.setTitle(mActivity.getString(R.string.string_loading));
            }
        }
    }

    @Override
    protected void onSearchQuery(String query) {
        if (mLoading) {
            return;
        }
        startTransactionTask();
    }

    @Override
    public boolean onBackPress() {
        if (super.onBackPress()) {
            return true;
        }
        return hideSearch();
    }

    @Override
    public boolean hideSearch() {
        if (super.hideSearch()) {
            if (!mLoading) {
                mTransactionAdapter.setSearch(false);
                startTransactionTask();
            }
            return true;
        }
        return false;
    }

    @Override
    public void showWalletList() {
        if (mSearching || getActivity() == null || !finishedResume()) {
            return;
        }
        mActivity.switchFragmentThread(NavigationActivity.Tabs.WALLETS.ordinal());
    }

    @Override
    public void hideWalletList() {
    }

    @Override
    protected void blockHeightUpdate() {
        if (null != mTransactionAdapter) {
            mTransactionAdapter.notifyDataSetChanged();
        }
    }

    private void buildFragments(Bundle bundle) {
        if (bundle.getString(Constants.WALLET_FROM).equals(SuccessFragment.TYPE_REQUEST)
                || bundle.getString(Constants.WALLET_FROM).equals(SuccessFragment.TYPE_SEND)) {
            Fragment details = new TransactionDetailFragment();
            details.setArguments(bundle);
            mActivity.pushFragment(details, NavigationActivity.Tabs.WALLET.ordinal());
        }
    }

    private void startTransactionTask() {
        if (mWallet == null || !mWallet.isSynced()) {
            return;
        }
        if (mTransactionTask != null) {
            mTransactionTask.cancel(false);
        }
        mTransactionAdapter.setLoading(mLoading);
        mTransactionAdapter.setWallet(mWallet);
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);

        mTransactionTask = new TransactionTask(mSearchQuery, mWallet);
        mTransactionTask.execute();
    }

    private void updateTransactionsListView(List<Transaction> transactions) {
        mTransactions.clear();
        mTransactions.addAll(transactions);
        mTransactionAdapter.createRunningSatoshi();
        mTransactionAdapter.notifyDataSetChanged();

        updateBalanceBar();
        updateSendRequestButtons();
    }

    private void updateSendRequestButtons() {
        if (mLoading) {
            updateSendRequestButtons(false, 0.5f);
            // mActivity.ShowFadingDialog(getString(R.string.wait_until_wallets_loaded));
        } else {
            updateSendRequestButtons(true, 1f);
            if (mWallet != null && mWallet.isArchived()) {
                updateSendRequestButtons(false, 0.5f);
            }
        }
    }

    private void updateSendRequestButtons(boolean enabled, float alpha) {
        mRequestButton.setClickable(enabled);
        mRequestButton.setEnabled(enabled);
        mRequestButton.setAlpha(alpha);
        mSendButton.setClickable(enabled);
        mSendButton.setEnabled(enabled);
        mSendButton.setAlpha(alpha);
    }

    protected void updateBalanceBar() {
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
        mTransactionAdapter.notifyDataSetChanged();
        updateBalances();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTransactionTask != null) {
            mTransactionTask.cancel(true);
            mTransactionTask = null;
        }
    }

    // Sum all transactions and show in total
    private void updateBalances() {
        if (mTransactions != null && mWallet != null && mWallet.isSynced()) {
            long totalSatoshis = mWallet.balance();
            setupBalanceView();
            mBitCoinBalance.setText(
                Utils.formatSatoshi(mAccount, totalSatoshis, true));
            mFiatBalance.setText(
                mWallet.currency().code + " " +
                CoreWrapper.formatCurrency(mAccount, totalSatoshis, mWallet.currency().code, true));
            mLoadingLayout.setVisibility(View.INVISIBLE);
        } else {
            mBitCoinBalance.setText("");
            mFiatBalance.setText("");
            mLoadingLayout.setVisibility(View.VISIBLE);
        }
    }

    private void toggleShowBalance() {
        if (mIsAnimating) {
            return;
        }
        mIsAnimating = true;
        mShowBalance = !mShowBalance;
        AirbitzApplication.setShowBalanceMode(mShowBalance);
        if (mShowBalance) {
            mHandler.post(animateBalanceShow);
        } else {
            mHandler.post(animateBalanceHide);
        }
    }

    @Override
    public void onExchangeRatesChange() {
        super.onExchangeRatesChange();
        if (!mLoading) {
            updateBalances();
            updateSendRequestButtons();
        }
    }

    @Override
    public void onWalletsLoaded() {
        super.onWalletsLoaded();
        if (mWallet != null) {
            updateBalances();
            updateSendRequestButtons();
            startTransactionTask();
        }
        mActivity.invalidateOptionsMenu();
    }

    @Override
    protected void walletChanged(Wallet newWallet) {
        super.walletChanged(newWallet);
        updateBalances();
        updateSendRequestButtons();
        startTransactionTask();
        mActivity.invalidateOptionsMenu();
    }

    @Override
    public void onRefresh() {
        if (mWallet != null) {
            mWallet.walletReconnect();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    class TransactionTask extends AsyncTask<Void, Integer, List<Transaction>> {
        String query;
        Wallet wallet;
        public TransactionTask(String query, Wallet wallet) {
            this.query = query;
            this.wallet = wallet;
        }

        @Override
        protected List<Transaction> doInBackground(Void... params) {
            if (TextUtils.isEmpty(query)) {
                return wallet.transactions();
            } else {
                mTransactionAdapter.setSearch(true);
                return wallet.transactionsSearch(query);
            }
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (!isAdded()) {
                return;
            }
            updateTransactionsListView(transactions);
            mTransactionTask = null;
        }

        @Override
        protected void onCancelled() {
            mTransactionTask = null;
            super.onCancelled();
        }
    }

    private void setupFooter(int layoutResd, int textRes, long bizId, View.OnClickListener listener) {
        View view = mListFooterView.findViewById(layoutResd);
        view.setOnClickListener(listener);
        view.setBackground(mActivity.getResources().getDrawable(R.drawable.wallet_list_standard));

        TextView tv = (TextView) view.findViewById(R.id.textview_name);
        tv.setText(textRes);

        final int placeholder = R.drawable.ic_request;
        final int background = R.drawable.bg_icon_request;
        final ImageView img = (ImageView) view.findViewById(R.id.imageview_contact_pic);
        img.setImageResource(R.drawable.ic_request);
        img.setBackgroundResource(background);
        if (bizId > 0) {
            String uri = "airbitz://business/" + bizId;
            Picasso picasso = AirbitzApplication.getPicasso();
            picasso.load(uri)
                    .placeholder(placeholder)
                    .transform(new RoundedTransformation(10, 0))
                    .into(img, new Callback.EmptyCallback() {
                        @Override
                        public void onSuccess() {
                            img.setBackgroundResource(android.R.color.transparent);
                        }
                        @Override
                        public void onError() {
                            img.setImageResource(placeholder);
                            img.setBackgroundResource(background);
                        }
                    });
        }
    }
}
