package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.TransactionAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.models.Image;
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment implements CoreAPI.OnExchangeRatesChange {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private ClearableEditText mSearchField;

    private ImageButton mExportButton;
    private ImageButton mSearchButton;
    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private boolean searchPage = false;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private ImageView mMoverCoin;
    private TextView mMoverType;
    private ImageView mBottomCoin;
    private TextView mBottomType;
    private TextView mTopType;
    private boolean firstTime = true;

    private RelativeLayout exportLayout;
    private LinearLayout sendRequestLayout;
    private LinearLayout mSearchLayout;

    private RelativeLayout mParentLayout;

    private ScrollView mScrollView;

    private Button mButtonBitcoinBalance;
    private Button mButtonFiatBalance;
    private Button mButtonMover;

    private RelativeLayout switchable;
    private RelativeLayout switchContainer;
    //private SeekBar mSeekBar;

    private boolean mOnBitcoinMode = true;

    private EditText mWalletNameButton;

    private ListView mListTransaction;

    private TransactionAdapter mTransactionAdapter;

    private List<Transaction> mTransactions;

    private String mWalletName;
    private Wallet mWallet;
    private CoreAPI mCoreAPI;
    private int mFiatCurrencyNum;
    private int mCurrencyIndex;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        Bundle bundle = getArguments();
        if(bundle != null){
            if(bundle.getString(WalletsFragment.FROM_SOURCE)!=null) {
                String walletUUID = bundle.getString(Wallet.WALLET_UUID);
                if(walletUUID==null || walletUUID.isEmpty()) {
                    Log.d("WalletFragement", "no detail info");
                } else {
                    mWallet = mCoreAPI.getWallet(walletUUID);
                    mWalletName = mWallet.getName();
                    mTransactions = mCoreAPI.loadTransactions(mWallet);
                    mCurrencyIndex = mCoreAPI.SettingsCurrencyIndex();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.fragment_wallet, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) parentView.findViewById(R.id.fragment_wallet_parent_layout);
        mScrollView = (ScrollView) parentView.findViewById(R.id.layout_scroll);

        mTransactionAdapter = new TransactionAdapter(getActivity(), mTransactions);
        mTransactionAdapter.setCurrencyNum(mWallet.getCurrencyNum());

        mSearchLayout = (LinearLayout) parentView.findViewById(R.id.fragment_wallet_search_layout);
        mSearchField = (ClearableEditText) parentView.findViewById(R.id.fragment_search_edittext);
        mSearchButton = (ImageButton) parentView.findViewById(R.id.fragment_wallet_search_button);

        mSendButton = (ResizableImageView) parentView.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = (ResizableImageView) parentView.findViewById(R.id.fragment_wallet_request_button);
        mWalletNameButton = (EditText) parentView.findViewById(R.id.fragment_wallet_walletname_edittext);

        mExportButton = (ImageButton) parentView.findViewById(R.id.fragment_wallet_export_button);
        mBackButton = (ImageButton) parentView.findViewById(R.id.fragment_wallet_back_button);
        mButtonMover = (Button) parentView.findViewById(R.id.button_mover);
        exportLayout = (RelativeLayout) parentView.findViewById(R.id.fragment_wallet_export_layout);
        sendRequestLayout = (LinearLayout) parentView.findViewById(R.id.fragment_wallet_sendrequest_layout);


        switchable = (RelativeLayout) parentView.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) parentView.findViewById(R.id.layout_balance);

        mMoverCoin = (ImageView) parentView.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) parentView.findViewById(R.id.button_mover_type);
        mBottomCoin = (ImageView) parentView.findViewById(R.id.bottom_coin);
        mBottomType = (TextView) parentView.findViewById(R.id.bottom_type);
        mTopType = (TextView) parentView.findViewById(R.id.top_type);

        mHelpButton = (ImageButton) parentView.findViewById(R.id.fragment_wallet_help_button);
        mTitleTextView = (TextView) parentView.findViewById(R.id.fragment_wallet_title_textview);

        mButtonBitcoinBalance = (Button) parentView.findViewById(R.id.back_button_top);
        mButtonFiatBalance = (Button) parentView.findViewById(R.id.back_button_bottom);
        mListTransaction = (ListView) parentView.findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mTransactions.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mSearchField.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mButtonBitcoinBalance.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonFiatBalance.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonMover.setTypeface(NavigationActivity.latoRegularTypeFace);

        mWalletNameButton.setText(mWalletName);

        mWalletNameButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    mWallet.setName(mWalletNameButton.getText().toString());
                    mCoreAPI.renameWallet(mWallet);
                }
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutTransition lt = new LayoutTransition();
                Animator animator = ObjectAnimator.ofFloat(null, "translationX",parentView.getWidth(), 0);
                lt.setAnimator(LayoutTransition.APPEARING,animator);
                lt.setAnimator(LayoutTransition.CHANGE_APPEARING, animator);
                lt.setStartDelay(LayoutTransition.APPEARING,0);
                lt.setStartDelay(LayoutTransition.CHANGE_APPEARING,0);
                lt.setDuration(500);
                mSearchLayout.setVisibility(View.VISIBLE);
                switchContainer.setVisibility(View.GONE);
                exportLayout.setVisibility(View.GONE);
                sendRequestLayout.setVisibility(View.GONE);
                searchPage = true;
                mTransactionAdapter.setSearch(true);
                mTransactionAdapter.notifyDataSetChanged();
            }
        });

        mButtonBitcoinBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(true);
                mListTransaction.setAdapter(mTransactionAdapter);
                mOnBitcoinMode = true;
            }
        });
        mButtonFiatBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(false);
                mListTransaction.setAdapter(mTransactionAdapter);
                mOnBitcoinMode = false;
            }
        });
        mButtonMover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListTransaction.setAdapter(mTransactionAdapter);
                if(mOnBitcoinMode){
                    switchBarInfo(false);
                    mOnBitcoinMode = false;
                }else{
                    switchBarInfo(true);
                    mOnBitcoinMode = true;
                }
            }
        });

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ExportFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment);
            }
        });
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal());
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal());
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString(Wallet.WALLET_UUID, mWallet.getUUID());
                Transaction trans = mTransactions.get(i);
                bundle.putString(Transaction.TXID, trans.getID());
                Fragment fragment = new TransactionDetailFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchPage){
                    LayoutTransition lt = new LayoutTransition();
                    Animator animator = ObjectAnimator.ofFloat(null, "translationX", 0, parentView.getWidth());
                    lt.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,animator);
                    lt.setAnimator(LayoutTransition.DISAPPEARING,animator);
                    lt.setStartDelay(LayoutTransition.DISAPPEARING, 0);
                    lt.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
                    lt.setDuration(500);
                    mSearchLayout.setVisibility(View.GONE);
                    switchContainer.setVisibility(View.VISIBLE);
                    exportLayout.setVisibility(View.VISIBLE);
                    sendRequestLayout.setVisibility(View.VISIBLE);
                    searchPage = false;
                    mTransactionAdapter.setSearch(false);
                    mSearchField.clearFocus();
                }else{
                    getActivity().onBackPressed();
                }
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Info", "Wallet info");
            }
        });

        UpdateWalletTotalBalance();
        mCoreAPI.addExchangeRateChangeListener(this);

        return parentView;
    }

    @Override public void onPause() {
        super.onPause();
        mCoreAPI.removeExchangeRateChangeListener(this);
    }

    @Override public void onResume(){
        firstTime = true;
        super.onResume();
    }

    // Sum all transactions and show in total
    private void UpdateWalletTotalBalance() {
        long totalSatoshis = 0;
        for(Transaction transaction : mTransactions) {
                totalSatoshis+=transaction.getAmountSatoshi();
        }
        mButtonBitcoinBalance.setText(mCoreAPI.getUserBTCSymbol()+" "+mCoreAPI.FormatDefaultCurrency(totalSatoshis, true, false));
        String temp = mCoreAPI.FormatCurrency(totalSatoshis, mWallet.getCurrencyNum(), false, true);
        mButtonFiatBalance.setText(temp.substring(0,temp.indexOf('.')+Math.min(3, temp.length()-temp.indexOf('.'))));
        switchBarInfo(mOnBitcoinMode);

        mBottomCoin.setImageResource(WalletsFragment.mCurrencyCoinDarkDrawables[mCurrencyIndex]);
        mBottomType.setText(mCoreAPI.getUserCurrencyAcronym());
        mTopType.setText(mCoreAPI.getUserBTCDenomination());
    }


    private List<Transaction> searchTransactions(String term) {
        return mCoreAPI.searchTransactionsIn(mWallet, term);
    }

    private void switchBarInfo(boolean isBitcoin){
        if(isBitcoin) {
            if(!firstTime) {
                Animator animator = ObjectAnimator.ofFloat(switchable, "translationY", (getActivity().getResources().getDimension(R.dimen.currency_switch_height)), 0);
                animator.setDuration(250);
                animator.start();
            }else{
                firstTime = false;
            }
            mButtonMover.setText(mButtonBitcoinBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setText(mTopType.getText());
        }else{
            Animator animator = ObjectAnimator.ofFloat(switchable,"translationY",0,(getActivity().getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(250);
            animator.start();
            mButtonMover.setText(mButtonFiatBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_usd_white);//TODO
            mMoverType.setText(mBottomType.getText());
        }
        mTransactionAdapter.setIsBitcoin(isBitcoin);
        mTransactionAdapter.notifyDataSetChanged();
    }

    @Override
    public void OnExchangeRatesChange() {
        UpdateWalletTotalBalance();
    }
}
