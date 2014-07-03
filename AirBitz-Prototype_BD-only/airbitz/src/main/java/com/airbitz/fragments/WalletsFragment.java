package com.airbitz.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.airbitz.AirbitzApplication;
import com.airbitz.R;
import com.airbitz.activities.NavigationActivity;
import com.airbitz.adapters.WalletAdapter;
import com.airbitz.api.CoreAPI;
import com.airbitz.api.tABC_AccountSettings;
import com.airbitz.models.Wallet;
import com.airbitz.objects.DynamicListView;
import com.airbitz.utils.Common;
import com.airbitz.utils.ListViewUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/12/14.
 */
public class WalletsFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, DynamicListView.OnListReordered {

    private static final int BTC = 0;
    private static final int CURRENCY = 1;
    public static final String FROM_SOURCE = "com.airbitz.WalletsFragment.FROM_SOURCE";
    public static final String CREATE = "com.airbitz.WalletsFragment.CREATE";


    private String mCurrencyResourceString = "usd"; // whatever the currency selection is

    private Button mBitCoinBalanceButton;
    private Button mFiatBalanceButton;
    private Button mButtonMover;
    //private SeekBar mSeekBar;

    private TextView walletsHeader;
    private TextView archiveHeader;

    private LinearLayout newWalletLayout;
    private EditText nameEditText;
    private TextView onlineTextView;
    private TextView offlineTextView;
    private Switch newWalletSwitch;
    private Button cancelButton;
    private Button doneButton;
    private Spinner newWalletSpinner;
    private LinearLayout spinnerLayout;

    private boolean archiveClosed = false;

    private RelativeLayout topSwitch;
    private RelativeLayout bottomSwitch;
    private RelativeLayout switchable;
    private RelativeLayout switchContainer;

    private DynamicListView mLatestWalletListView;

    private ImageButton mHelpButton;
    private ImageButton mAddButton;

    private ImageView mMoverCoin;
    private ImageView mMoverType;
    private ImageView mBottomCoin;
    private ImageView mBottomType;

    private Bundle bundle;

    private TextView mTitleTextView;

    private WalletAdapter mLatestWalletAdapter;

    private boolean mSwitchWordOne = true;
    private boolean mOnBitcoinMode = true;

    private List<Wallet> mLatestWalletList;
    private List<Wallet> archivedWalletList;

    private String[] mCurrencyList;
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

        tABC_AccountSettings settings = mCoreAPI.loadAccountSettings();
        mFiatCurrencyNum = settings.getCurrencyNum();
        int[] currencyNumbers = mCoreAPI.getCurrencyNumbers();
        mCurrencyIndex = -1;
        for(int i=0; i<currencyNumbers.length; i++) {
            if(currencyNumbers[i] == mFiatCurrencyNum)
                mCurrencyIndex = i;
        }
        if((mCurrencyIndex==-1) || (mCurrencyIndex > mCurrencyCoinDarkDrawables.length)) { // default usd
            Log.d("WalletsFragment", "currency index out of bounds "+mCurrencyIndex);
            mCurrencyIndex = currencyNumbers.length - 1;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallets, container, false);

        mCurrencyList = mCoreAPI.getCurrencyAbbreviations();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mLatestWalletAdapter = new WalletAdapter(getActivity(), mLatestWalletList);

        newWalletLayout = (LinearLayout) view.findViewById(R.id.layout_new_wallet);
        nameEditText = (EditText) view.findViewById(R.id.name_edittext);
        onlineTextView = (TextView) view.findViewById(R.id.online_textview);
        offlineTextView = (TextView) view.findViewById(R.id.offline_textview);
        newWalletSwitch = (Switch) view.findViewById(R.id.new_wallet_switch);
        cancelButton = (Button) view.findViewById(R.id.button_cancel);
        doneButton = (Button) view.findViewById(R.id.button_done);
        newWalletSpinner = (Spinner) view.findViewById(R.id.new_wallet_spinner);
        spinnerLayout = (LinearLayout) view.findViewById(R.id.spinner_layout);

        mBitCoinBalanceButton = (Button) view.findViewById(R.id.back_button_top);
        mFiatBalanceButton = (Button) view.findViewById(R.id.back_button_bottom);
        mButtonMover = (Button) view.findViewById(R.id.button_mover);

