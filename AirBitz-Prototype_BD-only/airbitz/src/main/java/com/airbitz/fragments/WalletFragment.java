package com.airbitz.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.models.AccountTransaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.ClearableEditText;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private ClearableEditText mSearchField;

    private ImageButton mExportButton;
    private ImageButton mHelpButton;
    private ImageButton mBackButton;

    private boolean searchPage = false;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private ImageView mMoverCoin;
    private ImageView mMoverType;
    private ImageView mBottomCoin;
    private ImageView mBottomType;

    private RelativeLayout exportLayout;
    private LinearLayout sendRequestLayout;

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

    private List<AccountTransaction> mAccountTransactions;

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
                    mAccountTransactions = mCoreAPI.loadTransactions(mWallet);
                    tABC_AccountSettings settings = mCoreAPI.loadAccountSettings();
                    mFiatCurrencyNum = settings.getCurrencyNum();
                    int[] currencyNumbers = mCoreAPI.getCurrencyNumbers();
                    mCurrencyIndex = -1;
                    for(int i=0; i<currencyNumbers.length; i++) {
                        if(currencyNumbers[i] == mFiatCurrencyNum)
                            mCurrencyIndex = i;
                    }
                    if((mCurrencyIndex==-1) || (mCurrencyIndex > WalletsFragment.mCurrencyCoinDarkDrawables.length)) { // default usd
                        Log.d("WalletFragment", "currency index out of bounds "+mCurrencyIndex);
                        mCurrencyIndex = currencyNumbers.length - 1;
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.layout_parent);
        mScrollView = (ScrollView) view.findViewById(R.id.layout_scroll);

        mTransactionAdapter = new TransactionAdapter(getActivity(), mAccountTransactions);

        mSearchField = (ClearableEditText) view.findViewById(R.id.edittext_search);

        mSendButton = (ResizableImageView) view.findViewById(R.id.button_send);
        mRequestButton = (ResizableImageView) view.findViewById(R.id.button_request);
        mWalletNameButton = (EditText) view.findViewById(R.id.button_balance);

        mExportButton = (ImageButton) view.findViewById(R.id.button_export);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);
       // mSeekBar = (SeekBar) view.findViewById(R.id.seekbar_slider);
        //mSeekBar.setOnSeekBarChangeListener(this);

        exportLayout = (RelativeLayout) view.findViewById(R.id.layout_export);
        sendRequestLayout = (LinearLayout) view.findViewById(R.id.layout_send_request);


        switchable = (RelativeLayout) view.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) view.findViewById(R.id.layout_balance);

        mMoverCoin = (ImageView) view.findViewById(R.id.button_mover_coin);
        mMoverType = (ImageView) view.findViewById(R.id.button_mover_type);
        mBottomCoin = (ImageView) view.findViewById(R.id.bottom_coin);
        mBottomType = (ImageView) view.findViewById(R.id.bottom_type);

        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mButtonBitcoinBalance = (Button) view.findViewById(R.id.back_button_top);
        mButtonFiatBalance = (Button) view.findViewById(R.id.back_button_bottom);
        mListTransaction = (ListView) view.findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mAccountTransactions.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);

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

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {
                    switchContainer.setVisibility(View.GONE);
                    exportLayout.setVisibility(View.GONE);
                    sendRequestLayout.setVisibility(View.GONE);
                    searchPage = true;
                    mTransactionAdapter.setSearch(searchPage);
                    mTransactionAdapter.notifyDataSetChanged();
                }
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
                AccountTransaction trans = mAccountTransactions.get(i);
                bundle.putString(AccountTransaction.TXID, trans.getID());
                Fragment fragment = new TransactionDetailFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment);
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchPage){
                    switchContainer.setVisibility(View.VISIBLE);
                    exportLayout.setVisibility(View.VISIBLE);
                    sendRequestLayout.setVisibility(View.VISIBLE);
                    searchPage = false;
                    mTransactionAdapter.setSearch(searchPage);
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

        return view;
    }

    // Sum all wallets except for archived and show in total
    private void UpdateWalletTotalBalance() {
        long totalSatoshis = 0;
        for(AccountTransaction transaction : mAccountTransactions) {
                totalSatoshis+=transaction.getAmountSatoshi();
        }
        mButtonBitcoinBalance.setText(mCoreAPI.formatSatoshi(totalSatoshis));
        mButtonFiatBalance.setText(mCoreAPI.conversion(totalSatoshis, false));
        switchBarInfo(mOnBitcoinMode);

        mBottomCoin.setImageResource(WalletsFragment.mCurrencyCoinDarkDrawables[mCurrencyIndex]);
        mBottomType.setImageResource(WalletsFragment.mCurrencyTypeDarkDrawables[mCurrencyIndex]);
    }


    private List<AccountTransaction> searchTransactions(String term) {
        return mCoreAPI.searchTransactionsIn(mWallet, term);
    }

    private void switchBarInfo(boolean isBitcoin){
        RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(switchContainer.getWidth(), switchContainer.getHeight()/2);
        if(isBitcoin) {
            rLP.addRule(RelativeLayout.ABOVE, R.id.bottom_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mButtonBitcoinBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setImageResource(R.drawable.ico_btc_white);
            for(AccountTransaction trans: mAccountTransactions){
                try {
                    trans.setAmountFiat(mCoreAPI.SatoshiToCurrency(trans.getBalance(), mWallet.getCurrencyNum()));
                } catch (Exception e) {
                    trans.setAmountFiat(0);
                    e.printStackTrace();
                }
            }
            mTransactionAdapter.notifyDataSetChanged();
        }else{
            rLP.addRule(RelativeLayout.BELOW, R.id.top_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mButtonFiatBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_usd_white);
            mMoverType.setImageResource(R.drawable.ico_usd_white);
            double conv = 0.1145;
            for(AccountTransaction trans: mAccountTransactions){
                try {
                    trans.setAmountFiat(mCoreAPI.SatoshiToCurrency(trans.getBalance(), mWallet.getCurrencyNum()));
                } catch (Exception e) {
                    trans.setAmountFiat(0);
                    e.printStackTrace();
                }
            }
        }
    }
}
