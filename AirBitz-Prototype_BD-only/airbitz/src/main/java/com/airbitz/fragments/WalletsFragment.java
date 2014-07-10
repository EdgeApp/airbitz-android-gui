package com.airbitz.fragments;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
        CoreAPI.OnExchangeRatesChange {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";

    private RelativeLayout mParentLayout;
    private RelativeLayout mContainerLayout;

    private Boolean firstTime = true;

    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private Button mBitCoinBalanceButton;
    private Button mFiatBalanceButton;
    private Button mButtonMover;

    private TextView walletsHeader;
    private TextView archiveHeader;

    private LinearLayout mAddWalletLayout;
    private EditText mAddWalletNameEditText;
    private TextView mAddWalletOnlineTextView;
    private TextView mAddWalletOfflineTextView;
    private Switch mAddWalletOnOffSwitch;
    private Button mAddWalletCancelButton;
    private Button mAddWalletDoneButton;
    private Spinner mAddWalletCurrencySpinner;
    private LinearLayout mAddWalletCurrencyLayout;

    private View mInvisibleCover;

    private TextView mBalanceLabel;

    private boolean archiveClosed = false;

    private RelativeLayout mBalanceTopLayout;
    private RelativeLayout mBalanceBottomLayout;
    private RelativeLayout mBalanceSwitchLayout;
    private RelativeLayout mBalanceContainer;

    private DynamicListView mLatestWalletListView;

    private ImageButton mHelpButton;
    private ImageButton mAddButton;

    private ImageView mMoverCoin;
    private TextView mMoverType;
    private ImageView mBottomCoin;
    private TextView mBottomType;
    private TextView mTopType;

    private Bundle bundle;

    private TextView mTitleTextView;

    private WalletAdapter mLatestWalletAdapter;

    private boolean mSwitchWordOne = true;
    private boolean mOnBitcoinMode = true;

    private List<Wallet> mLatestWalletList;
    private List<Wallet> archivedWalletList;

    private List<String> mCurrencyList;
    private CoreAPI mCoreAPI;
    private int mFiatCurrencyNum;
    private int mCurrencyIndex;

    //TODO fill in the correct drawables for the icons. See CoreAPI.mFauxCurrencies for the order. Right now all are filled in USD.
    //for future ease of compatibility, the drawable name should conform to the acronym name in the FauxCurrencies, ie USD, CAD, etc as the drawable should
    public static int[] mCurrencyCoinWhiteDrawables = {R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white,
            R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white,
            R.drawable.ico_coin_usd_white, R.drawable.ico_coin_usd_white};
    public static int[] mCurrencyTypeWhiteDrawables = {R.drawable.ico_usd_white, R.drawable.ico_usd_white,
            R.drawable.ico_usd_white, R.drawable.ico_usd_white, R.drawable.ico_usd_white,
            R.drawable.ico_usd_white, R.drawable.ico_usd_white};
    public static int[] mCurrencyCoinDarkDrawables = {R.drawable.ico_coin_usd_dark, R.drawable.ico_coin_usd_dark,
            R.drawable.ico_coin_usd_dark, R.drawable.ico_coin_usd_dark, R.drawable.ico_coin_usd_dark, R.drawable.ico_coin_usd_dark, R.drawable.ico_coin_usd_dark};
    public static int[] mCurrencyTypeDarkDrawables = {R.drawable.ico_usd_dark, R.drawable.ico_usd_dark,
            R.drawable.ico_usd_dark, R.drawable.ico_usd_dark, R.drawable.ico_usd_dark, R.drawable.ico_usd_dark, R.drawable.ico_usd_dark};

    private AddWalletTask mAddWalletTask;
    private boolean fragmentsCreated = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCoreAPI = CoreAPI.getApi();
        mLatestWalletList = mCoreAPI.loadWallets();
        archivedWalletList = new ArrayList<Wallet>();
        bundle = this.getArguments();
        if(bundle != null && bundle.getBoolean(CREATE)){
            bundle.remove(CREATE);
            bundle.putBoolean(CREATE, false);
            buildFragments();
        }

        mCurrencyIndex = mCoreAPI.SettingsCurrencyIndex();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallets, container, false);

        mParentLayout = (RelativeLayout) view;
        mContainerLayout = (RelativeLayout) view.findViewById(R.id.fragment_wallets_container);

        mCurrencyList = new ArrayList<String>();
        mCurrencyList.addAll(Arrays.asList(mCoreAPI.getCurrencyAcronyms()));

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mLatestWalletAdapter = new WalletAdapter(getActivity(), mLatestWalletList);

        mBalanceLabel = (TextView) view.findViewById(R.id.fragment_wallets_balance_textview);

        mAddWalletLayout = (LinearLayout) view.findViewById(R.id.fragment_wallets_addwallet_layout);
        mAddWalletNameEditText = (EditText) view.findViewById(R.id.fragment_wallets_addwallet_name_edittext);
        mAddWalletOnlineTextView = (TextView) view.findViewById(R.id.fragment_wallets_addwallet_online_textview);
        mAddWalletOfflineTextView = (TextView) view.findViewById(R.id.fragment_wallets_addwallet_offline_textview);
        mAddWalletOnOffSwitch = (Switch) view.findViewById(R.id.fragment_wallets_addwallet_onoff_switch);
        mAddWalletCancelButton = (Button) view.findViewById(R.id.fragment_wallets_addwallet_cancel_button);
        mAddWalletDoneButton = (Button) view.findViewById(R.id.fragment_wallets_addwallet_done_button);
        mAddWalletCurrencySpinner = (Spinner) view.findViewById(R.id.fragment_wallets_addwallet_currency_spinner);
        mAddWalletCurrencyLayout = (LinearLayout) view.findViewById(R.id.fragment_wallets_addwallet_currency_layout);

        mInvisibleCover = view.findViewById(R.id.fragment_wallets_invisible_cover);

        mBitCoinBalanceButton = (Button) view.findViewById(R.id.back_button_top);
        mFiatBalanceButton = (Button) view.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);

        mBalanceTopLayout = (RelativeLayout) view.findViewById(R.id.top_switch);
        mBalanceBottomLayout = (RelativeLayout) view.findViewById(R.id.bottom_switch);
        mBalanceSwitchLayout = (RelativeLayout) view.findViewById(R.id.switchable);
        mBalanceContainer = (RelativeLayout) view.findViewById(R.id.layout_balance);

        mHelpButton = (ImageButton) view.findViewById(R.id.fragment_wallets_help_button);
        mAddButton = (ImageButton) view.findViewById(R.id.fragment_wallets_add_button);

        mMoverCoin = (ImageView) view.findViewById(R.id.button_mover_coin);
        mMoverType = (TextView) view.findViewById(R.id.button_mover_type);
        mBottomCoin = (ImageView) view.findViewById(R.id.bottom_coin);
        mBottomType = (TextView) view.findViewById(R.id.bottom_type);
        mTopType = (TextView) view.findViewById(R.id.top_type);

        walletsHeader = (TextView) view.findViewById(R.id.fragment_wallets_wallets_header);
        archiveHeader = (TextView) view.findViewById(R.id.fragment_wallets_archive_header);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWalletType();
            }
        });

        mHelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.showHelpInfo(getActivity(), "Wallet Info", "Wallet info description");
            }
        });

        mBitCoinBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(true);
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                mOnBitcoinMode = true;
            }
        });
        mFiatBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBarInfo(false);
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                mOnBitcoinMode = false;
            }
        });
        mButtonMover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLatestWalletListView.setAdapter(mLatestWalletAdapter);
                if(mOnBitcoinMode){
                    switchBarInfo(false);
                    mOnBitcoinMode = false;
                }else{
                    switchBarInfo(true);
                    mOnBitcoinMode = true;
                }
            }
        });

        mOnBitcoinMode = true;

        mInvisibleCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });
        mAddWalletLayout.setVisibility(View.GONE);
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

