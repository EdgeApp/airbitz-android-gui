package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.objects.HighlightOnPressButton;
import com.airbitz.objects.HighlightOnPressImageButton;
import com.airbitz.objects.HighlightOnPressSpinner;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletsFragment extends Fragment
        implements DynamicListView.OnListReordered,
        CoreAPI.OnExchangeRatesChange,
        NavigationActivity.OnWalletUpdated {
    public final String TAG = getClass().getSimpleName();
    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";

    private RelativeLayout mParentLayout;

    private Button mBitCoinBalanceButton;
    private Button mFiatBalanceButton;
    private Button mButtonMover;

    private TextView walletsHeader;
    private RelativeLayout archiveHeader;

    private LinearLayout mAddWalletLayout;
    private EditText mAddWalletNameEditText;
    private TextView mAddWalletOnlineTextView;
    private TextView mAddWalletOfflineTextView;
    private Switch mAddWalletOnOffSwitch;
    private HighlightOnPressButton mAddWalletCancelButton;
    private HighlightOnPressButton mAddWalletDoneButton;
    private HighlightOnPressSpinner mAddWalletCurrencySpinner;
    private LinearLayout mAddWalletCurrencyLayout;

    private View mInvisibleCover;

    private TextView mBalanceLabel;

    private boolean archiveClosed = false;

    private RelativeLayout mBalanceSwitchLayout;

    private DynamicListView mLatestWalletListView;

    private HighlightOnPressImageButton mHelpButton;
    private HighlightOnPressImageButton mAddButton;

    private ImageView mMoverCoin;
    private TextView mMoverType;
    private TextView mBottomType;
    private TextView mTopType;

    private Bundle bundle;

    private TextView mTitleTextView;

    private WalletAdapter mLatestWalletAdapter;

    private boolean mOnBitcoinMode = true;

    private List<Wallet> mLatestWalletList;

    private List<String> mCurrencyList;
    private CoreAPI mCoreAPI;
    private View mView;
    private int mCurrencyIndex;

    //TODO fill in the correct drawables for the icons. See CoreAPI.mFauxCurrencies for the order. Right now all are filled in USD.
    //for future ease of compatibility, the drawable name should conform to the acronym name in the FauxCurrencies, ie USD, CAD, etc as the drawable should
    public static int[] mCurrencyCoinWhiteDrawables = {R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white,
            R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white,
            R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white};

    private AddWalletTask mAddWalletTask;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();

        mLatestWalletList = mCoreAPI.loadWallets(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bundle = this.getArguments();
        if(bundle != null && bundle.getBoolean(CREATE)) {
            Common.LogD(TAG, "onCreateView creating flow to TransactionDetails");
            bundle.remove(CREATE);
            bundle.putBoolean(CREATE, false);
            buildFragments();
        } else {
            Common.LogD(TAG, "onCreateView stopping in Wallets");
        }

        if(mView==null) {
            mView = inflater.inflate(R.layout.fragment_wallets, container, false);

            mOnBitcoinMode = true;
        }

        mParentLayout = (RelativeLayout) mView.findViewById(R.id.fragment_wallets_header_layout);

        mCurrencyList = new ArrayList<String>();
        mCurrencyList.addAll(Arrays.asList(mCoreAPI.getCurrencyAcronyms()));

        mLatestWalletAdapter = new WalletAdapter(getActivity(), mLatestWalletList);

        mBalanceLabel = (TextView) mView.findViewById(R.id.fragment_wallets_balance_textview);

        mInvisibleCover = mView.findViewById(R.id.fragment_wallets_invisible_cover);
        mAddWalletLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallets_addwallet_layout);
        mAddWalletNameEditText = (EditText) mView.findViewById(R.id.fragment_wallets_addwallet_name_edittext);
        mAddWalletOnlineTextView = (TextView) mView.findViewById(R.id.fragment_wallets_addwallet_online_textview);
        mAddWalletOfflineTextView = (TextView) mView.findViewById(R.id.fragment_wallets_addwallet_offline_textview);
        mAddWalletOnOffSwitch = (Switch) mView.findViewById(R.id.fragment_wallets_addwallet_onoff_switch);
        mAddWalletCancelButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_wallets_addwallet_cancel_button);
        mAddWalletDoneButton = (HighlightOnPressButton) mView.findViewById(R.id.fragment_wallets_addwallet_done_button);
        mAddWalletCurrencySpinner = (HighlightOnPressSpinner) mView.findViewById(R.id.fragment_wallets_addwallet_currency_spinner);
        mAddWalletCurrencyLayout = (LinearLayout) mView.findViewById(R.id.fragment_wallets_addwallet_currency_layout);

        mBitCoinBalanceButton = (Button) mView.findViewById(R.id.back_button_top);
        mFiatBalanceButton = (Button) mView.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) mView.findViewById(R.id.button_mover);

        mBalanceSwitchLayout = (RelativeLayout) mView.findViewById(R.id.switchable);

        mHelpButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallets_help_button);
        mAddButton = (HighlightOnPressImageButton) mView.findViewById(R.id.fragment_wallets_add_button);

        mMoverCoin = (ImageView) mView.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) mView.findViewById(R.id.button_mover_type);
        mBottomType = (TextView) mView.findViewById(R.id.bottom_type);
        mTopType = (TextView) mView.findViewById(R.id.top_type);

        walletsHeader = (TextView) mView.findViewById(R.id.fragment_wallets_wallets_header);
        archiveHeader = (RelativeLayout) mView.findViewById(R.id.fragment_wallets_archive_header);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddWalletLayout();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NavigationActivity)getActivity()).pushFragment(new HelpFragment(HelpFragment.WALLETS), NavigationActivity.Tabs.WALLET.ordinal());
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

        mInvisibleCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });
        mAddWalletLayout.setVisibility(View.GONE);
        mAddWalletLayout.setFocusableInTouchMode(true);

        mAddWalletNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    goDone();
                    return true;
                }
                return false;
            }
        });

        CurrencyAdapter mCurrencyAdapter = new CurrencyAdapter(getActivity(), mCurrencyList);
        mAddWalletCurrencySpinner.setAdapter(mCurrencyAdapter);
        int settingIndex = mCoreAPI.SettingsCurrencyIndex();
        mAddWalletCurrencySpinner.setSelection(settingIndex);

        mAddWalletOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mAddWalletOnlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    mAddWalletOfflineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    mAddWalletCurrencyLayout.setVisibility(View.INVISIBLE);
                    mAddWalletNameEditText.setVisibility(View.INVISIBLE);
                } else {
                    mAddWalletOnlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    mAddWalletOfflineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    mAddWalletCurrencyLayout.setVisibility(View.VISIBLE);
                    mAddWalletNameEditText.setVisibility(View.VISIBLE);
                }
            }
        });

        mAddWalletDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDone();
            }
        });

        mAddWalletCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });


        mTitleTextView = (TextView) mView.findViewById(R.id.fragment_wallets_title_textview);

        mLatestWalletListView = (DynamicListView) mView.findViewById(R.id.fragment_wallets_listview);
        setupLatestWalletListView();

        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBalanceLabel.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mFiatBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonMover.setTypeface(NavigationActivity.latoRegularTypeFace, Typeface.BOLD);

        archiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mLatestWalletAdapter.getArchivePos();
                mLatestWalletAdapter.switchCloseAfterArchive(pos);
                mLatestWalletAdapter.notifyDataSetChanged();
                archiveClosed = !archiveClosed;
                int size = mLatestWalletAdapter.getCount();
                if(archiveClosed) {
                    size = mLatestWalletAdapter.getArchivePos();
                }
                mLatestWalletListView.setArchiveClosed(archiveClosed);
                ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, size, getActivity());
            }
        });

        mLatestWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                Wallet wallet = a.getList().get(i);
                if (!wallet.isArchiveHeader() && !wallet.isHeader()) {
                    mParentLayout.requestFocus();
                    a.selectItem(view, i);
                    showWalletFragment(a.getList().get(i).getUUID());
                } else if (wallet.isArchiveHeader()) {
                    archiveClosed = !archiveClosed;
                    int pos = a.getPosition(wallet);
                    a.switchCloseAfterArchive(pos);
                    mLatestWalletAdapter.notifyDataSetChanged();

                    int size = mLatestWalletAdapter.getCount();
                    if(archiveClosed) {
                        size = mLatestWalletAdapter.getArchivePos();
                    }
                    mLatestWalletListView.setArchiveClosed(archiveClosed);
                    ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, size, getActivity());
                }
            }
        });

        mAddWalletNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    ((NavigationActivity) getActivity()).showSoftKeyboard(mAddWalletNameEditText);
                }
            }
        });

        mParentLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    ((NavigationActivity) getActivity()).hideSoftKeyboard(mAddWalletNameEditText);
                }
            }
        });

        return mView;
    }

    private void setupLatestWalletListView() {
        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mLatestWalletListView.setWalletList(mLatestWalletList);
        mLatestWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mLatestWalletListView.setHeaders(walletsHeader, archiveHeader);
        mLatestWalletListView.setArchiveClosed(archiveClosed);
        mLatestWalletListView.setOnListReorderedListener(this);
    }

    @Override
    public void OnExchangeRatesChange() {
        UpdateBalances();
    }

    // Sum all wallets except for archived and show in total
    private void UpdateBalances() {
        long totalSatoshis = 0;
        for(Wallet wallet : mLatestWalletList) {
            if(!wallet.isArchiveHeader() && !wallet.isHeader() && !wallet.isArchived())
                totalSatoshis+=wallet.getBalanceSatoshi();
        }
        mBottomType.setText(mCoreAPI.getUserCurrencyAcronym());
        mTopType.setText(mCoreAPI.getDefaultBTCDenomination());
        mBitCoinBalanceButton.setText(mCoreAPI.formatSatoshi(totalSatoshis, true));
        String temp = mCoreAPI.FormatDefaultCurrency(totalSatoshis, false, true);
        mFiatBalanceButton.setText(temp);
        if(mOnBitcoinMode) {
            mButtonMover.setText(mBitCoinBalanceButton.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setText(mTopType.getText());
        } else {
            mButtonMover.setText(mFiatBalanceButton.getText());
            mMoverCoin.setImageResource(0);
            mMoverType.setText(mBottomType.getText());
        }
    }

    private void animateBar() {
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
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY", (getActivity().getResources().getDimension(R.dimen.currency_switch_height)), 0);
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };

    Runnable animateSwitchDown = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout,"translationY",0,(getActivity().getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(100);
            animator.addListener(endListener);
            animator.start();
        }
    };

    Animator.AnimatorListener endListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationEnd(Animator animator) {
            mLatestWalletAdapter.setIsBitcoin(mOnBitcoinMode);
            mLatestWalletAdapter.notifyDataSetChanged();
        }

        @Override public void onAnimationCancel(Animator animator) { }
        @Override public void onAnimationStart(Animator animator) { }
        @Override public void onAnimationRepeat(Animator animator) { }
    };

    private void showWalletFragment(String uUID) {
        Bundle bundle = new Bundle();
        bundle.putString(FROM_SOURCE, "");
        Wallet w = mCoreAPI.getWalletFromUUID(uUID);
        bundle.putString(Wallet.WALLET_UUID, w.getUUID());
        Fragment fragment = new WalletFragment();
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment, NavigationActivity.Tabs.WALLET.ordinal());
    }

    public void addNewWallet(String name, int currencyNum){
        if(AirbitzApplication.isLoggedIn()) {
            mAddWalletTask = new AddWalletTask(name, currencyNum);
            mAddWalletTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            Common.LogD(TAG, "not logged in");
        }
    }

    // Callback when the listview was reordered by the user
    @Override
    public void onListReordered() {
        mCoreAPI.setWalletOrder(mLatestWalletList);
        UpdateBalances();
    }

    private void refreshWalletListView(List<Wallet> list) {
        mLatestWalletList.clear();
        mLatestWalletList.addAll(list);
        mLatestWalletAdapter.swapWallets();
        mLatestWalletAdapter.notifyDataSetChanged();
        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
    }

    @Override
    public void onWalletUpdated() {
        Common.LogD(TAG, "wallet list updated");
        refreshWalletListView(mCoreAPI.loadWallets(false));
        UpdateBalances();
    }

    /**
     * Represents an asynchronous creation of the first wallet
     */
    public class AddWalletTask extends AsyncTask<Void, Void, Boolean> {

        private final String mWalletName;
        private final int mCurrencyNum;

        AddWalletTask(String walletName, int currencyNum) {
            mWalletName = walletName;
            mCurrencyNum = currencyNum;
        }

        @Override
        protected void onPreExecute() {
            ((NavigationActivity)getActivity()).showModalProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreAPI.createWallet(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                    mWalletName, mCurrencyNum);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddWalletTask = null;
            ((NavigationActivity)getActivity()).showModalProgress(false);
            if (!success) {
                Common.LogD(TAG, "AddWalletTask failed");
            } else {
                refreshWalletListView(mCoreAPI.loadWallets(false));
            }
        }

        @Override
        protected void onCancelled() {
            mAddWalletTask = null;
        }
    }


    public void showAddWalletLayout() {
        if (mAddWalletLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        mAddWalletCancelButton.setClickable(true);
        mAddWalletDoneButton.setClickable(true);

        mAddWalletNameEditText.setText("");
        mAddWalletOnlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
        mAddWalletOfflineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
        mAddWalletNameEditText.setHint(getString(R.string.fragment_wallets_addwallet_name_hint));
        mAddWalletNameEditText.setHintTextColor(getResources().getColor(R.color.text_hint));
        mAddWalletOnOffSwitch.setChecked(false);

        Animation mSlideInTop = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top);
        mAddWalletLayout.startAnimation(mSlideInTop);
        mAddWalletLayout.setVisibility(View.VISIBLE);
        mInvisibleCover.setVisibility(View.VISIBLE);
        mAddWalletNameEditText.requestFocus();
    }

    private void goDone(){
        if(!Common.isBadWalletName(mAddWalletNameEditText.getText().toString())) {
            if (!mAddWalletOnOffSwitch.isChecked()) {
                int[] nums = mCoreAPI.getCurrencyNumbers();
                addNewWallet(mAddWalletNameEditText.getText().toString(), nums[mAddWalletCurrencySpinner.getSelectedItemPosition()]);
            } else {
                ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment(), NavigationActivity.Tabs.WALLET.ordinal());
            }
            goCancel();
        } else {
            Common.alertBadWalletName(this.getActivity());
        }
    }

    private void goCancel() { //CANCEL
        mAddWalletCancelButton.setClickable(false);
        mAddWalletDoneButton.setClickable(false);
        ((NavigationActivity) getActivity()).hideSoftKeyboard(mParentLayout);
        mHandler.postDelayed(mDelayedAnimation, 100);
    }

    //This animation must run after the keyboard is down else a layout redraw occurs causing a visual glitch
    Runnable mDelayedAnimation = new Runnable() {
        @Override
        public void run() {
            Animation mSlideOutTop = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
            mAddWalletLayout.startAnimation(mSlideOutTop);
            mAddWalletLayout.setVisibility(View.INVISIBLE);
            mInvisibleCover.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getActivity().getSharedPreferences("com.airbitz.app", Context.MODE_PRIVATE);
        archiveClosed = prefs.getBoolean("archiveClosed", false);
        if (archiveClosed) {
            archiveClosed = false;
            archiveHeader.performClick();
        }

        mCurrencyIndex = mCoreAPI.SettingsCurrencyIndex();
        mLatestWalletListView.setHeaderVisibilityOnReturn();
        UpdateBalances();

        mCoreAPI.addExchangeRateChangeListener(this);
        ((NavigationActivity) getActivity()).setOnWalletUpdated(this);
    }

    @Override public void onPause() {
        super.onPause();
        SharedPreferences prefs = getActivity().getSharedPreferences("com.airbitz.app", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("archiveClosed", archiveClosed).apply();
        mCoreAPI.removeExchangeRateChangeListener(this);
        ((NavigationActivity) getActivity()).setOnWalletUpdated(null);
    }

    public void buildFragments(){
        if(bundle.getString(FROM_SOURCE).equals(SuccessFragment.TYPE_REQUEST) || bundle.getString(FROM_SOURCE).equals(SuccessFragment.TYPE_SEND)){
            Fragment frag = new WalletFragment();
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag, NavigationActivity.Tabs.WALLET.ordinal());

            Fragment frag2 = new TransactionDetailFragment();
            frag2.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag2, NavigationActivity.Tabs.WALLET.ordinal());
        }
    }

}
