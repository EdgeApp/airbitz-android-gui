package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.ResizableImageView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created on 2/13/14.
 */
public class WalletFragment extends Fragment
        implements CoreAPI.OnExchangeRatesChange,
        NavigationActivity.OnWalletUpdated {
    private final String TAG = getClass().getSimpleName();

    private EditText mSearchField;
    private LinearLayout mSearchLayout;

    private HighlightOnPressImageButton mExportButton;
    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mBackButton;
    private HighlightOnPressImageButton mSearchButton;

    private boolean searchPage = false;

    private ResizableImageView mRequestButton;
    private ResizableImageView mSendButton;

    private TextView mTitleTextView;

    private int SEARCH_ANIMATION_DURATION = 500;

    private ImageView mMoverCoin;
    private TextView mMoverType;
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

    private boolean mOnBitcoinMode = true;

    private EditText mWalletNameEditText;

    private ListView mListTransaction;

    private TransactionAdapter mTransactionAdapter;

    private List<Transaction> mTransactions;
    private List<Transaction> mAllTransactions;
    private LinkedHashMap<String, Uri> mContacts;

    private Wallet mWallet;
    private CoreAPI mCoreAPI;
    private View mView;

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
                    mWallet = mCoreAPI.getWalletFromUUID(walletUUID);
                    mTransactions = mCoreAPI.loadAllTransactions(mWallet);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_wallet, container, false);
            mOnBitcoinMode = true;
            searchPage=false;
        }

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.fragment_wallet_parent_layout);
        mScrollView = (ScrollView) mView.findViewById(R.id.fragment_wallet_scrollview);

        mAllTransactions = new ArrayList<Transaction>();
        mAllTransactions.addAll(mTransactions);

        mContacts = Common.GetMatchedContactsList(getActivity(), null);
        mTransactionAdapter = new TransactionAdapter(getActivity(), mWallet, mTransactions, mContacts);

        mSearchField = (EditText) mView.findViewById(R.id.fragment_search_edittext);
        mSearchButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_search_button);
        mSearchLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallet_search_layout);

        mSendButton = (ResizableImageView) mView.findViewById(R.id.fragment_wallet_send_button);
        mRequestButton = (ResizableImageView) mView.findViewById(R.id.fragment_wallet_request_button);
        mWalletNameEditText = (EditText) mView.findViewById(R.id.fragment_wallet_walletname_edittext);

        mExportButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_export_button);
        mBackButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_back_button);
        mButtonMover = (Button) mView.findViewById(R.id.button_mover);
        exportLayout = (RelativeLayout) mView.findViewById(R.id.fragment_wallet_export_layout);
        sendRequestLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallet_sendrequest_layout);

        mDummyFocus = mView.findViewById(R.id.fragment_wallet_dummy_focus);

        switchable = (RelativeLayout) mView.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) mView.findViewById(R.id.layout_balance);

        mMoverCoin = (ImageView) mView.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) mView.findViewById(R.id.button_mover_type);
        mBottomType = (TextView) mView.findViewById(R.id.bottom_type);
        mTopType = (TextView) mView.findViewById(R.id.top_type);

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallet_help_button);
        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_wallet_title_textview);

        mButtonBitcoinBalance = (Button) mView.findViewById(R.id.back_button_top);
        mButtonFiatBalance = (Button) mView.findViewById(R.id.back_button_bottom);
        mListTransaction = (ListView) mView.findViewById(R.id.listview_transaction);
        mListTransaction.setAdapter(mTransactionAdapter);

        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, mTransactions.size());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mWalletNameEditText.setTypeface(NavigationActivity.latoBlackTypeFace);
        mSearchField.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mButtonBitcoinBalance.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonFiatBalance.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonMover.setTypeface(NavigationActivity.latoRegularTypeFace, Typeface.BOLD);

        mWalletNameEditText.setText(mWallet.getName());

        mWalletNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    ((NavigationActivity) getActivity()).showSoftKeyboard(mWalletNameEditText);
                } else {
                    if (!mWalletNameEditText.getText().toString().trim().isEmpty()) {
                        mWallet.setName(mWalletNameEditText.getText().toString());
                        mCoreAPI.renameWallet(mWallet);
                    }
                }
            }
        });

        mSearchField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    ((NavigationActivity) getActivity()).showSoftKeyboard(mSearchField);
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
                        UpdateTransactionsListView(mAllTransactions);
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
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    mDummyFocus.requestFocus();
                    return true;
                }
                return false;
            }
        });

        mWalletNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mWalletNameEditText.getText().toString().trim().isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
                        builder.setMessage(getResources().getString(R.string.error_invalid_wallet_name_description))
                                .setTitle(getString(R.string.error_invalid_wallet_name_title))
                                .setCancelable(false)
                                .setNeutralButton(getResources().getString(R.string.string_ok),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        }
                                );
                        AlertDialog alert = builder.create();
                        alert.show();
                        return false;
                    } else {
                        mDummyFocus.requestFocus();
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
                    ((NavigationActivity)getActivity()).hideSoftKeyboard(mDummyFocus);
                }
            }
        });

        mButtonBitcoinBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnBitcoinMode = true;
                animateBar();
            }
        });

        mButtonFiatBalance.setOnClickListener(new View.OnClickListener() {
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
                mDummyFocus.requestFocus();
                Fragment fragment = new ExportFragment();
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendButton.setClickable(false);
                Bundle bundle = new Bundle();
                bundle.putString(RequestFragment.FROM_UUID, mWallet.getUUID());
                ((NavigationActivity) getActivity()).resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.REQUEST.ordinal());
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.REQUEST.ordinal(), bundle);
                mSendButton.setClickable(true);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRequestButton.setClickable(false);
                Bundle bundle = new Bundle();
                bundle.putString(SendFragment.UUID, mWallet.getUUID());
                ((NavigationActivity) getActivity()).resetFragmentThreadToBaseFragment(NavigationActivity.Tabs.SEND.ordinal());
                ((NavigationActivity) getActivity()).switchFragmentThread(NavigationActivity.Tabs.SEND.ordinal(), bundle);
                mRequestButton.setClickable(true);
            }
        });

        mListTransaction.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mDummyFocus.requestFocus();
                TransactionAdapter a = (TransactionAdapter) adapterView.getAdapter();
                a.selectItem(view, i);
                Bundle bundle = new Bundle();
                bundle.putString(Wallet.WALLET_UUID, mWallet.getUUID());
                Transaction trans = mTransactions.get(i);
                bundle.putString(Transaction.TXID, trans.getID());
                Fragment fragment = new TransactionDetailFragment();
                fragment.setArguments(bundle);
                ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchPage){
                    SetSearchVisibility(false);
                    mTransactionAdapter.setSearch(false);
                    UpdateTransactionsListView(mCoreAPI.loadAllTransactions(mWallet));
                }else{
                    getActivity().onBackPressed();
                }
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.TRANSACTIONS), NavigationActivity.Tabs.WALLET.ordinal());
            }
        });

        mDummyFocus.requestFocus();
        return mView;
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
            UpdateTransactionsListView(transactions);
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
                    exportLayout.setVisibility(View.INVISIBLE);
                }
            });
            sendRequestLayout.animate().alpha(0f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sendRequestLayout.setVisibility(View.GONE);
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
            switchContainer.animate().alpha(1f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    switchContainer.setVisibility(View.VISIBLE);
                }
            });
            exportLayout.animate().alpha(1f).setDuration(SEARCH_ANIMATION_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    exportLayout.setVisibility(View.VISIBLE);
                }
            });
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
        }
        searchPage = visible;
        mTransactionAdapter.setSearch(visible);
        mTransactionAdapter.notifyDataSetChanged();
    }

    private void UpdateTransactionsListView(List<Transaction> transactions) {
        mTransactions.clear();
        mTransactions.addAll(transactions);
        mTransactionAdapter.notifyDataSetChanged();
        mTransactionAdapter.createRunningSatoshi();
        ListViewUtility.setTransactionListViewHeightBasedOnChildren(mListTransaction, transactions.size());
    }

    @Override public void onPause() {
        super.onPause();
        mCoreAPI.removeExchangeRateChangeListener(this);
        ((NavigationActivity) getActivity()).setOnWalletUpdated(null);
    }

    @Override public void onResume(){
        super.onResume();
        mCoreAPI.addExchangeRateChangeListener(this);
        ((NavigationActivity) getActivity()).setOnWalletUpdated(this);
        mWallet = mCoreAPI.getWalletFromUUID(mWallet.getUUID());
        UpdateTransactionsListView(mCoreAPI.loadAllTransactions(mWallet));
        UpdateBalances();
        mRequestButton.setPressed(false);
        mSendButton.setPressed(false);
    }

    // Sum all transactions and show in total
    private void UpdateBalances() {
        long totalSatoshis = 0;
        for(Transaction transaction : mAllTransactions) {
                totalSatoshis+=transaction.getAmountSatoshi();
        }
        mBottomType.setText((mCoreAPI.getCurrencyAcronyms())[mCoreAPI.CurrencyIndex(mWallet.getCurrencyNum())]);
        mTopType.setText(mCoreAPI.getDefaultBTCDenomination());
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
    }


    private List<Transaction> searchTransactions(String term) {
        return mCoreAPI.searchTransactionsIn(mWallet, term);
    }

    private void animateBar(){
        if(mOnBitcoinMode) {
            mHandler.post(animateSwitchUp);
        }else{
            mHandler.post(animateSwitchDown);
        }
        UpdateBalances();
    }

    private Handler mHandler = new Handler();
    Runnable animateSwitchUp = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(switchable, "translationY", (getActivity().getResources().getDimension(R.dimen.currency_switch_height)), 0);
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };

    Runnable animateSwitchDown = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(switchable,"translationY",0,(getActivity().getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };

    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationEnd(Animator animator) {
            mTransactionAdapter.setIsBitcoin(mOnBitcoinMode);
            mTransactionAdapter.notifyDataSetChanged();
        }

        @Override public void onAnimationCancel(Animator animator) { }
        @Override public void onAnimationStart(Animator animator) { }
        @Override public void onAnimationRepeat(Animator animator) { }
    };

    @Override
    public void OnExchangeRatesChange() {
        UpdateBalances();
    }

    @Override
    public void onWalletUpdated() {
        if(mWallet!=null) {
            Common.LogD(TAG, "Reloading wallet");
            mCoreAPI.reloadWallet(mWallet);
            mWalletNameEditText.setText(mWallet.getName());
            UpdateTransactionsListView(mCoreAPI.loadAllTransactions(mWallet));
        }
    }
}