//        mAddWalletCurrencySpinner.setSelection(0);TODO tie in to settings, set current currency as default

        mAddWalletCurrencySpinner.setAdapter(mCurrencyAdapter);

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


        mTitleTextView = (TextView) view.findViewById(R.id.fragment_wallets_title_textview);

        mLatestWalletListView = (DynamicListView) view.findViewById(R.id.fragment_wallets_listview);

        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mLatestWalletListView.setWalletList(mLatestWalletList);
        mLatestWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mLatestWalletListView.setHeaders(walletsHeader, archiveHeader);
        mLatestWalletListView.setArchivedList(archivedWalletList);
        mLatestWalletListView.setArchiveClosed(archiveClosed);
        mLatestWalletListView.setOnListReorderedListener(this);

        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBalanceLabel.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mFiatBalanceButton.setTypeface(NavigationActivity.latoRegularTypeFace);
        mButtonMover.setTypeface(NavigationActivity.latoRegularTypeFace);

        archiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mLatestWalletAdapter.getArchivePos();
                mLatestWalletAdapter.switchCloseAfterArchive(pos);
                mLatestWalletAdapter.notifyDataSetChanged();
                ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
                archiveClosed = !archiveClosed;
                mLatestWalletListView.setArchiveClosed(archiveClosed);
            }
        });

        mLatestWalletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WalletAdapter a = (WalletAdapter) adapterView.getAdapter();
                Wallet wallet = a.getList().get(i);
                if(!wallet.isArchiveHeader() && !wallet.isHeader()) {
                    showWalletFragment(a.getList().get(i).getName());
                }else if(wallet.isArchiveHeader()){
                    int pos = a.getPosition(wallet);
                    a.switchCloseAfterArchive(pos);
                    mLatestWalletAdapter.notifyDataSetChanged();
                    ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
                    archiveClosed = !archiveClosed;
                    mLatestWalletListView.setArchiveClosed(archiveClosed);
                }
            }
        });
        UpdateBalances();
        mCoreAPI.addExchangeRateChangeListener(this);

        return view;
    }

    @Override public void onPause() {
        super.onPause();
        mCoreAPI.removeExchangeRateChangeListener(this);
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
        mBitCoinBalanceButton.setText(mCoreAPI.getUserBTCSymbol()+" "+mCoreAPI.FormatDefaultCurrency(totalSatoshis, true, false));
        mFiatBalanceButton.setText(mCoreAPI.FormatDefaultCurrency(totalSatoshis, false, true));
        firstTime = true;
        switchBarInfo(mOnBitcoinMode);

        mBottomCoin.setImageResource(mCurrencyCoinDarkDrawables[mCurrencyIndex]);
        mBottomType.setText(mCoreAPI.getUserCurrencyAcronym());
        mTopType.setText(mCoreAPI.getUserBTCDenomination());
    }

    private void switchBarInfo(boolean isBitcoin){
        //RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getActivity().getResources().getDimension(R.dimen.currency_switch_height));
        if(isBitcoin) {
            if(!firstTime) {
                Animator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout, "translationY", (getActivity().getResources().getDimension(R.dimen.currency_switch_height)), 0);
                animator.setDuration(250);
                animator.start();
            }else{
                firstTime = false;
            }
            mButtonMover.setText(mBitCoinBalanceButton.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setText(mTopType.getText());
            for(Wallet wallet: mLatestWalletList){
                if(!wallet.isHeader() && !wallet.isArchiveHeader()) {
                    try {
                        wallet.setBalanceFormatted(mCoreAPI.FormatDefaultCurrency(wallet.getBalanceSatoshi(), isBitcoin, true));
                    } catch (Exception e) {
                        wallet.setBalanceFormatted("0");
                        e.printStackTrace();
                    }
                }
            }
            mLatestWalletAdapter.notifyDataSetChanged();
        }else{
            Animator animator = ObjectAnimator.ofFloat(mBalanceSwitchLayout,"translationY",0,(getActivity().getResources().getDimension(R.dimen.currency_switch_height)));
            animator.setDuration(250);
            animator.start();
            mButtonMover.setText(mFiatBalanceButton.getText());
            mMoverCoin.setImageResource(mCurrencyCoinWhiteDrawables[mCurrencyIndex]);
            mMoverType.setText(mBottomType.getText());
            double conv = 0.1145;
            for(Wallet wallet: mLatestWalletList){
                if(!wallet.isHeader() && !wallet.isArchiveHeader()) {
                    try {
                        wallet.setBalanceFormatted(mCoreAPI.FormatDefaultCurrency(wallet.getBalanceSatoshi(), isBitcoin, true));
                    } catch (Exception e) {
                        wallet.setBalanceFormatted("0");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void showWalletFragment(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FROM_SOURCE, "");
        Wallet w = mCoreAPI.getWalletFromName(name);
        bundle.putString(Wallet.WALLET_UUID, w.getUUID());
        Fragment fragment = new WalletFragment();
        fragment.setArguments(bundle);
        ((NavigationActivity) getActivity()).pushFragment(fragment);
    }

//    @Override
//    public void onBackPressed() {
//        if(mRequestClass == null){
//            finish();
//        }
//        else{
//            super.onBackPressed();
//        }
//
//    }
//

    public void addNewWallet(String name, int currencyNum){
        if(AirbitzApplication.isLoggedIn()) {
            mAddWalletTask = new AddWalletTask(name, currencyNum);
            mAddWalletTask.execute((Void) null);
        } else {
            Log.d("WalletsFragment", "not logged in");
        }
    }

    // Callback when the listview was reordered by the user
    @Override
    public void onListReordered() {
        List<Wallet> list = mLatestWalletListView.mWalletList;
        mCoreAPI.setWalletOrder(list);
        UpdateBalances();
    }

    private void refreshWalletList(List<Wallet> list) {
        mLatestWalletList.clear();
        mLatestWalletList.addAll(list);
        mLatestWalletAdapter.swapWallets();
        mLatestWalletAdapter.notifyDataSetChanged();
        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
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

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreAPI.createWallet(AirbitzApplication.getUsername(), AirbitzApplication.getPassword(),
                    mWalletName, mCurrencyNum);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddWalletTask = null;
            if (!success) {
                Log.d("WalletsFragment", "AddWalletTask failed");
            } else {
                refreshWalletList(mCoreAPI.loadWallets());
            }
        }

        @Override
        protected void onCancelled() {
            mAddWalletTask = null;
        }
    }


    public void showDialogWalletType(){
        if(mAddWalletLayout.getVisibility() == View.GONE) {
            LayoutTransition lt = new LayoutTransition();
            Animator animator = ObjectAnimator.ofFloat(null, "translationY", -(getResources().getDimension(R.dimen.fragment_wallet_addwallet_height)), 0);
            lt.setAnimator(LayoutTransition.APPEARING, animator);
            lt.setAnimator(LayoutTransition.CHANGE_APPEARING, animator);
            lt.setStartDelay(LayoutTransition.APPEARING, 0);
            lt.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
            lt.setDuration(300);
            mContainerLayout.setLayoutTransition(lt);
            mAddWalletLayout.setVisibility(View.VISIBLE);
            mInvisibleCover.setVisibility(View.VISIBLE);
            mAddWalletNameEditText.requestFocus();
            final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void goDone(){
        if(!mAddWalletNameEditText.getText().toString().isEmpty()) {
            if (!mAddWalletOnOffSwitch.isChecked()) {
                int[] nums = mCoreAPI.getCurrencyNumbers();
                addNewWallet(mAddWalletNameEditText.getText().toString(), nums[mAddWalletCurrencySpinner.getSelectedItemPosition()]);
            } else {
                ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment());
            }
            mAddWalletNameEditText.setText("");
//                mAddWalletCurrencySpinner.setSelection(1);
            mAddWalletOnlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
            mAddWalletOfflineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
            mAddWalletNameEditText.setHint(getString(R.string.fragment_wallets_addwallet_name_hint));
            mAddWalletNameEditText.setHintTextColor(getResources().getColor(R.color.text_hint));
            mAddWalletOnOffSwitch.setChecked(false);
            LayoutTransition lt = new LayoutTransition();
            Animator animator = ObjectAnimator.ofFloat(null,"translationY",0,-(getResources().getDimension(R.dimen.fragment_wallet_addwallet_height)));
            lt.setAnimator(LayoutTransition.DISAPPEARING,animator);
            lt.setStartDelay(LayoutTransition.DISAPPEARING,0);
            lt.setDuration(300);
            mContainerLayout.setLayoutTransition(lt);
            mAddWalletLayout.setVisibility(View.GONE);
            mInvisibleCover.setVisibility(View.GONE);
            if((mParentLayout.getRootView().getHeight()-mParentLayout.getHeight())>200){
                final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    private void goCancel() { //CANCEL
        mAddWalletNameEditText.setText("");
//            mAddWalletCurrencySpinner.setSelection(1);
        mAddWalletOnlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
        mAddWalletOfflineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
        mAddWalletNameEditText.setHint(getString(R.string.fragment_wallets_addwallet_name_hint));
        mAddWalletOnOffSwitch.setChecked(false);
        LayoutTransition lt = new LayoutTransition();
        Animator animator = ObjectAnimator.ofFloat(null,"translationY",0,-(getResources().getDimension(R.dimen.fragment_wallet_addwallet_height)));
        lt.setAnimator(LayoutTransition.DISAPPEARING,animator);
        lt.setStartDelay(LayoutTransition.DISAPPEARING,0);
        lt.setDuration(300);
        mContainerLayout.setLayoutTransition(lt);
        mAddWalletLayout.setVisibility(View.GONE);
        mInvisibleCover.setVisibility(View.GONE);
        if((mParentLayout.getRootView().getHeight()-mParentLayout.getHeight())>100){
            final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mLatestWalletListView.setHeaderVisibilityOnReturn();
    }

    public void buildFragments(){
        if(bundle.getString(FROM_SOURCE).equals("REQUEST") || bundle.getString(FROM_SOURCE).equals("SEND")){
            Fragment frag = new WalletFragment();
            frag.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag);
            Fragment frag2 = new TransactionDetailFragment();
            frag2.setArguments(bundle);
            ((NavigationActivity) getActivity()).pushFragment(frag2);
        }
    }
}