        topSwitch = (RelativeLayout) view.findViewById(R.id.top_switch);
        bottomSwitch = (RelativeLayout) view.findViewById(R.id.bottom_switch);
        switchable = (RelativeLayout) view.findViewById(R.id.switchable);
        switchContainer = (RelativeLayout) view.findViewById(R.id.layout_balance);

        mHelpButton = (ImageButton) view.findViewById(R.id.button_help);
        mAddButton = (ImageButton) view.findViewById(R.id.button_add);

        mMoverCoin = (ImageView) view.findViewById(R.id.button_mover_coin);
        mMoverType = (ImageView) view.findViewById(R.id.button_mover_type);
        mBottomCoin = (ImageView) view.findViewById(R.id.bottom_coin);
        mBottomType = (ImageView) view.findViewById(R.id.bottom_type);

        walletsHeader = (TextView) view.findViewById(R.id.wallets_header);
        archiveHeader = (TextView) view.findViewById(R.id.archive_header);

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
                mBitCoinBalanceButton.setEnabled(false);
                mFiatBalanceButton.setEnabled(false);
                mButtonMover.setEnabled(false);
                mBitCoinBalanceButton.setClickable(false);
                mFiatBalanceButton.setClickable(false);
                mButtonMover.setClickable(false);
                mBitCoinBalanceButton.setFocusable(false);
                mFiatBalanceButton.setFocusable(false);
                mButtonMover.setFocusable(false);
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
        //switchBarInfo(true);

        /*nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus){
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });*/

        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    goDone();
                    return true;
                }
                return false;
            }
        });

        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mCurrencyList);

