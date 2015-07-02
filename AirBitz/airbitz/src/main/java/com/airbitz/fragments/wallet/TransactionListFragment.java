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
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.fragments.BaseFragment;
import com.airbitz.fragments.HelpFragment;
import com.airbitz.fragments.request.RequestFragment;
import com.airbitz.fragments.send.SendFragment;
import com.airbitz.fragments.send.SuccessFragment;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionListFragment extends WalletsFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = getClass().getSimpleName();

    private MenuItem mSearchItem;
    private SearchView mSearchView;
    private boolean mSearchPage = false;
    private View mRequestButton;
    private View mSendButton;
    private ImageView mMoverCoin;
    private TextView mMoverType;
    private TextView mBottomType;
    private TextView mTopType;
    private Button mBitCoinBalanceButton;
    private Button mFiatBalanceButton;
    private Button mButtonMover;
    private RelativeLayout mBalanceSwitchLayout;
    private RelativeLayout mSwitchView;
    private SwipeRefreshLayout mSwipeLayout;

    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animator) {
            updateBalanceBar();
        }

        @Override
        public void onAnimationCancel(Animator animator) { }

        @Override
        public void onAnimationStart(Animator animator) { }

        @Override
        public void onAnimationRepeat(Animator animator) { }
    };
    Runnable animateSwitchUp = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY",
                    (mActivity.getResources().getDimension(R.dimen.currency_switch_height)), 0);
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };
    Runnable animateSwitchDown = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY", 0,
                    (mActivity.getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };

    private ListView mListTransaction;
    private ViewGroup mListHeaderView;
    private TransactionAdapter mTransactionAdapter;
    private List<Transaction> mTransactions = new ArrayList<Transaction>();
    private List<Transaction> mAllTransactions = new ArrayList<Transaction>();
    private LinkedHashMap<String, Uri> mCombinedPhotos;
    private View mView;
    private TransactionTask mTransactionTask;
    private SearchTask mSearchTask;
    private Handler mHandler = new Handler();
    ExecutorService mExecutor = Executors.newFixedThreadPool(100);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null && bundle.getBoolean(WalletsFragment.CREATE)) {
            bundle.putBoolean(WalletsFragment.CREATE, false);
            buildFragments(bundle);
        } else {
            Log.d(TAG, "onCreateView stopping in Wallets");
        }

        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_transaction_list, container, false);
            mSearchPage = false;
        }

        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.fragment_wallet_swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        mCombinedPhotos = Common.GetMatchedContactsList(mActivity, null);

        mTransactionAdapter = new TransactionAdapter(mActivity, mTransactions, mCombinedPhotos);
        mTransactionAdapter.setLoading(true);
        mListTransaction = (ListView) mView.findViewById(R.id.listview_transaction);
        if (mListHeaderView == null) {
            mListHeaderView = (ViewGroup) inflater.inflate(R.layout.custom_transaction_listview_header, null, false);
            mListTransaction.addHeaderView(mListHeaderView, null, false);
        }
        mListTransaction.setAdapter(mTransactionAdapter);

        mSendButton = mListHeaderView.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = mListHeaderView.findViewById(R.id.fragment_wallet_request_button);
        mBitCoinBalanceButton = (Button) mListHeaderView.findViewById(R.id.back_button_top);
        mFiatBalanceButton = (Button) mListHeaderView.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) mListHeaderView.findViewById(R.id.button_mover);
        mBalanceSwitchLayout = (RelativeLayout) mListHeaderView.findViewById(R.id.switchable);
        mSwitchView = (RelativeLayout) mListHeaderView.findViewById(R.id.layout_balance);
        mMoverCoin = (ImageView) mListHeaderView.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) mListHeaderView.findViewById(R.id.button_mover_type);
        mBottomType = (TextView) mListHeaderView.findViewById(R.id.bottom_type);
        mTopType = (TextView) mListHeaderView.findViewById(R.id.top_type);

        mBitCoinBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnBitcoinMode = true;
                animateBar();
            }
        });

        mFiatBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnBitcoinMode = false;
                animateBar();
            }
        });

        mButtonMover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnBitcoinMode = !mOnBitcoinMode;
                animateBar();
            }
        });

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mSendButton);
                mSendButton.setClickable(false);
                Bundle bundle = new Bundle();
                bundle.putString(RequestFragment.FROM_UUID, mWallet.getUUID());
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
                bundle.putString(SendFragment.UUID, mWallet.getUUID());
                mActivity.resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.SEND.ordinal());
                mActivity.switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal(), bundle);
                mRequestButton.setClickable(true);
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!isAdded()) {
                    return;
                }

                int newIdx = i - 1;
                // if (mSearchLayout.getVisibility() == View.VISIBLE) {
                //     newIdx = i;
                // }

                mActivity.hideSoftKeyboard(mSendButton);
                // Make sure this is not the header view and offset i by 1
                if (i >= 0) {
                    Transaction trans = mTransactions.get(newIdx);
                    mTransactionAdapter.selectItem(view, newIdx);

                    Bundle bundle = new Bundle();
                    bundle.putString(Wallet.WALLET_UUID, mWallet.getUUID());
                    bundle.putString(Transaction.TXID, trans.getID());
                    Fragment fragment = new TransactionDetailFragment();
                    fragment.setArguments(bundle);

                    mActivity.pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
                }
            }
        });
        positionBalanceBar();
        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isMenuExpanded()) {
            super.onCreateOptionsMenu(menu, inflater);
            return;
        }
        inflater.inflate(R.menu.menu_transaction_list, menu);

        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String text) {
                try {
                    if (mSearchTask != null && mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mSearchTask.cancel(true);
                    }
                    if (TextUtils.isEmpty(text)) {
                        updateTransactionsListView(mAllTransactions);
                    } else {
                        mSearchTask = new SearchTask();
                        mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, text);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                hideTitleView();
                mSearchPage = true;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            public boolean onClose() {
                hideSearch();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isMenuExpanded()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
        case android.R.id.home:
            return hideSearch();
        case R.id.action_help:
            mActivity.pushFragment(new HelpFragment(HelpFragment.TRANSACTIONS));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBackPress() {
        if (super.onBackPress()) {
            return true;
        }
        return hideSearch();
    }

    private void buildFragments(Bundle bundle) {
        if (bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_REQUEST)
                || bundle.getString(WalletsFragment.FROM_SOURCE).equals(SuccessFragment.TYPE_SEND)) {
            Fragment details = new TransactionDetailFragment();
            details.setArguments(bundle);
            mActivity.pushFragment(details, NavigationActivity.Tabs.WALLET.ordinal());
        }
    }

    private boolean hideSearch() {
        if (mSearchPage) {
            mSearchPage = false;
            showTitleView();

            mTransactionAdapter.setSearch(false);
            startTransactionTask();
            mSearchView.onActionViewCollapsed();
            return true;
        }
        return false;
    }

    private void startTransactionTask() {
        if (mTransactionTask != null) {
            mTransactionTask.cancel(false);
        }
        mTransactionAdapter.setLoading(mLoading);
        mTransactionAdapter.setWallet(mWallet);
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);

        mRequestButton.setClickable(false);
        mSendButton.setClickable(false);

        mTransactionTask = new TransactionTask();
        mTransactionTask.execute(mWallet);
    }

    private void updateTransactionsListView(List<Transaction> transactions) {
        mTransactions.clear();
        mTransactions.addAll(transactions);
        mTransactionAdapter.createRunningSatoshi();
        mTransactionAdapter.notifyDataSetChanged();

        findBizIdThumbnails();
        updateBalanceBar();
        updateSendRequestButtons();
    }

    private void updateSendRequestButtons(boolean enabled, float alpha) {
        mRequestButton.setClickable(enabled);
        mRequestButton.setEnabled(enabled);
        mRequestButton.setAlpha(alpha);
        mSendButton.setClickable(enabled);
        mSendButton.setEnabled(enabled);
        mSendButton.setAlpha(alpha);
    }

    private void updateSendRequestButtons() {
        if (mCoreApi.walletsStillLoading()) {
            updateSendRequestButtons(false, 0.5f);
            mActivity.ShowFadingDialog(getString(R.string.wait_until_wallets_loaded));
        } else {
            updateSendRequestButtons(true, 1f);
            if (mWallet != null && mWallet.isArchived()) {
                updateSendRequestButtons(false, 0.5f);
            }
        }
    }

    private void positionBalanceBar() {
        mOnBitcoinMode = AirbitzApplication.getBitcoinSwitchMode();
        if (!mOnBitcoinMode) {
            mBalanceSwitchLayout.setY(mBitCoinBalanceButton.getY() + mActivity.getResources().getDimension(R.dimen.currency_switch_height));
        }
        else {
            mBalanceSwitchLayout.setY(mBitCoinBalanceButton.getY());
        }
    }

    private void updateBalanceBar() {
        positionBalanceBar();
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
        mTransactionAdapter.notifyDataSetChanged();
        updateBalances();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mTransactionTask!=null) {
            mTransactionTask.cancel(true);
            mTransactionTask = null;
        }
    }

    // Sum all transactions and show in total
    private void updateBalances() {
        if (mWallet != null) {
            long totalSatoshis = mWallet.getBalanceSatoshi();

            mBottomType.setText(mCoreApi.currencyCodeLookup(mWallet.getCurrencyNum()));
            mTopType.setText(mCoreApi.getDefaultBTCDenomination());
            mBitCoinBalanceButton.setText(mCoreApi.formatSatoshi(totalSatoshis, true));
            String temp = mCoreApi.FormatCurrency(totalSatoshis, mWallet.getCurrencyNum(), false, true);
            mFiatBalanceButton.setText(temp);

            if (mOnBitcoinMode) {
                mButtonMover.setText(mBitCoinBalanceButton.getText());
                mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
                mMoverType.setText(mTopType.getText());
            } else {
                mButtonMover.setText(mFiatBalanceButton.getText());
                mMoverCoin.setImageResource(0);
                mMoverType.setText(mBottomType.getText());
            }
        } else {
            mBottomType.setText("");
            mTopType.setText("");
            mBitCoinBalanceButton.setText("");
            mFiatBalanceButton.setText("");
            mButtonMover.setText("");
            mMoverType.setText("");
        }
    }

    private List<Transaction> searchTransactions(String term) {
        return mCoreApi.searchTransactionsIn(mWallet, term);
    }

    private void animateBar() {
        AirbitzApplication.setBitcoinSwitchMode(mOnBitcoinMode);
        if (mOnBitcoinMode) {
            mHandler.post(animateSwitchUp);
        } else {
            mHandler.post(animateSwitchDown);
        }
        updateBalances();
    }

    @Override
    public void onExchangeRatesChange() {
        super.onExchangeRatesChange();
        if (!mLoading) {
            updateBalances();
        }
    }

    @Override
    public void onWalletUpdated() {
        super.onWalletUpdated();
        if (mWallet != null) {
            mCoreApi.reloadWallet(mWallet);
            startTransactionTask();
        }
    }

    @Override
    public void onWalletsLoaded() {
        super.onWalletsLoaded();
        startTransactionTask();
    }

    @Override
    protected void walletChanged(Wallet newWallet) {
        super.walletChanged(newWallet);
        startTransactionTask();
    }

    @Override
    public void onRefresh() {
        if (mWallet != null) {
            mCoreApi.connectWatcher(mWallet.getUUID());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    private void findBizIdThumbnails() {
        for (Transaction transaction : mTransactions) {
            if (!mCombinedPhotos.containsKey(transaction.getName()) && transaction.getmBizId() != 0) {
                GetBizIdThumbnailAsyncTask task = new GetBizIdThumbnailAsyncTask(transaction.getName(), transaction.getmBizId());
                task.executeOnExecutor(mExecutor);
            }
        }
    }

    class TransactionTask extends AsyncTask<Wallet, Integer, List<Transaction>> {

        public TransactionTask() {
        }

        @Override
        protected List<Transaction> doInBackground(Wallet... wallet) {
            return mCoreApi.loadAllTransactions(wallet[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (!isAdded()) {
                return;
            }
            mAllTransactions = transactions;
            updateTransactionsListView(transactions);

            mTransactionTask = null;
        }

        @Override
        protected void onCancelled() {
            mTransactionTask = null;
            super.onCancelled();
        }
    }

    class SearchTask extends AsyncTask<String, Integer, List<Transaction>> {

        public SearchTask() {
        }

        @Override
        protected List<Transaction> doInBackground(String... strings) {
            return searchTransactions(strings[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (!isAdded()) {
                return;
            }
            updateTransactionsListView(transactions);
            mSearchTask = null;
        }

        @Override
        protected void onCancelled() {
            mSearchTask = null;
            super.onCancelled();
        }
    }

    class GetBizIdThumbnailAsyncTask extends AsyncTask<Void, Void, BusinessDetail> {
        private AirbitzAPI api = AirbitzAPI.getApi();
        private String mName;
        private long mBizId;

        GetBizIdThumbnailAsyncTask(String name, long id) {
            mName = name;
            mBizId = id;
        }

        @Override
        protected BusinessDetail doInBackground(Void... voids) {
            return api.getHttpBusiness((int) mBizId);
        }

        @Override
        protected void onPostExecute(BusinessDetail business) {
            if (business != null && business.getSquareImageLink() != null) {
                Uri uri = Uri.parse(business.getSquareImageLink());
                Log.d(TAG, "Got " + uri);
                mCombinedPhotos.put(mName, uri);
                mTransactionAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
