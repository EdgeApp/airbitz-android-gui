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
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.api.AirbitzAPI;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.BusinessDetail;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.utils.Common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WalletFragment extends BaseFragment
        implements CoreAPI.OnExchangeRatesChange,
        NavigationActivity.OnWalletUpdated,
        SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = getClass().getSimpleName();
    private EditText mSearchField;
    private LinearLayout mSearchLayout;
    private HighlightOnPressImageButton mExportButton;
    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mSearchButton;
    private boolean searchPage = false;
    private LinearLayout mRequestButton;
    private LinearLayout mSendButton;
    private TextView mTitleTextView;
    private int SEARCH_ANIMATION_DURATION = 350;
    private float mSearchBarHeight;
    private float mListViewY;
    private ImageView mMoverCoin;
    private TextView mMoverType;
    private TextView mBottomType;
    private TextView mTopType;
    private RelativeLayout mHeaderView;
    private RelativeLayout mParentLayout;
    private Button mBitCoinBalanceButton;
    private Button mFiatBalanceButton;
    private Button mButtonMover;
    private RelativeLayout mBalanceSwitchLayout;
    private RelativeLayout mSwitchView;
    private SwipeRefreshLayout mSwipeLayout;
    private boolean mOnBitcoinMode = true;
    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationEnd(Animator animator) {
            updateBalanceBar();
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {
        }
    };
    Runnable animateSwitchUp = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY", (mActivity.getResources().getDimension(R.dimen.currency_switch_height)), 0);
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };
    Runnable animateSwitchDown = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY", 0, (mActivity.getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };
    private EditText mWalletNameEditText;
    private ListView mListTransaction;
    private ViewGroup mListHeaderView;
    private TransactionAdapter mTransactionAdapter;
    private List<Transaction> mTransactions;
    private List<Transaction> mAllTransactions;
    private LinkedHashMap<String, Uri> mCombinedPhotos;
    private Wallet mWallet;
    private CoreAPI mCoreAPI;
    private View mView;
    private TransactionTask mTransactionTask;
    private SearchTask mSearchTask;
    private Handler mHandler = new Handler();
    private NavigationActivity mActivity;
    ExecutorService mExecutor = Executors.newFixedThreadPool(100);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mActivity = (NavigationActivity) getActivity();

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.getString(WalletsFragment.FROM_SOURCE) != null) {
                String walletUUID = bundle.getString(Wallet.WALLET_UUID);
                if (walletUUID == null || walletUUID.isEmpty()) {
                    Log.d(TAG, "no detail info");
                } else {
                    mWallet = mCoreAPI.getWalletFromUUID(walletUUID);
                }
                if (mTransactions == null) {
                    mTransactions = new ArrayList<Transaction>();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_wallet, container, false);
            searchPage = false;
        }

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.fragment_wallet_parent_layout);
        mSwipeLayout = (SwipeRefreshLayout) mView.findViewById(R.id.fragment_wallet_swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        mAllTransactions = new ArrayList<Transaction>();
        mAllTransactions.addAll(mTransactions);

        mCombinedPhotos = Common.GetMatchedContactsList(mActivity, null);
        mTransactionAdapter = new TransactionAdapter(mActivity, mWallet, mTransactions, mCombinedPhotos);

        mSearchField = (EditText) mView.findViewById(R.id.fragment_search_edittext);
        mSearchButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_search_button);
        mSearchLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallet_search_layout);

        mWalletNameEditText = (EditText) mView.findViewById(R.id.fragment_wallet_walletname_edittext);

        mExportButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_export_button);


        mButtonMover = (Button) mView.findViewById(R.id.button_mover);
        mHeaderView = (RelativeLayout) mView.findViewById(R.id.fragment_wallet_export_layout);

        mBalanceSwitchLayout = (RelativeLayout) mView.findViewById(R.id.switchable);
        mSwitchView = (RelativeLayout) mView.findViewById(R.id.layout_balance);

        mMoverCoin = (ImageView) mView.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) mView.findViewById(R.id.button_mover_type);
        mBottomType = (TextView) mView.findViewById(R.id.bottom_type);
        mTopType = (TextView) mView.findViewById(R.id.top_type);

        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_back);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchPage) {
                    SetSearchVisibility(false);
                    mTransactionAdapter.setSearch(false);
                    startTransactionTask();
                } else {
                    mActivity.onBackPressed();
                }
            }
        });

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.layout_title_header_button_help);
        mHelpButton.setVisibility(View.VISIBLE);
        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.pushFragment(new HelpFragment(HelpFragment.TRANSACTIONS), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mTitleTextView = (TextView) mView.findViewById(R.id.layout_title_header_textview_title);
        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mTitleTextView.setText(R.string.fragment_wallet_title);

        mBitCoinBalanceButton = (Button) mView.findViewById(R.id.back_button_top);
        mFiatBalanceButton = (Button) mView.findViewById(R.id.back_button_bottom);

        mListTransaction = (ListView) mView.findViewById(R.id.listview_transaction);
        if (mListHeaderView == null) {
            mListHeaderView = (ViewGroup) inflater.inflate(R.layout.custom_req_send_buttons, null, false);
            mListTransaction.addHeaderView(mListHeaderView, null, false);
        }
        mSendButton = (LinearLayout) mListHeaderView.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = (LinearLayout) mListHeaderView.findViewById(R.id.fragment_wallet_request_button);
        mListTransaction.setAdapter(mTransactionAdapter);

        mWalletNameEditText.setTypeface(NavigationActivity.latoBlackTypeFace);
        mSearchField.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mFiatBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonMover.setTypeface(NavigationActivity.latoRegularTypeFace, Typeface.BOLD);

        mWalletNameEditText.setText(mWallet.getName());
        mWalletNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mWalletNameEditText);
                } else {
                    if (!Common.isBadWalletName(mWalletNameEditText.getText().toString())) {
                        mWallet.setName(mWalletNameEditText.getText().toString());
                        mCoreAPI.renameWallet(mWallet);
                    }
                }
            }
        });

        mWalletNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (Common.isBadWalletName(mWalletNameEditText.getText().toString())) {
                        Common.alertBadWalletName(mActivity);
                        return false;
                    } else {
                        mActivity.hideSoftKeyboard(mWalletNameEditText);
                        return true;
                    }
                }
                return false;
            }
        });

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mActivity.showSoftKeyboard(mSearchField);
                }
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetSearchVisibility(true);
            }
        });

        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (mSearchLayout.getVisibility() == View.GONE) {
                    return;
                }

                try {
                    // Only include cached searches if text is empty.
                    if (mSearchTask != null && mSearchTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mSearchTask.cancel(true);
                    }
                    if (editable.toString().isEmpty()) {
                        updateTransactionsListView(mAllTransactions);
                    } else {
                        mSearchTask = new SearchTask();
                        mSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editable.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mActivity.hideSoftKeyboard(mSearchField);
                    return true;
                }
                return false;
            }
        });

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
        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.hideSoftKeyboard(mExportButton);
                Fragment fragment = new ExportFragment();
                Bundle bundle = new Bundle();
                bundle.putString(RequestFragment.FROM_UUID, mWallet.getUUID());
                fragment.setArguments(bundle);
                mActivity.pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
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

                mActivity.hideSoftKeyboard(mSendButton);
                // Make sure this is not the header view and offset i by 1
                if (i > 0) {
                    int newIdx = i - 1;
                    Transaction trans = mTransactions.get(newIdx);
                    mTransactionAdapter.selectItem(view, newIdx);

                    Bundle bundle = new Bundle();
                    bundle.putString(Wallet.WALLET_UUID, mWallet.getUUID());
                    bundle.putString(Transaction.TXID, trans.getID());
                    if (trans.getAmountSatoshi() < 0) {
                        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_SEND);
                    } else {
                        bundle.putString(WalletsFragment.FROM_SOURCE, SuccessFragment.TYPE_REQUEST);
                    }

                    Fragment fragment = new TransactionDetailFragment();
                    fragment.setArguments(bundle);
                    mActivity.pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
                }
            }
        });


        mActivity.hideSoftKeyboard(mSendButton);
        return mView;
    }

    private void SetSearchVisibility(boolean visible) {
        if (visible) {
            mSearchLayout.setX(mParentLayout.getWidth());
            mSearchLayout.setVisibility(View.VISIBLE);
            if(mSearchBarHeight == 0) {
                mSearchBarHeight = mSearchLayout.getHeight();
                mListViewY = mListTransaction.getY();
            }
            mActivity.hideSoftKeyboard(mSearchField);
            mSwitchView.setVisibility(View.GONE);
            mListTransaction.removeHeaderView(mListHeaderView);
            mHeaderView.setVisibility(View.INVISIBLE);

            mSearchLayout.animate().translationX(0).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchLayout.setVisibility(View.VISIBLE);
                }
            });
            mSearchField.requestFocus();
        } else {
            mActivity.hideSoftKeyboard(mSearchField);
            mSwitchView.setVisibility(View.VISIBLE);
            mListTransaction.addHeaderView(mListHeaderView);
            mHeaderView.setVisibility(View.VISIBLE);
            mSearchLayout.animate().translationX(mParentLayout.getWidth()).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchLayout.setVisibility(View.GONE);
                }
            });
        }
        searchPage = visible;
        mTransactionAdapter.setSearch(visible);
        mTransactionAdapter.notifyDataSetChanged();
    }

    private void startTransactionTask() {
        if (mTransactionTask != null) {
            mTransactionTask.cancel(false);
        }
        mTransactionTask = new TransactionTask();
        mTransactionTask.execute(mWallet);
    }

    private void updateTransactionsListView(List<Transaction> transactions) {
        mTransactions.clear();
        mTransactions.addAll(transactions);
        mTransactionAdapter.createRunningSatoshi();
        mTransactionAdapter.notifyDataSetChanged();
        FindBizIdThumbnails();
    }

    Runnable startTxListUpdate = new Runnable() {
        @Override
        public void run() {
            updateTransactionsListView(new ArrayList<Transaction>(mTransactions));
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mCoreAPI.addExchangeRateChangeListener(this);
        mActivity.setOnWalletUpdated(this);

        mWallet = mCoreAPI.getWalletFromUUID(mWallet.getUUID());
        if (!mTransactions.isEmpty()) {
            mHandler.post(startTxListUpdate);
        }
        startTransactionTask();

        updateBalanceBar();
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
        mRequestButton.setPressed(false);
        mSendButton.setPressed(false);
    }

    private void updateBalanceBar() {
        mOnBitcoinMode = AirbitzApplication.getBitcoinSwitchMode();
        if(!mOnBitcoinMode) {
            mBalanceSwitchLayout.setY(mBitCoinBalanceButton.getY() + mActivity.getResources().getDimension(R.dimen.currency_switch_height));
        }
        else {
            mBalanceSwitchLayout.setY(mBitCoinBalanceButton.getY());
        }
        mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
        mTransactionAdapter.notifyDataSetChanged();
        UpdateBalances();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mTransactionTask!=null) {
            mTransactionTask.cancel(true);
            mTransactionTask = null;
        }
        mCoreAPI.removeExchangeRateChangeListener(this);
        mActivity.setOnWalletUpdated(null);
    }

    // Sum all transactions and show in total
    private void UpdateBalances() {
        long totalSatoshis = mWallet.getBalanceSatoshi();

        mBottomType.setText((mCoreAPI.getCurrencyAcronyms())[mCoreAPI.CurrencyIndex(mWallet.getCurrencyNum())]);
        mTopType.setText(mCoreAPI.getDefaultBTCDenomination());
        mBitCoinBalanceButton.setText(mCoreAPI.formatSatoshi(totalSatoshis, true));
        String temp = mCoreAPI.FormatCurrency(totalSatoshis, mWallet.getCurrencyNum(), false, true);
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
    }

    private List<Transaction> searchTransactions(String term) {
        return mCoreAPI.searchTransactionsIn(mWallet, term);
    }

    private void animateBar() {
        AirbitzApplication.setBitcoinSwitchMode(mOnBitcoinMode);
        if (mOnBitcoinMode) {
            mHandler.post(animateSwitchUp);
        } else {
            mHandler.post(animateSwitchDown);
        }
        UpdateBalances();
    }

    @Override
    public void OnExchangeRatesChange() {
        UpdateBalances();
    }

    @Override
    public void onWalletUpdated() {
        if (mWallet != null) {
            Log.d(TAG, "Reloading wallet");
            mCoreAPI.reloadWallet(mWallet);
            mWalletNameEditText.setText(mWallet.getName());

            startTransactionTask();
        }
    }

    @Override public void onRefresh() {
        if(mWallet != null) {
            mCoreAPI.connectWatcher(mWallet.getUUID());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(false);
                }
            }, 1000);
        }
    }

    private void FindBizIdThumbnails() {
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
        protected void onPreExecute() {
            if (mTransactions.isEmpty()) {
                mActivity.showModalProgress(true);
            }
        }

        @Override
        protected List<Transaction> doInBackground(Wallet... wallet) {
            return mCoreAPI.loadAllTransactions(wallet[0]);
        }

        @Override
        protected void onPostExecute(List<Transaction> transactions) {
            if (!isAdded()) {
                return;
            }
            updateTransactionsListView(transactions);

            mTransactionTask = null;
            mActivity.showModalProgress(false);
        }

        @Override
        protected void onCancelled() {
            mTransactionTask = null;
            super.onCancelled();
            mActivity.showModalProgress(false);
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