//        newWalletSpinner.setSelection(0);

        newWalletSpinner.setAdapter(dataAdapter);

        newWalletSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    onlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    offlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    nameEditText.setVisibility(View.INVISIBLE);
                } else {
                    onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                    offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                    spinnerLayout.setVisibility(View.VISIBLE);
                    nameEditText.setVisibility(View.VISIBLE);
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDone();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goCancel();
            }
        });


        mTitleTextView = (TextView) view.findViewById(R.id.textview_title);

        mLatestWalletListView = (DynamicListView) view.findViewById(R.id.layout_listview);

        mLatestWalletListView.setAdapter(mLatestWalletAdapter);
        mLatestWalletListView.setWalletList(mLatestWalletList);
        mLatestWalletListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mLatestWalletListView.setHeaders(walletsHeader, archiveHeader);
        mLatestWalletListView.setArchivedList(archivedWalletList);
        mLatestWalletListView.setArchiveClosed(archiveClosed);
        mLatestWalletListView.setOnListReorderedListener(this);

        ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(), getActivity());

        mTitleTextView.setTypeface(NavigationActivity.montserratBoldTypeFace);
        mBitCoinBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mFiatBalanceButton.setTypeface(NavigationActivity.helveticaNeueTypeFace);
        mButtonMover.setTypeface(NavigationActivity.helveticaNeueTypeFace);

        archiveHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = mLatestWalletAdapter.getArchivePos();
                //a.switchCloseAfterArchive(pos);
                System.out.println("Map Sizes before: " +mLatestWalletAdapter.getMapSize()+" vs " + mLatestWalletList.size());
                if(archiveClosed){
                    while(!archivedWalletList.isEmpty()){
                        mLatestWalletList.add(archivedWalletList.get(0));
                        mLatestWalletAdapter.addWallet(archivedWalletList.get(0));
                        archivedWalletList.remove(0);
                    }
                }else {
                    pos++;
                    while(pos<mLatestWalletList.size()){
                        archivedWalletList.add(mLatestWalletList.get(pos));
                        mLatestWalletAdapter.removeWallet(mLatestWalletList.get(pos));
                        mLatestWalletList.remove(pos);
                    }
                }
                System.out.println("Map Sizes after: " +mLatestWalletAdapter.getMapSize()+" vs " + mLatestWalletList.size());
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
                    //a.switchCloseAfterArchive(pos);
                    System.out.println("Map Sizes before: " +a.getMapSize()+" vs " + mLatestWalletList.size());
                    if(archiveClosed){
                        while(!archivedWalletList.isEmpty()){
                            mLatestWalletList.add(archivedWalletList.get(0));
                            mLatestWalletAdapter.addWallet(archivedWalletList.get(0));
                            archivedWalletList.remove(0);
                        }
                    }else {
                        pos++;
                        while(pos<mLatestWalletList.size()){
                            archivedWalletList.add(mLatestWalletList.get(pos));
                            mLatestWalletAdapter.removeWallet(mLatestWalletList.get(pos));
                            mLatestWalletList.remove(pos);
                        }
                    }
                    System.out.println("Map Sizes after: " +a.getMapSize()+" vs " + mLatestWalletList.size());
                    mLatestWalletAdapter.notifyDataSetChanged();
                    ListViewUtility.setWalletListViewHeightBasedOnChildren(mLatestWalletListView, mLatestWalletList.size(),getActivity());
                    archiveClosed = !archiveClosed;
                    mLatestWalletListView.setArchiveClosed(archiveClosed);
                }
            }
        });
        /*RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams((int)getActivity().getResources().getDimension(R.dimen.spinner_width_password),(int)getActivity().getResources().getDimension(R.dimen.drop_down_height));
        rLP.addRule(RelativeLayout.ABOVE, R.id.bottom_switch);
        switchable.setLayoutParams(rLP);*/
        /*if(switchable.getVisibility()==View.VISIBLE){
            System.out.println("I Should be visible, Height: "+switchable.getHeight() +", Width: "+switchable.getWidth());
        }*/

        UpdateBalances();
        return view;
    }

    // Sum all wallets except for archived and show in total
    private void UpdateBalances() {
        long totalSatoshis = 0;
        for(Wallet wallet : mLatestWalletList) {
            if(!wallet.isArchiveHeader() && !wallet.isHeader() && !wallet.isArchived())
                totalSatoshis+=wallet.getBalance();
        }
        mBitCoinBalanceButton.setText(mCoreAPI.formatSatoshi(totalSatoshis));
        mFiatBalanceButton.setText(mCoreAPI.conversion(totalSatoshis, false));
        switchBarInfo(mOnBitcoinMode);

        mBottomCoin.setImageResource(mCurrencyCoinDarkDrawables[mCurrencyIndex]);
        mBottomType.setImageResource(mCurrencyTypeDarkDrawables[mCurrencyIndex]);
    }

    private void switchBarInfo(boolean isBitcoin){
        RelativeLayout.LayoutParams rLP = new RelativeLayout.LayoutParams(switchContainer.getWidth(), switchContainer.getHeight()/2);
        if(isBitcoin) {
            rLP.addRule(RelativeLayout.ABOVE, R.id.bottom_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mBitCoinBalanceButton.getText());
            mMoverCoin.setImageResource(R.drawable.ico_coin_btc_white);
            mMoverType.setImageResource(R.drawable.ico_btc_white);
            for(Wallet wallet: mLatestWalletList){
                if(!wallet.isHeader() && !wallet.isArchiveHeader()) {
                    try {
                        wallet.setAmount(mCoreAPI.conversion(wallet.getBalance(), isBitcoin));
                    } catch (Exception e) {
                        wallet.setAmount("0");
                        e.printStackTrace();
                    }
                }
            }
            mLatestWalletAdapter.notifyDataSetChanged();
        }else{
            rLP.addRule(RelativeLayout.BELOW, R.id.top_switch);
            switchable.setLayoutParams(rLP);
            mButtonMover.setText(mFiatBalanceButton.getText());
            mMoverCoin.setImageResource(mCurrencyCoinWhiteDrawables[mCurrencyIndex]);
            mMoverType.setImageResource(mCurrencyTypeWhiteDrawables[mCurrencyIndex]);
            double conv = 0.1145;
            for(Wallet wallet: mLatestWalletList){
                if(!wallet.isHeader() && !wallet.isArchiveHeader()) {
                    try {
                        wallet.setAmount(mCoreAPI.conversion(wallet.getBalance(), isBitcoin));
                    } catch (Exception e) {
                        wallet.setAmount("0");
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
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        float heightDiff = mBitCoinBalanceButton.getHeight();
        float moveX = heightDiff*(seekBar.getMax()-progress)/seekBar.getMax();
        RelativeLayout.LayoutParams absParams =
                (RelativeLayout.LayoutParams)mButtonMover.getLayoutParams();

        absParams.leftMargin = 0;
        absParams.topMargin = (int) moveX;

        mButtonMover.setLayoutParams(absParams);
    }

    public void onStartTrackingTouch(SeekBar seekBar) { }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar.getProgress() > seekBar.getMax()/2) {
            seekBar.setProgress(seekBar.getMax());
            setSwitchSelection(BTC);
        } else {
            seekBar.setProgress(0);
            setSwitchSelection(CURRENCY);
        }
        onProgressChanged(seekBar, seekBar.getProgress(), true);
    }

    private void setSwitchSelection(int selection) {

        switch(selection) {
            case BTC:
                //onProgressChanged(mSeekBar, 100, true);
                mButtonMover.setText(mBitCoinBalanceButton.getText());
                mButtonMover.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ico_coin_btc_white), null,
                        getResources().getDrawable(R.drawable.ico_btc_white), null);
               break;
            case CURRENCY:
                //onProgressChanged(mSeekBar, 0, true);
                mButtonMover.setText(mFiatBalanceButton.getText());
                String left = "ico_coin_"+mCurrencyResourceString+"_white";
                String right = "ico_"+mCurrencyResourceString+"_white";
                int leftID = getResources().getIdentifier(left,"drawable",getActivity().getPackageName());
                int rightID = getResources().getIdentifier(right,"drawable",getActivity().getPackageName());
                Drawable leftD = getResources().getDrawable(leftID);
                Drawable rightD = getResources().getDrawable(rightID);
                mButtonMover.setCompoundDrawablesWithIntrinsicBounds(leftD, null, rightD, null);
                break;
            default:
                break;
        }
    }

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
//            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mCoreAPI.createWallet(mWalletName, mCurrencyNum);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddWalletTask = null;
//            showProgress(false);
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

        newWalletLayout.setVisibility(View.VISIBLE);
        nameEditText.requestFocus();

        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle("Wallet Type");

        alertDialogBuilder
                .setMessage("Do you want to create online or offline wallet?")
                .setCancelable(false)
                .setPositiveButton("Online",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        if(mSwitchWordOne){
                            addItemToLatestTransactionList("Baseball Team", "B15.000", mLatestWalletList);
                            mSwitchWordOne = false;
                        }
                        else{
                            addItemToLatestTransactionList("Fantasy Football", "B10.000", mLatestWalletList);
                            mSwitchWordOne = true;
                        }

                        dialog.cancel();
                    }
                })
                .setNegativeButton("Offline",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment());
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();*/
    }

    private void goDone(){
            if(!nameEditText.getText().toString().isEmpty()) {
                if (!newWalletSwitch.isChecked()) {
                    int[] nums = mCoreAPI.getCurrencyNumbers();
                    addNewWallet(nameEditText.getText().toString(), nums[newWalletSpinner.getSelectedItemPosition()]);
                } else {
                    ((NavigationActivity) getActivity()).pushFragment(new OfflineWalletFragment());
                }
                nameEditText.setText("");
//                newWalletSpinner.setSelection(1);
                onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
                offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
                nameEditText.setHint("Name your wallet");
                nameEditText.setHintTextColor(getResources().getColor(R.color.text_hint));
                newWalletSwitch.setChecked(false);
                newWalletLayout.setVisibility(View.GONE);
                mBitCoinBalanceButton.setEnabled(true);
                mFiatBalanceButton.setEnabled(true);
                mButtonMover.setEnabled(true);
                mBitCoinBalanceButton.setClickable(true);
                mFiatBalanceButton.setClickable(true);
                mButtonMover.setClickable(true);
                mBitCoinBalanceButton.setFocusable(true);
                mFiatBalanceButton.setFocusable(true);
                mButtonMover.setFocusable(true);
                if(nameEditText.hasFocus()){
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }else{
                nameEditText.setHint("Wallet Name Required");
                nameEditText.setHintTextColor(getResources().getColor(R.color.red));
            }
    }

    private void goCancel() { //CANCEL
        nameEditText.setText("");
//            newWalletSpinner.setSelection(1);
        onlineTextView.setTextColor(getResources().getColor(R.color.identifier_white));
        offlineTextView.setTextColor(getResources().getColor(R.color.identifier_off_text));
        nameEditText.setHint("Name your wallet");
        newWalletSwitch.setChecked(false);
        newWalletLayout.setVisibility(View.GONE);
        mBitCoinBalanceButton.setEnabled(true);
        mFiatBalanceButton.setEnabled(true);
        mButtonMover.setEnabled(true);
        mBitCoinBalanceButton.setClickable(true);
        mFiatBalanceButton.setClickable(true);
        mButtonMover.setClickable(true);
        mBitCoinBalanceButton.setFocusable(true);
        mFiatBalanceButton.setFocusable(true);
        mButtonMover.setFocusable(true);
        if(nameEditText.hasFocus()){
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