package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.airbitz.models.Transaction;
import com.airbitz.models.Wallet;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment implements CoreAPI.OnExchangeRatesChange {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private EditText mSearchField;
    private LinearLayout mSearchLayout;

    private ImageButton mExportButton;
    private ImageButton mHelpButton;
    private ImageButton mBackButton;
    private ImageButton mSearchButton;

    private boolean searchPage = false;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private int SEARCH_ANIMATION_DURATION = 500;

    private ImageView mMoverCoin;
    private TextView mMoverType;
    private ImageView mBottomCoin;
    private TextView mBottomType;
    private TextView mTopType;

    private View mDummyFocus;

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

    private List<Transaction> mTransactions;
    private List<Transaction> mTotalTransactions;

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
                    Log.d("WalletFragment", "no detail info");
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
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        mParentLayout = (RelativeLayout) view.findViewById(R.id.fragment_wallet_parent_layout);
        mScrollView = (ScrollView) view.findViewById(R.id.fragment_wallet_scrollview);

        mTotalTransactions = new ArrayList<Transaction>();
        mTotalTransactions.addAll(mTransactions);

        mTransactionAdapter = new TransactionAdapter(getActivity(), mTransactions);
        mTransactionAdapter.setCurrencyNum(mWallet.getCurrencyNum());

        mSearchField = (EditText) view.findViewById(R.id.fragment_search_edittext);
        mSearchButton = (ImageButton) view.findViewById(R.id.fragment_wallet_search_button);
        mSearchLayout = (LinearLayout) view.findViewById(R.id.fragment_wallet_search_layout);

        mSendButton = (ResizableImageView) view.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = (ResizableImageView) view.findViewById(R.id.fragment_wallet_request_button);
        mWalletNameButton = (EditText) view.findViewById(R.id.fragment_wallet_walletname_edittext);

        mExportButton = (ImageButton) view.findViewById(R.id.fragment_wallet_export_button);
        mBackButton = (ImageButton) view.findViewById(R.id.fragment_wallet_back_button);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);
        exportLayout = (RelativeLayout) view.findViewById(R.id.fragment_wallet_export_layout);
        sendRequestLayout = (LinearLayout) view.findViewById(R.id.fragment_wallet_sendrequest_layout);

        mDummyFocus = view.findViewById(R.id.fragment_wallet_dummy_focus);

        switchable = (RelativeLayout) view.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) view.findViewById(R.id.layout_balance);

        mMoverCoin = (ImageView) view.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) view.findViewById(R.id.button_mover_type);
        mBottomCoin = (ImageView) view.findViewById(R.id.bottom_coin);
        mBottomType = (TextView) view.findViewById(R.id.bottom_type);
        mTopType = (TextView) view.findViewById(R.id.top_type);

        mHelpButton = (ImageButton) view.findViewById(R.id.fragment_wallet_help_button);
        mTitleTextView = (TextView) view.findViewById(R.id.fragment_wallet_title_textview);

        mButtonBitcoinBalance = (Button) view.findViewById(R.id.back_button_top);
        mButtonFiatBalance = (Button) view.findViewById(R.id.back_button_bottom);
        mListTransaction = (ListView) view.findViewById(R.id.listview_transaction);
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
                if(hasFocus){
                    final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() <= 100) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
                }else {
                    mWallet.setName(mWalletNameButton.getText().toString());
                    mCoreAPI.renameWallet(mWallet);
                }
            }
        });

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus){
                    final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() <= 100) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
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
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override public void afterTextChanged(Editable editable) {

                if (mSearchLayout.getVisibility() == View.GONE) {
                    return;
                }

                try {
                    // Only include cached searches if text is empty.
                    if(mSearchTask != null && mSearchTask.getStatus()== AsyncTask.Status.RUNNING){
                        mSearchTask.cancel(true);
                    }
                    if(editable.toString().isEmpty()){
                        mTransactions.clear();
                        mTransactions.addAll(mTotalTransactions);
                        mTransactionAdapter.notifyDataSetChanged();
                    }else {
                        mSearchTask = new SearchTask();
                        mSearchTask.execute(editable.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mSearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mWalletNameButton.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if(mWalletNameButton.getText().toString().trim().isEmpty()){
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),R.style.AlertDialogCustom));
                        builder.setMessage(getResources().getString(R.string.error_invalid_wallet_name_description))
                                .setTitle(getString(R.string.error_invalid_wallet_name_title))
                                .setCancelable(false)
                                .setNeutralButton(getResources().getString(R.string.string_ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        return false;
                    }else{
                        return true;
                    }
                }
                return false;
            }
        });

        mDummyFocus.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    final View activityRootView = getActivity().findViewById(R.id.activity_navigation_root);
                    if (activityRootView.getRootView().getHeight() - activityRootView.getHeight() > 100) {
                        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    }
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
                Bundle bundle = new Bundle();
                bundle.putString(RequestFragment.FROM_UUID, mWallet.getUUID());
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal(), bundle);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(SendFragment.UUID, mWallet.getUUID());
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal(), bundle);
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
                    SetSearchVisibility(false);
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
        mDummyFocus.requestFocus();
        return view;
    }

    private SearchTask mSearchTask;
    class SearchTask extends AsyncTask<String, Integer, List<Transaction>> {

        public SearchTask() { }

        @Override protected List<Transaction> doInBackground(String... strings) {
            return searchTransactions(strings[0]);
        }

        @Override protected void onPostExecute(List<Transaction> transactions) {
            if(getActivity()==null)
                return;
            mTransactions.clear();
            mTransactions.addAll(transactions);
            mTransactionAdapter.notifyDataSetChanged();
            mSearchTask = null;
        }

        @Override protected void onCancelled(){
            mSearchTask = null;
            super.onCancelled();
        }
    }


    private void SetSearchVisibility(boolean visible) {
        if(visible) {
            mSearchLayout.setX(mParentLayout.getWidth());
            mSearchLayout.setVisibility(View.VISIBLE);
            mSearchLayout.animate().translationX(0).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchLayout.setVisibility(View.VISIBLE);
                }
            });
            switchContainer.animate().alpha(0f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    switchContainer.setVisibility(View.GONE);
                }
            });
            exportLayout.animate().alpha(0f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    exportLayout.setVisibility(View.GONE);
                }
            });
            sendRequestLayout.animate().alpha(0f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sendRequestLayout.setVisibility(View.INVISIBLE);
                }
            });
            mScrollView.animate().translationY(-getActivity().getResources().getDimension(R.dimen.fragment_wallet_search_scroll_animation_height)).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScrollView.setY(getActivity().getResources().getDimension(R.dimen.fragment_wallet_search_scroll_animation_top));
                    mScrollView.setVisibility(View.VISIBLE);
                }
            });
            mSearchField.requestFocus();
        } else {
            mDummyFocus.requestFocus();
            mSearchLayout.animate().translationX(mParentLayout.getWidth()).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSearchLayout.setVisibility(View.GONE);
                }
            });
            switchContainer.setVisibility(View.VISIBLE);
            switchContainer.animate().alpha(1f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    switchContainer.setVisibility(View.VISIBLE);
                }
            });
            exportLayout.setVisibility(View.VISIBLE);
            exportLayout.animate().alpha(1f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    exportLayout.setVisibility(View.VISIBLE);
                }
            });
            sendRequestLayout.setVisibility(View.VISIBLE);
            sendRequestLayout.animate().alpha(1f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sendRequestLayout.setVisibility(View.VISIBLE);
                }
            });
            mScrollView.setY(-getActivity().getResources().getDimension(R.dimen.fragment_wallet_search_scroll_animation_bottom));
            mScrollView.animate().translationY(0).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScrollView.setVisibility(View.VISIBLE);
                }
            });
            mScrollView.setVisibility(View.VISIBLE);
        }
        searchPage = visible;
        mTransactionAdapter.setSearch(visible);
        mTransactionAdapter.notifyDataSetChanged();
    }

    @Override public void onPause() {
        super.onPause();
        mCoreAPI.removeExchangeRateChangeListener(this);
    }

    @Override public void onResume(){
        mCoreAPI.addExchangeRateChangeListener(this);
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
        mButtonFiatBalance.setText(temp);
        if(mOnBitcoinMode) {
            mButtonMover.setText(mButtonBitcoinBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setText(mTopType.getText());
        } else {
            mButtonMover.setText(mButtonFiatBalance.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_usd_white);//todo
            mMoverType.setText(mBottomType.getText());
        }
        mBottomCoin.setImageResource(WalletsFragment.mCurrencyCoinDarkDrawables[mCurrencyIndex]);//todo
        mBottomType.setText((mCoreAPI.getCurrencyAcronyms())[mCoreAPI.CurrencyIndex(mWallet.getCurrencyNum())]);
        mTopType.setText(mCoreAPI.getDefaultBTCDenomination());
    }


    private List<Transaction> searchTransactions(String term) {
        return mCoreAPI.searchTransactionsIn(mWallet, term);
    }

    private void switchBarInfo(boolean isBitcoin){
        if(isBitcoin) {
            Animator animator = ObjectAnimator.ofFloat(switchable, "translationY", (getActivity().getResources().getDimension(R.dimen.currency_switch_height)), 0);
            animator.setDuration(250);
            animator.start();
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
